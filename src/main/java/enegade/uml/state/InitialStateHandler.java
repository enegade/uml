package enegade.uml.state;

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

import enegade.uml.jdi.DebuggerEvent;

/**
 * @author enegade
 */
public interface InitialStateHandler< D extends DebuggerEvent<?> >
{
    void handleInitialState(D debuggingEvent, StateBuilder stateBuilder);
}
