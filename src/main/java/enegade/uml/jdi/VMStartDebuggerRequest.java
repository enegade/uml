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

import com.sun.jdi.request.EventRequest;

/**
 * @author enegade
 */
public class VMStartDebuggerRequest extends DebuggerRequest<EventRequest, VMStartDebuggerEvent>
{
    public VMStartDebuggerRequest(Handler<VMStartDebuggerEvent> handler)
    {
        super(handler);
    }
}
