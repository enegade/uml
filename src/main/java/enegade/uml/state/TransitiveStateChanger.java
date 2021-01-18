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
public class TransitiveStateChanger<D extends DebuggerEvent<?>, T extends Task> implements StateChanger<D>
{
    private T fromTask;

    private Task toTask;

    private TransitionCondition<T> transitionCondition;

    InitialStateHandlerAdapter<D> initialStateHandlerAdapter;

    private Class<D> debuggerEventType;

    public TransitiveStateChanger(T fromTask, InitialStateHandlerTask<D> toTask,
                                  TransitionCondition<T> transitionCondition,
                                  Class<D> debuggerEventType)
    {
        this(fromTask, new InitialStateHandlerWrapper<>(toTask), transitionCondition, debuggerEventType);
    }

    public TransitiveStateChanger(T fromTask, InitialStateHandlerAdapter<D> initialStateHandlerAdapter,
                                  TransitionCondition<T> transitionCondition,
                                  Class<D> debuggerEventType)
    {
        this.fromTask = fromTask;
        this.toTask = initialStateHandlerAdapter.getTask();
        this.transitionCondition = transitionCondition;
        this.initialStateHandlerAdapter = initialStateHandlerAdapter;
        this.debuggerEventType = debuggerEventType;
    }

    public Task getFromTask()
    {
        return this.fromTask;
    }

    @Override
    public Task getToTask()
    {
        return this.toTask;
    }

    public Class<D> getDebuggerEventType()
    {
        return this.debuggerEventType;
    }

    public boolean test()
    {
        return this.transitionCondition.test(this.fromTask);
    }

    @Override
    public void change(D debuggerEvent, StateBuilder stateBuilder)
    {
        this.initialStateHandlerAdapter.handleInitialState(debuggerEvent, stateBuilder);
    }
}
