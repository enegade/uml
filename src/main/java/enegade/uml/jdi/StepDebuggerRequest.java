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
import com.sun.jdi.request.StepRequest;
import enegade.uml.debugger.Task;
import enegade.uml.excepion.UnsupportedOperationException;

/**
 * @author enegade
 */
public class StepDebuggerRequest extends ClassVisibleDebuggerRequest<StepRequest, StepDebuggerEvent>
{
    public StepDebuggerRequest(Handler<StepDebuggerEvent> handler)
    {
        super(handler);
    }

    public StepDebuggerRequest(Handler<StepDebuggerEvent> handler, StepRequest eventRequest, Task task)
    {
        super(handler, eventRequest, task);
    }

    @Override
    public void addThreadFilter(ThreadReference thread)
    {
        if(this.eventRequest == null) {
            this.threadReferences.add(thread);
        } else {
            throw new UnsupportedOperationException();
        }
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
