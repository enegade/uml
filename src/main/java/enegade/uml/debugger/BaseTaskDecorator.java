package enegade.uml.debugger;

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
public class BaseTaskDecorator implements DecoratedTask
{
    private DecoratedTask task;

    public BaseTaskDecorator(DecoratedTask task)
    {
        this.task = task;

        task.setDecorator(this);
    }

    @Override
    public void setDecorator(DecoratedTask decorator)
    {
        task.setDecorator(decorator);
    }
}
