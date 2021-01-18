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
import com.sun.jdi.request.ThreadStartRequest;
import enegade.uml.debugger.Task;
import enegade.uml.excepion.UnsupportedOperationException;

/**
 * @author enegade
 */
public class ThreadStartDebuggerRequest extends ThreadVisibleDebuggerRequest<ThreadStartRequest, ThreadStartDebuggerEvent>
{
    public ThreadStartDebuggerRequest(Handler<ThreadStartDebuggerEvent> handler)
    {
        super(handler);
    }

    public ThreadStartDebuggerRequest(Handler<ThreadStartDebuggerEvent> handler, ThreadStartRequest eventRequest, Task task)
    {
        super(handler, eventRequest, task);
    }

    /**
     * Supporting has no sense
     */
    @Override
    protected void addThreadFilterToRequest(ThreadReference thread)
    {
        throw new UnsupportedOperationException();
    }
}
