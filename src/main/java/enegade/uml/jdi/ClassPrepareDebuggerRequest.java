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
import com.sun.jdi.request.ClassPrepareRequest;
import enegade.uml.debugger.Task;
import enegade.uml.excepion.UnsupportedOperationException;

/**
 * @author enegade
 */
public class ClassPrepareDebuggerRequest extends ClassVisibleDebuggerRequest<ClassPrepareRequest, ClassPrepareDebuggerEvent>
{
    public ClassPrepareDebuggerRequest(Handler<ClassPrepareDebuggerEvent> handler)
    {
        super(handler);
    }

    public ClassPrepareDebuggerRequest(Handler<ClassPrepareDebuggerEvent> handler, ClassPrepareRequest eventRequest, Task task)
    {
        super(handler, eventRequest, task);
    }

    @Override
    public void addThreadFilter(ThreadReference thread)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void addThreadFilterToRequest(ThreadReference thread)
    {
        throw new UnsupportedOperationException();
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
