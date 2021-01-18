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

import com.sun.jdi.request.EventRequest;
import enegade.uml.debugger.Task;

import java.util.Optional;

/**
 * @author enegade
 */
abstract public class DebuggerRequest< R extends EventRequest, D extends DebuggerEvent<?> >
{
    protected R eventRequest;

    private Task task;

    private Handler<D> handler;

    private long order = 0;

    private boolean enabled = false;

    public long getOrder()
    {
        return this.order;
    }

    public void setOrder(long order)
    {
        this.order = order;
    }

    public DebuggerRequest(Handler<D> handler)
    {
        this.handler = handler;
    }

    public DebuggerRequest(Handler<D> handler, R eventRequest, Task task)
    {
        this(handler);

        eventRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);

        this.eventRequest = eventRequest;
        this.task = task;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void enable()
    {
        this.enabled = true;

        if( this.eventRequest != null ) {
            this.eventRequest.enable();
        }
    }

    public void disable()
    {
        this.enabled = false;

        if( this.eventRequest != null ) {
            this.eventRequest.disable();
        }
    }

    public Handler<D> getHandler()
    {
        return this.handler;
    }

    public Optional<Task> getTask()
    {
        return Optional.ofNullable(this.task);
    }

    public Optional<R> getRequest()
    {
        return Optional.ofNullable(this.eventRequest);
    }

    public boolean hasRequest()
    {
        return this.eventRequest != null;
    }
}
