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
public class FilterSelector
{
    private boolean starting = false;

    private boolean trimming = false;

    private boolean hidden = false;

    private boolean suppressed = false;

    private boolean skipped = false;

    public boolean isStarting()
    {
        return starting;
    }

    public void setStarting(boolean starting)
    {
        this.starting = starting;
    }

    public boolean isTrimming()
    {
        return trimming;
    }

    public void setTrimming(boolean trimming)
    {
        this.trimming = trimming;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    public boolean isSuppressed()
    {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed)
    {
        this.suppressed = suppressed;
    }

    public boolean isSkipped()
    {
        return skipped;
    }

    public void setSkipped(boolean skipped)
    {
        this.skipped = skipped;
    }
}
