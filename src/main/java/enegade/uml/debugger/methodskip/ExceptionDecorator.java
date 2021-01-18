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
import enegade.uml.debugger.BaseExceptionDecorator;
import enegade.uml.debugger.ExceptionTask;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;

/**
 * @author enegade
 */
public class ExceptionDecorator  extends BaseExceptionDecorator implements MethodSkipTask
{
    private MethodSkipTask task;

    public ExceptionDecorator(MethodSkipTask task, ExceptionTask exceptionTask)
    {
        super(task, exceptionTask);

        this.task = task;
    }

    @Override
    public void handleInitialState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        task.handleInitialState(debuggerEvent, stateBuilder);

        this.exceptionCaught = false;

        addExceptionRequest(stateBuilder);
    }

    @Override
    public void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        task.handleMethodExitState(debuggerEvent, stateBuilder);

        this.exceptionCaught = false;
    }

    @Override
    public void setMethodCall(MethodCall methodCall)
    {
        task.setMethodCall(methodCall);
    }

    @Override
    public boolean isMethodSkipped()
    {
        return task.isMethodSkipped();
    }

    @Override
    public boolean isConditionStarted()
    {
        return task.isConditionStarted();
    }
}
