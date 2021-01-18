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

import enegade.uml.jdi.ExceptionDebuggerEvent;
import enegade.uml.state.ExceptionRequestBuilder;
import enegade.uml.state.StateBuilder;
import enegade.uml.state.TaskStateChanger;

/**
 * @author enegade
 */
public abstract class BaseExceptionDecorator extends BaseTaskDecorator
{
    private ExceptionTask exceptionTask;

    private DecoratedTask decorator;

    protected boolean exceptionCaught;

    public BaseExceptionDecorator(DecoratedTask task, ExceptionTask exceptionTask)
    {
        super(task);

        this.exceptionTask = exceptionTask;
        this.decorator = this;
    }

    protected void addExceptionRequest(StateBuilder stateBuilder)
    {
        TaskStateChanger<ExceptionDebuggerEvent> changer = new TaskStateChanger<>(this::handleExceptionState, decorator);
        ExceptionRequestBuilder requestBuilder = ExceptionRequestBuilder.from()
                .setStateChanger(changer);

        stateBuilder.addRequest(requestBuilder);
    }

    public void handleExceptionState(ExceptionDebuggerEvent debuggerEvent, StateBuilder stateBuilder)
    {
        if( this.exceptionTask.isCaught() ) {
            this.exceptionCaught = true;
        } else {
            this.exceptionCaught = false;

            stateBuilder.setChangeState(false);
        }
    }

    public boolean isExceptionCaught()
    {
        return this.exceptionCaught;
    }

    @Override
    public void setDecorator(DecoratedTask decorator)
    {
        super.setDecorator(decorator);

        this.decorator = decorator;
    }
}
