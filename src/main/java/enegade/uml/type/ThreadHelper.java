package enegade.uml.type;

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

import com.sun.jdi.*;
import enegade.uml.jdi.ThreadableDebuggerEvent;

/**
 * @author enegade
 */
public class ThreadHelper
{
    public static long getLocationThreadObjectId(ThreadableDebuggerEvent<?> debuggerEvent)
    {
        StackFrame frame;
        try {
            frame = debuggerEvent.getThreadReference().frame(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Location location = frame.location();

        ReferenceType type = location.declaringType();

        Field threadIdField = type.fieldByName("tid");
        ObjectReference threadObject = frame.thisObject();
        LongValue threadIdValue = (LongValue) threadObject.getValue(threadIdField);
        long threadObjectId = threadIdValue.value();

        return threadObjectId;
    }

    public static long getThreadObjectId(ThreadableDebuggerEvent<?> debuggerEvent)
    {
        ThreadReference thread = debuggerEvent.getThreadReference();

        Field threadIdField = thread.referenceType().fieldByName("tid");
        LongValue threadIdValue = (LongValue) thread.getValue(threadIdField);
        long threadObjectId = threadIdValue.value();

        return threadObjectId;
    }
}
