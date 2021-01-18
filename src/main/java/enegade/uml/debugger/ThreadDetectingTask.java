package enegade.uml.debugger;

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

import com.sun.jdi.*;
import enegade.uml.type.MethodCall;
import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.jdi.ThreadStartDebuggerEvent;
import enegade.uml.state.*;
import enegade.uml.type.MethodInfo;
import enegade.uml.type.ThreadHelper;
import enegade.uml.type.ThreadInfo;

import java.util.List;
import java.util.Map;

/**
 * @author enegade
 */
public class ThreadDetectingTask implements InitialStateHandlerTask<ThreadStartDebuggerEvent>
{
    private Map<Long, ThreadInfo> threadInfoMap;

    private ThreadInfo threadInfo;

    public ThreadDetectingTask(ThreadInfo threadInfo, Map<Long, ThreadInfo> threadInfoMap)
    {
        this.threadInfo = threadInfo;
        this.threadInfoMap = threadInfoMap;
    }

    public void handleInitialState(ThreadStartDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        ReferenceType threadReferenceType = null;
        for ( ReferenceType candidate : debuggerEvent.getEvent().virtualMachine().allClasses() ) {
            if ( candidate.isPrepared() && candidate.name().equals("java.lang.Thread") ) {
                threadReferenceType = candidate;
                break;
            }
        }

        Method method = null;
        for (Method candidate : threadReferenceType.methods()) {
            String name = MethodCall.getSelectorName( new MethodInfo(candidate) );
            if (name.equals("void <init>(java.lang.ThreadGroup, java.lang.Runnable, java.lang.String, long, java.security.AccessControlContext, boolean)")) {
                method = candidate;
                break;
            }
        }

        Location location;
        try {
            List<Location> locations = method.locationsOfLine(456);
            location = locations.get( locations.size() - 1 );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        TaskStateChanger<BreakpointDebuggerEvent> changer = new TaskStateChanger<>(this::handleCreatingBreakpointState, this);
        BreakpointRequestBuilder requestBuilder = BreakpointRequestBuilder.from()
                .setStateChanger(changer)
                .setEventsRequested(true)
                .setLocation(location)
                .setOrder( this.threadInfo.getObjectId() );

        stateBuilder.addRequest(requestBuilder);




        method = null;
        for (Method candidate : threadReferenceType.methods()) {
            String name = MethodCall.getSelectorName( new MethodInfo(candidate));
            if (name.equals("void start()")) {
                method = candidate;
                break;
            }
        }

        try {
            location = method.allLineLocations().get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        changer = new TaskStateChanger<>(this::handleStartingBreakpointState, this);
        requestBuilder = BreakpointRequestBuilder.from()
                .setStateChanger(changer)
                .setEventsRequested(true)
                .setLocation(location)
                .setOrder( this.threadInfo.getObjectId() + 1 );

        stateBuilder.addRequest(requestBuilder);
    }

    public void handleCreatingBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        long threadObjectId = ThreadHelper.getLocationThreadObjectId(debuggerEvent);

        ThreadInfo threadInfo = new ThreadInfo(threadObjectId);
        threadInfo.setCreatedBy(this.threadInfo);

        this.threadInfoMap.put(threadObjectId, threadInfo);

        stateBuilder.setChangeState(false);
    }

    public void handleStartingBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        long threadObjectId = ThreadHelper.getLocationThreadObjectId(debuggerEvent);

        ThreadInfo threadInfo = this.threadInfoMap.get(threadObjectId);
        if(threadInfo == null) {
            threadInfo = new ThreadInfo(threadObjectId);

            this.threadInfoMap.put(threadObjectId, threadInfo);
        }

        threadInfo.setRanBy(this.threadInfo);

        stateBuilder.setChangeState(false);
    }
}
