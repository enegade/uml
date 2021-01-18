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

import com.sun.jdi.Field;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author enegade
 */
public class FieldInfo implements Serializable
{
    private final static long serialVersionUID = 1;

    private String name;

    private String typeName;

    private String genericTypeName;

    private Visibility visibility;

    private boolean isStatic;

    public FieldInfo(Field field)
    {
        this.typeName = field.typeName();
        this.name = field.name();
        this.genericTypeName = field.genericSignature();

        this.visibility = Visibility.getVisibility(field);
        this.isStatic = field.isStatic();
    }

    public FieldInfo(FieldInfo fieldInfo)
    {
        this.name = fieldInfo.name;
        this.typeName = fieldInfo.typeName;;
        this.genericTypeName = fieldInfo.genericTypeName;
        this.visibility = fieldInfo.visibility;
        this.isStatic = fieldInfo.isStatic;
    }

    public Visibility getVisibility()
    {
        return visibility;
    }

    public boolean isStatic()
    {
        return isStatic;
    }

    public String getName()
    {
        return name;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public Optional<String> getGenericTypeName()
    {
        return Optional.ofNullable(this.genericTypeName);
    }
}
