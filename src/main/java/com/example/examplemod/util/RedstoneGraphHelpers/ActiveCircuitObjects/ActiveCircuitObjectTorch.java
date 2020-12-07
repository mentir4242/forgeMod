package com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects;

import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;
import com.example.examplemod.util.RedstoneGraphHelpers.Edge;
import com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActiveCircuitObjectTorch extends AbstractActiveCircuitObject{
    boolean torchIsOn=true;

    boolean torchIsOnFuture=true;



    public ActiveCircuitObjectTorch(CircuitObject circuitObject, Set<ActiveEdge> inputEdges, Set<ActiveEdge> outputEdges){
        super(circuitObject, inputEdges, outputEdges);
    }

    public ActiveCircuitObjectTorch(CircuitObject circuitObject){
        super(circuitObject);
    }

    public int getOutputPower(){return outputIsOn() ? 15 : 0;}

    @Override
    public String getState() {
        //placeholder for better logic later, or so I hope
        return Boolean.toString( torchIsOn);
    }

    @Override
    public String getOutgoingEdgeState() {
        //placeholder for better logic later, or so I hope
        return Boolean.toString( torchIsOn);
    }

    //if any of the inputs are on, the torch is off (next)
    @Override
    public void queryUpdate() {
        //if all inputs are off, the torch will turn on kinda soon
        //if at least one is one, torch will be off
        torchIsOnFuture=thisIsGettingPowered()==false;
    }

    @Override
    public Formula deepInputSearch(List<AbstractActiveCircuitObject> nodesWeAlreadyVisited){
        if (nodesWeAlreadyVisited.contains(this)){
            List clonedNodesWeAlreadyVisited = new ArrayList<>(nodesWeAlreadyVisited);
            while(clonedNodesWeAlreadyVisited.get(0)!=this){
                clonedNodesWeAlreadyVisited.remove(0);
            }
            return new LoopFormula(new ArrayList<>(clonedNodesWeAlreadyVisited));
        }

        if(getInputEdges().size()==0){
            return new InputFormula(this);
        } else {
            Set<Formula> formulaSet = new HashSet<>();
            for (ActiveEdge activeEdge : getInputEdges()){
                List clonedNodesWeAlreadyVisited = new ArrayList<>(nodesWeAlreadyVisited);
                clonedNodesWeAlreadyVisited.add(this);
                formulaSet.add(activeEdge.getSource().deepInputSearch(clonedNodesWeAlreadyVisited));
            }
            return new NotFormula( new OrFormula(formulaSet));
        }
    }

    @Override
    public void applyUpdate() {
        torchIsOn= torchIsOnFuture;
    }

    @Override
    public boolean outputIsOn() {
        return torchIsOn;
    }
}
