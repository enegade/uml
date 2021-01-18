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
import enegade.uml.selector.ObjectSelector;
import enegade.uml.selector.TypeSelector;
import enegade.uml.type.ObjectInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author enegade
 */
public class OrphanObjectSuppressingFilter implements SuppressingFilter
{
    private List<TypeSelector> typeSelectors = new ArrayList<>();

    public OrphanObjectSuppressingFilter(List<ObjectInfo> orphans)
    {
        for(ObjectInfo objectInfo : orphans) {
            TypeSelector typeSelector = new TypeSelector();

            ObjectSelector objectSelector = new ObjectSelector();
            objectSelector.setId( objectInfo.getId() );

            typeSelector.addObject(objectSelector);
            this.typeSelectors.add(typeSelector);
        }
    }

    @Override
    public boolean isSuppressed(CallGraph.EntranceView entranceView)
    {
        for (TypeSelector typeSelector : this.typeSelectors) {
            for( ObjectSelector objectSelector : typeSelector.getObjects() ) {
                boolean presented = entranceView.getEntranceTypeInfo().get().getObjectInfo()
                        .filter( objectInfo -> objectSelector.getId() == objectInfo.getId() )
                        .isPresent();
                if( presented ) {
                    return true;
                }
            }
        }

        return false;
    }
}
