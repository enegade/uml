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

import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ThreadStartRequest;
import enegade.uml.excepion.UnsupportedOperationException;
import enegade.uml.jdi.DebuggerRequest;
import enegade.uml.jdi.ThreadStartDebuggerEvent;
import enegade.uml.jdi.ThreadStartDebuggerRequest;

/**
 * @author enegade
 */
public class ThreadStartRequestBuilder extends ThreadVisibleRequestBuilder<ThreadStartDebuggerEvent, ThreadStartRequestBuilder>
{
    public static ThreadStartRequestBuilder from()
    {
        return new ThreadStartRequestBuilder();
    }

    @Override
    protected ThreadStartRequestBuilder getThis() {
        return this;
    }

    @Override
    public ThreadStartRequestBuilder getCopy()
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected DebuggerRequest<ThreadStartRequest, ThreadStartDebuggerEvent>
    createWithEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<ThreadStartRequest, ThreadStartDebuggerEvent> debuggerRequest;
        ThreadStartRequest request = requestManager.createThreadStartRequest();
        debuggerRequest = new ThreadStartDebuggerRequest( this.getHandler().get(), request, this.getTask().get() );

        return debuggerRequest;
    }

    @Override
    protected DebuggerRequest<ThreadStartRequest, ThreadStartDebuggerEvent>
    createWithoutEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<ThreadStartRequest, ThreadStartDebuggerEvent> debuggerRequest;
        debuggerRequest = new ThreadStartDebuggerRequest( this.getHandler().get() );

        return debuggerRequest;
    }

    /**
     * Supporting has no sense
     */
    @Override
    public ThreadStartRequestBuilder addThreadFilter(ThreadReference thread)
    {
        throw new UnsupportedOperationException();
    }
}
