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

import enegade.uml.type.MethodCall;
import enegade.uml.debugger.BaseConditionDecorator;
import enegade.uml.debugger.ConditionTask;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;

/**
 * @author enegade
 */
public class ConditionDecorator extends BaseConditionDecorator implements MethodSkipTask
{
    private MethodSkipTask task;

    public ConditionDecorator(MethodSkipTask task, ConditionTask conditionTask)
    {
        super(task, conditionTask);

        this.task = task;
    }

    @Override
    public void setMethodCall(MethodCall methodCall)
    {
        task.setMethodCall(methodCall);
    }

    @Override
    public void handleInitialState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        task.handleInitialState(debuggerEvent, stateBuilder);

        processConditionState(debuggerEvent, stateBuilder);
    }

    @Override
    public void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        task.handleMethodExitState(debuggerEvent, stateBuilder);

        this.conditionStarted = false;
    }

    @Override
    public boolean isMethodSkipped()
    {
        return task.isMethodSkipped();
    }

    @Override
    public boolean isExceptionCaught()
    {
        return task.isExceptionCaught();
    }

    @Override
    protected ConditionTask.CONDITION_SOURCE getConditionSource()
    {
        return ConditionTask.CONDITION_SOURCE.METHOD_SKIP;
    }
}
