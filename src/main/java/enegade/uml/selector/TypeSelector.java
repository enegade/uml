package enegade.uml.selector;

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

import enegade.uml.type.MethodCall;
import enegade.uml.type.MethodInfo;
import enegade.uml.type.TypeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author enegade
 */
public class TypeSelector extends FilterSelector
{
    private String name = "";

    private boolean selected = false;

    private List<MethodSelector> methods = new ArrayList<>();

    private List<FieldSelector> fields = new ArrayList<>();

    private List<ObjectSelector> objects = new ArrayList<>();

    public static TypeSelector from(TypeInfo typeInfo)
    {
        return TypeSelector.from(typeInfo, m -> {});
    }

    public static TypeSelector from(TypeInfo typeInfo, Consumer<MethodSelector> methodSelectorBuilder)
    {
        TypeSelector typeSelector = new TypeSelector();
        typeSelector.setName( typeInfo.getTypeName() );

        for ( MethodInfo methodInfo : typeInfo.getMethods() ) {
            MethodSelector methodSelector = new MethodSelector();
            methodSelector.setName(MethodCall.getSelectorName(methodInfo));
            typeSelector.addMethod(methodSelector);

            methodSelectorBuilder.accept(methodSelector);
        }

        return typeSelector;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMethod(MethodSelector methodSelector)
    {
        this.methods.add(methodSelector);
    }

    public void addObject(ObjectSelector objectSelector)
    {
        this.objects.add(objectSelector);
    }

    public TypeSelector setSelected(boolean selected)
    {
        this.selected = selected;
        return this;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public List<MethodSelector> getMethods()
    {
        return List.copyOf(this.methods);
    }

    public List<ObjectSelector> getObjects()
    {
        return List.copyOf(this.objects);
    }
}
