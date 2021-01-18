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
import enegade.uml.selector.FilterSelector;
import enegade.uml.selector.MethodSelector;
import enegade.uml.selector.TypeSelector;

import java.util.Optional;

/**
 * @author enegade
 */
public class StartingFilterImpl extends ConfiguredFilter implements StartingFilter
{
    public StartingFilterImpl(SelectorChangeMapper changeMapper)
    {
        super(changeMapper);
    }

    @Override
    public boolean isStarting(CallGraph.EntranceView entranceView)
    {
        return isEntranceSelected(entranceView);
    }

    public boolean isAnyMethodSelected()
    {
        return getSelectors().stream()
                .anyMatch( typeSelector -> {
                    if( typeSelector.isStarting() ) {
                        return true;
                    }

                    return typeSelector.getMethods().stream()
                            .anyMatch(MethodSelector::isStarting);
                } );
    }

    @Override
    protected boolean isSelected(FilterSelector selector)
    {
        return selector.isStarting();
    }
}
