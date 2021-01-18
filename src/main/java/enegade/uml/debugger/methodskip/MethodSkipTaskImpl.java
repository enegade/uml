package enegade.uml.debugger.methodskip;

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
import com.sun.jdi.event.MethodExitEvent;
import enegade.uml.CallGraph;
import enegade.uml.type.MethodCall;
import enegade.uml.debugger.DecoratedTask;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.MethodExitRequestBuilder;
import enegade.uml.state.StateBuilder;
import enegade.uml.state.TaskStateChanger;

/**
 * @author enegade
 */
public class MethodSkipTaskImpl implements MethodSkipTask
{
    private MethodCall methodCall;

    private boolean methodSkipped = false;

    private CallGraph callGraph;

    private MethodSkipTask decorator;

    public MethodSkipTaskImpl(CallGraph callGraph)
    {
        this.callGraph = callGraph;
        this.decorator = this;
    }

    @Override
    public void setDecorator(DecoratedTask decorator)
    {
        this.decorator = (MethodSkipTask) decorator;
    }

    public void setMethodCall(MethodCall methodCall)
    {
        this.methodCall = methodCall;
    }

    public void handleInitialState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        this.methodSkipped = false;

        if( this.decorator.isConditionStarted() ) {
            return;
        }

        TaskStateChanger<MethodExitDebuggerEvent> changer = new TaskStateChanger<>(this.decorator::handleMethodExitState, this.decorator);
        MethodExitRequestBuilder requestBuilder = MethodExitRequestBuilder.from()
                .setStateChanger(changer)
                .setEventsRequested(true)
                .addClassFilter( methodCall.getTypeName() )
                .setOrder(2);

        stateBuilder.addRequest(requestBuilder);
    }

    public void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        MethodExitEvent event = debuggerEvent.getEvent();
        if ( this.callGraph.isFrameSame(event) ) {
            this.methodSkipped = true;

            StackFrame frame;
            try {
                frame = event.thread().frame(0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Location location = frame.location();
            String name = location.method().name();

            MethodCall methodCall = this.callGraph.getEntranceView().getMethodCall().get();
            methodCall.setExitMethodName(name);

            this.callGraph.closeEntrance();
        } else {
            this.methodSkipped = false;

            stateBuilder.setChangeState(false);
        }
    }

    public boolean isMethodSkipped()
    {
        return methodSkipped;
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
