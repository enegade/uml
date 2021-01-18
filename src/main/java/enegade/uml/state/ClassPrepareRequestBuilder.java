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
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import enegade.uml.jdi.ClassPrepareDebuggerEvent;
import enegade.uml.jdi.ClassPrepareDebuggerRequest;
import enegade.uml.jdi.DebuggerRequest;
import enegade.uml.excepion.UnsupportedOperationException;

/**
 * @author enegade
 */
public class ClassPrepareRequestBuilder extends ClassVisibleRequestBuilder<ClassPrepareDebuggerEvent, ClassPrepareRequestBuilder>
{
    public static ClassPrepareRequestBuilder from()
    {
        return new ClassPrepareRequestBuilder();
    }

    @Override
    protected ClassPrepareRequestBuilder getThis() {
        return this;
    }

    @Override
    public ClassPrepareRequestBuilder getCopy()
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected DebuggerRequest<ClassPrepareRequest, ClassPrepareDebuggerEvent>
    createWithEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<ClassPrepareRequest, ClassPrepareDebuggerEvent> debuggerRequest;
        ClassPrepareRequest request = requestManager.createClassPrepareRequest();
        debuggerRequest = new ClassPrepareDebuggerRequest( this.getHandler().get(), request, this.getTask().get() );

        return debuggerRequest;
    }

    @Override
    protected DebuggerRequest<ClassPrepareRequest, ClassPrepareDebuggerEvent>
    createWithoutEventsRequested(EventRequestManager requestManager)
    {
        DebuggerRequest<ClassPrepareRequest, ClassPrepareDebuggerEvent> debuggerRequest;
        debuggerRequest = new ClassPrepareDebuggerRequest( this.getHandler().get() );

        return debuggerRequest;
    }

    @Override
    public ClassPrepareRequestBuilder addThreadFilter(ThreadReference thread)
    {
        throw new UnsupportedOperationException();
    }
}
