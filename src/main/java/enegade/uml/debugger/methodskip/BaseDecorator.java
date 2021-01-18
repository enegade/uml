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
import enegade.uml.debugger.BaseTaskDecorator;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;

/**
 * @author enegade
 */
public class BaseDecorator extends BaseTaskDecorator implements MethodSkipTask
{
    protected MethodSkipTask task;

    public BaseDecorator(MethodSkipTask task)
    {
        super(task);

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
    }

    @Override
    public void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        task.handleMethodExitState(debuggerEvent, stateBuilder);
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
    public boolean isConditionStarted()
    {
        return task.isConditionStarted();
    }
}
