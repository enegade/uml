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

import enegade.uml.Config;
import enegade.uml.DebuggingContext;
import enegade.uml.TypeCollector;
import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.ThreadStartDebuggerEvent;
import enegade.uml.state.*;
import enegade.uml.EventLoop;
import enegade.uml.type.ThreadInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author enegade
 */
public class UmlDebugger  extends BaseDebugger implements InitialStateStartingHandler
{
    private DebuggingContext debuggingContext;

    private EventLoop eventLoop;

    private List<ThreadDebugger> threadDebuggers = new ArrayList<>();

    private TypeCollector typeCollector;

    private Map<Long, ThreadInfo> threadInfoMap;

    private Config config;

    public UmlDebugger(EventLoop eventLoop, Config config)
    {
        this.config = config;
        this.debuggingContext = new DebuggingContext();
        this.eventLoop = eventLoop;
        this.typeCollector = new TypeCollector();

        this.threadInfoMap = new HashMap<>();
    }

    public Map<Long, ThreadInfo> getThreadInfoMap()
    {
        return threadInfoMap;
    }

    @Override
    public void debug()
    {
        StateChannel threadStartChannel = new StateChannel(this.eventLoop);
        this.stateChannels.add(threadStartChannel);

        threadStartChannel.setStateFrom(this);

        StateChannel classPrepareChannel = new StateChannel(this.eventLoop);
        this.stateChannels.add(classPrepareChannel);

        ClassPrepareTask classPrepareTask = new ClassPrepareTask(this.config);

        classPrepareChannel.setStateFrom(classPrepareTask);
    }

    @Override
    public void handleInitialState(StateBuilder stateBuilder)
    {
        TaskStateChanger<ThreadStartDebuggerEvent> threadStartChanger = new TaskStateChanger<>(this::handleThreadStartState, this);
        ThreadStartRequestBuilder threadStartRequestBuilder = ThreadStartRequestBuilder.from()
                .setStateChanger(threadStartChanger)
                .setEventsRequested(true);

        stateBuilder.addRequest(threadStartRequestBuilder);


        TaskStateChanger<MethodExitDebuggerEvent> methodExitChanger = new TaskStateChanger<>(this::handleMethodExitState, this);
        MethodExitRequestBuilder methodExitRequestBuilder = MethodExitRequestBuilder.from()
                .setStateChanger(methodExitChanger)
                .setOrder(10);

        stateBuilder.addRequest(methodExitRequestBuilder);

        TaskStateChanger<BreakpointDebuggerEvent> breakpointChanger = new TaskStateChanger<>(this::handleBreakpointState, this);
        BreakpointRequestBuilder breakpointRequestBuilder = BreakpointRequestBuilder.from()
                .setStateChanger(breakpointChanger)
                .setOrder(10);

        stateBuilder.addRequest(breakpointRequestBuilder);
    }

    public void handleMethodExitState(MethodExitDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        stateBuilder.setChangeState(false);

        Task task = debuggerEvent.getTask().get();
        if( !(task instanceof StartupTask) ) {
            return;
        }

        if( !((StartupTask) task).isFinished() ) {
            return;
        }

        stop();
    }

    public void handleBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        stateBuilder.setChangeState(false);

        Task task = debuggerEvent.getTask().get();
        if( !(task instanceof StartupTask) ) {
            return;
        }

        if( !((StartupTask) task).isFinished() ) {
            return;
        }

        stop();
    }

    public void handleThreadStartState(ThreadStartDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        ThreadDebugger threadDebugger = new ThreadDebugger(debuggerEvent, this.typeCollector, this.debuggingContext,
                this.eventLoop, this.threadInfoMap, this.config);
        threadDebuggers.add(threadDebugger);

        threadDebugger.debug();

        stateBuilder.setChangeState(false);
    }

    public List<ThreadDebugger> getThreadDebuggers()
    {
        return List.copyOf(threadDebuggers);
    }

    @Override
    public void stop()
    {
        super.stop();

        for (Debugger debugger : this.threadDebuggers) {
            debugger.stop();
        }
    }
}
