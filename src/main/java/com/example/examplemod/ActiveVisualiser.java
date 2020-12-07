package com.example.examplemod;

import com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects.AbstractActiveCircuitObject;
import com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects.ActiveEdge;
import com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects.ActiveRedstoneGraph;
import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;
import com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel.Formula;
import com.example.examplemod.util.utility;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ActiveVisualiser {

    private ActiveRedstoneGraph activeRedstoneGraph;
    private Graph graph;
    private boolean labelsAreFormulas = false;

    public ActiveVisualiser(ActiveRedstoneGraph activeRedstoneGraph) {
        this.activeRedstoneGraph = activeRedstoneGraph;
        this.graph = buildGraph();
    }

    private static void setupGraph(ActiveRedstoneGraph activeRedstoneGraph, Graph graph) {
        for (AbstractActiveCircuitObject abstractActiveCircuitObject : activeRedstoneGraph.getCircuitObjects()) {
            buildAndAddActiveCircuitObject(abstractActiveCircuitObject, graph);
        }
        for (ActiveEdge activeEdge : activeRedstoneGraph.getEdges()) {
            buildAndAddActiveEdge(activeEdge, graph);
        }
    }

    private static void buildAndAddActiveCircuitObject(AbstractActiveCircuitObject abstractActiveCircuitObject,
                                                       Graph graph) {
        Node node = graph.addNode(abstractActiveCircuitObject.toString());
        CircuitObject circuitObject = abstractActiveCircuitObject.getCircuitObject();
        //node.setAttribute("ui.label", circuitObject.circuitObjectType.name());
        node.setAttribute(
                "ui.label",
                utility.getStringRepresentationOfInt(abstractActiveCircuitObject.getId()));
        //System.out.println(circuitObject.circuitObjectType.name());
        node.setAttribute("ui.class", circuitObject.circuitObjectType.name());
        node.setAttribute("xyz",
                -circuitObject.blockPos.getX(),
                circuitObject.blockPos.getZ(),
                circuitObject.blockPos.getY()
        );
    }

    private static void buildAndAddActiveEdge(ActiveEdge activeEdge,
                                              Graph graph) {
        Edge edge = graph.addEdge(
                activeEdge.toString(),
                activeEdge.getSource().toString(),
                activeEdge.getDestination().toString(),
                true
        );
    }

    public ActiveRedstoneGraph getActiveRedstoneGraph() {
        return activeRedstoneGraph;
    }

    public synchronized void labelsAsFormulasSwitch() {
        if (labelsAreFormulas==false) {
            activeRedstoneGraph.getCircuitObjects().forEach(
                    (abstractActiveCircuitObject -> {
                        Node node = graph.getNode(abstractActiveCircuitObject.toString());
                        node.setAttribute("ui.label", abstractActiveCircuitObject.deepInputSearch(new ArrayList<>()).simplifyThisBoolisch().toString());
                    })
            );
            labelsAreFormulas = true;
        } else {
            activeRedstoneGraph.getCircuitObjects().forEach(
                    (abstractActiveCircuitObject -> {
                        Node node = graph.getNode(abstractActiveCircuitObject.toString());
                        node.setAttribute("ui.label", utility.getStringRepresentationOfInt(abstractActiveCircuitObject.getId()));
                    })
            );
            labelsAreFormulas = false;
        }
    }

    public synchronized void showLabels() {
        for (AbstractActiveCircuitObject abstractActiveCircuitObject : activeRedstoneGraph.getCircuitObjects()) {
            Node node = graph.getNode(abstractActiveCircuitObject.toString());
            node.setAttribute("ui.style", "text-mode: normal;");
        }
    }

    public synchronized void hideLabels() {
        for (AbstractActiveCircuitObject abstractActiveCircuitObject : activeRedstoneGraph.getCircuitObjects()) {
            Node node = graph.getNode(abstractActiveCircuitObject.toString());
            node.setAttribute("ui.style", "text-mode: hidden;");
        }
    }

    public Graph getGraph() {
        return graph;
    }

    public void redstoneTick() {
        activeRedstoneGraph.redstoneTick();
        updateGraph();
    }

    public synchronized void updateGraph() {
        for (AbstractActiveCircuitObject abstractActiveCircuitObject : activeRedstoneGraph.getCircuitObjects()) {
            Node node = graph.getNode(abstractActiveCircuitObject.toString());

            String typeName = abstractActiveCircuitObject.getCircuitObject().circuitObjectType.name();
            if(abstractActiveCircuitObject.outputIsOn()){
                removeUIClass(node, typeName+"_OFF");
                addUIClass(node, typeName+"_ON");
            } else {
                removeUIClass(node, typeName+"_ON");
                addUIClass(node, typeName+"_OFF");
            }



            for (ActiveEdge activeEdge : abstractActiveCircuitObject.getOutputEdges()) {
                Edge edge = graph.getEdge(activeEdge.toString());
                if (abstractActiveCircuitObject.outputIsOn()) {
                    if (activeEdge.sourceIsPoweringDestination()) {
                        edge.setAttribute("ui.class", "Powered");

                    } else {
                        edge.setAttribute("ui.class", "PoweredButNotReaching");
                    }
                } else {
                    edge.setAttribute("ui.class", "Unpowered");
                }

            }
        }
    }

    private Graph buildGraph() {
        Graph graph = new MultiGraph("Redstone Graph",
                true,
                true,
                activeRedstoneGraph.getCircuitObjects().size(),
                activeRedstoneGraph.getEdges().size()
        );
        setupGraph(activeRedstoneGraph, graph);
        this.graph = graph;
        updateGraph();
        return graph;
    }

    public synchronized void setSelectedNode(String nodeId) {
        graph.nodes().filter(node -> node.toString().equals(nodeId)).forEach(node -> {
            addUIClass(node, "clickedPerm");
        });
    }

    private void addUIClass(Node node, String uIClassName){
        Object currentClasses = node.getAttribute("ui.class");
        if(currentClasses instanceof Object[]){
            Object[] objects = (Object[]) node.getAttribute("ui.class");
            Set<String> uiClasses = new HashSet<>();
            for(Object object : objects){
                uiClasses.add((String)object);
            }
            uiClasses.add(uIClassName);
            node.setAttribute("ui.class", uiClasses.toArray());
        } else {
            Set<String> uiClasses = new HashSet<>();
            uiClasses.add((String) node.getAttribute("ui.class"));
            uiClasses.add(uIClassName);
            node.setAttribute("ui.class", uiClasses.toArray());
        }
        //printUIClass(node);
    }

    private void removeUIClass(Node node, String uIClassName){
        Object currentClasses = node.getAttribute("ui.class");
        if(currentClasses instanceof Object[]){
            Object[] objects = (Object[]) node.getAttribute("ui.class");
            Set<String> uiClasses = new HashSet<>();
            for(Object object : objects){
                uiClasses.add((String)object);
            }
            uiClasses.remove(uIClassName);
            node.setAttribute("ui.class", uiClasses.toArray());
        } else {
            Set<String> uiClasses = new HashSet<>();
            uiClasses.add((String) node.getAttribute("ui.class"));
            uiClasses.remove(uIClassName);
            node.setAttribute("ui.class", uiClasses.toArray());
        }
        //printUIClass(node);
    }

    private void printUIClass(Node node){
        Object currentClasses = node.getAttribute("ui.class");
        if(currentClasses instanceof Object[]){
            Object[] objects = (Object[]) node.getAttribute("ui.class");
            Set<String> uiClasses = new HashSet<>();
            for(Object object : objects){
                uiClasses.add((String)object);
            }
            System.out.println("printUiClasses (more than 1):"+uiClasses);
//            node.setAttribute("ui.class", uiClasses.toArray());
        } else {
            Set<String> uiClasses = new HashSet<>();
            uiClasses.add((String) node.getAttribute("ui.class"));
            System.out.println("printUiClasses (mostly 1):"+uiClasses);
//            node.setAttribute("ui.class", uiClasses.toArray());
        }
    }

    public synchronized void unselectSelectedNode(String nodeId) {
        graph.nodes().filter(node -> node.toString().equals(nodeId)).forEach(node ->
                removeUIClass(node, "clickedPerm")
        );
    }
}
