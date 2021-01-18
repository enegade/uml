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
import com.sun.jdi.event.Event;

/**
 * @author enegade
 */
abstract public class ThreadableDebuggerEvent<E extends Event> extends DebuggerEvent<E>
{
    public ThreadableDebuggerEvent(E event)
    {
        super(event);
    }

    public abstract ThreadReference getThreadReference();
}
