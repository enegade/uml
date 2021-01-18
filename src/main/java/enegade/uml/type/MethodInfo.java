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

import com.sun.jdi.Method;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author enegade
 */
public class MethodInfo implements Serializable
{
    private final static long serialVersionUID = 1;

    private String name;

    private String signature;

    private String genericSignature;

    private int hashCode;

    private String returnTypeName;

    private List<String> argumentTypeNames;

    private boolean isStatic;

    private boolean isNative;

    private Visibility visibility;

    private boolean isAbstract;

    public MethodInfo(Method method)
    {
        this.name = method.name();
        this.signature = method.signature();
        this.hashCode = method.hashCode();
        this.isStatic = method.isStatic();
        this.isNative = method.isNative();

        this.returnTypeName = method.returnTypeName();
        this.argumentTypeNames = new ArrayList<>( method.argumentTypeNames() );

        this.visibility = Visibility.getVisibility(method);
        this.isAbstract = method.isAbstract();

        this.genericSignature = method.genericSignature();
    }

    public String getName()
    {
        return name;
    }

    public String getSignature()
    {
        return signature;
    }

    public Optional<String> getGenericSignature()
    {
        return Optional.ofNullable(this.genericSignature);
    }

    public boolean isAbstract()
    {
        return isAbstract;
    }

    public Visibility getVisibility()
    {
        return visibility;
    }

    public int getHashCode()
    {
        return hashCode;
    }

    public String getReturnTypeName()
    {
        return returnTypeName;
    }

    public List<String> getArgumentTypeNames()
    {
        return List.copyOf(this.argumentTypeNames);
    }

    public boolean isStatic()
    {
        return isStatic;
    }

    public boolean isNative()
    {
        return isNative;
    }
}
