package enegade.uml.callgraph;

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
import enegade.uml.Config;
import enegade.uml.TypeCollector;
import enegade.uml.selector.ConditionSelector;
import enegade.uml.selector.ConditionType;
import enegade.uml.type.ExceptionCase;
import enegade.uml.type.LoopCondition;
import enegade.uml.type.MethodCall;

import java.util.*;

/**
 * @author enegade
 */
public class CallGraphBuilder
{
    private List<StartingFilter> startingFilters = new ArrayList<>();

    private List<TrimmingFilter> trimmingFilters = new ArrayList<>();

    private List<SkippingFilter> skippingFilters = new ArrayList<>();

    private List<SuppressingFilter> suppressingFilters = new ArrayList<>();

    private List<HidingFilter> hidingFilters = new ArrayList<>();

    private DepthFilter depthFilter;

    private CallGraph builtCallGraph;

    private Map<String, Map<String, List<ConditionSelector>>> conditionSelectors = new HashMap<>();

    public CallGraphBuilder(Config config)
    {
        String depth = config.getProperty("filter_depth");
        this.depthFilter = new DepthFilter( Integer.parseInt(depth) );

        addTrimmingFilter(this.depthFilter);

        CommonHidingFilter commonHidingFilter = new CommonHidingFilter();
        addHidingFilter(commonHidingFilter);

        CommonSuppressingFilter commonSuppressingFilter = new CommonSuppressingFilter();
        addSuppressingFilter(commonSuppressingFilter);
    }

    public void addStartingFilter(StartingFilter startingFilter)
    {
        this.startingFilters.add(startingFilter);
    }

    public void addTrimmingFilter(TrimmingFilter trimmingFilter)
    {
        this.trimmingFilters.add(trimmingFilter);
    }

    public void addSkippingFilter(SkippingFilter skippingFilter)
    {
        this.skippingFilters.add(skippingFilter);
    }

    public void addSuppressingFilter(SuppressingFilter suppressingFilter)
    {
        this.suppressingFilters.add(suppressingFilter);
    }

    public void addHidingFilter(HidingFilter hidingFilter)
    {
        this.hidingFilters.add(hidingFilter);
    }

    public CallGraph buildCallGraph(CallGraph fromCallGraph)
    {
        TypeCollector typeCollector = new TypeCollector();
        CallGraph builtCallGraph = new CallGraph( typeCollector, fromCallGraph.getThreadObjectId() );

        this.depthFilter.setCallGraph(builtCallGraph);

        this.builtCallGraph = builtCallGraph;

        transferEntrances( fromCallGraph.getEntranceView(), false, true );

        return this.builtCallGraph;
    }

    private void addMethodCall(CallGraph.EntranceView entranceView, boolean linked, boolean suppressed)
    {
        this.builtCallGraph.openNewEntrance(entranceView, linked, suppressed);

        for( ExceptionCase exception : entranceView.getThrewExceptions() ) {
            this.builtCallGraph.addThrewException(exception);
        }

        for( ExceptionCase exception : entranceView.getCaughtExceptions() ) {
            this.builtCallGraph.addCaughtException(exception);
        }
    }

    private void transferEntrances(CallGraph.EntranceView callerEntranceView, boolean graphStarted, boolean linked)
    {
        boolean isRoot = callerEntranceView.isRoot();
        if(isRoot) {
            for (CallGraph.EntranceView entranceView : callerEntranceView.getEntrances()) {
                transferEntrances(entranceView, graphStarted, linked);
            }
            return;
        }

        boolean hidden = false;
        for(HidingFilter hidingFilter : this.hidingFilters) {
            if( hidingFilter.isHidden(callerEntranceView) ) {
                hidden = true;
                break;
            }
        }
        if(hidden) {
            if(graphStarted) {
                addThrewExceptions(callerEntranceView);
            }

            return;
        }

        if(!graphStarted) {
            if( callerEntranceView.getEntrances().isEmpty() ) {
                graphStarted = false;
            } else {
                for (StartingFilter startingFilter : this.startingFilters) {
                    if (startingFilter.isStarting(callerEntranceView)) {
                        graphStarted = true;
                        break;
                    }
                }
            }
        }

        boolean skipped = false;
        for(SkippingFilter skippingFilter : this.skippingFilters) {
            if( skippingFilter.isSkipped(callerEntranceView) ) {
                skipped = true;
                break;
            }
        }

        boolean suppressed = false;
        if( callerEntranceView.isSuppressed() ) {
            suppressed = true;
        } else {
            for(SuppressingFilter suppressingFilter : this.suppressingFilters) {
                if( suppressingFilter.isSuppressed(callerEntranceView) ) {
                    suppressed = true;
                    break;
                }
            }
        }

        boolean trimmed = false;
        if( callerEntranceView.isTrimmed() ) {
            trimmed = true;
        } else {
            for(TrimmingFilter trimmingFilter : this.trimmingFilters) {
                if( callerEntranceView.getEntrances().isEmpty() ) {
                    trimmed = false;
                } else {
                    if (trimmingFilter.isTrimming(callerEntranceView)) {
                        trimmed = true;
                        break;
                    }
                }
            }
        }

        if(graphStarted) {
            if (!skipped) {
                addMethodCall(callerEntranceView, linked, suppressed);

                if (trimmed) {
                    this.builtCallGraph.setTrimmed(true);
                }
            } else {
                addThrewExceptions(callerEntranceView);
            }
        }

        if(!trimmed && !suppressed) {
            for (CallGraph.EntranceView entranceView : callerEntranceView.getEntrances()) {
                transferEntrances(entranceView, graphStarted, !skipped);
            }
        }

        if(graphStarted && !skipped) {
            this.builtCallGraph.closeEntrance();
        }
    }

    private void addThrewExceptions(CallGraph.EntranceView callerEntranceView)
    {
        List<ExceptionCase> caughtExceptions = callerEntranceView.getCaughtExceptions();

        for( ExceptionCase exception : callerEntranceView.getThrewExceptions() ) {
            if( !caughtExceptions.contains(exception) ) {
                this.builtCallGraph.addThrewException(exception);
            }
        }
        for( ExceptionCase exception : callerEntranceView.getPassedExceptions() ) {
            if( !caughtExceptions.contains(exception) ) {
                this.builtCallGraph.addThrewException(exception);
            }
        }
    }

    public Optional<List<ConditionSelector>> getConditionSelectors(String typeName, String methodSelectorName)
    {
        Map<String, List<ConditionSelector>> methodMap = this.conditionSelectors.get(typeName);
        if(methodMap == null) {
            return Optional.empty();
        }

        return Optional.ofNullable( methodMap.get(methodSelectorName) );
    }

    public void traverseCallGraph(CallGraph callGraph)
    {
        this.conditionSelectors = new HashMap<>();

        this.traverseEntrances( callGraph.getEntranceView() );
    }

    private void traverseEntrances(CallGraph.EntranceView callerEntranceView)
    {
        boolean isRoot = callerEntranceView.isRoot();
        if(isRoot) {
            for (CallGraph.EntranceView entranceView : callerEntranceView.getEntrances()) {
                traverseEntrances(entranceView);
            }
            return;
        }

        MethodCall methodCall = callerEntranceView.getMethodCall().get();
        String typeName = methodCall.getTypeName();
        String selectorName = methodCall.getSelectorName();
        this.conditionSelectors.putIfAbsent( typeName, new HashMap<>() );
        Map<String, List<ConditionSelector>> methodMap = this.conditionSelectors.get(typeName);
        methodMap.putIfAbsent( selectorName, new ArrayList<>() );
        List<ConditionSelector> selectors = methodMap.get(selectorName);
        for( LoopCondition loopCondition : callerEntranceView.getLoopConditions() ) {
            ConditionSelector conditionSelector = new ConditionSelector(ConditionType.LOOP);
            conditionSelector.setStartLine( loopCondition.getStaringMethodCall().getCallLineNumber() );
            conditionSelector.setEndLine( loopCondition.getEndingMethodCall().getCallLineNumber() );
            selectors.add(conditionSelector);
        }

        for (CallGraph.EntranceView entranceView : callerEntranceView.getEntrances()) {
            traverseEntrances(entranceView);
        }
    }
}
