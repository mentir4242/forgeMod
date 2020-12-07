package com.example.examplemod.util;

import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;
import com.example.examplemod.util.RedstoneGraphHelpers.Edge;

import java.util.HashSet;
import java.util.Set;

public class RedstoneGraph {
    Set<Edge> Edges;
    Set<CircuitObject> circuitObjects;

    public RedstoneGraph(Set<Edge> edges, Set<CircuitObject> circuitObjects) {
        Edges = edges;
        this.circuitObjects = circuitObjects;
    }

    public Set<Edge> getEdges() {
        return Edges;
    }

    public void setEdges(Set<Edge> edges) {
        Edges = edges;
    }

    public Set<CircuitObject> getCircuitObjects() {
        return circuitObjects;
    }

    public void setCircuitObjects(Set<CircuitObject> circuitObjects) {
        this.circuitObjects = circuitObjects;
    }

    public RedstoneGraph clone(){
        Set<Edge> clonedEdges=new HashSet<>();
        Set<CircuitObject> clonesCircuitObjects=new HashSet<>();
        for (Edge edge : Edges) {
            clonedEdges.add(edge.clone());
        }
        for(CircuitObject circuitObject: circuitObjects){
            clonesCircuitObjects.add(circuitObject.clone());
        }
        return new RedstoneGraph(clonedEdges,clonesCircuitObjects);
    }
}
