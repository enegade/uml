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

import enegade.uml.debugger.Task;
import enegade.uml.jdi.ExceptionDebuggerEvent;
import enegade.uml.state.InitialStateHandlerAdapter;
import enegade.uml.state.StateBuilder;

/**
 * @author enegade
 */
public class ExceptionHandlerAdapter implements InitialStateHandlerAdapter<ExceptionDebuggerEvent>
{
    private StepMethodCallTask task;

    public ExceptionHandlerAdapter(StepMethodCallTask task)
    {
        this.task = task;
    }

    @Override
    public Task getTask()
    {
        return this.task;
    }

    @Override
    public void handleInitialState(ExceptionDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        this.task.handleInitialState(debuggerEvent.getThreadReference(), stateBuilder);
    }
}
