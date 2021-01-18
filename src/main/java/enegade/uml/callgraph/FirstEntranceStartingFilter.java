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
import enegade.uml.type.MethodCall;
import enegade.uml.selector.MethodSelector;
import enegade.uml.selector.TypeSelector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author enegade
 */
public class FirstEntranceStartingFilter implements StartingFilter
{
    private Map<String, TypeSelector> typeSelectors = new HashMap<>();

    public FirstEntranceStartingFilter(CallGraph callGraph)
    {
        CallGraph.EntranceView rootEntrance = callGraph.getEntranceView();
        for( CallGraph.EntranceView entranceView : rootEntrance.getEntrances() ) {
            MethodCall methodCall = entranceView.getMethodCall().get();
            String typeName = SelectorHelper.getTypeNameForSequence(entranceView);

            this.typeSelectors.computeIfAbsent( typeName, tn -> {
                TypeSelector typeSelector = new TypeSelector();
                typeSelector.setName(tn);
                return typeSelector;
            } );

            TypeSelector typeSelector = this.typeSelectors.get(typeName);

            MethodSelector methodSelector = new MethodSelector();
            methodSelector.setName( methodCall.getSelectorName() );

            typeSelector.addMethod(methodSelector);
        }
    }

    @Override
    public boolean isStarting(CallGraph.EntranceView entranceView)
    {
        return SelectorHelper
                .getMethodSelector( new ArrayList<>( this.typeSelectors.values() ), entranceView )
                .isPresent();
    }
}
