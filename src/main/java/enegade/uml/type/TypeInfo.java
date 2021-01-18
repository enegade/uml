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

import com.sun.jdi.*;
import enegade.uml.excepion.LogicException;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author enegade
 */
public class TypeInfo implements Serializable
{
    private final static long serialVersionUID = 1;

    private transient ClassType classType;

    private transient InterfaceType interfaceType;

    private boolean hasCalledMethods = false;

    private Map<String, String> dependencies = new HashMap<>();

    private Map<String, String> dependentByTypes = new HashMap<>();

    private boolean dependent = true;

    private TypeInfo superClassTypeInfo;

    private List<TypeInfo> interfaces = new ArrayList<>();

    private boolean isInterface = false;

    private boolean isAbstract = false;

    private Map<Integer, MethodInfo> methods = new HashMap<>();

    private String typeName;

    private TypeInfo createdFrom;

    private List<FieldInfo> fields;

    private Declaration declaration;

    public TypeInfo(ReferenceType referenceType)
    {
        if(referenceType instanceof ClassType) {
            this.classType = (ClassType) referenceType;
            this.isInterface = false;
            this.isAbstract = classType.isAbstract();
            if( classType.isEnum() ) {
                this.declaration = Declaration.ENUM;
            } else if( classType.isAbstract() ) {
                this.declaration = Declaration.ABSTRACT;
            } else {
                this.declaration = Declaration.CLASS;
            }
        } else if(referenceType instanceof InterfaceType) {
            this.interfaceType = (InterfaceType) referenceType;
            this.isInterface = true;
            this.isAbstract = true;
            this.declaration = Declaration.INTERFACE;
        } else {
            throw new LogicException();
        }

        this.typeName = referenceType.name();

        try {
            this.fields = getReferenceType().get().fields().stream()
                    .map(FieldInfo::new)
                    .collect(Collectors.toList());
        } catch (ClassNotPreparedException e) {
            int s = 1;
            this.fields = new ArrayList<>();
        }
    }

    public TypeInfo(TypeInfo typeInfo)
    {
        if( !isDeserialized() ) {
            throw new LogicException();
        }

        this.isInterface = typeInfo.isInterface;
        this.isAbstract = typeInfo.isAbstract;
        this.typeName = typeInfo.typeName;
        this.declaration = typeInfo.declaration;

        this.fields = typeInfo.fields.stream()
                .map(FieldInfo::new)
                .collect(Collectors.toList());

        this.createdFrom = typeInfo;
    }

    public Declaration getDeclaration()
    {
        return declaration;
    }

    public boolean isAbstract()
    {
        return isAbstract;
    }

    public boolean hasCalledMethods()
    {
        return this.hasCalledMethods;
    }

    public void setHasCalledMethods(boolean hasCalledMethods)
    {
        this.hasCalledMethods = hasCalledMethods;
    }

    public TypeInfo setDependent(boolean dependent)
    {
        this.dependent = dependent;
        return this;
    }

    public boolean isDependent()
    {
        return dependent;
    }

    public static boolean isTypeSupported(ReferenceType referenceType)
    {
        if(referenceType instanceof ClassType) {
            return true;
        } else if(referenceType instanceof InterfaceType) {
            return true;
        } else {
            return false;
        }
    }

    public List<FieldInfo> getFields()
    {
        return List.copyOf(this.fields);
    }

    public void addDependency(TypeInfo typeInfo)
    {
        String typeName = typeInfo.getTypeName();
        if( typeName.equals( this.getTypeName() ) ) {
            return;
        }

        this.dependencies.putIfAbsent(typeName, typeName);

        typeInfo.addDependentBy(this);
    }

    private void addDependentBy(TypeInfo typeInfo)
    {
        String typeName = typeInfo.getTypeName();
        this.dependentByTypes.putIfAbsent(typeName, typeName);
    }

    public Collection<String> getDependencies()
    {
        return dependencies.values();
    }

    public Collection<String> getDependentByTypes()
    {
        return dependentByTypes.values();
    }

    public boolean isDeserialized()
    {
        return getReferenceType().isEmpty();
    }

    public Optional<TypeInfo> getCreatedFrom()
    {
        return Optional.ofNullable(this.createdFrom);
    }

    public String getTypeName()
    {
        return typeName;
    }

    public List<TypeInfo> getSuperTypeInfoList()
    {
        List<TypeInfo> superTypeInfoList = new ArrayList<>();
        if( this.superClassTypeInfo != null ) {
            superTypeInfoList.add(this.superClassTypeInfo);
        }
        superTypeInfoList.addAll(this.interfaces);

        return superTypeInfoList;
    }

    public Optional<ReferenceType> getReferenceType()
    {
        ReferenceType referenceType;
        if(this.isInterface) {
            referenceType = this.interfaceType;
        } else {
            referenceType = this.classType;
        }

        return Optional.ofNullable(referenceType);
    }

    public Optional<InterfaceType> getInterfaceType()
    {
        return Optional.ofNullable(this.interfaceType);
    }

    public boolean isInterface()
    {
        return isInterface;
    }

    public Optional<ClassType> getClassType()
    {
        return Optional.ofNullable(this.classType);
    }

    public Optional<TypeInfo> getSuperClassTypeInfo()
    {
        return Optional.ofNullable(this.superClassTypeInfo);
    }

    public void setSuperClassTypeInfo(TypeInfo superClassTypeInfo)
    {
        this.superClassTypeInfo = superClassTypeInfo;
    }

    public List<TypeInfo> getInterfaces()
    {
        return List.copyOf(this.interfaces);
    }

    public void addInterface(TypeInfo interfaceTypeInfo)
    {
        this.interfaces.add(interfaceTypeInfo);
    }

    public List<MethodInfo> getMethods()
    {
        return List.copyOf( this.methods.values() );
    }

    public List<MethodInfo> getNonStaticMethods()
    {
        return this.methods.values().stream()
                .filter( Predicate.not(MethodInfo::isStatic) )
                .collect(Collectors.toList());
    }

    public List<MethodInfo> getStaticMethods()
    {
        return this.methods.values().stream()
                .filter(MethodInfo::isStatic)
                .collect(Collectors.toList());
    }

    public Map<String, MethodInfo> getObjectMethods()
    {
        Map<String, MethodInfo> methodInfoMap;
        if(this.superClassTypeInfo != null) {
            methodInfoMap = this.superClassTypeInfo.getObjectMethods();
        } else {
            methodInfoMap = new HashMap<>();
        }

        for( MethodInfo methodInfo : getNonStaticMethods() ) {
            methodInfoMap.put( methodInfo.getSignature(), methodInfo );
        }

        return methodInfoMap;
    }

    public void addMethod(MethodInfo methodInfo)
    {
        this.methods.putIfAbsent( methodInfo.getHashCode(), methodInfo );
    }
}
