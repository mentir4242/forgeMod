package com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects;

import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;
import com.example.examplemod.util.RedstoneGraphHelpers.Edge;
import com.example.examplemod.util.RedstoneGraphHelpers.EdgeType;

public class ActiveEdge {

    public boolean isAtLockOrSubtractPosition=false;

    private AbstractActiveCircuitObject source;
    private AbstractActiveCircuitObject destination;
    private int distance;
    private EdgeType edgeType=EdgeType.both;

    public ActiveEdge(boolean isAtLockOrSubtractPosition, AbstractActiveCircuitObject source, AbstractActiveCircuitObject destination, int distance, EdgeType edgeType) {
        this.isAtLockOrSubtractPosition = isAtLockOrSubtractPosition;
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.edgeType = edgeType;
    }

    public boolean isAtLockOrSubtractPosition() {
        return isAtLockOrSubtractPosition;
    }

    public AbstractActiveCircuitObject getSource() {
        return source;
    }

    public AbstractActiveCircuitObject getDestination() {
        return destination;
    }

    public int getDistance() {
        return distance;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public boolean sourceIsPoweringDestination(){

        if (source.outputIsOn()){
            if(source.getOutputPower()>this.getDistance()){
                return true;
            }
        }
        return false;
    }

    //dont wanna give back a negative signal strengt. Just does not seem smart.
    public int getRedstoneSignalStrengthAtDestination(){
        if(source.getOutputPower()>=this.getDistance()){
            return source.getOutputPower()-this.getDistance();
        }
        else {
            return 0;
        }
    }

}
