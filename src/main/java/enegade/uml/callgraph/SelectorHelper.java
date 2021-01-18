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

import java.util.List;
import java.util.Optional;

/**
 * @author enegade
 */
public class SelectorHelper
{
    /**
     * entranceView mast not be root
     */
    public static Optional<MethodSelector> getMethodSelector(List<TypeSelector> typeSelectors,
                                                             CallGraph.EntranceView entranceView)
    {
        return SelectorHelper.getTypeSelector(typeSelectors, entranceView)
                .flatMap( typeSelector -> SelectorHelper.getMethodSelector(typeSelector, entranceView) );
    }

    public static Optional<MethodSelector> getMethodSelector(TypeSelector typeSelector,
                                                             CallGraph.EntranceView entranceView)
    {
        MethodCall methodCall = entranceView.getMethodCall().get();

        for( MethodSelector methodSelector : typeSelector.getMethods() ) {
            if( methodCall.getSelectorName().equals( methodSelector.getName() ) ) {
                return Optional.of(methodSelector);
            }
        }

        return Optional.empty();
    }

    public static Optional<TypeSelector> getTypeSelector(List<TypeSelector> typeSelectors,
                                                         CallGraph.EntranceView entranceView)
    {
        String typeName = SelectorHelper.getTypeNameForSequence(entranceView);

        for(TypeSelector typeSelector : typeSelectors) {
            if( typeName.equals( typeSelector.getName() ) ) {
                return Optional.of(typeSelector);
            }
        }

        return Optional.empty();
    }

    /**
     * entranceView mast not be root
     */
    public static String getTypeNameForSequence(CallGraph.EntranceView entranceView)
    {
        MethodCall methodCall = entranceView.getMethodCall().get();

        return methodCall.getTypeName();
    }
}
