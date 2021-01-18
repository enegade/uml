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

import com.sun.jdi.ThreadReference;
import enegade.uml.*;
import enegade.uml.debugger.loopexit.ExceptionDecorator;
import enegade.uml.debugger.loopexit.LoopDetector;
import enegade.uml.debugger.loopexit.LoopExitTask;
import enegade.uml.debugger.loopexit.LoopExitTaskImpl;
import enegade.uml.debugger.methodskip.MethodSkipTask;
import enegade.uml.debugger.methodskip.MethodSkipTaskImpl;
import enegade.uml.debugger.stepmethodcall.*;
import enegade.uml.jdi.*;
import enegade.uml.state.*;
import enegade.uml.type.ThreadHelper;
import enegade.uml.type.ThreadInfo;

import java.util.Arrays;
import java.util.Map;

/**
 * @author enegade
 */
public class ThreadDebugger extends BaseDebugger
{
    private CallGraph callGraph;

    private DebuggingContext debuggingContext;

    private EventLoop eventLoop;

    private ThreadStartDebuggerEvent debuggerEvent;

    private Map<Long, ThreadInfo> threadInfoMap;

    private TypeCollector typeCollector;

    private Config config;

    public ThreadDebugger(ThreadStartDebuggerEvent debuggerEvent, TypeCollector typeCollector,
                          DebuggingContext debuggingContext, EventLoop eventLoop, Map<Long, ThreadInfo> threadInfoMap,
                          Config config)
    {
        this.config = config;
        this.typeCollector = typeCollector;
        this.debuggingContext = debuggingContext;
        this.eventLoop = eventLoop;
        this.debuggerEvent = debuggerEvent;
        this.threadInfoMap = threadInfoMap;
    }

    public ThreadReference getThread()
    {
        return this.debuggerEvent.getThreadReference();
    }

    public CallGraph getCallGraph()
    {
        return callGraph;
    }

    private void addThreadDetectingTask(ThreadInfo threadInfo, ThreadStartDebuggerEvent debuggerEvent)
    {
        ThreadDetectingTask threadDetectingTask = new ThreadDetectingTask(threadInfo, this.threadInfoMap);

        StateChannel threadChannel = new StateChannel(this.eventLoop);
        threadChannel.setThreadReference( threadInfo.getThreadReference().get() );

        this.stateChannels.add(threadChannel);

        threadChannel.setStateFrom(threadDetectingTask, debuggerEvent);
    }

    @Override
    public void debug()
    {
        ThreadReference threadReference = this.debuggerEvent.getThreadReference();





        long threadId = threadReference.uniqueID();

        long threadObjectId = ThreadHelper.getThreadObjectId(debuggerEvent);

        ThreadInfo threadInfo;
        if( threadId == ThreadInfo.MAIN_THREAD_ID ) {
            threadInfo = new ThreadInfo(threadObjectId);
            threadInfo.setThreadReference(threadReference);
            threadInfo.setUniqueId(threadId);

            this.threadInfoMap.put(threadObjectId, threadInfo);

            addThreadDetectingTask(threadInfo, debuggerEvent);
        } else {
            threadInfo = this.threadInfoMap.get(threadObjectId);
            if(threadInfo != null) {
                threadInfo.setThreadReference(threadReference);
                threadInfo.setUniqueId(threadId);

                addThreadDetectingTask(threadInfo, debuggerEvent);
            } else {
                int stop = 1;
            }
        }

        if(threadInfo == null) {
            return;
        }



        String[] stringParts = this.config.getProperty("thread_creating_order").split(",");
        long[] threadCreatingOrder = Arrays.stream(stringParts)
                .mapToLong(Long::parseLong).toArray();

        if( !threadInfo.isCreatingOrderSame(threadCreatingOrder) ) {
            return;
        }



        this.callGraph = new CallGraph( typeCollector, threadInfo.getObjectId() );



        StateChannel exceptionChannel = new StateChannel(this.eventLoop);
        exceptionChannel.setThreadReference(threadReference);

        this.stateChannels.add(exceptionChannel);

        ExceptionTask exceptionTask = new ExceptionTask(this.callGraph);

        exceptionChannel.setStateFrom(exceptionTask, this.debuggerEvent);



        StateChannel conditionChannel = new StateChannel(this.eventLoop);
        conditionChannel.setThreadReference(threadReference);

        this.stateChannels.add(conditionChannel);

        ConditionTask conditionTask = new ConditionTask(this.callGraph);

        conditionChannel.setStateFrom(conditionTask, this.debuggerEvent);



        StateChannel startupChannel = new StateChannel(this.eventLoop);
        startupChannel.setThreadReference(threadReference);

        this.stateChannels.add(startupChannel);

        StartupTask startupTask = new StartupTask(threadReference, this.callGraph, this.config);

        startupChannel.setStateFrom(startupTask, this.debuggerEvent);



        StateChannel mainChannel = new StateChannel(this.eventLoop);
        mainChannel.setThreadReference(threadReference);


        StepMethodCallTask stepMethodCallTask = new StepMethodCallTaskImpl( this.callGraph, threadReference.uniqueID(), this.config );


        LoopExitTask loopExitTask = new LoopExitTaskImpl(this.callGraph);
        loopExitTask = new enegade.uml.debugger.loopexit.ConditionDecorator(loopExitTask, conditionTask);
        loopExitTask = new ExceptionDecorator(loopExitTask, exceptionTask);


        MethodSkipTask methodSkipTask = new MethodSkipTaskImpl(this.callGraph);
        methodSkipTask = new enegade.uml.debugger.methodskip.ConditionDecorator(methodSkipTask, conditionTask);
        methodSkipTask = new enegade.uml.debugger.methodskip.ExceptionDecorator(methodSkipTask, exceptionTask);


        LoopDetector loopDetector = new LoopDetector(this.callGraph);

        stepMethodCallTask = new LoopDetectorDecorator(stepMethodCallTask, loopDetector, loopExitTask, this.callGraph);
        stepMethodCallTask = new NewEntranceDecorator(stepMethodCallTask, this.callGraph);
        stepMethodCallTask = new StartupDecorator(stepMethodCallTask, this.config);
        stepMethodCallTask = new ConditionDecorator(stepMethodCallTask, this.callGraph, conditionTask);
        stepMethodCallTask = new MethodSkipDecorator(stepMethodCallTask, methodSkipTask, this.debuggingContext);

        TransitiveStateChanger<StepDebuggerEvent, StepMethodCallTask> changer2 = new TransitiveStateChanger<>(stepMethodCallTask, methodSkipTask,
                StepMethodCallTask::isMethodSkipped, StepDebuggerEvent.class);
        mainChannel.addStateChanger(changer2);

        MethodExitHandlerAdapter methodExitHandlerAdapter = new MethodExitHandlerAdapter(stepMethodCallTask);
        TransitiveStateChanger<MethodExitDebuggerEvent, MethodSkipTask> changer3 = new TransitiveStateChanger<>(methodSkipTask, methodExitHandlerAdapter,
                MethodSkipTask::isMethodSkipped, MethodExitDebuggerEvent.class);
        mainChannel.addStateChanger(changer3);

        TransitiveStateChanger<StepDebuggerEvent, StepMethodCallTask> changer4 = new TransitiveStateChanger<>(stepMethodCallTask, loopExitTask,
                StepMethodCallTask::isLoopDetected, StepDebuggerEvent.class);
        mainChannel.addStateChanger(changer4);

        BreakpointHandlerAdapter breakpointHandlerAdapter = new BreakpointHandlerAdapter(stepMethodCallTask);
        TransitiveStateChanger<BreakpointDebuggerEvent, LoopExitTask> changer5 = new TransitiveStateChanger<>(loopExitTask, breakpointHandlerAdapter,
                LoopExitTask::isLoopExited, BreakpointDebuggerEvent.class);
        mainChannel.addStateChanger(changer5);

        TransitiveStateChanger<MethodExitDebuggerEvent, LoopExitTask> changer6 = new TransitiveStateChanger<>(loopExitTask, methodExitHandlerAdapter,
                LoopExitTask::isLoopExited, MethodExitDebuggerEvent.class);
        mainChannel.addStateChanger(changer6);

        ExceptionHandlerAdapter exceptionHandlerAdapter = new ExceptionHandlerAdapter(stepMethodCallTask);
        TransitiveStateChanger<ExceptionDebuggerEvent, LoopExitTask> changer7 = new TransitiveStateChanger<>(loopExitTask, exceptionHandlerAdapter,
                LoopExitTask::isExceptionCaught, ExceptionDebuggerEvent.class);
        mainChannel.addStateChanger(changer7);

        TransitiveStateChanger<ExceptionDebuggerEvent, MethodSkipTask> changer8 = new TransitiveStateChanger<>(methodSkipTask, exceptionHandlerAdapter,
                MethodSkipTask::isExceptionCaught, ExceptionDebuggerEvent.class);
        mainChannel.addStateChanger(changer8);

        TransitiveStateChanger<BreakpointDebuggerEvent, LoopExitTask> changer9 = new TransitiveStateChanger<>(loopExitTask, breakpointHandlerAdapter,
                LoopExitTask::isConditionStarted, BreakpointDebuggerEvent.class);
        mainChannel.addStateChanger(changer9);

        StepHandlerAdapter stepHandlerAdapter = new StepHandlerAdapter(stepMethodCallTask);
        TransitiveStateChanger<StepDebuggerEvent, StepMethodCallTask> changer10 = new TransitiveStateChanger<>(stepMethodCallTask, stepHandlerAdapter,
                task -> {
                    if( !task.isConditionExited() ) {
                        return false;
                    }
                    return task.getConditionSource() == ConditionTask.CONDITION_SOURCE.METHOD_CALL;
                }, StepDebuggerEvent.class);
        mainChannel.addStateChanger(changer10);

        TransitiveStateChanger<StepDebuggerEvent, StepMethodCallTask> changer11 = new TransitiveStateChanger<>(stepMethodCallTask, loopExitTask,
                task -> {
                    if( !task.isConditionExited() ) {
                        return false;
                    }
                    return task.getConditionSource() == ConditionTask.CONDITION_SOURCE.LOOP_EXIT;
                }, StepDebuggerEvent.class);
        mainChannel.addStateChanger(changer11);

        TransitiveStateChanger<BreakpointDebuggerEvent, MethodSkipTask> changer12 = new TransitiveStateChanger<>(methodSkipTask, breakpointHandlerAdapter,
                MethodSkipTask::isConditionStarted, BreakpointDebuggerEvent.class);
        mainChannel.addStateChanger(changer12);

        TransitiveStateChanger<StepDebuggerEvent, StepMethodCallTask> changer13 = new TransitiveStateChanger<>(stepMethodCallTask, methodSkipTask,
                task -> {
                    if( !task.isConditionExited() ) {
                        return false;
                    }
                    return task.getConditionSource() == ConditionTask.CONDITION_SOURCE.METHOD_SKIP;
                }, StepDebuggerEvent.class);
        mainChannel.addStateChanger(changer13);

        this.stateChannels.add(mainChannel);



        ThreadStartHandlerAdapter threadStartHandlerAdapter = new ThreadStartHandlerAdapter(stepMethodCallTask);
        mainChannel.setStateFrom(threadStartHandlerAdapter, this.debuggerEvent);



        StateChannel returnValueChannel = new StateChannel(this.eventLoop);
        returnValueChannel.setThreadReference(threadReference);

        this.stateChannels.add(returnValueChannel);

        ReturnValueTask returnValueTask = new ReturnValueTask(this.callGraph);

        returnValueChannel.setStateFrom(returnValueTask, this.debuggerEvent);


        // MethodEntryTask that create MethodEntryRequest when run in combination with the StepRequest
        // cause strange behavior - VM spawns more then one MethodEntryEvent within one EventLoop cycle.
        // Commented code show this case.
//        StateChannel methodEntryChannel = new StateChannel(this.eventLoop);
//        methodEntryChannel.setThreadReference(threadReference);
//
//        this.stateChannels.add(methodEntryChannel);
//
//        MethodEntryTask methodEntryTask = new MethodEntryTask();
//
//        methodEntryChannel.setStateFrom(methodEntryTask, this.debuggerEvent);
    }
}
