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

import enegade.uml.CallGraph;
import enegade.uml.selector.TypeSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author enegade
 */
public class CommonSuppressingFilter implements SuppressingFilter
{
    private List<TypeSelector> typeSelectors = new ArrayList<>();

    public CommonSuppressingFilter()
    {
        TypeSelector typeSelector = new TypeSelector();
        typeSelector.setName("java.lang.String");

        this.typeSelectors.add(typeSelector);
    }

    @Override
    public boolean isSuppressed(CallGraph.EntranceView entranceView)
    {
        String typeName = SelectorHelper.getTypeNameForSequence(entranceView);
        for (TypeSelector typeSelector : this.typeSelectors) {
            if( typeSelector.getName().equals(typeName) ) {
                return true;
            }
        }

        return false;
    }
}
