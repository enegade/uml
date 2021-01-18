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

import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.EventRequest;
import enegade.uml.debugger.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @author enegade
 */
abstract public class ThreadVisibleDebuggerRequest< R extends EventRequest, D extends DebuggerEvent<?> > extends DebuggerRequest<R, D>
{
    protected List<ThreadReference> threadReferences = new ArrayList<>();

    public ThreadVisibleDebuggerRequest(Handler<D> handler)
    {
        super(handler);
    }

    public ThreadVisibleDebuggerRequest(Handler<D> handler, R eventRequest, Task task)
    {
        super(handler, eventRequest, task);
    }

    public void addThreadFilter(ThreadReference thread)
    {
        addThreadFilterToRequest(thread);

        this.threadReferences.add(thread);
    }

    public List<ThreadReference> getThreadFilters()
    {
        return List.copyOf(this.threadReferences);
    }

    protected abstract void addThreadFilterToRequest(ThreadReference thread);
}
