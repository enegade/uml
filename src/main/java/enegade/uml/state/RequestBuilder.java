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

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import enegade.uml.debugger.Task;
import enegade.uml.jdi.DebuggerEvent;
import enegade.uml.jdi.DebuggerRequest;
import enegade.uml.jdi.Handler;

import java.util.Optional;

/**
 * @author enegade
 */
abstract public class RequestBuilder<D extends DebuggerEvent<?>, B extends RequestBuilder<D, B>>
{
    private TaskStateChanger<D> stateChanger;

    private Handler<D> handler;

    private Task task;

    private boolean eventsRequested = false;

    private long order = 0;

    abstract protected B getThis();

    protected void copy(RequestBuilder<D, B> copy)
    {
        copy.stateChanger = this.stateChanger;
        copy.handler = this.handler;
        copy.task = this.task;
        copy.eventsRequested = this.eventsRequested;
        copy.order = this.order;
    }

    public abstract B getCopy();

    public long getOrder()
    {
        return order;
    }

    public B setOrder(long order)
    {
        this.order = order;

        return getThis();
    }

    public Optional<Task> getTask()
    {
        return Optional.ofNullable(this.task);
    }

    public B setTask(Task task)
    {
        this.task = task;

        return getThis();
    }

    public Optional< Handler<D> > getHandler()
    {
        return Optional.ofNullable(this.handler);
    }

    public B setHandler(Handler<D> handler)
    {
        this.handler = handler;

        return getThis();
    }

    public Optional< TaskStateChanger<D> > getStateChanger()
    {
        return Optional.ofNullable(this.stateChanger);
    }

    public B setStateChanger(TaskStateChanger<D> stateChanger)
    {
        this.stateChanger = stateChanger;

        return getThis();
    }

    public boolean isEventsRequested()
    {
        return eventsRequested;
    }

    public B setEventsRequested(boolean eventsRequested)
    {
        this.eventsRequested = eventsRequested;

        return getThis();
    }

    public DebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>> build(EventRequestManager requestManager)
    {
        DebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>> debuggerRequest;
        if( this.isEventsRequested() ) {
            debuggerRequest = this.createWithEventsRequested(requestManager);
        } else {
            debuggerRequest = this.createWithoutEventsRequested(requestManager);
        }

        debuggerRequest.setOrder(this.order);

        return debuggerRequest;
    }

    abstract protected DebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>>
    createWithEventsRequested(EventRequestManager requestManager);

    abstract protected DebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>>
    createWithoutEventsRequested(EventRequestManager requestManager);
}
