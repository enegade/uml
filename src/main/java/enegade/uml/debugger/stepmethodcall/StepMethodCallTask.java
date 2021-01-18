package enegade.uml.debugger.stepmethodcall;

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
import enegade.uml.type.MethodCall;
import enegade.uml.debugger.ConditionTask;
import enegade.uml.debugger.DecoratedTask;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;
import enegade.uml.type.StepInfo;

import java.util.Optional;

/**
 * @author enegade
 */
public interface StepMethodCallTask extends DecoratedTask
{
    void handleInitialState(ThreadReference thread, StateBuilder stateBuilder);

    void handleStepState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder);

    boolean isMethodEnter();

    boolean isMethodExit();

    Optional<MethodCall> getNewEntrance();

    StepInfo getLastStepInfo();

    boolean isMethodSkipped();

    boolean isLoopDetected();

    boolean isConditionExited();

    boolean isStarted();

    ConditionTask.CONDITION_SOURCE getConditionSource();
}
