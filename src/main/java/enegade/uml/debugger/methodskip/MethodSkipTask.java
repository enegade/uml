package enegade.uml.debugger.methodskip;

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
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.*;

/**
 * @author enegade
 */
public interface MethodSkipTask extends InitialStateHandlerTask<StepDebuggerEvent>, DecoratedTask
{
    void setMethodCall(MethodCall methodCall);

    void handleInitialState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder);

    void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder);

    boolean isMethodSkipped();

    boolean isExceptionCaught();

    boolean isConditionStarted();
}
