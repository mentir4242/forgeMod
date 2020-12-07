package com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects;

import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;
import com.example.examplemod.util.RedstoneGraphHelpers.Edge;

import java.util.HashSet;
import java.util.Set;

public class ActiveCircuitObjectInput extends AbstractActiveCircuitObject {

    private boolean onState;

    public void setOnState(boolean onState) {
        this.onState = onState;
    }

    public boolean isOnState() {
        return onState;
    }

    public ActiveCircuitObjectInput(CircuitObject circuitObject, Set<ActiveEdge> outputEdges, boolean onState){
        super(circuitObject, new HashSet<>(), outputEdges);
        this.onState=onState;
    }

    public ActiveCircuitObjectInput(CircuitObject circuitObject, boolean onState){
        super(circuitObject);
        this.onState=onState;
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

    }

    public int getOutputPower(){return onState ? 15 : 0;}

    @Override
    public void applyUpdate() {

    }

    @Override
    public boolean outputIsOn() {
        return onState;
    }
}
