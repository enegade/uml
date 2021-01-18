package enegade.uml.debugger.loopexit;

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
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.LocatableEvent;
import enegade.uml.CallGraph;
import enegade.uml.type.MethodCall;
import enegade.uml.debugger.DecoratedTask;
import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.BreakpointRequestBuilder;
import enegade.uml.state.MethodExitRequestBuilder;
import enegade.uml.state.StateBuilder;
import enegade.uml.state.TaskStateChanger;
import enegade.uml.type.StepInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author enegade
 */
public class LoopExitTaskImpl implements LoopExitTask
{
    private MethodCall loopEndingMethodCall;

    private CallGraph callGraph;

    private boolean loopExited = false;

    private LoopExitTask decorator;

    public LoopExitTaskImpl(CallGraph callGraph)
    {
        this.callGraph = callGraph;
        this.decorator = this;
    }

    @Override
    public void setDecorator(DecoratedTask decorator)
    {
        this.decorator = (LoopExitTask) decorator;
    }

    @Override
    public void setLoopEndingMethodCall(MethodCall loopEndingMethodCall)
    {
        this.loopEndingMethodCall = loopEndingMethodCall;
    }

    @Override
    public void handleInitialState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        this.loopExited = false;

        if( this.decorator.isConditionStarted() ) {
            return;
        }

        ThreadReference threadReference = debuggerEvent.getThreadReference();

        StackFrame pFrame;
        try{
            pFrame = threadReference.frame(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Location pLocation = pFrame.location();

        List<Location> methodLocations;
        try {
            methodLocations = pLocation.method().allLineLocations();
        } catch (Exception e) {
            int stop = 1;
            methodLocations = new ArrayList<>();
        }

        CallGraph.EntranceView entranceView = this.callGraph.getEntranceView();
        List<StepInfo> stepInfos = new ArrayList<>( entranceView.getStepInfoList() );
        stepInfos.sort( Comparator.comparingInt(StepInfo::getLineNumber).reversed() );

        for (Location methodLocation : methodLocations) {
            if (methodLocation.lineNumber() <= this.loopEndingMethodCall.getCallLineNumber()) {
                continue;
            }

            if( methodLocation.lineNumber() <= stepInfos.get(0).getLineNumber() ) {
                continue;
            }

            TaskStateChanger<BreakpointDebuggerEvent> changer = new TaskStateChanger<>(this.decorator::handleBreakpointState, this.decorator);
            BreakpointRequestBuilder requestBuilder = BreakpointRequestBuilder.from()
                    .setStateChanger(changer)
                    .setEventsRequested(true)
                    .setLocation(methodLocation);

            stateBuilder.addRequest(requestBuilder);
        }

        MethodCall methodCall = this.callGraph.getEntranceView().getMethodCall().get();
        TaskStateChanger<MethodExitDebuggerEvent> changer = new TaskStateChanger<>(this.decorator::handleMethodExitState, this.decorator);
        MethodExitRequestBuilder requestBuilder = MethodExitRequestBuilder.from()
                .setStateChanger(changer)
                .setEventsRequested(true)
                .addClassFilter( methodCall.getTypeName() )
                .setOrder(2);

        stateBuilder.addRequest(requestBuilder);
    }

    @Override
    public void handleBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        LocatableEvent event = debuggerEvent.getEvent();
        if ( this.callGraph.isFrameSame(event) ) {
            this.loopExited = true;
        } else {
            this.loopExited = false;

            stateBuilder.setChangeState(false);
        }
    }

    @Override
    public void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        LocatableEvent event = debuggerEvent.getEvent();
        if( this.callGraph.isFrameSame(event) ) {
            this.callGraph.closeEntrance();
            this.loopExited = true;
        } else {
            this.loopExited = false;

            stateBuilder.setChangeState(false);
        }
    }

    @Override
    public boolean isLoopExited()
    {
        return loopExited;
    }

    @Override
    public boolean isExceptionCaught()
    {
        return false;
    }

    @Override
    public boolean isConditionStarted()
    {
        return false;
    }
}
