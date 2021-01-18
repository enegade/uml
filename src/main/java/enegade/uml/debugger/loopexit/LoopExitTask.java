package enegade.uml.debugger.loopexit;

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

import enegade.uml.type.MethodCall;
import enegade.uml.debugger.DecoratedTask;
import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.*;

/**
 * @author enegade
 */
public interface LoopExitTask extends InitialStateHandlerTask<StepDebuggerEvent>, DecoratedTask
{
    void setLoopEndingMethodCall(MethodCall loopEndingMethodCall);

    void handleInitialState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder);

    void handleBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder);

    void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder);

    boolean isLoopExited();

    boolean isConditionStarted();

    boolean isExceptionCaught();
}
