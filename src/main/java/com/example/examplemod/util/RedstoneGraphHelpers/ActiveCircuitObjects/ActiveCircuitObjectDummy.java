package com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects;

import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;

import java.util.Set;

public class ActiveCircuitObjectDummy extends AbstractActiveCircuitObject{

    private boolean onState=false;
    private boolean onStateFuture;

    public ActiveCircuitObjectDummy(CircuitObject circuitObject,
                                       Set<ActiveEdge> inputEdges,
                                       Set<ActiveEdge> outputEdges){
        //super(circuitObject.circuitObjectType, circuitObject.blockPos);
        super(circuitObject, inputEdges, outputEdges);
    }

    public ActiveCircuitObjectDummy(CircuitObject circuitObject){
        //super(circuitObject.circuitObjectType, circuitObject.blockPos);
        //set to null so we get an error later when(if) needed, because we SHOULD always set these later.
        super(circuitObject);
    }

//    public AbstractActiveCircuitObject(CircuitObjectType circuitObjectType,
//                                       BlockPos blockPos,
//                                       Set<ActiveEdge> inputEdges,
//                                       Set<ActiveEdge> outputEdges){
//        super(circuitObjectType, blockPos);
//        this.inputEdges=inputEdges;
//        this.outputEdges=outputEdges;
//    }





    //gets a state that gets fed as a style sheet to the node
    public String getState(){
        return null;
    };

    //gets a state that gets fed as a style sheet to all outgoing edges
    public String getOutgoingEdgeState(){
        return null;
    };

    //gets but doesnt apply the next update (if torch is on and redstone signal is coming in, next
    //state would be torch of.
    public  void queryUpdate(){
        onStateFuture=thisIsGettingPowered();
    };

    //apply the update that was query-ed in the method before
    public void applyUpdate(){
        onState=onStateFuture;
    };

    //true if the CircuitObject Emits Redstone-power
    public boolean outputIsOn(){
        return onState;
    };

    public int getOutputPower(){
        int maxPower=0;
        for ( ActiveEdge activeEdge: getInputEdges()){
            maxPower=Math.max(maxPower, activeEdge.getRedstoneSignalStrengthAtDestination());
        }
        return maxPower;
    };


}
