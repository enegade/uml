package enegade.uml.state;

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
import enegade.uml.jdi.DebuggerEvent;

/**
 * @author enegade
 */
public class InitialStateHandlerWrapper< D extends DebuggerEvent<?> > implements InitialStateHandlerAdapter<D>
{
    private InitialStateHandlerTask<D> task;

    public InitialStateHandlerWrapper(InitialStateHandlerTask<D> task)
    {
        this.task = task;
    }

    @Override
    public Task getTask()
    {
        return this.task;
    }

    @Override
    public void handleInitialState(D debuggingEvent, StateBuilder stateBuilder)
    {
        this.task.handleInitialState(debuggingEvent, stateBuilder);
    }
}
