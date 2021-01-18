package enegade.uml.jdi;

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
public interface Handler<D extends DebuggerEvent<?>>
{
    void handle(D debuggerEvent);
}
