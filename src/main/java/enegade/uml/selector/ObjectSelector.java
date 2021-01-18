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

/**
 * @author enegade
 */
public class ObjectSelector
{
    private long id;

    private boolean selected = false;

    public long getId()
    {
        return id;
    }

    public ObjectSelector setId(long id)
    {
        this.id = id;
        return this;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public boolean isSelected()
    {
        return selected;
    }
}
