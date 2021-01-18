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

import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodExitRequest;
import enegade.uml.CallGraph;
import enegade.uml.jdi.DebuggerRequest;
import enegade.uml.jdi.MethodExitDebuggerEvent;
import enegade.uml.jdi.MethodExitDebuggerRequest;

/**
 * @author enegade
 */
public class MethodExitRequestBuilder extends ClassVisibleRequestBuilder<MethodExitDebuggerEvent, MethodExitRequestBuilder>
{
    private CallGraph.EntranceView entranceView;

    public CallGraph.EntranceView getEntranceView()
    {
        return entranceView;
    }

    public MethodExitRequestBuilder setEntranceView(CallGraph.EntranceView entranceView)
    {
        this.entranceView = entranceView;
        return this;
    }

    public static MethodExitRequestBuilder from()
    {
        return new MethodExitRequestBuilder();
    }

    public static MethodExitRequestBuilder from(MethodExitRequestBuilder requestBuilder)
    {
        return requestBuilder.getCopy();
    }

    @Override
    public MethodExitRequestBuilder getCopy()
    {
        MethodExitRequestBuilder copy = new MethodExitRequestBuilder();
        this.copy(copy);
        copy.entranceView = this.entranceView;

        return copy;
    }

    @Override
    protected MethodExitRequestBuilder getThis() {
        return this;
    }

    @Override
    protected DebuggerRequest<MethodExitRequest, MethodExitDebuggerEvent>
    createWithEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<MethodExitRequest, MethodExitDebuggerEvent> debuggerRequest;
        MethodExitRequest request = requestManager.createMethodExitRequest();
        debuggerRequest = new MethodExitDebuggerRequest( this.getHandler().get(), request, this.getTask().get() );

        return debuggerRequest;
    }

    @Override
    protected DebuggerRequest<MethodExitRequest, MethodExitDebuggerEvent>
    createWithoutEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<MethodExitRequest, MethodExitDebuggerEvent> debuggerRequest;
        debuggerRequest = new MethodExitDebuggerRequest( this.getHandler().get() );

        return debuggerRequest;
    }
}
