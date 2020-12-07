package com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects;

import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;

import java.util.HashSet;
import java.util.Set;

public class ActiveCircuitObjectOutput extends AbstractActiveCircuitObject{
    //dont know if needed
//    public String getDefaultState();
    boolean isPowered;
    boolean isPoweredFuture;

    public ActiveCircuitObjectOutput(CircuitObject circuitObject,
                                       Set<ActiveEdge> inputEdges){
        super(circuitObject, inputEdges, new HashSet<>());
    }

    public ActiveCircuitObjectOutput(CircuitObject circuitObject){
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
    }

    //gets a state that gets fed as a style sheet to all outgoing edges
    public String getOutgoingEdgeState(){
        return null;
    }

    //gets but doesnt apply the next update (if torch is on and redstone signal is coming in, next
    //state would be torch of.
    public void queryUpdate(){
        isPoweredFuture=thisIsGettingPowered();
    }

    //apply the update that was query-ed in the method before
    public void applyUpdate(){
        isPowered=isPoweredFuture;
    };

    //true if the CircuitObject Emits Redstone-power
    public boolean outputIsOn(){
        return isPowered;
    };

    public int getOutputPower(){
        return 0;
    };
}
