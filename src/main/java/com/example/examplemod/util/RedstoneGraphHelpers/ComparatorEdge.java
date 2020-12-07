package com.example.examplemod.util.RedstoneGraphHelpers;

import java.util.Objects;

public class ComparatorEdge extends Edge {
    public boolean isAtSubtractspot; /*true if the edge goes into the subtract possition*/

    public boolean isAtSubtractspot() {
        return isAtSubtractspot;
    }

    public ComparatorEdge(CircuitObject source, CircuitObject destination,
                          boolean isAtSubtractspot, int distance) {
        super(source, destination, distance);
        this.isAtSubtractspot = isAtSubtractspot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ComparatorEdge that = (ComparatorEdge) o;
        return isAtSubtractspot == that.isAtSubtractspot;
    }

    @Override
    public void setEdgeType(EdgeType edgeType){
        System.out.println("why you do this? setting edge types in comparator");
        if(edgeType!=EdgeType.both
        || this.getEdgeType() != EdgeType.both){
            throw new IllegalStateException("ComparatorEdges should not have any EdgeTypes besider both in " +
                    "our currecnt modell");
        }
    }

    @Override
    public String toString(){
        String addon = isAtSubtractspot ? " isAtSubtractSpot" : "";
        return super.toString()+addon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isAtSubtractspot);
    }

    public Edge clone(){
        return new ComparatorEdge(
                this.getSource().clone(),
                this.getDestination().clone(),
                this.isAtSubtractspot,
                this.getDistance()
        );
    }
}
