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
import com.sun.jdi.event.LocatableEvent;
import enegade.uml.LineReader;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author enegade
 */
public class StepInfo implements Serializable
{
    private final static long serialVersionUID = 1;

    private String line = "";

    private long codeIndex;

    private int lineNumber;

    private MethodCall calledMethod;

    public StepInfo(LocatableEvent event)
    {
        StackFrame frame;
        try {
            frame = event.thread().frame(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Location location = frame.location();


//        String line = LineReader.INSTANCE.readLine(location);
//
//        this.line = line;
        this.codeIndex = location.codeIndex();
        this.lineNumber = location.lineNumber();
    }

    public Optional<MethodCall> getCalledMethod()
    {
        return Optional.ofNullable(this.calledMethod);
    }

    public StepInfo setCalledMethod(MethodCall calledMethod)
    {
        this.calledMethod = calledMethod;
        return this;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public long getCodeIndex()
    {
        return codeIndex;
    }

    public String getLine()
    {
        return line;
    }
}
