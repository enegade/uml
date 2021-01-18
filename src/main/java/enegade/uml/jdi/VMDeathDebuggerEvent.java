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

import com.sun.jdi.event.VMDeathEvent;

/**
 * @author enegade
 */
public class VMDeathDebuggerEvent extends DebuggerEvent<VMDeathEvent>
{
    public VMDeathDebuggerEvent(VMDeathEvent event)
    {
        super(event);
    }

    @Override
    public boolean isSelfSpawn()
    {
        return true;
    }
}
