package enegade.uml.selector;

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

import enegade.uml.type.MethodCall;

import java.util.ArrayList;
import java.util.List;

/**
 * @author enegade
 */
public class ConditionSelector
{
    private ConditionType conditionType;

    private String title = "";

    private int startLine;

    private int endLine;

    private List<ConditionSelector> components = new ArrayList<>();

    public ConditionSelector(ConditionType conditionType)
    {
        this.conditionType = conditionType;
    }

    public boolean isCompound()
    {
        switch (this.conditionType) {
            case CATCH_BLOCK:
                return true;
        }

        return false;
    }

    public boolean isWithin(MethodCall methodCall)
    {
        return methodCall.getCallLineNumber() >= this.getStartLine()
                && methodCall.getCallLineNumber() <= this.getEndLine();
    }

    public String getTitle()
    {
        return title;
    }

    public ConditionSelector setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public int getStartLine()
    {
        return startLine;
    }

    public ConditionSelector setStartLine(int startLine)
    {
        this.startLine = startLine;
        return this;
    }

    public int getEndLine()
    {
        return endLine;
    }

    public ConditionSelector setEndLine(int endLine)
    {
        this.endLine = endLine;
        return this;
    }

    public ConditionType getConditionType()
    {
        return conditionType;
    }

    public List<ConditionSelector> getComponents()
    {
        return List.copyOf(this.components);
    }

    public void addComponent(ConditionSelector component)
    {
        this.components.add(component);
    }
}
