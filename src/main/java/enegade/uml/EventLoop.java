package enegade.uml;

/*
 * Copyright 2020 enegade
 *
 * This file is part of the enegade.uml package.
 *
 * This file is distributed "AS IS", WITHOUT ANY WARRANTIES.
 *
 * For the full copyright and license information, please view the LICENSE
 * file distributed with this project.
 */

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import enegade.uml.debugger.Task;
import enegade.uml.jdi.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author enegade
 */
public class EventLoop
{
    private boolean running = true;

    private VirtualMachine virtualMachine;

    private List<DebuggerRequest<?, ?>> debuggerRequests = new ArrayList<>();

    public VirtualMachine getVirtualMachine()
    {
        return this.virtualMachine;
    }

    public EventLoop(VirtualMachine virtualMachine)
    {
        this.virtualMachine = virtualMachine;
    }

    public void addDebuggerRequest(DebuggerRequest<?, ?> debuggerRequest)
    {
        this.debuggerRequests.add(debuggerRequest);
    }

    public void removeDebuggerRequest(DebuggerRequest<?, ?> debuggerRequest)
    {
        this.debuggerRequests.remove(debuggerRequest);

        debuggerRequest.getRequest().ifPresent( request -> this.getEventManager().deleteEventRequest(request) );
    }

    public EventRequestManager getEventManager()
    {
        return this.virtualMachine.eventRequestManager();
    }

    public void run()
    {
        try {
            EventQueue queue = virtualMachine.eventQueue();

            while ( this.running ) {
                EventSet eventSet = queue.remove();

                if( eventSet.size() > 2 ) {
                    int stop = 1;
                }

                Map< Class<? extends Event>, List<Event> > group = eventSet.stream()
                        .collect(Collectors.groupingBy(Event::getClass));

                for( Map.Entry< Class<? extends Event>, List<Event> > entry : group.entrySet() ) {
                    handleEvents( entry.getKey(), entry.getValue() );
                }

                virtualMachine.resume();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleEvents(Class<? extends Event> eventClass, List<?> events)
    {
        if( BreakpointEvent.class.isAssignableFrom(eventClass) ) {
            this.handleEvents( (List<BreakpointEvent>) events, BreakpointDebuggerEvent::new, BreakpointDebuggerRequest.class);
        } else if( ClassPrepareEvent.class.isAssignableFrom(eventClass) ) {
            this.handleEvents( (List<ClassPrepareEvent>) events, ClassPrepareDebuggerEvent::new, ClassPrepareDebuggerRequest.class);
        } else if( ExceptionEvent.class.isAssignableFrom(eventClass) ) {
            this.handleEvents( (List<ExceptionEvent>) events, ExceptionDebuggerEvent::new, ExceptionDebuggerRequest.class);
        } else if( MethodEntryEvent.class.isAssignableFrom(eventClass) ) {
            this.handleEvents( (List<MethodEntryEvent>) events, MethodEntryDebuggerEvent::new, MethodEntryDebuggerRequest.class);
        } else if( MethodExitEvent.class.isAssignableFrom(eventClass) ) {
            this.handleEvents( (List<MethodExitEvent>) events, MethodExitDebuggerEvent::new, MethodExitDebuggerRequest.class);
        } else if( StepEvent.class.isAssignableFrom(eventClass) ) {
            this.handleEvents( (List<StepEvent>) events, StepDebuggerEvent::new, StepDebuggerRequest.class);
        } else if( ThreadStartEvent.class.isAssignableFrom(eventClass) ) {
            this.handleEvents( (List<ThreadStartEvent>) events, ThreadStartDebuggerEvent::new, ThreadStartDebuggerRequest.class);
        } else if( VMDeathEvent.class.isAssignableFrom(eventClass) ) {
            this.handleEvents( (List<VMDeathEvent>) events, VMDeathDebuggerEvent::new, VMDeathDebuggerRequest.class);

            this.running = false;
        } else if( VMStartEvent.class.isAssignableFrom(eventClass) ) {
            this.handleEvents( (List<VMStartEvent>) events, VMStartDebuggerEvent::new, VMStartDebuggerRequest.class);
        } else {
            throw new RuntimeException();
        }
    }

    private <E extends Event, D extends DebuggerEvent<E>, R extends DebuggerRequest<?, D> > void
    handleEvents(List<E> events, Function<E, D> creator, Class<R> debuggerRequestClass)
    {
        List<D> debuggerEvents = events.stream()
                .map(creator).collect(Collectors.toList());

        List<R> debuggerRequests = this.debuggerRequests.stream()
                .filter( debuggerRequest -> debuggerRequestClass.isInstance(debuggerRequest) )
                .map( debuggerRequest -> {
                    @SuppressWarnings("unchecked")
                    R instance = (R) debuggerRequest;
                    return instance;
                } )
                .collect(Collectors.toList());

        List<R> eventSpawningRequests = debuggerRequests.stream()
                .filter(DebuggerRequest::hasRequest)
                .collect(Collectors.toList());

        if( eventSpawningRequests.isEmpty() ) {
            if( !debuggerEvents.get(0).isSelfSpawn() ) {
                throw new RuntimeException();
            }

            for (D debuggerEvent : debuggerEvents) {
                handleEvent(debuggerEvent, debuggerRequests, null);
            }
            return;
        }

        Map<R, D> eventMap = new HashMap<>();

        for(D debuggerEvent : debuggerEvents) {
            R eventSpawningRequest = eventSpawningRequests.stream()
                    .filter( debuggerRequest -> debuggerRequest.getRequest().get() == debuggerEvent.getEvent().request() )
                    .findAny().orElseThrow( () -> new RuntimeException("") );

            eventMap.put(eventSpawningRequest, debuggerEvent);
        }

        List<Map.Entry<R, D>> entries
                = new ArrayList<>( eventMap.entrySet() );
        entries.sort( (r1, r2) -> this.compareRequestOrders( r1.getKey(), r2.getKey() ) );

        for( Map.Entry<R, D> entry : entries ) {
            handleEvent( entry.getValue(), debuggerRequests, entry.getKey() );
        }
    }

    private <D extends DebuggerEvent<?>, R extends DebuggerRequest<?, D> > void
    handleEvent(D debuggerEvent, List<R> debuggerRequests, @Nullable R eventSpawningRequest)
    {
        if( eventSpawningRequest != null ) {
            Task task = eventSpawningRequest.getTask().get();
            debuggerEvent.setTask(task);

            if( eventSpawningRequest.isEnabled() ) {
                eventSpawningRequest.getHandler().handle(debuggerEvent);
            }
        }

        List<R> otherDebuggerRequests = debuggerRequests.stream()
                .filter( debuggerRequest -> !debuggerRequest.hasRequest() )
                .filter( debuggerRequest -> {
                    if( !(debuggerRequest instanceof ThreadVisibleDebuggerRequest) ) {
                        return true;
                    }

                    if( !(debuggerEvent instanceof ThreadableDebuggerEvent) ) {
                        throw new RuntimeException();
                    }

                    List<ThreadReference> threadReferences = ((ThreadVisibleDebuggerRequest<?, ?>) debuggerRequest).getThreadFilters();
                    if( threadReferences.isEmpty() ) {
                        return true;
                    }

                    return threadReferences.contains( ((ThreadableDebuggerEvent) debuggerEvent).getThreadReference() );
                } )
                .collect(Collectors.toList());
        otherDebuggerRequests.sort( this::compareRequestOrders );
        for( R debuggerRequest : otherDebuggerRequests ) {
            if( debuggerRequest.isEnabled() ) {
                debuggerRequest.getHandler().handle(debuggerEvent);
            }
        }
    }

    private int compareRequestOrders(DebuggerRequest<?, ?> r1, DebuggerRequest<?, ?> r2)
    {
        long order1 = r1.getOrder();
        long order2 = r2.getOrder();
        if(order1 == order2) {
            throw new RuntimeException();
        }

        return Long.compare(order1, order2);
    }
}
