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
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import enegade.uml.jdi.DebuggerEvent;
import enegade.uml.jdi.ThreadVisibleDebuggerRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author enegade
 */
abstract public class ThreadVisibleRequestBuilder<D extends DebuggerEvent<?>, B extends ThreadVisibleRequestBuilder<D, B>> extends RequestBuilder<D, B>
{
    protected List<ThreadReference> threads = new ArrayList<>();

    protected void copy(ThreadVisibleRequestBuilder<D, B> copy)
    {
        super.copy(copy);

        copy.threads = new ArrayList<>(this.threads);
    }

    public B addThreadFilter(ThreadReference thread)
    {
        this.threads.add(thread);

        return getThis();
    }

    public List<ThreadReference> getThreadFilters()
    {
        return List.copyOf(this.threads);
    }

    @Override
    public ThreadVisibleDebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>> build(EventRequestManager requestManager)
    {
        ThreadVisibleDebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>> request;
        request = (ThreadVisibleDebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>>) super.build(requestManager);

        for( ThreadReference threadReference : this.threads ) {
            request.addThreadFilter(threadReference);
        }

        return request;
    }
}
