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

import enegade.uml.jdi.BreakpointDebuggerEvent;
import enegade.uml.jdi.ThreadableDebuggerEvent;
import enegade.uml.state.BreakpointRequestBuilder;
import enegade.uml.state.RequestBuilder;
import enegade.uml.state.StateBuilder;
import enegade.uml.state.TaskStateChanger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author enegade
 */
public abstract class BaseConditionDecorator extends BaseTaskDecorator
{
    private DecoratedTask decorator;

    private List<RequestBuilder<?, ?>> currentStateRequestBuilders = new ArrayList<>();

    private LinkedList< List<RequestBuilder<?, ?>> > builderStack = new LinkedList<>();

    protected boolean conditionStarted = false;

    private ConditionTask conditionTask;

    public BaseConditionDecorator(DecoratedTask task, ConditionTask conditionTask)
    {
        super(task);

        this.conditionTask = conditionTask;
        this.decorator = this;
    }

    protected void processConditionState(ThreadableDebuggerEvent<?> debuggerEvent, StateBuilder stateBuilder)
    {
        if(this.conditionStarted) {
            this.conditionStarted = false;

            this.currentStateRequestBuilders = builderStack.pop();
            for (RequestBuilder<?, ?> requestBuilder : this.currentStateRequestBuilders) {
                stateBuilder.addRequest( requestBuilder.getCopy() );
            }
        } else {
            long threadId = debuggerEvent.getThreadReference().uniqueID();
            addConditionBreakpointRequest(threadId, stateBuilder);

            this.currentStateRequestBuilders = stateBuilder.getRequestBuilders().stream()
                    .map(RequestBuilder::getCopy)
                    .collect(Collectors.toList());
        }
    }

    protected void addConditionBreakpointRequest(long threadId, StateBuilder stateBuilder)
    {
        TaskStateChanger<BreakpointDebuggerEvent> changer = new TaskStateChanger<>(this::handleConditionBreakpointState, decorator);
        BreakpointRequestBuilder requestBuilder = BreakpointRequestBuilder.from()
                .setStateChanger(changer)
                .setOrder(threadId);

        stateBuilder.addRequest(requestBuilder);
    }

    public void handleConditionBreakpointState(BreakpointDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        Task task = debuggerEvent.getTask().get();
        if( !(task instanceof ConditionTask) ) {
            stateBuilder.setChangeState(false);
            return;
        }

        this.builderStack.push(this.currentStateRequestBuilders);

        this.conditionTask.setConditionSource( getConditionSource() );

        this.conditionStarted = true;
    }

    protected abstract ConditionTask.CONDITION_SOURCE getConditionSource();

    @Override
    public void setDecorator(DecoratedTask decorator)
    {
        super.setDecorator(decorator);

        this.decorator = decorator;
    }

    public boolean isConditionStarted()
    {
        return this.conditionStarted;
    }
}
