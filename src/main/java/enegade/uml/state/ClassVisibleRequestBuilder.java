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
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import enegade.uml.jdi.ClassVisibleDebuggerRequest;
import enegade.uml.jdi.DebuggerEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author enegade
 */
abstract public class ClassVisibleRequestBuilder<D extends DebuggerEvent<?>, B extends ClassVisibleRequestBuilder<D, B>> extends ThreadVisibleRequestBuilder<D, B>
{
    private List<ReferenceType> referenceTypes = new ArrayList<>();

    private List<String> classPatterns = new ArrayList<>();

    protected void copy(ClassVisibleRequestBuilder<D, B> copy)
    {
        super.copy(copy);

        copy.referenceTypes = new ArrayList<>(this.referenceTypes);
        copy.classPatterns = new ArrayList<>(this.classPatterns);
    }

    public B addClassFilter(ReferenceType refType)
    {
        this.referenceTypes.add(refType);

        return getThis();
    }

    public B addClassFilter(String classPattern)
    {
        this.classPatterns.add(classPattern);

        return getThis();
    }

    @Override
    public ClassVisibleDebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>> build(EventRequestManager requestManager)
    {
        ClassVisibleDebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>> request;
        request = (ClassVisibleDebuggerRequest<? extends EventRequest, ? extends DebuggerEvent<?>>) super.build(requestManager);

        for( ReferenceType referenceType : this.referenceTypes ) {
            request.addClassFilter(referenceType);
        }

        for( String classPattern : this.classPatterns ) {
            request.addClassFilter(classPattern);
        }

        return request;
    }
}
