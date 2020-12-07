package com.example.examplemod.util.RedstoneGraphHelpers;

import java.util.Objects;

public class RepeaterEdge extends Edge {
    public boolean isAtLockPossition; /*true if input is at lock-possition*/

    public RepeaterEdge(CircuitObject source, CircuitObject destination, boolean isAtLockPossition, int distance) {
        super(source, destination, distance);
        this.isAtLockPossition = isAtLockPossition;
    }

    public boolean isAtLockPossition() {
        return isAtLockPossition;
    }

    @Override
    public void setEdgeType(EdgeType edgeType){
        System.out.println("why you do this? setting edge types in comparator");
        if(edgeType!=EdgeType.both
        || this.getEdgeType() !=EdgeType.both){
            throw new IllegalStateException("RepeaterEdges should not have any EdgeTypes besider both in " +
                    "our currecnt modell, tried to set "+edgeType.name());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RepeaterEdge that = (RepeaterEdge) o;
        return isAtLockPossition == that.isAtLockPossition;
    }

    @Override
    public String toString(){
        String addon = isAtLockPossition ? " isAtLockPosition" : "";
        return super.toString()+addon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isAtLockPossition);
    }

    public Edge clone(){
        return new ComparatorEdge(
                this.getSource().clone(),
                this.getDestination().clone(),
                this.isAtLockPossition,
                this.getDistance()
        );
    }
}
