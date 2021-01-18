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

import enegade.uml.callgraph.SelectorHelper;
import enegade.uml.excepion.LogicException;
import enegade.uml.selector.ConditionSelector;
import enegade.uml.selector.MethodSelector;
import enegade.uml.selector.TypeSelector;
import enegade.uml.type.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author enegade
 */
public class PlantUml
{
    private List<TypeSelector> conditionalTypeSelectors = new ArrayList<>();

    private Map<String, String> hiddenTypes = new HashMap<>();

    public void setConditionalTypeSelectors(List<TypeSelector> conditionalTypeSelectors)
    {
        this.conditionalTypeSelectors = List.copyOf(conditionalTypeSelectors);
    }

    public void setHiddenTypes(List<TypeSelector> typeSelectors)
    {
        for (TypeSelector typeSelector : typeSelectors) {
            if( typeSelector.isHidden() || typeSelector.isSuppressed() ) {
                this.hiddenTypes.put( typeSelector.getName(), typeSelector.getName() );
            }
        }
        this.hiddenTypes.put("java.lang.Object", "java.lang.Object");
    }

    public void renderClassDiagram(List<TypeInfo> typeInfos)
    {
        try ( PrintWriter printWriter = new PrintWriter( new FileWriter("class.txt") ) ) {
            renderClassDiagram(typeInfos, printWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void renderClassDiagram(List<TypeInfo> typeInfos, PrintWriter printWriter)
    {
        printWriter.println("@startuml");
        printWriter.println("set namespaceSeparator none");
//        printWriter.println("skinparam classAttributeIconSize 0");
        for(TypeInfo typeInfo : typeInfos) {
            if( this.hiddenTypes.containsKey( typeInfo.getTypeName() ) ) {
                continue;
            }

            if( !typeInfo.isDependent() ) {
                continue;
            }

            String declarationString = getDeclarationString( typeInfo.getDeclaration() );
            printWriter.println(declarationString + " " + getMultiLineText( typeInfo.getTypeName(), true ) + " {");

//            for( FieldInfo fieldInfo : typeInfo.getFields() ) {
//                String typeString = fieldInfo.getGenericTypeName().orElse( fieldInfo.getTypeName() );
//                String modifier = getModifierString(fieldInfo);
//                String visibility = getVisibilityString( fieldInfo.getVisibility() );
//                printWriter.println( modifier + visibility + getMultiLineText( typeString + " " + fieldInfo.getName(), false ) );
//            }

            for( MethodInfo methodInfo : typeInfo.getMethods() ) {
                String modifier = getModifierString(methodInfo);
                String visibility = getVisibilityString( methodInfo.getVisibility() );
                String signature = methodInfo.getGenericSignature()
                        .map( s -> s + " " + methodInfo.getName() )
                        .orElse( MethodCall.getSelectorName(methodInfo) );
                printWriter.println( modifier + visibility + getMultiLineText(signature, false) );
            }
            printWriter.println("}");

            TypeInfo superClassTypeInfo = typeInfo;
            do {
                superClassTypeInfo = superClassTypeInfo.getSuperClassTypeInfo().orElse(null);
            } while ( superClassTypeInfo != null
                    && !( superClassTypeInfo.isDependent() && !this.hiddenTypes.containsKey( superClassTypeInfo.getTypeName() ) )  );
            if(superClassTypeInfo != null) {
                String arrow = " <|-- ";
                if( typeInfo.isAbstract() ) {
                    arrow = " <|.. ";
                }
                printWriter.println( getMultiLineText( superClassTypeInfo.getTypeName(), true ) + arrow
                        + getMultiLineText( typeInfo.getTypeName(), true ) );
            }

            for ( TypeInfo interfaceTypeInfo : getInterfaces(typeInfo, true) ) {
                printWriter.println( getMultiLineText( interfaceTypeInfo.getTypeName(), true  ) + " <|.. "
                        + getMultiLineText( typeInfo.getTypeName(), true  ) );
            }

            for (String dependency : typeInfo.getDependencies() ) {
                if( !this.hiddenTypes.containsKey(dependency) ) {
                    printWriter.println( getMultiLineText( typeInfo.getTypeName(), true  ) + " ..> "
                            + getMultiLineText(dependency, true) );
                }
            }
        }
        printWriter.println("@enduml");
    }

    private String getDeclarationString(Declaration declaration)
    {
        switch (declaration) {
            case CLASS:
                return "class";
            case ABSTRACT:
                return "abstract class";
            case INTERFACE:
                return "interface";
            case ENUM:
                return "enum";
        }

        throw new LogicException();
    }

    private String getModifierString(MethodInfo methodInfo)
    {
        String modifier = "";
        if( methodInfo.isAbstract() ) {
            modifier += "{abstract}";
        }
        if( methodInfo.isStatic() ) {
            modifier += "{static}";
        }

        return modifier;
    }

    private String getModifierString(FieldInfo fieldInfo)
    {
        String modifier = "";
        if( fieldInfo.isStatic() ) {
            modifier += "{static}";
        }

        return modifier;
    }

    private String getVisibilityString(Visibility visibility)
    {
        switch (visibility) {
            case PUBLIC:
                return "+";
            case PRIVATE:
                return "-";
            case PACKAGE_PRIVATE:
                return "~";
            case PROTECTED:
                return "#";
        }

        throw new LogicException();
    }

    private Set<TypeInfo> getInterfaces(TypeInfo interfaceCandidate, boolean isRoot)
    {
        if( !isRoot && interfaceCandidate.isDependent() && !this.hiddenTypes.containsKey( interfaceCandidate.getTypeName() ) ) {
            return Set.of(interfaceCandidate);
        }

        Set<TypeInfo> interfaces = new HashSet<>();
        for( TypeInfo interfaceTypeInfo : interfaceCandidate.getInterfaces() ) {
            interfaces.addAll( getInterfaces(interfaceTypeInfo, false) );
        }

        return interfaces;
    }

    public void renderSequenceDiagram(CallGraph callGraph)
    {
        try ( PrintWriter printWriter = new PrintWriter( new FileWriter("sequence.txt") ) ) {
            printWriter.println("@startuml");

            renderMethodCall(printWriter, callGraph.getEntranceView());

            printWriter.println("@enduml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void renderMethodCall(PrintWriter printWriter, CallGraph.EntranceView entranceView)
    {
        renderMethodCall(printWriter, entranceView, false);
    }

    private void renderMethodCall(PrintWriter printWriter, CallGraph.EntranceView openerEntranceView, boolean inConstructor)
    {
        List<ConditionSelector> conditions = new ArrayList<>();
        if( !openerEntranceView.isRoot() ) {
            MethodSelector methodSelector = SelectorHelper.getMethodSelector(this.conditionalTypeSelectors, openerEntranceView)
                    .orElse(null);
            if (methodSelector != null) {
                conditions = new ArrayList<>( methodSelector.getConditions() );
                conditions.sort(Comparator.comparingInt(ConditionSelector::getStartLine)
                        .thenComparingInt(ConditionSelector::getEndLine).reversed());
            }
        }

        LinkedList<ConditionSelector> conditionStack = new LinkedList<>();

        for (CallGraph.EntranceView entranceView : openerEntranceView.getEntrances()) {
            MethodCall methodCall = entranceView.getMethodCall().get();

            closeCondition(entranceView, conditionStack, printWriter);

            conditions.stream()
                    .flatMap( condition -> {
                        if( condition.isCompound() ) {
                            return condition.getComponents().stream();
                        } else {
                            return Stream.of(condition);
                        }
                    } )
                    .filter( condition -> condition.isWithin(methodCall)
                            && !conditionStack.contains(condition) )
                    .collect(Collectors.toList()).stream()
                    .forEachOrdered( condition -> {
                        conditionStack.push(condition);
                        printWriter.println( getConditionOpenTag(condition) + " " + getConditionTitle(condition) );
                    } );


            String sender = getCollaboratorName(openerEntranceView);
            String receiver;
            if (entranceView.isSuppressed()) {
                receiver = sender;
                printWriter.println(getSuppressedCallText(sender, entranceView));
            } else {
                receiver = getCollaboratorName(entranceView);
                boolean isConstructorCall = false;
                if (methodCall.getName().equals("<init>")) {
                    if (!(inConstructor && sender.equals(receiver))) {
                        isConstructorCall = true;
                    }
                }
                printWriter.println(getCallText(sender, receiver, entranceView, isConstructorCall));
            }

            if( canRenderActivation(entranceView) ) {
                printWriter.println("activate " + getMultiLineText(receiver, true));
            }

            if (!entranceView.isSuppressed()) {
                if (methodCall.getName().equals("<init>")) {
                    renderMethodCall(printWriter, entranceView, true);
                } else {
                    renderMethodCall(printWriter, entranceView);
                }
            }


            if (!methodCall.getMethodInfo().getReturnTypeName().equals("void") && methodCall.getReturnValue().isPresent()) {
                printWriter.println(getReturnText(receiver, sender, methodCall.getReturnValue().get()));
            }

            if( canRenderActivation(entranceView) ) {
                printWriter.println("deactivate " + getMultiLineText(receiver, true));
            }
        }

        for( ExceptionCase exception : openerEntranceView.getThrewExceptions() ) {
            String sender = getCollaboratorName(openerEntranceView);
            CallGraph.EntranceView catcher = openerEntranceView;
            while ( !catcher.canCatch(exception) ) {
                catcher = catcher.getOpener().get();
            }

            String receiver = getCollaboratorName(catcher);

            String text = getMultiLineText(sender, true) + " --> "
                    + getMultiLineText(receiver, true) + " : "
                    + getMultiLineText( exception.getName(), false );
            printWriter.println(text);
        }

        if( !openerEntranceView.isRoot() ) {
            closeCondition(openerEntranceView, conditionStack, printWriter);
        }
    }

    private boolean canRenderActivation(CallGraph.EntranceView entranceView)
    {
        MethodCall methodCall = entranceView.getMethodCall().get();

        if( !methodCall.getName().equals("<init>") ) {
            return true;
        }

        if( entranceView.isSuppressed() ) {
            return true;
        }

        return !entranceView.getEntrances().isEmpty();
    }

    private void closeCondition(CallGraph.EntranceView entranceView, LinkedList<ConditionSelector> conditionStack,
                                PrintWriter printWriter)
    {
        MethodCall openerMethodCall = entranceView.getMethodCall().get();
        while( !conditionStack.isEmpty() && !conditionStack.peek().isWithin(openerMethodCall) ) {
            conditionStack.pop();
            printWriter.println("end");
        }
    }

    private String getConditionOpenTag(ConditionSelector condition)
    {
        switch ( condition.getConditionType() ) {
            case LOOP: return "loop";
            case ALT: return "alt";
            case TRY: return "group try";
            case CATCH: return "group catch";
            case FINAL: return "group final";
        }

        throw new LogicException();
    }

    private String getConditionTitle(ConditionSelector condition)
    {
        String title = condition.getTitle();
        if( title.isEmpty() ) {
            return title;
        }

        boolean isGroup = false;
        switch ( condition.getConditionType() ) {
            case TRY:
            case CATCH:
            case FINAL:
                isGroup = true;
                break;
        }

        title = getMultiLineText(title, false);
        if(isGroup) {
            title = "[" + title + "]";
        }

        return title;
    }

    private String getSuppressedCallText(String sender, CallGraph.EntranceView entranceView)
    {
        MethodCall methodCall = entranceView.getMethodCall().get();

        return getMultiLineText(sender, true) + " " + getArrow(entranceView) + " "
                + getMultiLineText(sender, true) + " : "
                + getMultiLineText( getCollaboratorName(entranceView) + " " + getMethodText(methodCall), false );
    }

    private String getReturnText(String sender, String receiver, String returnValue)
    {
        return getMultiLineText(sender, true) + " --> "
                + getMultiLineText(receiver, true) + " : "
                + getMultiLineText(returnValue, false);
    }

    private String getCallText(String sender, String receiver, CallGraph.EntranceView entranceView, boolean isConstructorCall)
    {
        MethodCall methodCall = entranceView.getMethodCall().get();
        String constructorText = "";
        if(isConstructorCall) {
            constructorText = "** ";
        }

        return getMultiLineText(sender, true) + " " + getArrow(entranceView) + " "
                + getMultiLineText(receiver, true) + " " + constructorText + ": "
                + getMultiLineText( getMethodText(methodCall), false );
    }

    private String getMethodText(MethodCall methodCall)
    {
        String text = methodCall.getName()
                + "(" + String.join( ", ", methodCall.getArgumentValues().values() ) + ")";
        StepInfo stepInfo = methodCall.getStepInfo().orElse(null);
        if(stepInfo != null) {
//            text += "  " + stepInfo.getLine();
        }

        return text;
    }

    private String getArrow(CallGraph.EntranceView entranceView)
    {
        String color = "[#black]";
        if( entranceView.isTrimmed() ) {
            color = "[#red]";
        }

        String ending = ">";
        if( !entranceView.isLinked() ) {
            ending = ">o";
        }

        return "-" + color + ending;
    }

    private String getMultiLineText(String text, boolean isTypeName)
    {
        return getMultiLineText(text, isTypeName, 40);
    }

    private String getMultiLineText(String text, boolean isTypeName, int length)
    {
        text = text
                .replace("~", "\\~")
                .replace("*", "~*")
                .replace("/", "~/")
                .replace("\"", "~\"")
                .replace("-", "~-")
                .replace("_", "~_")
                .replace("<", "~<")
                .replace("#", "~#")
                .replace(".", "~.")
                .replace("[", "~[")
                .replace("]", "~]")
        ;

        List<String> parts = new ArrayList<>();
        String remaining = text;
        String substring;
        int endIndex;
        while (!remaining.equals("")) {
            endIndex = Math.min(remaining.length(), length);
            substring = remaining.substring(0, endIndex);
            parts.add(substring);
            remaining = remaining.substring(endIndex);
        }

        String delimiter = "\\n";
        if(!isTypeName) {
            delimiter = "\"\\n\"";
        }
        text = "\"" + String.join(delimiter, parts) + "\"";

        text = text
                .replace("\n", delimiter)
                .replace("\r", delimiter);

        return text;
    }

    private String getCollaboratorName(CallGraph.EntranceView entranceView)
    {
        if( entranceView.isRoot() ) {
            return "root";
        }

        MethodCall methodCall = entranceView.getMethodCall().get();

        String collaborator;
        if( methodCall.getMethodInfo().isStatic() ) {
            collaborator = methodCall.getTypeName();
        } else {
            ObjectInfo objectInfo = entranceView.getEntranceTypeInfo().get().getObjectInfo().get();
            collaborator = objectInfo.getTypeInfo().getTypeName() + "_" + objectInfo.getId();
        }

        return collaborator;
    }

    public void renderThreadGraph(Map<Long, ThreadInfo> threadInfoMap)
    {
        try ( PrintWriter printWriter = new PrintWriter( new FileWriter("threads.txt") ) ) {
            renderThreadGraph(threadInfoMap, printWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void renderThreadGraph(Map<Long, ThreadInfo> threadInfoMap, PrintWriter printWriter)
    {
        Map<Long, ThreadInfo> workingThreadInfoMap = new HashMap<>(threadInfoMap);

        printWriter.println("@startmindmap");

        ThreadInfo startingThreadInfo = workingThreadInfoMap.remove(1L);
        renderBranches(workingThreadInfoMap, startingThreadInfo, printWriter, "");

        printWriter.println("@endmindmap");
    }

    private void renderBranches(Map<Long, ThreadInfo> workingThreadInfoMap, ThreadInfo startingThreadInfo,
                                PrintWriter printWriter, String levelMark)
    {
        levelMark += "*";
        printWriter.println( getThreadTextInfo(levelMark, startingThreadInfo) );

        List<ThreadInfo> threadInfos = workingThreadInfoMap.values().stream()
                .filter( threadInfo -> threadInfo.getCreatedBy().orElse(null) == startingThreadInfo )
                .collect(Collectors.toList());
        for (ThreadInfo threadInfo : threadInfos) {
            workingThreadInfoMap.remove( threadInfo.getObjectId() );
        }

        for (ThreadInfo threadInfo : threadInfos) {
            renderBranches(workingThreadInfoMap, threadInfo, printWriter, levelMark);
        }
    }

    private String getThreadTextInfo(String levelMark, ThreadInfo threadInfo)
    {
        String textInfo = "objectId: " + threadInfo.getObjectId();
        ThreadInfo ranBy = threadInfo.getRanBy().orElse(null);
        if(ranBy != null) {
            textInfo += " ranBy: " + threadInfo.getRanBy().get().getObjectId();
        }
        textInfo += " createdByOrder: " + threadInfo.getCreatingOrder();
        textInfo += " ranByOrder: " + threadInfo.getRunningOrder();

        return levelMark + " " + getMultiLineText(textInfo, false);
    }
}
