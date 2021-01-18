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

import enegade.uml.type.MethodCall;

import java.util.HashMap;
import java.util.Map;

/**
 * @author enegade
 */
public class DebuggingContext
{
    private Map<String, Map<String, String>> inspectedClasses = new HashMap<>();

    public DebuggingContext()
    {
//        Map<String, String> inspectedMethods = new HashMap<>();
//        inspectedMethods.put( "<init>()V", "<init>()V" );
//        this.inspectedClasses.put( "java.util.concurrent.ConcurrentSkipListSet", inspectedMethods );
//
//        inspectedMethods = new HashMap<>();
//        inspectedMethods.put( "findVarHandle(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/VarHandle;",
//                "findVarHandle(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/VarHandle;" );
//        this.inspectedClasses.put( "java.lang.invoke.MethodHandles$Lookup", inspectedMethods );
//
//        inspectedMethods = new HashMap<>();
//        inspectedMethods.put( "findMethodHandleType(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;",
//                "findMethodHandleType(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;" );
//        this.inspectedClasses.put( "java.lang.invoke.MethodHandleNatives", inspectedMethods );
//
//        inspectedMethods = new HashMap<>();
//        inspectedMethods.put( "forProperty(Ljava/lang/String;)Ljava/util/regex/Pattern$CharPredicate;",
//                "forProperty(Ljava/lang/String;)Ljava/util/regex/Pattern$CharPredicate;" );
//        this.inspectedClasses.put( "java.util.regex.CharPredicates", inspectedMethods );
    }

    public boolean hasMethodInspected(MethodCall methodCall)
    {
        Map<String, String> inspectedMethods = this.inspectedClasses.get( methodCall.getTypeName() );
        if( inspectedMethods == null ) {
            return false;
        }

        String fullName = methodCall.getName() + methodCall.getSignature();
        String inspectedMethod = inspectedMethods.get(fullName);

        return inspectedMethod != null;
    }

    public void addInspectedMethod(MethodCall methodCall)
    {
        Map<String, String> inspectedMethods = this.inspectedClasses.get( methodCall.getTypeName() );
        if( inspectedMethods == null ) {
            inspectedMethods = new HashMap<>();
            this.inspectedClasses.put( methodCall.getTypeName(), inspectedMethods );
        }

        String fullName = methodCall.getName() + methodCall.getSignature();
        String inspectedMethod = inspectedMethods.get(fullName);
        if(inspectedMethod == null) {
            inspectedMethods.put(fullName, fullName);
        }
    }
}
