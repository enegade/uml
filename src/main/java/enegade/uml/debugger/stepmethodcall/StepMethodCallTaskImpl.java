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

import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.StepRequest;
import enegade.uml.CallGraph;
import enegade.uml.Config;
import enegade.uml.type.MethodCall;
import enegade.uml.type.StepInfo;
import enegade.uml.debugger.ConditionTask;
import enegade.uml.debugger.DecoratedTask;
import enegade.uml.excepion.LogicException;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.state.StateBuilder;
import enegade.uml.state.StepRequestBuilder;
import enegade.uml.state.TaskStateChanger;

import java.util.Optional;

/**
 * @author enegade
 */
public class StepMethodCallTaskImpl implements StepMethodCallTask
{
    private boolean isMethodEnter = false;

    private boolean isMethodExit = false;

    private MethodCall newEntrance;

    private StepInfo lastStepInfo;

    private CallGraph callGraph;

    private StepMethodCallTask decorator;

    private int counter = 0;

    private long threadId;

    private Config config;

    public StepMethodCallTaskImpl(CallGraph callGraph, long threadId, Config config)
    {
        this.callGraph = callGraph;
        this.decorator = this;
        this.threadId = threadId;
        this.config = config;
    }

    @Override
    public void setDecorator(DecoratedTask decorator)
    {
        this.decorator = (StepMethodCallTask) decorator;
    }

    @Override
    public void handleInitialState(ThreadReference thread, StateBuilder stateBuilder)
    {
        this.isMethodEnter = false;
        this.isMethodExit = false;
        this.newEntrance = null;

        if( !this.decorator.isStarted() ) {
            return;
        }

        TaskStateChanger<StepDebuggerEvent> changer = new TaskStateChanger<>(this.decorator::handleStepState, this.decorator);
        StepRequestBuilder requestBuilder = StepRequestBuilder.from()
                .setStateChanger(changer)
                .setOrder( thread.uniqueID() )
                .setEventsRequested(true)
                .setThread(thread)
                .setSize(StepRequest.STEP_LINE)
                .setDepth(StepRequest.STEP_INTO);

        stateBuilder.addRequest(requestBuilder);
    }

    @Override
    public void handleStepState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        StepEvent event = debuggerEvent.getEvent();
        ThreadReference threadReference = event.thread();

        StackFrame frame;
        try {
            frame = threadReference.frame(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Location location = frame.location();

        if( location.hashCode() != event.location().hashCode() ) {
            throw new RuntimeException();
        }

        CallGraph.EntranceView entranceView = this.callGraph.getEntranceView();

        this.isMethodEnter = false;
        this.isMethodExit = false;
        this.newEntrance = null;

        int threadFrameCount;
        try {
            threadFrameCount = threadReference.frameCount();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        if( entranceView.isRoot() ) {
            this.processMethodEntry(event, callGraph);
            this.isMethodEnter = true;
            this.isMethodExit = false;

            this.newEntrance.setStepInfo(this.lastStepInfo);
        } else {
            if (entranceView.getMethodCall().get().getHashCode() == location.method().hashCode()
                    && entranceView.getMethodCall().get().getFrameCount() == threadFrameCount) {
                this.isMethodEnter = false;
                this.isMethodExit = false;
            } else if (entranceView.getMethodCall().get().getFrameCount() == threadFrameCount) {
                this.processMethodExit(event, callGraph);
                this.processMethodEntry(event, callGraph);
                this.isMethodEnter = true;
                this.isMethodExit = false;
            } else {
                if (entranceView.getMethodCall().get().getFrameCount() > threadFrameCount) {
                    while (!entranceView.isRoot() && entranceView.getMethodCall().get().getFrameCount() > threadFrameCount) {
                        this.processMethodExit(event, callGraph);
                        entranceView = callGraph.getEntranceView();
                    }

                    if (entranceView.isRoot()) {
                        throw new LogicException();
                    }

                    if (entranceView.getMethodCall().get().getFrameCount() == threadFrameCount
                            && entranceView.getMethodCall().get().getHashCode() != location.method().hashCode()) {
                        this.processMethodExit(event, callGraph);
                        this.processMethodEntry(event, callGraph);
                        this.isMethodEnter = true;
                        this.isMethodExit = false;
                    } else if (entranceView.getMethodCall().get().getFrameCount() < threadFrameCount) {
                        this.processMethodEntry(event, callGraph);
                        this.isMethodEnter = true;
                        this.isMethodExit = false;
                    } else {
                        this.isMethodEnter = false;
                        this.isMethodExit = true;
                    }
                } else {
                    this.processMethodEntry(event, callGraph);
                    this.isMethodEnter = true;
                    this.isMethodExit = false;

                    this.newEntrance.setStepInfo(this.lastStepInfo);
                }
            }
        }

        this.lastStepInfo = new StepInfo(event);

        if(this.newEntrance == null) {
            this.callGraph.addStepInfo(this.lastStepInfo);
        }


        stateBuilder.setChangeState(false);
    }

    private void processMethodEntry(StepEvent event, CallGraph callGraph)
    {
        String printEntrances = this.config.getProperty("print_entrances");
        if( printEntrances.equals("1") ) {
            countSteps();
        }

        this.newEntrance = new MethodCall(event);
    }

    private void processMethodExit(StepEvent event, CallGraph callGraph)
    {
        MethodCall methodCall = callGraph.getEntranceView().getMethodCall().get();

        Location location = event.location();
        String name = "closed by " + location.method().name();
        methodCall.setExitMethodName(name);

        callGraph.closeEntrance();
    }

    @Override
    public boolean isMethodEnter()
    {
        return isMethodEnter;
    }

    @Override
    public boolean isMethodExit()
    {
        return isMethodExit;
    }

    @Override
    public boolean isMethodSkipped()
    {
        return false;
    }

    @Override
    public boolean isLoopDetected()
    {
        return false;
    }

    @Override
    public boolean isConditionExited()
    {
        return false;
    }

    @Override
    public ConditionTask.CONDITION_SOURCE getConditionSource()
    {
        throw new LogicException();
    }

    @Override
    public Optional<MethodCall> getNewEntrance()
    {
        return Optional.ofNullable(this.newEntrance);
    }

    @Override
    public StepInfo getLastStepInfo()
    {
        return lastStepInfo;
    }

    @Override
    public boolean isStarted()
    {
        return true;
    }

    private void countSteps()
    {
        int count = ++counter;
        if(count == 429) {
            int stop = 1;
        }

        System.out.println( this.threadId + " - " + count);
    }
}
