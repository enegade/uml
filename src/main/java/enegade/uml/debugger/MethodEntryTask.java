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

import enegade.uml.jdi.MethodEntryDebuggerEvent;
import enegade.uml.jdi.ThreadStartDebuggerEvent;
import enegade.uml.state.*;

/**
 * @author enegade
 */
public class MethodEntryTask implements InitialStateHandlerTask<ThreadStartDebuggerEvent>
{
    public void handleInitialState(ThreadStartDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        TaskStateChanger<MethodEntryDebuggerEvent> changer = new TaskStateChanger<>(this::handleMethodEntryState, this);
        MethodEntryRequestBuilder requestBuilder = MethodEntryRequestBuilder.from()
                .setStateChanger(changer)
                .setEventsRequested(true);

        stateBuilder.addRequest(requestBuilder);
    }

    public void handleMethodEntryState(MethodEntryDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        stateBuilder.setChangeState(false);
    }
}
