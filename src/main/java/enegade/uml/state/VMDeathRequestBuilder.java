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
import com.sun.jdi.request.VMDeathRequest;
import enegade.uml.jdi.DebuggerRequest;
import enegade.uml.jdi.VMDeathDebuggerEvent;
import enegade.uml.jdi.VMDeathDebuggerRequest;

/**
 * @author enegade
 */
public class VMDeathRequestBuilder extends RequestBuilder<VMDeathDebuggerEvent, VMDeathRequestBuilder>
{
    public static VMDeathRequestBuilder from()
    {
        return new VMDeathRequestBuilder();
    }

    @Override
    protected VMDeathRequestBuilder getThis() {
        return this;
    }

    @Override
    public VMDeathRequestBuilder getCopy()
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected DebuggerRequest<VMDeathRequest, VMDeathDebuggerEvent>
    createWithEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<VMDeathRequest, VMDeathDebuggerEvent> debuggerRequest;
        VMDeathRequest request = requestManager.createVMDeathRequest();
        debuggerRequest = new VMDeathDebuggerRequest( this.getHandler().get(), request, this.getTask().get() );

        return debuggerRequest;
    }

    @Override
    protected DebuggerRequest<VMDeathRequest, VMDeathDebuggerEvent>
    createWithoutEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<VMDeathRequest, VMDeathDebuggerEvent> debuggerRequest;
        debuggerRequest = new VMDeathDebuggerRequest( this.getHandler().get() );

        return debuggerRequest;
    }
}
