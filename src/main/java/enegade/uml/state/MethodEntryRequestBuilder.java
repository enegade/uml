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

import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import enegade.uml.jdi.DebuggerRequest;
import enegade.uml.jdi.MethodEntryDebuggerEvent;
import enegade.uml.jdi.MethodEntryDebuggerRequest;

/**
 * @author enegade
 */
public class MethodEntryRequestBuilder extends ClassVisibleRequestBuilder<MethodEntryDebuggerEvent, MethodEntryRequestBuilder>
{
    public static MethodEntryRequestBuilder from()
    {
        return new MethodEntryRequestBuilder();
    }

    @Override
    protected MethodEntryRequestBuilder getThis() {
        return this;
    }

    @Override
    public MethodEntryRequestBuilder getCopy()
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected DebuggerRequest<MethodEntryRequest, MethodEntryDebuggerEvent>
    createWithEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<MethodEntryRequest, MethodEntryDebuggerEvent> debuggerRequest;
        MethodEntryRequest request = requestManager.createMethodEntryRequest();
        debuggerRequest = new MethodEntryDebuggerRequest( this.getHandler().get(), request, this.getTask().get() );

        return debuggerRequest;
    }

    @Override
    protected DebuggerRequest<MethodEntryRequest, MethodEntryDebuggerEvent>
    createWithoutEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<MethodEntryRequest, MethodEntryDebuggerEvent> debuggerRequest;
        debuggerRequest = new MethodEntryDebuggerRequest( this.getHandler().get() );

        return debuggerRequest;
    }
}
