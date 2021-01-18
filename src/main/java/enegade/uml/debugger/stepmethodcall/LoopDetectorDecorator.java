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
import enegade.uml.CallGraph;
import enegade.uml.type.LoopCondition;
import enegade.uml.type.MethodCall;
import enegade.uml.debugger.loopexit.LoopExitTask;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;
import enegade.uml.debugger.loopexit.LoopDetector;

/**
 * @author enegade
 */
public class LoopDetectorDecorator extends BaseDecorator
{
    private LoopDetector loopDetector;

    private LoopExitTask loopExitTask;

    private CallGraph callGraph;

    private boolean loopDetected;

    public LoopDetectorDecorator(StepMethodCallTask task, LoopDetector loopDetector, LoopExitTask loopExitTask,
                                 CallGraph callGraph)
    {
        super(task);

        this.loopDetector = loopDetector;
        this.loopExitTask = loopExitTask;
        this.callGraph = callGraph;
    }

    @Override
    public void handleInitialState(ThreadReference thread, StateBuilder stateBuilder)
    {
        task.handleInitialState(thread, stateBuilder);

        this.loopDetected = false;
    }

    @Override
    public void handleStepState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        super.handleStepState(debuggerEvent, stateBuilder);

        if( !this.task.isMethodEnter() ) {
            return;
        }

        MethodCall methodCall = this.task.getNewEntrance().get();
        boolean found = this.loopDetector.detectLoop(methodCall);

        if(!found) {
            this.loopDetected = false;

            return;
        }

        MethodCall loopStartingMethodCall = this.loopDetector.getLoopStartingMethodCall().get();
        MethodCall loopEndingMethodCall = this.loopDetector.getLoopEndingMethodCall().get();
        this.loopExitTask.setLoopEndingMethodCall(loopEndingMethodCall);

        LoopCondition loopCondition = new LoopCondition(loopStartingMethodCall, loopEndingMethodCall);
        this.callGraph.addLoopCondition(loopCondition);

        this.loopDetected = true;

        stateBuilder.clearRequests();
    }

    @Override
    public boolean isLoopDetected()
    {
        return this.loopDetected;
    }
}
