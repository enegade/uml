package enegade.uml;

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

import com.sun.jdi.*;
import enegade.uml.type.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author enegade
 */
public class TypeCollector implements Serializable
{
    private final static long serialVersionUID = 1;

    private Map<String, TypeInfo> typeInfos = new HashMap<>();

    private Map<String, TypeInfo> completedTypeInfos = new HashMap<>();

    private Map<String, TypeInfo> objectTypes = new HashMap<>();

    private Map<String, TypeInfo> staticMethodTypes = new HashMap<>();

    private Map<Long, ObjectInfo> objectInfos = new HashMap<>();

    public List<ObjectInfo> getObjectInfos()
    {
        return List.copyOf( this.objectInfos.values() );
    }

    public List<TypeInfo> getObjectTypes()
    {
        return List.copyOf( this.objectTypes.values() );
    }

    public List<TypeInfo> getStaticMethodTypes()
    {
        return List.copyOf( this.staticMethodTypes.values() );
    }

    public ObjectInfo createObjectInfo(ObjectReference thisObject)
    {
        long id = thisObject.uniqueID();
        ObjectInfo objectInfo = this.objectInfos.get(id);
        if(objectInfo == null) {
            objectInfo = new ObjectInfo(thisObject, this);
            this.objectInfos.put(id, objectInfo);
        }

        return objectInfo;
    }

    public ObjectInfo createObjectInfo(ObjectInfo fromObjectInfo)
    {
        long id = fromObjectInfo.getId();
        ObjectInfo objectInfo = this.objectInfos.get(id);
        if(objectInfo == null) {
            objectInfo = new ObjectInfo(fromObjectInfo, this);
            this.objectInfos.put(id, objectInfo);
        }

        return objectInfo;
    }

    public TypeInfo createTypeInfo(ReferenceType fromType)
    {
        String typeName = fromType.name();
        TypeInfo typeInfo = this.typeInfos.get(typeName);
        if(typeInfo == null) {
            typeInfo = new TypeInfo(fromType);

            this.typeInfos.put(typeName, typeInfo);
        }

        return typeInfo;
    }

    public TypeInfo createTypeInfo(TypeInfo fromTypeInfo)
    {
        String typeName = fromTypeInfo.getTypeName();
        TypeInfo typeInfo = this.typeInfos.get(typeName);
        if(typeInfo == null) {
            typeInfo = new TypeInfo(fromTypeInfo);

            this.typeInfos.put(typeName, typeInfo );
        }

        return typeInfo;
    }

    public List<TypeInfo> getTypeInfos()
    {
        return List.copyOf( this.typeInfos.values() );
    }

    private boolean isTypeCompleted(TypeInfo typeInfo)
    {
        return this.completedTypeInfos.containsKey( typeInfo.getTypeName() );
    }

    private void markAsCompleted(TypeInfo typeInfo)
    {
        this.completedTypeInfos.put( typeInfo.getTypeName(), typeInfo );
    }

    public void addMethodHierarchy(MethodInfo methodInfo, TypeInfo typeInfo)
    {
        TypeInfo createdFrom = typeInfo.getCreatedFrom().orElse(null);
        List<MethodInfo> methods;
        if(createdFrom != null) {
            methods = createdFrom.getMethods();
        } else {
            methods = typeInfo.getReferenceType().get().methods().stream()
                    .map(MethodInfo::new).collect( Collectors.toList() );
        }

        for (MethodInfo method : methods) {
            if( method.getName().equals( methodInfo.getName() )
                    && method.getSignature().equals( methodInfo.getSignature() ) ) {
                typeInfo.addMethod(method);
            }
        }

        for( TypeInfo superTypeInfo : typeInfo.getSuperTypeInfoList() ) {
            addMethodHierarchy(methodInfo, superTypeInfo);
        }
    }

    public void addTypeHierarchy(TypeInfo typeInfo)
    {
        if( isTypeCompleted(typeInfo) ) {
            return;
        }

        TypeInfo superTypeInfo = null;
        TypeInfo createdFrom = typeInfo.getCreatedFrom().orElse(null);
        if(createdFrom == null) {
            ClassType superType = typeInfo.getClassType().get().superclass();
            if (superType != null) {
                superTypeInfo = createTypeInfo(superType);
            }
        } else {
            superTypeInfo = createdFrom.getSuperClassTypeInfo().orElse(null);
            if(superTypeInfo != null) {
                superTypeInfo = createTypeInfo(superTypeInfo);
            }
        }

        if(superTypeInfo != null) {
            typeInfo.setSuperClassTypeInfo(superTypeInfo);
            addTypeHierarchy(superTypeInfo);
        }

        addInterfaceTypeHierarchy(typeInfo);

        markAsCompleted(typeInfo);
    }

    private void addInterfaceTypeHierarchy(TypeInfo typeInfo)
    {
        if( isTypeCompleted(typeInfo) ) {
            return;
        }

        List<TypeInfo> interfaceTypeInfos;
        TypeInfo createdFrom = typeInfo.getCreatedFrom().orElse(null);
        if(createdFrom == null) {
            List<InterfaceType> interfaceTypes;
            if ( typeInfo.isInterface() ) {
                interfaceTypes = typeInfo.getInterfaceType().get().superinterfaces();
            } else {
                interfaceTypes = typeInfo.getClassType().get().interfaces();
            }

            interfaceTypeInfos = interfaceTypes.stream()
                    .map(this::createTypeInfo).collect(Collectors.toList());
        } else {
            interfaceTypeInfos = createdFrom.getInterfaces().stream()
                    .map(this::createTypeInfo).collect(Collectors.toList());
        }

        for (TypeInfo interfaceTypeInfo : interfaceTypeInfos) {
            typeInfo.addInterface(interfaceTypeInfo);

            addInterfaceTypeHierarchy(interfaceTypeInfo);
        }

        markAsCompleted(typeInfo);
    }

    public void addObjectType(TypeInfo typeInfo)
    {
        this.objectTypes.putIfAbsent( typeInfo.getTypeName(), typeInfo );
    }

    public void addStaticMethodType(TypeInfo typeInfo)
    {
        this.staticMethodTypes.putIfAbsent( typeInfo.getTypeName(), typeInfo );
    }
}
