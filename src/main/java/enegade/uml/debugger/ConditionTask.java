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

import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import enegade.uml.CallGraph;
import enegade.uml.type.MethodCall;
import enegade.uml.type.StepInfo;
import enegade.uml.excepion.LogicException;
import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.jdi.ClassPrepareDebuggerEvent;
import enegade.uml.jdi.ThreadStartDebuggerEvent;
import enegade.uml.state.*;

import java.util.*;

/**
 * @author enegade
 */
public class ConditionTask implements InitialStateHandlerTask<ThreadStartDebuggerEvent>
{
    public static final String CONDITION_CLASS = "jditest.Debuggee";

    private CallGraph callGraph;

    private ConditionData currentCondition;

    private LinkedList<ConditionData> conditionStack = new LinkedList<>();

    private boolean conditionStarted = false;

    private Map<Location, BreakpointRequestBuilder> breakpointBuilders = new HashMap<>();

    public ConditionTask(CallGraph callGraph)
    {
        this.callGraph = callGraph;
    }

    public enum CONDITION_SOURCE {
        LOOP_EXIT, METHOD_SKIP, METHOD_CALL
    }

    public CONDITION_SOURCE getConditionSource()
    {
        if(this.currentCondition == null) {
            throw new LogicException();
        }

        return this.currentCondition.getSource();
    }

    public void setConditionSource(CONDITION_SOURCE conditionSource)
    {
        if(this.currentCondition == null) {
            throw new LogicException();
        }

        this.currentCondition.setSource(conditionSource);
    }

    public void handleInitialState(ThreadStartDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        VirtualMachine virtualMachine = debuggerEvent.getEvent().virtualMachine();

        ReferenceType referenceType = null;
        for (ReferenceType candidate : virtualMachine.allClasses()) {
            if ( candidate.isPrepared() && candidate.name().equals( ConditionTask.CONDITION_CLASS ) ) {
                referenceType = candidate;
                break;
            }
        }

        if(referenceType != null) {
            createBreakpointRequest(referenceType, stateBuilder);
        } else {
            addPrepareRequest( debuggerEvent.getThreadReference().uniqueID(), stateBuilder );
        }
    }

    private void addPrepareRequest(long threadId, StateBuilder stateBuilder)
    {
        TaskStateChanger<ClassPrepareDebuggerEvent> changer = new TaskStateChanger<>(this::handleClassPrepareState, this);
        ClassPrepareRequestBuilder requestBuilder = ClassPrepareRequestBuilder.from()
                .setStateChanger(changer)
                .setOrder( threadId + 1 );

        stateBuilder.addRequest(requestBuilder);
    }

    public void handleClassPrepareState(ClassPrepareDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        Task task = debuggerEvent.getTask().get();
        if( !(task instanceof ClassPrepareTask) ) {
            stateBuilder.setChangeState(false);
            return;
        }

        ReferenceType refType = debuggerEvent.getEvent().referenceType();
        if( !refType.name().equals( ConditionTask.CONDITION_CLASS ) ) {
            stateBuilder.setChangeState(false);
            return;
        }

        createBreakpointRequest(refType, stateBuilder);
    }

    private void createBreakpointRequest(ReferenceType referenceType, StateBuilder stateBuilder)
    {
        Method method = null;
        for (Method candidate : referenceType.methods()) {
            if (candidate.name().equals("compositeLoopCase")) {
                method = candidate;
                break;
            }
        }

        Location location;
        try {
            List<Location> locations = method.locationsOfLine(80);
            location = locations.get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        TaskStateChanger<BreakpointDebuggerEvent> changer = new TaskStateChanger<>(this::handleBreakpointState, this);
        BreakpointRequestBuilder requestBuilder = BreakpointRequestBuilder.from()
                .setStateChanger(changer)
                .setEventsRequested(true)
                .setLocation(location);

        this.breakpointBuilders.put( location, requestBuilder.getCopy() );

        stateBuilder.addRequest(requestBuilder);
    }

    public void handleBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        BreakpointEvent event = debuggerEvent.getEvent();

        MethodCall methodCall = new MethodCall(event);
        StepInfo stepInfo = new StepInfo(event);
        methodCall.setIsCondition(true);

        this.callGraph.openNewEntrance(methodCall);
        this.callGraph.addStepInfo(stepInfo);

        this.conditionStarted = true;

        if(this.currentCondition != null) {
            this.conditionStack.push(this.currentCondition);
        }

        this.currentCondition = new ConditionData(methodCall, CONDITION_SOURCE.METHOD_CALL);

        this.breakpointBuilders.remove( event.location() );

        for ( BreakpointRequestBuilder requestBuilder : this.breakpointBuilders.values() ) {
            stateBuilder.addRequest(requestBuilder);
        }

        addPrepareRequest( debuggerEvent.getThreadReference().uniqueID(), stateBuilder );
    }

    public boolean isConditionStarted()
    {
        return conditionStarted;
    }

    public void exitCondition()
    {
        this.conditionStarted = false;
    }

    public MethodCall getConditionMethod()
    {
        if(this.currentCondition == null) {
            throw new LogicException();
        }

        return this.currentCondition.getMethod();
    }

    private static class ConditionData
    {
        private MethodCall method;

        private CONDITION_SOURCE source;

        public ConditionData(MethodCall method, CONDITION_SOURCE source)
        {
            this.method = method;
            this.source = source;
        }

        public MethodCall getMethod()
        {
            return method;
        }

        public CONDITION_SOURCE getSource()
        {
            return source;
        }

        public void setSource(CONDITION_SOURCE source)
        {
            this.source = source;
        }
    }
}
