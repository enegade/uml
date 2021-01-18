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

import com.sun.jdi.request.VMDeathRequest;
import enegade.uml.debugger.Task;

/**
 * @author enegade
 */
public class VMDeathDebuggerRequest extends DebuggerRequest<VMDeathRequest, VMDeathDebuggerEvent>
{
    public VMDeathDebuggerRequest(Handler<VMDeathDebuggerEvent> handler)
    {
        super(handler);
    }

    public VMDeathDebuggerRequest(Handler<VMDeathDebuggerEvent> handler, VMDeathRequest eventRequest, Task task)
    {
        super(handler, eventRequest, task);
    }
}
