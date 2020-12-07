package com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects;

import com.example.examplemod.util.RedstoneGraphHelpers.*;

import java.util.Set;

public class ActiveCircuitObjectRepeater extends AbstractActiveCircuitObject{

    //repeater have a delay and we use a state to mimic it. The rules for it are somewhat involved:
    private boolean[] states;
    private boolean stateFuture;
    private int repeaterDelay;
    private boolean repeaterIsOn;
    private boolean isLocked;
    private boolean isLockedFuture;


    public ActiveCircuitObjectRepeater(CircuitObject circuitObject,
                                       Set<ActiveEdge> inputEdges,
                                       Set<ActiveEdge> outputEdges){

        super(circuitObject, inputEdges, outputEdges);
        states=new boolean[9];
        repeaterDelay= ((CircuitObjectRepeater)circuitObject).delay;
    }

    public ActiveCircuitObjectRepeater(CircuitObject circuitObject){

        super(circuitObject);
        states=new boolean[9];
        repeaterDelay= ((CircuitObjectRepeater)circuitObject).delay;
    }

    @Override
    public String getState() {
        //placeholder for better logic later, or so I hope
        return null;
    }

    public int getOutputPower(){return outputIsOn() ? 15 : 0;}

    @Override
    public String getOutgoingEdgeState() {
        //placeholder for better logic later, or so I hope
        return null;
    }

    //if any of the inputs are on, the torch is off (next)
    @Override
    public void queryUpdate() {
        stateFuture=thisIsGettingPowered();
        isLockedFuture=thisIsGettingLocked();
    }

    //if any source locks this, this is getting locked.
    private boolean thisIsGettingLocked() {
        for (ActiveEdge activeEdge : getInputEdges()){
            if(activeEdge.sourceIsPoweringDestination()){
                if(activeEdge.isAtLockOrSubtractPosition==true){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void applyUpdate() {

        //if we are locked, nothing ever changes in Terms of outputs. So we remember old state here that here.
        boolean repeaterIsOnOld=repeaterIsOn;

        //move everything in the state array one into the future.
        if (states.length - 1 >= 0) System.arraycopy(states, 0, states, 1, states.length - 1);

        states[0]=stateFuture;
        isLocked=isLockedFuture;

        //the rules for when repeaters are on are somewhat involved, I hope I got them right here.
        if(repeaterIsOn){
            switch (repeaterDelay){
                //basically if the signal outage was not longer than the delay, it is ignored.
                case 1: repeaterIsOn = states[0];
                break;
                case 2: repeaterIsOn = states[0] || states[1];
                break;
                case 3: repeaterIsOn = states[0] || states[1] || states[2];
                break;
                case 4: repeaterIsOn = states[0] || states[1] || states[2] || states[3];
                break;
            }
        } else {
            //and if a signal came in, it is applied after waiting repeaterDelay-long.
            repeaterIsOn = states[repeaterDelay-1];
        }


        //if we are locked, nothing ever changes in Terms of outputs. So we set back to old state here.
        if(isLocked){
            repeaterIsOn=repeaterIsOnOld;
        }
    }

    @Override
    public boolean outputIsOn() {
        //repeater have
       return repeaterIsOn;
    }

}
