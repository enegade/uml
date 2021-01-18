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

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.event.MethodExitEvent;
import enegade.uml.CallGraph;
import enegade.uml.TypeCollector;
import enegade.uml.type.EntranceTypeInfo;
import enegade.uml.type.MethodCall;
import enegade.uml.debugger.stepmethodcall.StepMethodCallHelper;
import enegade.uml.debugger.stepmethodcall.StepMethodCallTask;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.jdi.ThreadStartDebuggerEvent;
import enegade.uml.state.*;
import enegade.uml.type.MethodCallHelper;
import enegade.uml.type.TypeInfo;

import java.util.LinkedList;

/**
 * @author enegade
 */
public class ReturnValueTask implements InitialStateHandlerTask<ThreadStartDebuggerEvent>
{
    private CallGraph callGraph;

    private LinkedList<MethodExitRequestBuilder> builderStack = new LinkedList<>();

    public ReturnValueTask(CallGraph callGraph)
    {
        this.callGraph = callGraph;
    }

    public void handleInitialState(ThreadStartDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        long threadId = debuggerEvent.getThreadReference().uniqueID();
        addStepRequest(threadId, stateBuilder);
    }

    private void addStepRequest(long threadId, StateBuilder stateBuilder)
    {
        TaskStateChanger<StepDebuggerEvent> changer = new TaskStateChanger<>(this::handleStepState, this);
        StepRequestBuilder requestBuilder = StepRequestBuilder.from()
                .setStateChanger(changer)
                .setOrder( threadId );

        stateBuilder.addRequest(requestBuilder);
    }

    public void handleStepState(StepDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        Task task = debuggerEvent.getTask().get();
        if( !(task instanceof StepMethodCallTask) ) {
            stateBuilder.setChangeState(false);
            return;
        }

        StepMethodCallTask stepMethodCallTask = (StepMethodCallTask) task;

        if( !StepMethodCallHelper.canGetReturnValue(stepMethodCallTask) ) {
            stateBuilder.setChangeState(false);
            return;
        }

        long threadId = debuggerEvent.getThreadReference().uniqueID();
        addStepRequest(threadId, stateBuilder);


        CallGraph.EntranceView entranceView = this.callGraph.getEntranceView();
        MethodCall methodCall = entranceView.getMethodCall().get();

        TaskStateChanger<MethodExitDebuggerEvent> changer2 = new TaskStateChanger<>(this::handleMethodExitState, this);
        MethodExitRequestBuilder methodExitRequestBuilder = MethodExitRequestBuilder.from()
                .setStateChanger(changer2)
                .setEventsRequested(true)
                .addClassFilter( methodCall.getTypeName() )
                .setEntranceView(entranceView)
                .setOrder(1);

        stateBuilder.addRequest( MethodExitRequestBuilder.from(methodExitRequestBuilder) );

        this.builderStack.push(methodExitRequestBuilder);
    }

    public void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        MethodExitEvent event = debuggerEvent.getEvent();

        CallGraph.EntranceView entranceView = this.builderStack.peek().getEntranceView();
        MethodCall methodCall = entranceView.getMethodCall().get();

        if( MethodCallHelper.isFrameSame(methodCall, event) ) {
            this.builderStack.pop();

            methodCall.setReturnValue( event.returnValue() );

            try {
                Type returnType = event.method().returnType();
                if (returnType instanceof ReferenceType) {
                    ReferenceType referenceType = (ReferenceType) returnType;
                    if (TypeInfo.isTypeSupported(referenceType)) {
                        TypeCollector typeCollector = this.callGraph.getTypeCollector();
                        TypeInfo returnTypeInfo = typeCollector.createTypeInfo(referenceType);

                        EntranceTypeInfo entranceTypeInfo = entranceView.getEntranceTypeInfo().get();
                        TypeInfo methodTypeInfo = entranceTypeInfo.getTypeInfo();
                        entranceTypeInfo.setReturnTypeInfo(returnTypeInfo);

                        if( !entranceView.isSuppressed() ) {
                            methodTypeInfo.addDependency(returnTypeInfo);
                        }

                        CallGraph.EntranceView opener = entranceView.getOpener().get();
                        CallGraph.addLinkedDependency(opener, entranceView.isLinked(), entranceView.isSuppressed(),
                                returnTypeInfo);
                    }
                }
            } catch (ClassNotLoadedException e) {
                int s = 1;
            }

            if( !this.builderStack.isEmpty() ) {
                MethodExitRequestBuilder builder = MethodExitRequestBuilder.from(this.builderStack.peek());
                stateBuilder.addRequest(builder);
            }

            long threadId = debuggerEvent.getThreadReference().uniqueID();
            addStepRequest(threadId, stateBuilder);
        } else {
            stateBuilder.setChangeState(false);
        }
    }
}
