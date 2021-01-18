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
import com.sun.jdi.request.MethodEntryRequest;
import enegade.uml.debugger.Task;

/**
 * @author enegade
 */
public class MethodEntryDebuggerRequest extends ClassVisibleDebuggerRequest<MethodEntryRequest, MethodEntryDebuggerEvent>
{
    public MethodEntryDebuggerRequest(Handler<MethodEntryDebuggerEvent> handler)
    {
        super(handler);
    }

    public MethodEntryDebuggerRequest(Handler<MethodEntryDebuggerEvent> handler, MethodEntryRequest eventRequest, Task task)
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
        this.eventRequest.addClassFilter(clazz);
    }

    @Override
    public void addClassFilter(String classPattern)
    {
        this.eventRequest.addClassFilter(classPattern);
    }
}
