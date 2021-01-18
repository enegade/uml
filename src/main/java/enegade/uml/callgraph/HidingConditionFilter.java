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

/**
 * @author enegade
 */
public class HidingConditionFilter extends ConfiguredFilter implements HidingFilter
{
    public HidingConditionFilter(SelectorChangeMapper changeMapper)
    {
        super(changeMapper);
    }

    @Override
    public boolean isHidden(CallGraph.EntranceView entranceView)
    {
        CallGraph.EntranceView opener = entranceView.getOpener().get();
        if( opener.isRoot() ) {
            return false;
        }

        MethodCall methodCall = entranceView.getMethodCall().get();

        return SelectorHelper.getMethodSelector( getSelectors(), opener )
                .filter( methodSelector -> {
                    return methodSelector.getHidingConditions().stream()
                            .anyMatch( hc -> hc.isWithin(methodCall) );
                } )
                .isPresent();
    }
}
