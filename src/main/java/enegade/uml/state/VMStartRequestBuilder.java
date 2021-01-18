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
import enegade.uml.jdi.DebuggerRequest;
import enegade.uml.jdi.VMStartDebuggerEvent;
import enegade.uml.jdi.VMStartDebuggerRequest;

/**
 * @author enegade
 */
public class VMStartRequestBuilder extends RequestBuilder<VMStartDebuggerEvent, VMStartRequestBuilder>
{
    public static VMStartRequestBuilder from()
    {
        return new VMStartRequestBuilder();
    }

    @Override
    protected VMStartRequestBuilder getThis() {
        return this;
    }

    @Override
    public VMStartRequestBuilder getCopy()
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected DebuggerRequest<? extends EventRequest, VMStartDebuggerEvent>
    createWithEventsRequested(EventRequestManager requestManager)
    {
        throw new RuntimeException();
    }

    @Override
    protected DebuggerRequest<? extends EventRequest, VMStartDebuggerEvent>
    createWithoutEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<? extends EventRequest, VMStartDebuggerEvent> debuggerRequest;
        debuggerRequest = new VMStartDebuggerRequest( this.getHandler().get() );

        return debuggerRequest;
    }
}
