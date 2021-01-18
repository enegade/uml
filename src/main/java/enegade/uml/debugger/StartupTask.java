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

import com.sun.jdi.*;
import com.sun.jdi.event.BreakpointEvent;
import enegade.uml.CallGraph;
import enegade.uml.Config;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.type.MethodCall;
import enegade.uml.type.MethodInfo;
import enegade.uml.type.StepInfo;
import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.jdi.ClassPrepareDebuggerEvent;
import enegade.uml.jdi.ThreadStartDebuggerEvent;
import enegade.uml.state.*;

/**
 * @author enegade
 */
public class StartupTask implements InitialStateHandlerTask<ThreadStartDebuggerEvent>
{
    private ThreadReference thread;

    private CallGraph callGraph;

    private VirtualMachine virtualMachine;


    private String startupFromClass;

    private String startupFromMethod;

    private String startupToClass;

    private String startupToMethod;


    private StateHandler<BreakpointDebuggerEvent> breakpointHandler;

    private String breakpointClass;

    private String breakpointMethod;


    private int frameCount;


    private boolean finished = false;

    public StartupTask(ThreadReference thread, CallGraph callGraph, Config config)
    {
        this.thread = thread;
        this.callGraph = callGraph;

        this.virtualMachine = thread.virtualMachine();

        this.startupFromClass = config.getProperty("startup.from.class");
        this.startupFromMethod = config.getProperty("startup.from.method");
        this.startupToClass = config.getProperty("startup.to.class");
        this.startupToMethod = config.getProperty("startup.to.method");
    }

    public void handleInitialState(ThreadStartDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        if( this.startupFromClass.isEmpty() ) {
            if( this.startupToClass.isEmpty() ) {
                stateBuilder.setChangeState(false);
            } else {
                this.breakpointClass = this.startupToClass;
                this.breakpointMethod = this.startupToMethod;
                this.breakpointHandler = this::handleFinishingBreakpointState;
                this.setupHandler(stateBuilder);
            }
        } else {
            this.breakpointClass = this.startupFromClass;
            this.breakpointMethod = this.startupFromMethod;
            this.breakpointHandler = this::handleStartingBreakpointState;
            this.setupHandler(stateBuilder);
        }
    }

    private void setupHandler(StateBuilder stateBuilder)
    {
        ReferenceType referenceType = null;
        for (ReferenceType candidate : this.virtualMachine.allClasses()) {
            if ( candidate.isPrepared() && candidate.name().equals(this.breakpointClass) ) {
                referenceType = candidate;
                break;
            }
        }

        if(referenceType != null) {
            createBreakpointRequest(referenceType, stateBuilder);
        } else {
            TaskStateChanger<ClassPrepareDebuggerEvent> changer = new TaskStateChanger<>(this::handleClassPrepareState, this);
            ClassPrepareRequestBuilder requestBuilder = ClassPrepareRequestBuilder.from()
                    .setStateChanger(changer)
                    .setOrder( this.thread.uniqueID() );

            stateBuilder.addRequest(requestBuilder);
        }
    }

    public void handleClassPrepareState(ClassPrepareDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        Task task = debuggerEvent.getTask().get();
        if( !(task instanceof ClassPrepareTask) ) {
            stateBuilder.setChangeState(false);
            return;
        }

        ReferenceType refType = debuggerEvent.getEvent().referenceType();
        if( !refType.name().equals(this.breakpointClass) ) {
            stateBuilder.setChangeState(false);
            return;
        }

        createBreakpointRequest(refType, stateBuilder);
    }

    private void createBreakpointRequest(ReferenceType referenceType, StateBuilder stateBuilder)
    {
        Method method = null;
        for (Method candidate : referenceType.methods()) {
            String name = MethodCall.getSelectorName( new MethodInfo(candidate));
            if (name.equals(this.breakpointMethod)) {
                method = candidate;
                break;
            }
        }

        Location location = method.location();

        TaskStateChanger<BreakpointDebuggerEvent> changer = new TaskStateChanger<>(this.breakpointHandler, this);
        BreakpointRequestBuilder requestBuilder = BreakpointRequestBuilder.from()
                .setStateChanger(changer)
                .setEventsRequested(true)
                .setLocation(location);

        stateBuilder.addRequest(requestBuilder);
    }

    public void handleStartingBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        BreakpointEvent event = debuggerEvent.getEvent();

        MethodCall methodCall = new MethodCall(event);
        StepInfo stepInfo = new StepInfo(event);

        callGraph.openNewEntrance(methodCall);
        callGraph.addStepInfo(stepInfo);


        try {
            this.frameCount = event.thread().frameCount();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        if( this.startupToClass.isEmpty() ) {
            TaskStateChanger<MethodExitDebuggerEvent> methodExitChanger = new TaskStateChanger<>(this::handleMethodExitState, this);
            MethodExitRequestBuilder methodExitRequestBuilder = MethodExitRequestBuilder.from()
                    .setStateChanger(methodExitChanger)
                    .setEventsRequested(true)
                    .addClassFilter(this.startupFromClass);

            stateBuilder.addRequest(methodExitRequestBuilder);
        } else {
            this.breakpointClass = this.startupToClass;
            this.breakpointMethod = this.startupToMethod;
            this.breakpointHandler = this::handleFinishingBreakpointState;
            this.setupHandler(stateBuilder);
        }
    }

    public void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        stateBuilder.setChangeState(false);

        int frameCount;
        try {
            frameCount = debuggerEvent.getEvent().thread().frameCount();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(frameCount != this.frameCount) {
            return;
        }

        Method method = debuggerEvent.getEvent().method();
        String name = MethodCall.getSelectorName( new MethodInfo(method) );

        if (!name.equals(this.startupFromMethod)) {
            return;
        }

        this.finished = true;
    }

    public void handleFinishingBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        stateBuilder.setChangeState(false);

        this.finished = true;
    }

    public boolean isFinished()
    {
        return this.finished;
    }
}
