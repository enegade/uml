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

import enegade.uml.type.MethodCall;
import enegade.uml.debugger.BaseTaskDecorator;
import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;

/**
 * @author enegade
 */
public class BaseDecorator extends BaseTaskDecorator implements LoopExitTask
{
    protected LoopExitTask task;

    public BaseDecorator(LoopExitTask task)
    {
        super(task);

        this.task = task;
    }

    @Override
    public void setLoopEndingMethodCall(MethodCall loopStartingMethodCall)
    {
        task.setLoopEndingMethodCall(loopStartingMethodCall);
    }

    @Override
    public void handleInitialState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        task.handleInitialState(debuggerEvent, stateBuilder);
    }

    @Override
    public void handleBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        task.handleBreakpointState(debuggerEvent, stateBuilder);
    }

    @Override
    public void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        task.handleMethodExitState(debuggerEvent, stateBuilder);
    }

    @Override
    public boolean isLoopExited()
    {
        return task.isLoopExited();
    }

    @Override
    public boolean isExceptionCaught()
    {
        return task.isExceptionCaught();
    }

    @Override
    public boolean isConditionStarted()
    {
        return task.isConditionStarted();
    }
}
