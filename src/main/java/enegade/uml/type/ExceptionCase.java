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

import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.event.ExceptionEvent;

import java.io.Serializable;

/**
 * @author enegade
 */
public class ExceptionCase implements Serializable
{
    private final static long serialVersionUID = 1;

    private String name;

    private String typeName;

    private String methodName;

    private int line;

    public ExceptionCase(ExceptionEvent event)
    {
        ObjectReference exception = event.exception();
        this.name = exception.referenceType().name();

        Location location = event.location();
        this.typeName = location.declaringType().name();
        this.methodName = location.method().name();
        this.line = location.lineNumber();
    }

    public String getName()
    {
        return name;
    }
}
