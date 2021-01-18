package enegade.uml.debugger;

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

import enegade.uml.state.StateChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author enegade
 */
public abstract class BaseDebugger implements Debugger
{
    protected List<StateChannel> stateChannels = new ArrayList<>();

    @Override
    public void stop()
    {
        for (StateChannel channel : this.stateChannels) {
            channel.clearState();
        }
    }
}
