package enegade.uml.type;

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

import com.sun.jdi.ObjectReference;
import enegade.uml.CallGraph;
import enegade.uml.TypeCollector;
import enegade.uml.excepion.LogicException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author enegade
 */
public class ObjectInfo implements Serializable
{
    private final static long serialVersionUID = 1;

    private long id;

    private CallGraph.EntranceView createdBy;

//    private List<CallGraph.EntranceView> calledByMethods = new ArrayList<>();
//    private List<CallGraph.EntranceView> calledMethods = new ArrayList<>();

    private List<String> calledByMethods = new ArrayList<>();
    private List<String> calledMethods = new ArrayList<>();

    private transient ObjectReference thisObject;

    private TypeInfo typeInfo;

    public ObjectInfo(ObjectReference thisObject, TypeCollector typeCollector)
    {
        this.thisObject = thisObject;
        this.id = thisObject.uniqueID();

        this.typeInfo = typeCollector.createTypeInfo( thisObject.referenceType() );;
    }

    public ObjectInfo(ObjectInfo fromObjectInfo, TypeCollector typeCollector)
    {
        if( !isDeserialized() ) {
            throw new LogicException();
        }

        this.id = fromObjectInfo.id;

        this.typeInfo = typeCollector.createTypeInfo(fromObjectInfo.typeInfo);
    }

    public boolean isDeserialized()
    {
        return this.thisObject == null;
    }

    public void setTypeInfo(TypeInfo typeInfo)
    {
        this.typeInfo = typeInfo;
    }

    public TypeInfo getTypeInfo()
    {
        return typeInfo;
    }

    public boolean isOrphan()
    {
        if( this.calledByMethods.isEmpty() ) {
            if( this.calledMethods.isEmpty() ) {
                return true;
            } else {
                return false;

//                boolean nonOrphanCalled = false;
//                for( CallGraph.EntranceView entranceView : this.calledMethods ) {
//                    ObjectInfo calledObjectInfo = entranceView.getEntranceTypeInfo()
//                            .flatMap(EntranceTypeInfo::getObjectInfo).orElse(null);
//                    if(calledObjectInfo == null) {
//                        nonOrphanCalled = true;
//                        break;
//                    } else {
//                        if( !calledObjectInfo.isOrphan() ) {
//                            nonOrphanCalled = true;
//                            break;
//                        }
//                    }
//                }
//
//                return !nonOrphanCalled;
            }
        }

        return false;
    }

    public long getId()
    {
        return id;
    }

    public Optional<CallGraph.EntranceView> getCreatedBy()
    {
        return Optional.ofNullable(this.createdBy);
    }

    public void setCreatedBy(CallGraph.EntranceView createdBy)
    {
        this.createdBy = createdBy;
    }

    public void addCalledByMethod(CallGraph.EntranceView entranceView)
    {
//        this.calledByMethods.add(entranceView);

        entranceView.getMethodCall().ifPresent( methodCall -> this.calledByMethods.add( methodCall.getName() ) );
    }

    public void addCalledMethod(CallGraph.EntranceView entranceView)
    {
//        this.calledMethods.add(entranceView);

        entranceView.getMethodCall().ifPresent( methodCall -> this.calledMethods.add( methodCall.getName() ) );
    }
}
