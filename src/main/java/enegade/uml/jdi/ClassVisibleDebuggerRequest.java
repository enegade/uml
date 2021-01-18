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
import com.sun.jdi.request.EventRequest;
import enegade.uml.debugger.Task;

/**
 * @author enegade
 */
public abstract class ClassVisibleDebuggerRequest< R extends EventRequest, D extends DebuggerEvent<?> > extends ThreadVisibleDebuggerRequest<R, D>
{
    public ClassVisibleDebuggerRequest(Handler<D> handler)
    {
        super(handler);
    }

    public ClassVisibleDebuggerRequest(Handler<D> handler, R eventRequest, Task task)
    {
        super(handler, eventRequest, task);
    }

    public abstract void addClassFilter(ReferenceType clazz);

    public abstract void addClassFilter(String classPattern);
}
