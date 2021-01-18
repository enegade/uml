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
import enegade.uml.debugger.BaseExceptionDecorator;
import enegade.uml.debugger.ExceptionTask;
import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;

/**
 * @author enegade
 */
public class ExceptionDecorator extends BaseExceptionDecorator implements LoopExitTask
{
    private LoopExitTask task;

    public ExceptionDecorator(LoopExitTask task, ExceptionTask exceptionTask)
    {
        super(task, exceptionTask);

        this.task = task;
    }

    @Override
    public void handleInitialState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        this.task.handleInitialState(debuggerEvent, stateBuilder);

        this.exceptionCaught = false;

        addExceptionRequest(stateBuilder);
    }

    @Override
    public void handleBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        this.task.handleBreakpointState(debuggerEvent, stateBuilder);

        this.exceptionCaught = false;
    }

    @Override
    public void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        this.task.handleMethodExitState(debuggerEvent, stateBuilder);

        this.exceptionCaught = false;
    }

    @Override
    public void setLoopEndingMethodCall(MethodCall loopStartingMethodCall)
    {
        task.setLoopEndingMethodCall(loopStartingMethodCall);
    }

    @Override
    public boolean isLoopExited()
    {
        return task.isLoopExited();
    }

    @Override
    public boolean isConditionStarted()
    {
        return task.isConditionStarted();
    }
}
