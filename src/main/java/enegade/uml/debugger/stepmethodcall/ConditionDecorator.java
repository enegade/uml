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
import enegade.uml.type.MethodCall;
import enegade.uml.type.StepInfo;
import enegade.uml.debugger.ConditionTask;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;

import java.util.List;

/**
 * @author enegade
 */
public class ConditionDecorator extends BaseDecorator
{
    private CallGraph callGraph;

    private ConditionTask conditionTask;

    private boolean conditionExited = false;

    public ConditionDecorator(StepMethodCallTask task, CallGraph callGraph, ConditionTask conditionTask)
    {
        super(task);

        this.callGraph = callGraph;
        this.conditionTask = conditionTask;
    }

    @Override
    public void handleInitialState(ThreadReference thread, StateBuilder stateBuilder)
    {
        task.handleInitialState(thread, stateBuilder);

        this.conditionExited = false;
    }

    @Override
    public void handleStepState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder) {
        super.handleStepState(debuggerEvent, stateBuilder);

        this.conditionExited = false;

        if( !this.conditionTask.isConditionStarted() ) {
            return;
        }


        System.out.println("condition");

        CallGraph.EntranceView entranceView = this.callGraph.getEntranceView();
        MethodCall currentMethod = entranceView.getMethodCall().get();
        if( currentMethod != this.conditionTask.getConditionMethod() ) {
            return;
        }

        List<StepInfo> stepInfos = entranceView.getStepInfoList();
        if (!stepInfos.isEmpty()) {
            StepInfo stepInfo = stepInfos.get(stepInfos.size() - 1);

//                if( stepInfo.getLineNumber() == 82 ) {
            if (stepInfo.getCodeIndex() == 49) {
                this.callGraph.closeEntrance();

                this.conditionExited = true;

                this.conditionTask.exitCondition();

                stateBuilder.clearRequests();
            }
        }
    }

    @Override
    public boolean isConditionExited()
    {
        return this.conditionExited;
    }

    @Override
    public ConditionTask.CONDITION_SOURCE getConditionSource()
    {
        return this.conditionTask.getConditionSource();
    }
}
