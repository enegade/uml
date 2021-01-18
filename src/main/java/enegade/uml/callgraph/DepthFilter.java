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

/**
 * @author enegade
 */
public class DepthFilter implements TrimmingFilter
{
    private CallGraph callGraph;

    private int depth;

    public DepthFilter(int depth)
    {
        this.depth = depth;
    }

    public DepthFilter setCallGraph(CallGraph callGraph)
    {
        this.callGraph = callGraph;
        return this;
    }

    @Override
    public boolean isTrimming(CallGraph.EntranceView entranceView)
    {
        return this.callGraph.getEntranceView().getDepth() > this.depth;
    }
}
