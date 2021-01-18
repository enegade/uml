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

import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import enegade.uml.jdi.DebuggerRequest;
import enegade.uml.jdi.StepDebuggerEvent;
import enegade.uml.jdi.StepDebuggerRequest;

import java.util.Collections;

/**
 * @author enegade
 */
public class StepRequestBuilder extends ClassVisibleRequestBuilder<StepDebuggerEvent, StepRequestBuilder>
{
    private ThreadReference thread;

    private int size;

    private int depth;

    public ThreadReference getThread() {
        return thread;
    }

    public StepRequestBuilder setThread(ThreadReference thread)
    {
        this.thread = thread;

        return this;
    }

    public int getSize()
    {
        return size;
    }

    public StepRequestBuilder setSize(int size)
    {
        this.size = size;

        return this;
    }

    public int getDepth()
    {
        return depth;
    }

    public StepRequestBuilder setDepth(int depth)
    {
        this.depth = depth;

        return this;
    }

    public static StepRequestBuilder from()
    {
        return new StepRequestBuilder();
    }

    @Override
    protected StepRequestBuilder getThis() {
        return this;
    }

    @Override
    public StepRequestBuilder getCopy()
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected DebuggerRequest<StepRequest, StepDebuggerEvent>
    createWithEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<StepRequest, StepDebuggerEvent> debuggerRequest;
        StepRequest request = requestManager.createStepRequest(this.thread, this.size, this.depth);
        debuggerRequest = new StepDebuggerRequest( this.getHandler().get(), request, this.getTask().get() );

        return debuggerRequest;
    }

    @Override
    protected DebuggerRequest<StepRequest, StepDebuggerEvent>
    createWithoutEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<StepRequest, StepDebuggerEvent> debuggerRequest;
        debuggerRequest = new StepDebuggerRequest( this.getHandler().get() );

        return debuggerRequest;
    }

    @Override
    public StepDebuggerRequest build(EventRequestManager requestManager)
    {
        if( isEventsRequested() ) {
            this.threads = Collections.emptyList();
        }

        return (StepDebuggerRequest) super.build(requestManager);
    }
}
