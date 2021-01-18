package enegade.uml.state;

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
import com.sun.jdi.request.EventRequestManager;
import enegade.uml.debugger.Task;
import enegade.uml.excepion.LogicException;
import enegade.uml.jdi.DebuggerEvent;
import enegade.uml.EventLoop;
import enegade.uml.excepion.UnsupportedOperationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author enegade
 */
public class StateChannel
{
    private EventLoop eventLoop;

    private List< TransitiveStateChanger<?, ?> > stateChangers = new ArrayList<>();

    private static Map<Task, StateChannel> taskBindingMap = new HashMap<>();

    private State state;

    private ThreadReference threadReference;

    public StateChannel(EventLoop eventLoop)
    {
        this.eventLoop = eventLoop;
    }

    public void setThreadReference(ThreadReference threadReference)
    {
        this.threadReference = threadReference;
    }

    private EventRequestManager getRequestManager()
    {
        return this.eventLoop.getEventManager();
    }

    private void changeState(State state)
    {
        if(this.state != null) {
            this.state.disable(this.eventLoop);
        }

        state.enable(this.eventLoop);

        this.state = state;
    }

    public void clearState()
    {
        if(this.state == null) {
            return;
        }

        this.state.disable(this.eventLoop);
        this.state = null;
    }

    public void addStateChanger(TransitiveStateChanger<?, ?> stateChanger)
    {
        taskBindingValidate( stateChanger.getFromTask() );
        taskBindingValidate( stateChanger.getToTask() );

        this.stateChangers.add(stateChanger);
    }

    private void taskBindingValidate(Task task)
    {
        StateChannel channel = StateChannel.taskBindingMap.get(task);
        if(channel != null && channel != this) {
            throw new RuntimeException();
        } else {
            StateChannel.taskBindingMap.put(task, this);
        }
    }

    public void setStateFrom(InitialStateStartingHandler task)
    {
        if(this.state != null) {
            throw new LogicException();
        }

        taskBindingValidate(task);

        StateBuilder stateBuilder = new StateBuilder();
        task.handleInitialState(stateBuilder);

        State state = buildState(stateBuilder, task);
        if(state == null) {
            return;
        }

        changeState(state);
    }

    public <D extends DebuggerEvent<?>> void setStateFrom(InitialStateHandlerTask<D> task, D debuggerEvent)
    {
        InitialStateHandlerAdapter<D> adapter = new InitialStateHandlerWrapper<>(task);

        this.setStateFrom(adapter, debuggerEvent);
    }

    public <D extends DebuggerEvent<?>> void setStateFrom(InitialStateHandlerAdapter<D> initialStateHandlerAdapter, D debuggerEvent)
    {
        Task task = initialStateHandlerAdapter.getTask();
        taskBindingValidate(task);

        StateBuilder stateBuilder = new StateBuilder();
        initialStateHandlerAdapter.handleInitialState(debuggerEvent, stateBuilder);

        State state = buildState(stateBuilder, task);
        if(state == null) {
            return;
        }

        changeState(state);
    }

    private <D extends DebuggerEvent<?>> void setHandler(RequestBuilder<D, ?> requestBuilder)
    {
        requestBuilder.setHandler( debuggerEvent -> {
            this.handleState( debuggerEvent, requestBuilder.getStateChanger().get() );
        } );
    }

    private <D extends DebuggerEvent<?>> void handleState(D debuggerEvent, TaskStateChanger<D> stateChanger)
    {
        State state = getStateToChange(stateChanger, debuggerEvent);
        if(state == null) {
            return;
        }

        if( state.isEmpty() ) {
            Task task = stateChanger.getTask();

            List< TransitiveStateChanger<?, ?> > candidates = this.stateChangers.stream()
                    .filter( transitiveStateChanger -> transitiveStateChanger.getFromTask() == task )
                    .filter( transitiveStateChanger -> transitiveStateChanger.getDebuggerEventType() == debuggerEvent.getType() )
                    .filter( TransitiveStateChanger::test )
                    .collect( Collectors.toList() );
            if( candidates.size() != 1 ) {
                throw new RuntimeException();
            }

            @SuppressWarnings("unchecked")
            TransitiveStateChanger<D, ?> transitiveChanger = (TransitiveStateChanger<D, ?>) candidates.get(0);

            state = getStateToChange(transitiveChanger, debuggerEvent);

            if(state == null) {
                return;
            }
        }

        changeState(state);
    }

    private <D extends DebuggerEvent<?>> State getStateToChange(StateChanger<D> stateChanger, D debuggerEvent)
    {
        StateBuilder stateBuilder = new StateBuilder();
        stateChanger.change(debuggerEvent, stateBuilder);

        return buildState( stateBuilder, stateChanger.getToTask() );
    }

    private State buildState(StateBuilder stateBuilder, Task task)
    {
        if( !stateBuilder.isChangeState() ) {
            return null;
        }

        List< RequestBuilder<? extends DebuggerEvent<?>, ?> > requestBuilders = stateBuilder.getRequestBuilders();

        for (RequestBuilder<? extends DebuggerEvent<?>, ?> requestBuilder : requestBuilders) {
            setHandler(requestBuilder);

            requestBuilder.setTask(task);

            if( this.threadReference != null ) {
                if(requestBuilder instanceof ThreadVisibleRequestBuilder) {
                    ThreadVisibleRequestBuilder threadVisibleRequestBuilder = (ThreadVisibleRequestBuilder) requestBuilder;

                    if (!threadVisibleRequestBuilder.getThreadFilters().isEmpty()) {
                        throw new RuntimeException();
                    }

                    try {
                        threadVisibleRequestBuilder.addThreadFilter(this.threadReference);
                    } catch (UnsupportedOperationException e) {
                        int s = 1;
                    }
                }
            }
        }

        stateBuilder.setTask(task);

        return stateBuilder.build( getRequestManager() );
    }
}
