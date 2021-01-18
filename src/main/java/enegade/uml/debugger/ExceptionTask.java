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

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.ThreadStartEvent;
import enegade.uml.CallGraph;
import enegade.uml.type.ExceptionCase;
import enegade.uml.type.MethodCall;
import enegade.uml.jdi.ExceptionDebuggerEvent;
import enegade.uml.jdi.ThreadStartDebuggerEvent;
import enegade.uml.state.*;

/**
 * @author enegade
 */
public class ExceptionTask implements InitialStateHandlerTask<ThreadStartDebuggerEvent>
{
    private CallGraph callGraph;

    private boolean caught = false;

    public ExceptionTask(CallGraph callGraph)
    {
        this.callGraph = callGraph;
    }

    public void handleInitialState(ThreadStartDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        ThreadStartEvent event = debuggerEvent.getEvent();
        for (ReferenceType refT : event.virtualMachine().allClasses()) {
            if (refT.isPrepared() && refT.name().equals("java.lang.Throwable")) {
                TaskStateChanger<ExceptionDebuggerEvent> changer = new TaskStateChanger<>(this::handleExceptionState, this);
                ExceptionRequestBuilder requestBuilder = ExceptionRequestBuilder.from()
                        .setStateChanger(changer)
                        .setEventsRequested(true)
                        .setNotifyCaught(true)
                        .setNotifyUncaught(true)
                        .setRefType(refT);

                stateBuilder.addRequest(requestBuilder);

                break;
            }
        }
    }

    public void handleExceptionState(ExceptionDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        stateBuilder.setChangeState(false);
        this.caught = false;

        if( this.callGraph.isRoot() ) {
            return;
        }

        ExceptionEvent event = debuggerEvent.getEvent();

        Location location = event.catchLocation();
        if(location == null) {
            return;
        }


        int hashCode = location.method().hashCode();

        CallGraph.EntranceView entranceView = this.callGraph.getEntranceView();
        MethodCall methodCall = entranceView.getMethodCall().get();
        while ( !entranceView.isRoot() && methodCall.getHashCode() != hashCode ) {
            entranceView = entranceView.getOpener().get();
            if( !entranceView.isRoot() ) {
                methodCall = entranceView.getMethodCall().get();
            }
        }

        if( !entranceView.isRoot() ) {
            ExceptionCase exception = new ExceptionCase(event);
            this.callGraph.addThrewException(exception);

            entranceView = this.callGraph.getEntranceView();
            methodCall = entranceView.getMethodCall().get();
            while ( methodCall.getHashCode() != hashCode ) {
                this.callGraph.closeEntrance();
                entranceView = this.callGraph.getEntranceView();
                methodCall = entranceView.getMethodCall().get();
            }

            this.callGraph.addCaughtException(exception);

            this.caught = true;
        } else {
            this.caught = false;
        }
    }

    public boolean isCaught()
    {
        return caught;
    }
}
