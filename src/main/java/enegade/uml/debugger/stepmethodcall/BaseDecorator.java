package enegade.uml.debugger.stepmethodcall;

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

import com.sun.jdi.ThreadReference;
import enegade.uml.type.MethodCall;
import enegade.uml.debugger.BaseTaskDecorator;
import enegade.uml.debugger.ConditionTask;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;
import enegade.uml.type.StepInfo;

import java.util.Optional;

/**
 * @author enegade
 */
public class BaseDecorator extends BaseTaskDecorator implements StepMethodCallTask
{
    protected StepMethodCallTask task;

    public BaseDecorator(StepMethodCallTask task)
    {
        super(task);

        this.task = task;
    }

    @Override
    public void handleInitialState(ThreadReference thread, StateBuilder stateBuilder)
    {
        task.handleInitialState(thread, stateBuilder);
    }

    @Override
    public void handleStepState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        task.handleStepState(debuggerEvent, stateBuilder);
    }

    @Override
    public boolean isMethodEnter()
    {
        return this.task.isMethodEnter();
    }

    @Override
    public boolean isMethodExit()
    {
        return this.task.isMethodExit();
    }

    @Override
    public Optional<MethodCall> getNewEntrance()
    {
        return this.task.getNewEntrance();
    }

    @Override
    public StepInfo getLastStepInfo()
    {
        return this.task.getLastStepInfo();
    }

    @Override
    public boolean isMethodSkipped()
    {
        return this.task.isMethodSkipped();
    }

    @Override
    public boolean isLoopDetected()
    {
        return this.task.isLoopDetected();
    }

    @Override
    public boolean isConditionExited()
    {
        return this.task.isConditionExited();
    }

    @Override
    public boolean isStarted()
    {
        return this.task.isStarted();
    }

    @Override
    public ConditionTask.CONDITION_SOURCE getConditionSource()
    {
        return this.task.getConditionSource();
    }
}
