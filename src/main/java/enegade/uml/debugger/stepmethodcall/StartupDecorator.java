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
import enegade.uml.Config;
import enegade.uml.debugger.DecoratedTask;
import enegade.uml.debugger.StartupTask;
import enegade.uml.debugger.Task;
import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.state.BreakpointRequestBuilder;
import enegade.uml.state.StateBuilder;
import enegade.uml.state.TaskStateChanger;

/**
 * @author enegade
 */
public class StartupDecorator extends BaseDecorator
{
    private DecoratedTask decorator;

    private boolean started;

    public StartupDecorator(StepMethodCallTask task, Config config)
    {
        super(task);

        this.decorator = this;

        String startupFromClass = config.getProperty("startup.from.class");
        if( startupFromClass.isEmpty() ) {
            this.started = true;
        } else {
            this.started = false;
        }
    }

    @Override
    public void handleInitialState(ThreadReference thread, StateBuilder stateBuilder)
    {
        task.handleInitialState(thread, stateBuilder);

        if(!this.started) {
            TaskStateChanger<BreakpointDebuggerEvent> changer = new TaskStateChanger<>(this::handleStartingBreakpointState, this.decorator);
            BreakpointRequestBuilder requestBuilder = BreakpointRequestBuilder.from()
                    .setStateChanger(changer);

            stateBuilder.addRequest(requestBuilder);
        }
    }

    public void handleStartingBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        Task task = debuggerEvent.getTask().get();
        if( !(task instanceof StartupTask) ) {
            stateBuilder.setChangeState(false);
            return;
        }

        this.started = true;

        this.task.handleInitialState( debuggerEvent.getThreadReference(), stateBuilder );
    }

    @Override
    public void setDecorator(DecoratedTask decorator)
    {
        super.setDecorator(decorator);

        this.decorator = decorator;
    }

    @Override
    public boolean isStarted()
    {
        return this.started;
    }
}
