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
import com.sun.jdi.event.LocatableEvent;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author enegade
 */
public class MethodCall implements Serializable
{
    private final static long serialVersionUID = 1;

    private int callLineNumber;

    private Map<String, String> argumentValues;

    private transient List<ReferenceType> argumentTypes;

    private int frameCount;

    private long codeIndex;

    private StepInfo stepInfo;

    private String exitMethodName;

    private String returnValue;

    private transient ObjectReference thisObject;

    private transient ReferenceType type;

    private String typeName;

    private boolean isCondition = false;

    private MethodInfo methodInfo;

    public MethodCall(LocatableEvent event)
    {
        ThreadReference threadReference = event.thread();

        StackFrame frame;
        try {
            frame = threadReference.frame(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Location location = event.location();
        Method method = location.method();

        Map<String, String> argumentValues;
        try {
            List<LocalVariable> vars = method.arguments();

            Map<LocalVariable, Value> values = frame.getValues(vars);

            argumentValues = vars.stream()
                    .collect(HashMap::new, (m, var) -> {
                        Value val = values.get(var);
                        String stringVal = MethodCallHelper.getStringValue(val);
                        m.put(var.name(), stringVal);
                    }, HashMap::putAll);
        } catch (AbsentInformationException aie) {
            argumentValues = new HashMap<>();
        }
        this.argumentValues = argumentValues;

        MethodInfo methodInfo = new MethodInfo(method);
        this.methodInfo = methodInfo;


        // we interested only in loaded types
        this.argumentTypes = method.argumentTypeNames().stream()
                .map(typeName -> {
                    List<ReferenceType> loadedTypes = event.virtualMachine().classesByName(typeName);
                    return loadedTypes.isEmpty() ? null : loadedTypes.get(0);
                })
                .filter(Objects::nonNull)
                .filter(TypeInfo::isTypeSupported)
                .collect(Collectors.toList());

        try {
            this.frameCount = threadReference.frameCount();

            if (threadReference.frameCount() > 1) {
                StackFrame pFrame = threadReference.frame(1);
                Location pLocation = pFrame.location();
                this.callLineNumber = pLocation.lineNumber();
                this.codeIndex = pLocation.codeIndex();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if( !methodInfo.isStatic() ) {
            try {
                frame = threadReference.frame(0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            this.thisObject = frame.thisObject();
        }

        this.type = method.declaringType();
        this.typeName = this.type.name();
    }

    public Optional<ObjectReference> getThisObject()
    {
        return Optional.ofNullable(this.thisObject);
    }

    public ReferenceType getType()
    {
        return type;
    }

    public List<ReferenceType> getArgumentTypes()
    {
        return List.copyOf(this.argumentTypes);
    }

    public MethodInfo getMethodInfo()
    {
        return methodInfo;
    }

    public Optional<StepInfo> getStepInfo()
    {
        return Optional.ofNullable(this.stepInfo);
    }

    public MethodCall setStepInfo(StepInfo stepInfo)
    {
        this.stepInfo = stepInfo;
        return this;
    }

    public boolean isCondition()
    {
        return isCondition;
    }

    public void setIsCondition(boolean isCondition)
    {
        this.isCondition = isCondition;
    }

    public String getSelectorName()
    {
        return MethodCall.getSelectorName(this.methodInfo);
    }

    public static String getSelectorName(MethodInfo methodInfo)
    {
        return methodInfo.getReturnTypeName() + " " + methodInfo.getName() + "("
                + String.join( ", ", methodInfo.getArgumentTypeNames() ) + ")";
    }

    public Optional<String> getReturnValue()
    {
        return Optional.ofNullable(this.returnValue);
    }

    public MethodCall setReturnValue(Value returnValue)
    {
        this.returnValue = MethodCallHelper.getStringValue(returnValue);
        return this;
    }

    public MethodCall setExitMethodName(String exitMethodName)
    {
        this.exitMethodName = exitMethodName;
        return this;
    }

    public int getHashCode()
    {
        return this.methodInfo.getHashCode();
    }

    public String getSignature()
    {
        return this.methodInfo.getSignature();
    }

    public long getCodeIndex()
    {
        return codeIndex;
    }

    public String getTypeName()
    {
        return this.typeName;
    }

    public int getFrameCount()
    {
        return frameCount;
    }

    public int getCallLineNumber()
    {
        return callLineNumber;
    }

    public Map<String, String> getArgumentValues()
    {
        return this.argumentValues;
    }

    public String getName()
    {
        return this.methodInfo.getName();
    }
}
