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
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import enegade.uml.debugger.ThreadDebugger;
import enegade.uml.debugger.UmlDebugger;
import enegade.uml.io.StreamRedirectingThread;
import enegade.uml.selector.TypeSelector;
import enegade.uml.callgraph.*;
import enegade.uml.callgraph.json.FilterSelectorChangeMapper;
import enegade.uml.type.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author enegade
 */
public class UML
{
    private static final String CALL_GRAPH_DATA_FILENAME = "call_graph.data";

    private List<StreamRedirectingThread> streamRedirectingThreads = new ArrayList<>();

    private Config config;

    public static void main(String[] argv)
    {
        new UML();
    }

    public UML()
    {
        this.config = Config.fromFile();

        List<CallGraph> callGraphs = new ArrayList<>();

        Path path = Paths.get(UML.CALL_GRAPH_DATA_FILENAME);
        if( !Files.exists(path) ) {
            VirtualMachine virtualMachine = obtainVirtualMachine();
            EventLoop eventLoop = new EventLoop(virtualMachine);
            UmlDebugger umlDebugger = new UmlDebugger(eventLoop, this.config);

            umlDebugger.debug();

            redirectStreams(virtualMachine);
            try {
                eventLoop.run();
            } finally {
                closeRedirectedStreams();
            }

            Map<Long, ThreadInfo> threadInfoMap = umlDebugger.getThreadInfoMap();
            PlantUml plantUml = new PlantUml();
            plantUml.renderThreadGraph(threadInfoMap);


            callGraphs = umlDebugger.getThreadDebuggers().stream()
                    .filter( threadDebugger -> threadDebugger.getCallGraph() != null )
                    .map(ThreadDebugger::getCallGraph)
                    .collect(Collectors.toList());

            for( CallGraph callGraph : callGraphs ) {
                while (!callGraph.isRoot()) {
                    callGraph.closeEntrance();
                }
            }


            if( callGraphs.size() != 0 ) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(callGraphs);
                    String callGraphData = Base64.getEncoder().encodeToString(baos.toByteArray());

                    try (PrintWriter printWriter = new PrintWriter(new FileWriter(UML.CALL_GRAPH_DATA_FILENAME))) {
                        printWriter.print(callGraphData);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if( Files.exists(path) ) {
            String callGraphData;
            try (Stream<String> lines = Files.lines(path)) {
                callGraphData = lines.collect(Collectors.joining("\n"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            byte[] data = Base64.getDecoder().decode(callGraphData);
            try (ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(data))) {
                @SuppressWarnings("unchecked")
                List<CallGraph> readCallGraphs = (List<CallGraph>) ois.readObject();
                callGraphs = readCallGraphs;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        for(CallGraph callGraph : callGraphs) {
            CallGraphBuilder callGraphBuilder = new CallGraphBuilder(this.config);
            List<ConfiguredFilter> configuredFilters = new ArrayList<>();

            SelectorChangeMapper changeMapper = new FilterSelectorChangeMapper( callGraph.getThreadObjectId()
                    + "_types.json" );
            StartingFilterImpl startingFilter = new StartingFilterImpl(changeMapper);
            callGraphBuilder.addStartingFilter(startingFilter);
            configuredFilters.add(startingFilter);

            TrimmingFilterImpl trimmingFilter = new TrimmingFilterImpl(changeMapper);
            callGraphBuilder.addTrimmingFilter(trimmingFilter);
            configuredFilters.add(trimmingFilter);

            SuppressingFilterImpl suppressingFilter = new SuppressingFilterImpl(changeMapper);
            callGraphBuilder.addSuppressingFilter(suppressingFilter);
            configuredFilters.add(suppressingFilter);

            HidingFilterImpl hidingFilter = new HidingFilterImpl(changeMapper);
            callGraphBuilder.addHidingFilter(hidingFilter);
            configuredFilters.add(hidingFilter);

            HidingConditionFilter hidingConditionFilter = new HidingConditionFilter(changeMapper);
            callGraphBuilder.addHidingFilter(hidingConditionFilter);
            configuredFilters.add(hidingConditionFilter);

            SkippingFilterImpl skippingFilter = new SkippingFilterImpl(changeMapper);
            callGraphBuilder.addSkippingFilter(skippingFilter);
            configuredFilters.add(skippingFilter);


            List<TypeSelector> typeSelectors = createTypeSelectors(callGraph);
            changeMapper.requestChanges(typeSelectors);
            typeSelectors = changeMapper.mapChanges();

            for (ConfiguredFilter configuredFilter : configuredFilters) {
                configuredFilter.setSelectors(typeSelectors);
            }

            FirstEntranceStartingFilter firstEntranceStartingFilter;
            if( !startingFilter.isAnyMethodSelected() ) {
                firstEntranceStartingFilter =
                        new FirstEntranceStartingFilter(callGraph);
                callGraphBuilder.addStartingFilter(firstEntranceStartingFilter);
            }

            CallGraph builtCallGraph = callGraphBuilder.buildCallGraph(callGraph);


            CallGraphBuilder finalCallGraphBuilder = new CallGraphBuilder(this.config);
            firstEntranceStartingFilter =
                    new FirstEntranceStartingFilter(builtCallGraph);
            finalCallGraphBuilder.addStartingFilter(firstEntranceStartingFilter);

            List<ObjectInfo> orphans = builtCallGraph.getTypeCollector().getObjectInfos().stream()
                    .filter(ObjectInfo::isOrphan)
                    .collect(Collectors.toList());
            OrphanObjectSuppressingFilter orphanObjectSuppressingFilter =
                    new OrphanObjectSuppressingFilter(orphans);
            finalCallGraphBuilder.addSuppressingFilter(orphanObjectSuppressingFilter);


            CallGraph finalCallGraph = finalCallGraphBuilder.buildCallGraph(builtCallGraph);


            PlantUml plantUml = new PlantUml();
            plantUml.setConditionalTypeSelectors(typeSelectors);
            plantUml.setHiddenTypes(typeSelectors);
            plantUml.renderSequenceDiagram(finalCallGraph);

            List<TypeInfo> typeInfos = finalCallGraph.getTypeCollector().getTypeInfos();
            for(TypeInfo typeInfo : typeInfos) {
                if ( typeInfo.getDependencies().isEmpty() && typeInfo.getDependentByTypes().isEmpty() && !typeInfo.hasCalledMethods() ) {
                    typeInfo.setDependent(false);
                }
            }
            finalCallGraph.getTypeCollector().getObjectTypes().forEach( typeInfo -> typeInfo.setDependent(true) );

            plantUml.renderClassDiagram(typeInfos);
        }


        System.out.println("done!");
    }

    private List<TypeSelector> createTypeSelectors(CallGraph callGraph)
    {
        CallGraphBuilder callGraphBuilder = new CallGraphBuilder(this.config);
        callGraphBuilder.traverseCallGraph(callGraph);

        List<TypeSelector> typeSelectors = new ArrayList<>();
        for ( TypeInfo typeInfo : callGraph.getTypeCollector().getTypeInfos() ) {
            TypeSelector typeSelector = TypeSelector.from(typeInfo, methodSelector -> {
                callGraphBuilder
                        .getConditionSelectors( typeInfo.getTypeName(), methodSelector.getName() )
                        .ifPresent(methodSelector::setConditions);
            });
            typeSelectors.add(typeSelector);
        }

        return typeSelectors;
    }

    private void redirectStreams(VirtualMachine virtualMachine)
    {
        Process process = virtualMachine.process();

        StreamRedirectingThread errThread = new StreamRedirectingThread("error reader",
                process.getErrorStream(), System.err);
        this.streamRedirectingThreads.add(errThread);

        StreamRedirectingThread outThread = new StreamRedirectingThread("output reader",
                process.getInputStream(), System.out);
        this.streamRedirectingThreads.add(outThread);

        StreamRedirectingThread inputThread = new StreamRedirectingThread("input reader",
                System.in, process.getOutputStream());
        this.streamRedirectingThreads.add(inputThread);

        for(StreamRedirectingThread thread : this.streamRedirectingThreads) {
            thread.start();
        }
    }

    private void closeRedirectedStreams()
    {
        for(StreamRedirectingThread thread : this.streamRedirectingThreads) {
            thread.stopListening();
        }
    }

    private VirtualMachine obtainVirtualMachine()
    {
        String connectorName = "com.sun.jdi.CommandLineLaunch";

        Connector selectedConnector = null;

        for (Connector connector :
                Bootstrap.virtualMachineManager().allConnectors()) {
            if (connector.name().equals(connectorName)) {
                selectedConnector = connector;
            }
        }


        Map<String, Connector.Argument> arguments = selectedConnector.defaultArguments();

        Connector.Argument argument = arguments.get("options");
        String optionsArgument = this.config.getProperty("argument.options");
        argument.setValue(optionsArgument);

        argument = arguments.get("main");
        String mainArgument = this.config.getProperty("argument.main");
        argument.setValue(mainArgument);

        LaunchingConnector launcher = (LaunchingConnector) selectedConnector;

        VirtualMachine virtualMachine;
        try {
            virtualMachine = launcher.launch(arguments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        virtualMachine.setDebugTraceMode(VirtualMachine.TRACE_NONE);

        return virtualMachine;
    }
}
