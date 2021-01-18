package enegade.uml.state;

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

import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import enegade.uml.jdi.DebuggerRequest;
import enegade.uml.jdi.ExceptionDebuggerEvent;
import enegade.uml.jdi.ExceptionDebuggerRequest;

/**
 * @author enegade
 */
public class ExceptionRequestBuilder extends ClassVisibleRequestBuilder<ExceptionDebuggerEvent, ExceptionRequestBuilder>
{
    private ReferenceType refType;

    private boolean notifyCaught;

    private boolean notifyUncaught;

    public ReferenceType getRefType()
    {
        return refType;
    }

    @Override
    public ExceptionRequestBuilder getCopy()
    {
        throw new RuntimeException("Not implemented");
    }

    public ExceptionRequestBuilder setRefType(ReferenceType refType)
    {
        this.refType = refType;

        return this;
    }

    public boolean isNotifyCaught()
    {
        return notifyCaught;
    }

    public ExceptionRequestBuilder setNotifyCaught(boolean notifyCaught)
    {
        this.notifyCaught = notifyCaught;

        return this;
    }

    public boolean isNotifyUncaught()
    {
        return notifyUncaught;
    }

    public ExceptionRequestBuilder setNotifyUncaught(boolean notifyUncaught)
    {
        this.notifyUncaught = notifyUncaught;

        return this;
    }

    public static ExceptionRequestBuilder from()
    {
        return new ExceptionRequestBuilder();
    }

    @Override
    protected ExceptionRequestBuilder getThis() {
        return this;
    }

    @Override
    protected DebuggerRequest<ExceptionRequest, ExceptionDebuggerEvent>
    createWithEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<ExceptionRequest, ExceptionDebuggerEvent> debuggerRequest;
        ExceptionRequest request = requestManager.createExceptionRequest(this.refType, this.notifyCaught, this.notifyUncaught);
        debuggerRequest = new ExceptionDebuggerRequest( this.getHandler().get(), request, this.getTask().get() );

        return debuggerRequest;
    }

    @Override
    protected DebuggerRequest<ExceptionRequest, ExceptionDebuggerEvent>
    createWithoutEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<ExceptionRequest, ExceptionDebuggerEvent> debuggerRequest;
        debuggerRequest = new ExceptionDebuggerRequest( this.getHandler().get() );

        return debuggerRequest;
    }
}
