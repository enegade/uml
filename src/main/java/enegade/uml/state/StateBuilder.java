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
import enegade.uml.excepion.LogicException;
import enegade.uml.jdi.DebuggerEvent;
import enegade.uml.jdi.DebuggerRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author enegade
 */
public class StateBuilder
{
    private List< RequestBuilder<? extends DebuggerEvent<?>, ?> > requestBuilders = new ArrayList<>();

    private boolean changeState = true;

    private Task task;

    public StateBuilder setTask(Task task)
    {
        this.task = task;

        return this;
    }

    public StateBuilder setChangeState(boolean changeState)
    {
        this.changeState = changeState;

        return this;
    }

    public boolean isChangeState()
    {
        return changeState;
    }

    public StateBuilder addRequest(RequestBuilder<? extends DebuggerEvent<?>, ?> fromBuilder)
    {
        this.requestBuilders.add(fromBuilder);

        return this;
    }

    public StateBuilder clearRequests()
    {
        this.requestBuilders = new ArrayList<>();

        this.changeState = true;

        return this;
    }

    public List< RequestBuilder<? extends DebuggerEvent<?>, ?> > getRequestBuilders()
    {
        return this.requestBuilders;
    }

    public State build(EventRequestManager requestManager)
    {
        if(this.task == null) {
            throw new LogicException();
        }

        State state = new State(this.task);

        for (RequestBuilder<? extends DebuggerEvent<?>, ?> requestBuilder : this.requestBuilders) {
            DebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>> debuggerRequest = requestBuilder.build(requestManager);
            state.addDebuggerRequest(debuggerRequest);
        }

        return state;
    }

    public boolean isEmpty()
    {
        return this.requestBuilders.isEmpty();
    }
}
