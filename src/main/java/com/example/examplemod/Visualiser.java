package com.example.examplemod;

import com.example.examplemod.util.MakeGraphLookBetterUtility;
import com.example.examplemod.util.RedstoneGraph;
import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;
import com.example.examplemod.util.RedstoneGraphHelpers.ComparatorEdge;
import com.example.examplemod.util.RedstoneGraphHelpers.EdgeType;
import com.example.examplemod.util.RedstoneGraphHelpers.RepeaterEdge;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkImages;

import java.util.HashSet;
import java.util.Set;

/**
 * Visualises whatever Redstone Graph is given to it to the player.
 */
public class Visualiser {
    private Set<com.example.examplemod.util.RedstoneGraphHelpers.Edge> Edges;
    private Set<CircuitObject> Nodes;
    //public static Graph grabYouGraphHere;
    private boolean MC_style = true;

    public Visualiser(RedstoneGraph redstoneGraph) {
        this.Edges = redstoneGraph.getEdges();
        this.Nodes = redstoneGraph.getCircuitObjects();
    }

    public Visualiser(Set<com.example.examplemod.util.RedstoneGraphHelpers.Edge> edges, Set<CircuitObject> nodes) {
        //do this to make it work maybe
        //System.setProperty("org.graphstream.ui", "javafx");
        Edges = new HashSet<>(edges);
        Nodes = new HashSet<>(nodes);

        //this works in theory, maybe later it will work better
//        JavaFXWindow.startGraphStatic=startGraphStatic;
//        JavaFXWindow.main(new String[]{});
    }

    public static Graph getGraphFromRedStoneGraph(RedstoneGraph redstoneGraph) {
        Visualiser visualiser = new Visualiser(redstoneGraph);
        return visualiser.getGraph();
    }

    private static void setupGraphStatic(Graph graph,
                                          RedstoneGraph redstoneGraph,
                                          boolean MC_style) {
        Set<CircuitObject> Nodes = redstoneGraph.getCircuitObjects();
        Set<com.example.examplemod.util.RedstoneGraphHelpers.Edge> Edges = redstoneGraph.getEdges();
        for (CircuitObject circuitObject : Nodes) {
            buildAndAddNode(circuitObject, graph);
        }
        for (com.example.examplemod.util.RedstoneGraphHelpers.Edge edge : Edges) {
            buildAndAddEdge(edge, graph);
        }
        if (MC_style) {
            graph.setAttribute("ui.stylesheet", "url('file://stylesheetMC')");
        } else {
            graph.setAttribute("ui.stylesheet", "url('file://stylesheetTXT')");
        }
    }

    private static void setupGraphStatic(Graph graph,
                                         Set<CircuitObject> Nodes,
                                         Set<com.example.examplemod.util.RedstoneGraphHelpers.Edge> Edges,
                                         boolean MC_style) {
        for (CircuitObject circuitObject : Nodes) {
            buildAndAddNode(circuitObject, graph);
        }
        for (com.example.examplemod.util.RedstoneGraphHelpers.Edge edge : Edges) {
            buildAndAddEdge(edge, graph);
        }
        if (MC_style) {
            graph.setAttribute("ui.stylesheet", "url('file://stylesheetMC')");
        } else {
            graph.setAttribute("ui.stylesheet", "url('file://stylesheetTXT')");
        }
    }

    private static void buildAndAddNode(CircuitObject circuitObject, Graph graph) {
        Node node = graph.addNode(circuitObject.toString());
        node.setAttribute("ui.label", circuitObject.circuitObjectType.name());
        node.setAttribute("ui.class", circuitObject.circuitObjectType.name());
//        if(MC_style){
//            node.setAttribute("ui.stylesheet", "url('file://stylesheetTXT')");
//            node.setAttribute("ui.class", circuitObject.circuitObjectType.name());
//        }
        //testing manual layout here
        //X and Z are the horizontal coordinates in minecraft, so we use those
        //Did this take me hours of testing to find out? I can neither confirm nor deny that,
        //but it works better now
        //Y is up or down, so we use that in the place of Z.
        node.setAttribute("xyz",
                -circuitObject.blockPos.getX(),
                circuitObject.blockPos.getZ(),
                circuitObject.blockPos.getY()
        );
    }

    private static void buildAndAddEdge(com.example.examplemod.util.RedstoneGraphHelpers.Edge edge, Graph graph) {
        Edge edge1 = graph.addEdge(edge.toString(), edge.getSource().toString(), edge.getDestination().toString(), true);
        edge1.setAttribute("layout.weight", getSensibleDistance(edge.getDistance()));
        if (edge.getEdgeType() != EdgeType.both) {
            edge1.setAttribute("ui.class", "indirectEdge");
        }
        if (edge instanceof RepeaterEdge
                && ((RepeaterEdge) edge).isAtLockPossition) {
            edge1.setAttribute("ui.class", "ReaperterEdgeAtLockPosition");
        } else if (edge instanceof ComparatorEdge
                && ((ComparatorEdge) edge).isAtSubtractspot) {
            edge1.setAttribute("ui.class", "ComparatorEdgeAtSubtractspot");
        }
//        else{
//            //currently we do not a class for "default edges. Maybe we should?
//        }
    }

    private static float getSensibleDistance(int distance) {
        //for testing purpose
        return 1;
    }

    private static float getSensibleDistance2(int distance) {
        assert distance < 16;
        assert distance > -1;
        switch (distance) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 1.1f;
            case 3:
                return 1.2f;
            case 4:
                return 1.3f;
            case 5:
                return 1.4f;
            case 6:
                return 1.5f;
            case 7:
                return 1.6f;
            case 8:
                return 1.7f;
            case 9:
                return 1.8f;
            case 10:
                return 1.9f;
            case 11:
                return 2;
            case 12:
                return 2.1f;
            case 13:
                return 2.2f;
            case 14:
                return 2.3f;
            case 15:
                return 3f;
            default:
                throw new IllegalStateException("how did we get HERE??");
        }
    }

    public Graph getGraph() {
        Graph graph = new MultiGraph("Redstone Graph",
                true,
                true,
                Nodes.size(),
                Edges.size()
        );
        setupGraph(graph);
        return graph;
    }

    public void autoDisplay() {
        Statusupdate();
        //make it pretty i.e. more readable and understandable. Passing all the information we have
        //so we can make the most out of it
        RedstoneGraph prettyGraph = MakeGraphLookBetterUtility.doTheThingyByYourself(
                new RedstoneGraph(
                        Edges,
                        Nodes
                )
        );
        this.Edges = prettyGraph.getEdges();
        this.Nodes = prettyGraph.getCircuitObjects();

        Statusupdate();
        Graph graph = new MultiGraph("Redstone Graph",
                true,
                true,
                Nodes.size(),
                Edges.size());
        setupGraph(graph);
        //JavaFXWindow javaFXWindow=new JavaFXWindow(startGraphStatic);
//        System.out.println(Edges);
//        System.out.println(Nodes);
        //Viewer viewer = startGraphStatic.display();
        System.out.println("do the writing of an image file");
        FileSinkImages pic = FileSinkImages.createDefault();
        pic.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);

        try {
            pic.writeAll(graph, "other.png");
        } catch (Exception e) {
            System.out.println("file writing didnt work out so well");
            e.printStackTrace();
        }
        //how did startGraphStatic.display work so well??
//        JavaFXWindow.startGraphStatic=startGraphStatic;
//        JavaFXWindow.startRedstoneGraphStatic=new RedstoneGraph(Edges, Nodes);
//        JavaFXWindow.main(new String[]{});
        graph.display(false);
    }

    public void openDisplayWindow() {
        JavaFXWindow.sourceRedstoneGraph = new RedstoneGraph(Edges, Nodes);
        JavaFXWindow.main(new String[]{});
    }

    private String StatusMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Status Message:\n");
        message.append("Nodes: \n");
        if (Nodes.size() < 100) {
            for (CircuitObject circuitObject : Nodes) {
                message.append(circuitObject.toString());
                message.append("\n");
            }
        } else {
            message.append("many Nodes, " + Nodes.size() + " to be exact");
        }
        message.append("Edges: \n");
        if (Edges.size() < 100) {
            for (com.example.examplemod.util.RedstoneGraphHelpers.Edge edge : Edges) {
                message.append(edge.toString());
                message.append("\n");
            }
        } else {
            message.append("many Edges, " + Edges.size() + " to be exact");
        }
        return message.toString();
    }

    private void Statusupdate() {
        System.out.println(StatusMessage());
    }

    private void setupGraph(Graph graph) {
        for (CircuitObject circuitObject : Nodes) {
            buildAndAddNode(circuitObject, graph);
        }
        for (com.example.examplemod.util.RedstoneGraphHelpers.Edge edge : Edges) {
            buildAndAddEdge(edge, graph);
        }
        if (MC_style) {
            graph.setAttribute("ui.stylesheet", "url('file://stylesheetMC')");
        } else {
            graph.setAttribute("ui.stylesheet", "url('file://stylesheetTXT')");
        }
    }


}
