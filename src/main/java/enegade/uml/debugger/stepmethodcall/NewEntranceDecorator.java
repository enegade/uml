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

import enegade.uml.CallGraph;
import enegade.uml.type.MethodCall;
import enegade.uml.type.StepInfo;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;

/**
 * @author enegade
 */
public class NewEntranceDecorator extends BaseDecorator
{
    private CallGraph callGraph;

    public NewEntranceDecorator(StepMethodCallTask task, CallGraph callGraph)
    {
        super(task);

        this.callGraph = callGraph;
    }

    @Override
    public void handleStepState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        super.handleStepState(debuggerEvent, stateBuilder);

        if( !StepMethodCallHelper.canAddNewEntrance(this.task) ) {
            return;
        }

        MethodCall methodCall = task.getNewEntrance().get();

        StepInfo methodOpenerStepInfo = methodCall.getStepInfo().orElse(null);
        if(methodOpenerStepInfo != null) {
            methodOpenerStepInfo.setCalledMethod(methodCall);
            this.callGraph.addStepInfo(methodOpenerStepInfo);
        }

        this.callGraph.openNewEntrance(methodCall);
        this.callGraph.addStepInfo( task.getLastStepInfo() );
    }
}
