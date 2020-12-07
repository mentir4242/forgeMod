package com.example.examplemod.util.RedstoneGraphHelpers;

import java.util.Objects;

public class Edge {
    private EdgeType edgeType = EdgeType.both;
    private CircuitObject source;
    private CircuitObject destination;
    private int distance;

    public Edge(CircuitObject source, CircuitObject destination, int distance) {
        this.setSource(source);
        this.setDestination(destination);
        this.setDistance(distance);
    }

    public Edge(CircuitObject source, CircuitObject destination, int distance, EdgeType edgeType) {
        this.setSource(source);
        this.setDestination(destination);
        this.setDistance(distance);
        this.setEdgeType(edgeType);
    }

    public void setEdgeType(EdgeType edgeType) {
        this.edgeType = edgeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return getDistance() == edge.getDistance() &&
                getSource().equals(edge.getSource()) &&
                getDestination().equals(edge.getDestination()) &&
                getEdgeType() == edge.getEdgeType();
    }

    @Override
    public String toString(){
        String addon = getEdgeType() == EdgeType.both ? "" : (" "+ getEdgeType().name());

        return "edge from " + this.getSource().toString() +
                " to " + this.getDestination().toString() +
                " distance: " + this.getDistance() +
                addon
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSource(), getDestination(), getDistance());
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public CircuitObject getSource() {
        return source;
    }

    public void setSource(CircuitObject source) {
        this.source = source;
    }

    public CircuitObject getDestination() {
        return destination;
    }

    public void setDestination(CircuitObject destination) {
        this.destination = destination;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public Edge clone(){
        return new Edge(this.source.clone(), this.destination.clone(), this.getDistance(), this.edgeType);
    }
}
