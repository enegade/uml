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
import enegade.uml.jdi.ThreadStartDebuggerEvent;
import enegade.uml.state.InitialStateHandlerAdapter;
import enegade.uml.state.StateBuilder;

/**
 * @author enegade
 */
public class ThreadStartHandlerAdapter implements InitialStateHandlerAdapter<ThreadStartDebuggerEvent>
{
    private StepMethodCallTask task;

    public ThreadStartHandlerAdapter(StepMethodCallTask task)
    {
        this.task = task;
    }

    @Override
    public Task getTask()
    {
        return this.task;
    }

    @Override
    public void handleInitialState(ThreadStartDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        this.task.handleInitialState(debuggerEvent.getThreadReference(), stateBuilder);
    }
}
