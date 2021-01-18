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

import enegade.uml.CallGraph;
import enegade.uml.TypeCollector;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author enegade
 */
public class EntranceTypeInfo implements Serializable
{
    private final static long serialVersionUID = 1;

    private List<TypeInfo> argumentTypes;

    private TypeInfo returnTypeInfo;

    private ObjectInfo objectInfo;

    private TypeInfo typeInfo;

    public EntranceTypeInfo(CallGraph callGraph, MethodCall methodCall)
    {
        TypeCollector typeCollector = callGraph.getTypeCollector();

        methodCall.getThisObject().ifPresent( thisObject ->
                this.objectInfo = typeCollector.createObjectInfo(thisObject) );

        this.typeInfo = typeCollector.createTypeInfo( methodCall.getType() );
        this.typeInfo.setHasCalledMethods(true);

        this.argumentTypes = methodCall.getArgumentTypes().stream()
                .map(typeCollector::createTypeInfo)
                .collect(Collectors.toList());

        collectHierarchy(typeCollector);
    }

    public EntranceTypeInfo(CallGraph callGraph, EntranceTypeInfo fromEntranceTypeInfo)
    {
        TypeCollector typeCollector = callGraph.getTypeCollector();

        fromEntranceTypeInfo.getObjectInfo().ifPresent(objectInfo ->
                this.objectInfo = typeCollector.createObjectInfo(objectInfo) );

        this.typeInfo = typeCollector.createTypeInfo( fromEntranceTypeInfo.getTypeInfo() );
        this.typeInfo.setHasCalledMethods(true);

        fromEntranceTypeInfo.getReturnTypeInfo().ifPresent(returnTypeInfo ->
                this.returnTypeInfo = typeCollector.createTypeInfo(returnTypeInfo) );

        this.argumentTypes = fromEntranceTypeInfo.getArgumentTypes().stream()
                .map(typeCollector::createTypeInfo)
                .collect(Collectors.toList());

        collectHierarchy(typeCollector);
    }

    private void collectHierarchy(TypeCollector typeCollector)
    {
        if(this.objectInfo != null) {
            TypeInfo objectTypeInfo = this.objectInfo.getTypeInfo();
            typeCollector.addTypeHierarchy(objectTypeInfo);
        } else {
            typeCollector.addStaticMethodType(this.typeInfo);
        }
    }

    public List<TypeInfo> getArgumentTypes()
    {
        return List.copyOf(this.argumentTypes);
    }

    public Optional<TypeInfo> getReturnTypeInfo()
    {
        return Optional.ofNullable(this.returnTypeInfo);
    }

    public void setReturnTypeInfo(TypeInfo returnTypeInfo)
    {
        this.returnTypeInfo = returnTypeInfo;
    }

    public Optional<ObjectInfo> getObjectInfo()
    {
        return Optional.ofNullable(this.objectInfo);
    }

    public TypeInfo getTypeInfo()
    {
        return typeInfo;
    }
}
