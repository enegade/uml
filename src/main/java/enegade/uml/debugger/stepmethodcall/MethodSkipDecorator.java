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
import enegade.uml.DebuggingContext;
import enegade.uml.type.MethodCall;
import enegade.uml.debugger.methodskip.MethodSkipTask;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;

/**
 * @author enegade
 */
public class MethodSkipDecorator extends BaseDecorator
{
    private MethodSkipTask methodSkipTask;

    private DebuggingContext debuggingContext;

    private boolean methodSkipped;

    public MethodSkipDecorator(StepMethodCallTask task, MethodSkipTask methodSkipTask, DebuggingContext debuggingContext)
    {
        super(task);

        this.methodSkipTask = methodSkipTask;
        this.debuggingContext = debuggingContext;
    }

    @Override
    public void handleInitialState(ThreadReference thread, StateBuilder stateBuilder)
    {
        task.handleInitialState(thread, stateBuilder);

        this.methodSkipped = false;
    }

    @Override
    public void handleStepState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        super.handleStepState(debuggerEvent, stateBuilder);

        if( !StepMethodCallHelper.canBeSkipped(this.task) ) {
            return;
        }

        MethodCall methodCall = this.task.getNewEntrance().get();
        if( this.debuggingContext.hasMethodInspected(methodCall) ) {
            this.methodSkipped = true;

            this.methodSkipTask.setMethodCall(methodCall);

            stateBuilder.clearRequests();
        } else {
            this.methodSkipped = false;

            this.debuggingContext.addInspectedMethod(methodCall);
        }
    }

    @Override
    public boolean isMethodSkipped()
    {
        return this.methodSkipped;
    }
}
