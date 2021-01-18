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

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.BreakpointRequest;
import enegade.uml.debugger.Task;
import enegade.uml.excepion.UnsupportedOperationException;

/**
 * @author enegade
 */
public class BreakpointDebuggerRequest extends ClassVisibleDebuggerRequest<BreakpointRequest, BreakpointDebuggerEvent>
{
    public BreakpointDebuggerRequest(Handler<BreakpointDebuggerEvent> handler)
    {
        super(handler);
    }

    public BreakpointDebuggerRequest(Handler<BreakpointDebuggerEvent> handler, BreakpointRequest eventRequest, Task task)
    {
        super(handler, eventRequest, task);
    }

    @Override
    protected void addThreadFilterToRequest(ThreadReference thread)
    {
        if( this.eventRequest != null ) {
            this.eventRequest.addThreadFilter(thread);
        }
    }

    @Override
    public void addClassFilter(ReferenceType clazz)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addClassFilter(String classPattern)
    {
        throw new UnsupportedOperationException();
    }
}
