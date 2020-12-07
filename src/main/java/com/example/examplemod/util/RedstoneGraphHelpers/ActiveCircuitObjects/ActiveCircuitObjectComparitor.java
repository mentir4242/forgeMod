package com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects;

import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;
import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObjectComparitor;
import com.example.examplemod.util.RedstoneGraphHelpers.Edge;

import java.util.Set;

public class ActiveCircuitObjectComparitor extends AbstractActiveCircuitObject{

    private boolean inSubtractMode;
    private int powerAtSubtractSpot;
    private int powerAtSubtractSpotFuture;
    private int inputPower;
    private int inputPowerFuture;

    public ActiveCircuitObjectComparitor(CircuitObject circuitObject,
                                         Set<ActiveEdge> inputEdges,
                                         Set<ActiveEdge> outputEdges) {
        super(circuitObject, inputEdges, outputEdges);
        this.inSubtractMode=((CircuitObjectComparitor)circuitObject).subtractMode;
    }

    public ActiveCircuitObjectComparitor(CircuitObject circuitObject) {
        super(circuitObject);
        this.inSubtractMode=((CircuitObjectComparitor)circuitObject).subtractMode;
    }

    @Override
    public String getState() {
        return null;
    }

    @Override
    public String getOutgoingEdgeState() {
        return null;
    }

    @Override
    public void queryUpdate() {
        powerAtSubtractSpotFuture=calculatePowerAtSubtractSpot();
        inputPowerFuture=calculateInputPower();
    }

    private int calculatePowerAtSubtractSpot(){
        int powerAtSubtractSpotFuture=0;
        for (ActiveEdge activeEdge : getInputEdges()){
            if(activeEdge.isAtLockOrSubtractPosition){
                powerAtSubtractSpotFuture= Math.max(powerAtSubtractSpotFuture,
                        activeEdge.getRedstoneSignalStrengthAtDestination());
            }
        }
        return powerAtSubtractSpotFuture;
    }

    private int calculateInputPower(){
        int inputPowerFuture=0;
        for (ActiveEdge activeEdge : getInputEdges()){
            if(activeEdge.isAtLockOrSubtractPosition==false){
                inputPowerFuture= Math.max(inputPowerFuture,
                        activeEdge.getRedstoneSignalStrengthAtDestination());
            }
        }
        return inputPowerFuture;
    }

    @Override
    public void applyUpdate() {
        powerAtSubtractSpot=powerAtSubtractSpotFuture;
        inputPower=inputPowerFuture;
    }

    @Override
    public boolean outputIsOn() {
        return getOutputPower()>0;
    }

    @Override
    public int getOutputPower() {
        //System.out.println("comparitorOutputPower() with inputPower: "+inputPowerFuture+" and powerAtSubtractSpot: "+powerAtSubtractSpotFuture);
        if(inSubtractMode){
            int power=inputPower-powerAtSubtractSpot;
            return power > 0 ? power : 0;
        } else {
            return inputPower>=powerAtSubtractSpot ? inputPower : 0;
        }
    }
}
