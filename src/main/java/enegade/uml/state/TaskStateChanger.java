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
public class TaskStateChanger<D extends DebuggerEvent<?>> implements StateChanger<D>
{
    private Task task;

    private StateHandler<D> stateHandler;

    public TaskStateChanger(StateHandler<D> stateHandler, Task task)
    {
        this.stateHandler = stateHandler;
        this.task = task;
    }

    @Override
    public void change(D debuggerEvent, StateBuilder stateBuilder)
    {
        this.stateHandler.handleState(debuggerEvent, stateBuilder);
    }

    public Task getTask()
    {
        return this.task;
    }

    @Override
    public Task getToTask()
    {
        return getTask();
    }
}
