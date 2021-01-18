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
import enegade.uml.selector.TypeSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author enegade
 */
public abstract class ConfiguredFilter
{
    private List<TypeSelector> selectors = new ArrayList<>();

    private SelectorChangeMapper changeMapper;

    public ConfiguredFilter(SelectorChangeMapper changeMapper)
    {
        this.changeMapper = changeMapper;
    }

    public SelectorChangeMapper getChangeMapper()
    {
        return this.changeMapper;
    }

    public void setSelectors(List<TypeSelector> selectors)
    {
        this.selectors = selectors;
    }

    public List<TypeSelector> getSelectors()
    {
        return List.copyOf(this.selectors);
    }

    protected boolean isEntranceSelected(CallGraph.EntranceView entranceView)
    {
        Optional<TypeSelector> optional = SelectorHelper.getTypeSelector(this.selectors, entranceView);

        return optional
                .filter(this::isSelected)
                .isPresent()
                ||
                optional
                .flatMap( typeSelector -> SelectorHelper.getMethodSelector(typeSelector, entranceView) )
                .filter(this::isSelected)
                .isPresent();
    }

    protected boolean isSelected(FilterSelector selector)
    {
        return false;
    }
}
