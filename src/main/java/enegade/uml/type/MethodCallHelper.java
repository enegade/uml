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

import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.LocatableEvent;

/**
 * @author enegade
 */
public class MethodCallHelper
{
    public static boolean isFrameSame(MethodCall methodCall, LocatableEvent event)
    {
        ThreadReference threadReference = event.thread();

        int frameCount;
        try {
            frameCount = threadReference.frameCount();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return methodCall.getFrameCount() == frameCount;
    }

    public static String getStringValue(Value value)
    {
        String stringValue = value == null ? "null" : value.toString();
        if( stringValue.length() > 100 ) {
            stringValue = stringValue.substring(0, 100);
        }

        return stringValue;
    }
}
