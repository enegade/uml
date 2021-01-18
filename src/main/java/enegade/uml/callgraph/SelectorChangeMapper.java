package enegade.uml.callgraph;

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

import enegade.uml.selector.TypeSelector;

import java.util.List;

/**
 * @author enegade
 */
public interface SelectorChangeMapper
{
    void requestChanges(List<TypeSelector> typeSelectors);

    List<TypeSelector> mapChanges();
}
