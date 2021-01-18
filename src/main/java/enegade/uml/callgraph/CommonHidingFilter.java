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
import enegade.uml.selector.MethodSelector;
import enegade.uml.selector.TypeSelector;
import enegade.uml.type.ObjectInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author enegade
 */
public class CommonHidingFilter implements HidingFilter
{
    private List<TypeSelector> typeSelectors = new ArrayList<>();

    public CommonHidingFilter()
    {
        TypeSelector typeSelector = new TypeSelector();
        typeSelector.setName("java.lang.Object");

        MethodSelector methodSelector = new MethodSelector();
        methodSelector.setName("void <init>()");

        typeSelector.addMethod(methodSelector);
        this.typeSelectors.add(typeSelector);
    }

    @Override
    public boolean isHidden(CallGraph.EntranceView entranceView)
    {
        ObjectInfo objectInfo = entranceView.getEntranceTypeInfo().get().getObjectInfo().orElse(null);
        if(objectInfo != null) {
            if( objectInfo.getTypeInfo().getTypeName().equals("java.lang.Object") ) {
                return false;
            }
        }

        // hidden by method type
        return SelectorHelper.getMethodSelector( this.typeSelectors, entranceView )
                .isPresent();
    }
}
