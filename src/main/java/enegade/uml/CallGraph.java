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

import com.sun.jdi.event.LocatableEvent;
import enegade.uml.excepion.LogicException;
import enegade.uml.type.*;
import enegade.uml.type.ExceptionCase;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author enegade
 */
public class CallGraph implements Serializable
{
    private final static long serialVersionUID = 1;

    private long threadObjectId;

    private Entrance entrance;

    private TypeCollector typeCollector;

    public CallGraph(TypeCollector typeCollector, long threadObjectId)
    {
        this.threadObjectId = threadObjectId;
        this.typeCollector = typeCollector;
        this.entrance = new Entrance();
    }

    public long getThreadObjectId()
    {
        return threadObjectId;
    }

    public TypeCollector getTypeCollector()
    {
        return this.typeCollector;
    }

    public boolean isRoot()
    {
        return this.entrance.isRoot();
    }

    public void openNewEntrance(MethodCall methodCall)
    {
        openNewEntrance(methodCall, null, true, false);
    }

    public void openNewEntrance(EntranceView entranceView, boolean linked, boolean suppressed)
    {
        openNewEntrance( entranceView.getMethodCall().get(), entranceView.getEntranceTypeInfo().get(),
                linked, suppressed ) ;
    }

    private void openNewEntrance(MethodCall methodCall, @Nullable EntranceTypeInfo fromEntranceTypeInfo,
                                boolean linked, boolean suppressed)
    {
        int depth = this.entrance.getDepth() + 1;

        Entrance entrance = new Entrance(methodCall, depth);
        entrance.setLinked(linked);
        entrance.setSuppressed(suppressed);

        this.entrance.addEntrance(entrance);
        entrance.setOpener(this.entrance);

        EntranceTypeInfo entranceTypeInfo;
        if(fromEntranceTypeInfo == null) {
            entranceTypeInfo = new EntranceTypeInfo(this, methodCall);
        } else {
            entranceTypeInfo = new EntranceTypeInfo(this, fromEntranceTypeInfo);
        }
        entrance.setEntranceTypeInfo(entranceTypeInfo);

        if( !this.entrance.isRoot() ) {
            this.entrance.getEntranceTypeInfo()
                    .flatMap(EntranceTypeInfo::getObjectInfo)
                    .ifPresent( currentObjectInfo -> {
                        entranceTypeInfo.getObjectInfo().ifPresent(objectInfo -> {
                            if ( currentObjectInfo != objectInfo ) {
                                if ( methodCall.getName().equals("<init>") ) {
                                    objectInfo.setCreatedBy( getEntranceView() );

                                    addLinkedDependency( getEntranceView(), linked, suppressed, objectInfo.getTypeInfo() );
                                } else {
                                    objectInfo.addCalledByMethod( getEntranceView() );
                                }

                                currentObjectInfo.addCalledMethod( new EntranceView(entrance) );
                            }
                        } );
                    } );
        }

        MethodInfo methodInfo = methodCall.getMethodInfo();

        if( methodInfo.isStatic() ) {
            entranceTypeInfo.getTypeInfo().addMethod(methodInfo);
        } else {
            TypeInfo objectTypeInfo = entranceTypeInfo.getObjectInfo().get().getTypeInfo();
            if(!suppressed) {
                this.typeCollector.addObjectType(objectTypeInfo);
            }
            this.typeCollector.addMethodHierarchy(methodInfo, objectTypeInfo);
        }


        TypeInfo methodType = entranceTypeInfo.getTypeInfo();
        for( TypeInfo argumentType : entranceTypeInfo.getArgumentTypes() ) {

            if(!suppressed) {
                methodType.addDependency(argumentType);
            }

            addLinkedDependency( getEntranceView(), linked, suppressed, argumentType );
        }

        TypeInfo returnedTypeInfo = entranceTypeInfo.getReturnTypeInfo().orElse(null);
        if(returnedTypeInfo != null) {

            if(!suppressed) {
                methodType.addDependency(returnedTypeInfo);
            }

            addLinkedDependency( getEntranceView(), linked, suppressed, returnedTypeInfo );
        }

        addLinkedDependency( getEntranceView(), linked, suppressed, entranceTypeInfo.getTypeInfo() );

        this.entrance = entrance;
    }

    public static void addLinkedDependency(EntranceView toTypeInEntrance, boolean linked, boolean suppressed, TypeInfo candidate)
    {
        if( toTypeInEntrance.isRoot() || !linked ) {
            return;
        }

        toTypeInEntrance.getEntranceTypeInfo().get().getTypeInfo().addDependency(candidate);
    }

    public void addStepInfo(StepInfo stepInfo)
    {
        this.entrance.addStepInfo(stepInfo);
    }

    public void addLoopCondition(LoopCondition loopCondition)
    {
        this.entrance.addLoopCondition(loopCondition);
    }

    public void addThrewException(ExceptionCase threwException)
    {
        this.entrance.addThrewException(threwException);
    }

    public void addCaughtException(ExceptionCase caughtException)
    {
        this.entrance.addCaughtException(caughtException);
    }

    public void setTrimmed(boolean trimmed)
    {
        this.entrance.setTrimmed(trimmed);
    }

    public void closeEntrance()
    {
        if( this.isRoot() ) {
            throw new LogicException();
        }

        Entrance opener = this.entrance.getOpener().get();

        for( ExceptionCase exception : this.entrance.getThrewExceptions() ) {
            if( !this.entrance.containsCaughtException(exception) ) {
                opener.addPassedException(exception);
            }
        }

        for( ExceptionCase exception : this.entrance.getPassedExceptions() ) {
            if( !this.entrance.containsCaughtException(exception) ) {
                opener.addPassedException(exception);
            }
        }

        this.entrance = opener;
    }

    public boolean isFrameSame(LocatableEvent event)
    {
        if( this.isRoot() ) {
            return false;
        }

        return MethodCallHelper.isFrameSame( this.entrance.getMethodCall().get(), event );
    }

    private static class Entrance implements Serializable
    {
        private final static long serialVersionUID = 1;

        private int depth;

        private boolean linked = true;

        private boolean suppressed = false;

        private boolean trimmed = false;

        private List<StepInfo> stepInfoList = new ArrayList<>();

        private List<Entrance> entrances = new ArrayList<>();

        private Entrance opener;

        private MethodCall methodCall;

        private EntranceTypeInfo entranceTypeInfo;

        private List<LoopCondition> loopConditions = new ArrayList<>();

        private List<ExceptionCase> passedExceptions = new ArrayList<>();

        private List<ExceptionCase> threwExceptions = new ArrayList<>();

        private List<ExceptionCase> caughtExceptions = new ArrayList<>();

        public Entrance()
        {
            this.depth = 0;
        }

        public Entrance(MethodCall methodCall, int depth)
        {
            this.methodCall = methodCall;
            this.depth = depth;
        }

        public void addLoopCondition(LoopCondition loopCondition)
        {
            this.loopConditions.add(loopCondition);
        }

        public List<LoopCondition> getLoopConditions()
        {
            return List.copyOf(this.loopConditions);
        }

        public List<ExceptionCase> getPassedExceptions()
        {
            return List.copyOf(this.passedExceptions);
        }

        public void addPassedException(ExceptionCase passedException)
        {
            this.passedExceptions.add(passedException);
        }

        public List<ExceptionCase> getThrewExceptions()
        {
            return List.copyOf(this.threwExceptions);
        }

        public void addThrewException(ExceptionCase threwException)
        {
            this.threwExceptions.add(threwException);
        }

        public boolean containsCaughtException(ExceptionCase exception)
        {
            return this.caughtExceptions.contains(exception);
        }

        public List<ExceptionCase> getCaughtExceptions()
        {
            return List.copyOf(this.caughtExceptions);
        }

        public void addCaughtException(ExceptionCase caughtException)
        {
            this.caughtExceptions.add(caughtException);
        }

        public boolean canCatch(ExceptionCase exception)
        {
            return this.caughtExceptions.contains(exception);
        }

        public Optional<EntranceTypeInfo> getEntranceTypeInfo()
        {
            return Optional.ofNullable(this.entranceTypeInfo);
        }

        public Entrance setEntranceTypeInfo(EntranceTypeInfo entranceTypeInfo)
        {
            this.entranceTypeInfo = entranceTypeInfo;
            return this;
        }

        public void setTrimmed(boolean trimmed)
        {
            this.trimmed = trimmed;
        }

        public boolean isTrimmed()
        {
            return this.trimmed;
        }

        public void addStepInfo(StepInfo stepInfo)
        {
            this.stepInfoList.add(stepInfo);
        }

        public List<StepInfo> getStepInfoList()
        {
            return List.copyOf(this.stepInfoList);
        }

        public boolean isRoot()
        {
            return this.opener == null;
        }

        public boolean isLinked()
        {
            return this.linked;
        }

        public void setLinked(boolean linked)
        {
            this.linked = linked;
        }

        public void setSuppressed(boolean suppressed)
        {
            this.suppressed = suppressed;
        }

        public boolean isSuppressed()
        {
            return this.suppressed;
        }

        public int getDepth()
        {
            return this.depth;
        }

        public void addEntrance(Entrance entrance)
        {
            this.entrances.add(entrance);
        }

        public List<Entrance> getEntrances()
        {
            return this.entrances;
        }

        public void setOpener(Entrance entrance)
        {
            this.opener = entrance;
        }

        public Optional<Entrance> getOpener()
        {
            return Optional.ofNullable(this.opener);
        }

        public Optional<MethodCall> getMethodCall()
        {
            return Optional.ofNullable(this.methodCall);
        }
    }

    public static class EntranceView implements Serializable
    {
        private final static long serialVersionUID = 1;

        private Entrance entrance;

        public EntranceView(Entrance entrance)
        {
            this.entrance = entrance;
        }

        public Optional<MethodCall> getMethodCall()
        {
            return this.entrance.getMethodCall();
        }

        public Optional<EntranceTypeInfo> getEntranceTypeInfo()
        {
            return this.entrance.getEntranceTypeInfo();
        }

        public List<StepInfo> getStepInfoList()
        {
            return this.entrance.getStepInfoList();
        }

        public List<LoopCondition> getLoopConditions()
        {
            return this.entrance.getLoopConditions();
        }

        public List<ExceptionCase> getPassedExceptions()
        {
            return this.entrance.getPassedExceptions();
        }

        public List<ExceptionCase> getThrewExceptions()
        {
            return this.entrance.getThrewExceptions();
        }

        public List<ExceptionCase> getCaughtExceptions()
        {
            return this.entrance.getCaughtExceptions();
        }

        public boolean canCatch(ExceptionCase exception)
        {
            return this.entrance.canCatch(exception);
        }

        public boolean isRoot()
        {
            return this.entrance.isRoot();
        }

        public int getDepth()
        {
            return this.entrance.getDepth();
        }

        public boolean isLinked()
        {
            return this.entrance.isLinked();
        }

        public boolean isSuppressed()
        {
            return this.entrance.isSuppressed();
        }

        public boolean isTrimmed()
        {
            return this.entrance.isTrimmed();
        }

        public Optional<EntranceView> getOpener()
        {
            if( this.isRoot() ) {
                return Optional.empty();
            }

            EntranceView entranceView = new EntranceView( this.entrance.getOpener().get() );
            return Optional.of(entranceView);
        }

        public List<EntranceView> getEntrances()
        {
            return this.entrance.getEntrances().stream()
                    .map(EntranceView::new).collect(Collectors.toList());
        }
    }

    public EntranceView getEntranceView()
    {
        return new EntranceView(this.entrance);
    }
}
