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

/**
 * @author enegade
 */
public class HidingFilterImpl extends ConfiguredFilter implements HidingFilter
{
    public HidingFilterImpl(SelectorChangeMapper changeMapper)
    {
        super(changeMapper);
    }

    @Override
    public boolean isHidden(CallGraph.EntranceView entranceView)
    {
        return isEntranceSelected(entranceView);
    }

    @Override
    protected boolean isSelected(FilterSelector selector)
    {
        return selector.isHidden();
    }
}
