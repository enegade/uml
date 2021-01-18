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

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.jdi.BreakpointDebuggerRequest;
import enegade.uml.jdi.DebuggerRequest;
import enegade.uml.excepion.UnsupportedOperationException;

/**
 * @author enegade
 */
public class BreakpointRequestBuilder extends ClassVisibleRequestBuilder<BreakpointDebuggerEvent, BreakpointRequestBuilder>
{
    private Location location;

    public Location getLocation()
    {
        return location;
    }

    public BreakpointRequestBuilder setLocation(Location location)
    {
        this.location = location;

        return this;
    }

    public static BreakpointRequestBuilder from()
    {
        return new BreakpointRequestBuilder();
    }

    @Override
    protected BreakpointRequestBuilder getThis()
    {
        return this;
    }

    @Override
    public BreakpointRequestBuilder getCopy()
    {
        BreakpointRequestBuilder copy = new BreakpointRequestBuilder();
        this.copy(copy);
        copy.location = this.location;

        return copy;
    }

    @Override
    protected DebuggerRequest<BreakpointRequest, BreakpointDebuggerEvent>
    createWithEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<BreakpointRequest, BreakpointDebuggerEvent> debuggerRequest;
        BreakpointRequest request = requestManager.createBreakpointRequest( this.location );
        debuggerRequest = new BreakpointDebuggerRequest( this.getHandler().get(), request, this.getTask().get() );

        return debuggerRequest;
    }

    @Override
    protected DebuggerRequest<BreakpointRequest, BreakpointDebuggerEvent>
    createWithoutEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<BreakpointRequest, BreakpointDebuggerEvent> debuggerRequest;
        debuggerRequest = new BreakpointDebuggerRequest( this.getHandler().get() );

        return debuggerRequest;
    }

    @Override
    public BreakpointRequestBuilder addClassFilter(ReferenceType refType)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public BreakpointRequestBuilder addClassFilter(String classPattern)
    {
        throw new UnsupportedOperationException();
    }
}
