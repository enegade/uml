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

import java.io.Serializable;

/**
 * @author enegade
 */
public class LoopCondition implements Serializable
{
    private final static long serialVersionUID = 1;

    private MethodCall staringMethodCall;

    private MethodCall endingMethodCall;

    public LoopCondition(MethodCall staringMethodCall, MethodCall endingMethodCall)
    {
        this.staringMethodCall = staringMethodCall;
        this.endingMethodCall = endingMethodCall;
    }

    public MethodCall getStaringMethodCall()
    {
        return staringMethodCall;
    }

    public MethodCall getEndingMethodCall()
    {
        return endingMethodCall;
    }
}
