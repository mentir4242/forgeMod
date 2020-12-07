package com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects;

import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;

import java.util.HashSet;
import java.util.Set;

public class ActiveCircuitObjectButton extends AbstractActiveCircuitObject {
    //-1 if button is out.
    private int ticksTillButtonIsOut;
    private int ticksTillButtonIsOutFuture;


    public void press(){ticksTillButtonIsOut=8;}

    public ActiveCircuitObjectButton(CircuitObject circuitObject, Set<ActiveEdge> outputEdges){
        super(circuitObject, new HashSet<>(), outputEdges);
    }

    public ActiveCircuitObjectButton(CircuitObject circuitObject){
        super(circuitObject);
    }

    public String getState() {
        return null;
    }

    //prolly just use outputIsOn
    @Override
    public String getOutgoingEdgeState() {
        return null;
    }

    @Override
    public void queryUpdate() {
        ticksTillButtonIsOutFuture=ticksTillButtonIsOut-1;
    }

    public int getOutputPower(){return outputIsOn() ? 15 : 0;}

    @Override
    public void applyUpdate() {
        ticksTillButtonIsOut=ticksTillButtonIsOutFuture;
    }

    @Override
    public boolean outputIsOn() {
        return ticksTillButtonIsOut>0;
    }
}
