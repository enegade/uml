package enegade.uml.debugger.stepmethodcall;

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
public abstract class StepMethodCallHelper
{
    public static boolean canAddNewEntrance(StepMethodCallTask task)
    {
        return task.isMethodEnter() && !task.isLoopDetected();
    }

    public static boolean canBeSkipped(StepMethodCallTask task)
    {
        return task.isMethodEnter() && !task.isLoopDetected();
    }

    public static boolean canGetReturnValue(StepMethodCallTask task)
    {
        return task.isMethodEnter() && !task.isLoopDetected();
    }
}
