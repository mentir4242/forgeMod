package com.example.examplemod.util.RedstoneGraphHelpers.ActiveGraph;

import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AbstractEdge;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.AbstractNode;
import org.graphstream.graph.implementations.MultiNode;

import java.util.HashMap;
import java.util.List;
@Deprecated
public class CircuitObjectNode extends MultiNode {

    CircuitObject circuitObject;

    public CircuitObjectNode(AbstractGraph graph, String id, CircuitObject circuitObject) {
        super(graph, id);
        this.circuitObject=circuitObject;
    }

}
