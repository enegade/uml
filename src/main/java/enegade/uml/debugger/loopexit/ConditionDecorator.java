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
import enegade.uml.debugger.BaseConditionDecorator;
import enegade.uml.debugger.ConditionTask;
import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;

/**
 * @author enegade
 */
public class ConditionDecorator extends BaseConditionDecorator implements LoopExitTask
{
    private LoopExitTask task;

    public ConditionDecorator(LoopExitTask task, ConditionTask conditionTask)
    {
        super(task, conditionTask);

        this.task = task;
    }

    @Override
    public void handleInitialState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        this.task.handleInitialState(debuggerEvent, stateBuilder);

        processConditionState(debuggerEvent, stateBuilder);
    }

    @Override
    public void handleBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        task.handleBreakpointState(debuggerEvent, stateBuilder);

        this.conditionStarted = false;
    }

    @Override
    public void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        task.handleMethodExitState(debuggerEvent, stateBuilder);

        this.conditionStarted = false;
    }

    @Override
    protected ConditionTask.CONDITION_SOURCE getConditionSource()
    {
        return ConditionTask.CONDITION_SOURCE.LOOP_EXIT;
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
    public boolean isExceptionCaught()
    {
        return task.isExceptionCaught();
    }
}
