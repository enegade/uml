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
import com.sun.jdi.event.ThreadStartEvent;

/**
 * @author enegade
 */
public class ThreadStartDebuggerEvent extends ThreadableDebuggerEvent<ThreadStartEvent>
{
    public ThreadStartDebuggerEvent(ThreadStartEvent event)
    {
        super(event);
    }

    @Override
    public ThreadReference getThreadReference()
    {
        return getEvent().thread();
    }

    @Override
    public boolean isSelfSpawn()
    {
        return false;
    }
}
