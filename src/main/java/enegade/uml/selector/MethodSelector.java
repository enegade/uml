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

import java.util.ArrayList;
import java.util.List;

/**
 * @author enegade
 */
public class MethodSelector extends FilterSelector
{
    private String name = "";

    private boolean selected = false;

    private List<ConditionSelector> conditions = new ArrayList<>();

    private List<ConditionSelector> hidingConditions = new ArrayList<>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public MethodSelector setSelected(boolean selected)
    {
        this.selected = selected;
        return this;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void addCondition(ConditionSelector condition)
    {
        this.conditions.add(condition);
    }

    public void setConditions(List<ConditionSelector> conditions)
    {
        this.conditions = List.copyOf(conditions);
    }

    public List<ConditionSelector> getConditions()
    {
        return List.copyOf(this.conditions);
    }

    public List<ConditionSelector> getHidingConditions()
    {
        return List.copyOf(this.hidingConditions);
    }
}
