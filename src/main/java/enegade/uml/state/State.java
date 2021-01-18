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
import enegade.uml.debugger.Task;
import enegade.uml.jdi.DebuggerEvent;
import enegade.uml.jdi.DebuggerRequest;
import enegade.uml.EventLoop;

import java.util.ArrayList;
import java.util.List;

/**
 * @author enegade
 */
public class State
{
    List< DebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>> > debuggerRequests = new ArrayList<>();

    private Task task;

    public State(Task task)
    {
        this.task = task;
    }

    public void addDebuggerRequest(DebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>> debuggerRequest)
    {
        this.debuggerRequests.add(debuggerRequest);
    }

    public boolean isEmpty()
    {
        return this.debuggerRequests.isEmpty();
    }

    public void enable(EventLoop eventLoop)
    {
        for( DebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>> debuggerRequest : this.debuggerRequests ) {
            debuggerRequest.enable();

            eventLoop.addDebuggerRequest(debuggerRequest);
        }
    }

    public void disable(EventLoop eventLoop)
    {
        for( DebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>> debuggerRequest : this.debuggerRequests ) {
            debuggerRequest.disable();

            eventLoop.removeDebuggerRequest(debuggerRequest);
        }
    }
}
