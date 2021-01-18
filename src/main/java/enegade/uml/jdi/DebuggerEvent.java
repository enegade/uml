package enegade.uml.jdi;

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

import com.sun.jdi.event.Event;
import enegade.uml.debugger.Task;

import java.util.Optional;

/**
 * @author enegade
 */
abstract public class DebuggerEvent<E extends Event>
{
    private E event;

    private Task task;

    public DebuggerEvent(E event)
    {
        this.event = event;
    }

    public E getEvent()
    {
        return this.event;
    }

    public Optional<Task> getTask()
    {
        return Optional.ofNullable(this.task);
    }

    public void setTask(Task task)
    {
        this.task = task;
    }

    public Class<? extends DebuggerEvent> getType()
    {
        return this.getClass();
    }

    public abstract boolean isSelfSpawn();
}
