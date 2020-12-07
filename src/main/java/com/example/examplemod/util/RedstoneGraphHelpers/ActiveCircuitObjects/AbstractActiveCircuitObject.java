package com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects;

import com.example.examplemod.util.RedstoneGraphHelpers.*;
import com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel.*;

import java.util.*;

public abstract class AbstractActiveCircuitObject{

    //dont know if needed
//    public String getDefaultState();
    private Set<ActiveEdge> inputEdges;
    private Set<ActiveEdge> outputEdges;
    private CircuitObject circuitObject;

    //-1 if not set
    private int id =-1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AbstractActiveCircuitObject setIdReturn(int id){
        this.id=id;
        return this;
    }



    public CircuitObject getCircuitObject() {
        return circuitObject;
    }

    public void setInputEdges(Set<ActiveEdge> inputEdges) {
        this.inputEdges = inputEdges;
    }

    public void setOutputEdges(Set<ActiveEdge> outputEdges) {
        this.outputEdges = outputEdges;
    }

    public AbstractActiveCircuitObject(CircuitObject circuitObject,
                                       Set<ActiveEdge> inputEdges,
                                       Set<ActiveEdge> outputEdges){
        //super(circuitObject.circuitObjectType, circuitObject.blockPos);
        this.inputEdges=inputEdges;
        this.outputEdges=outputEdges;
        this.circuitObject=circuitObject;
    }

    public AbstractActiveCircuitObject(CircuitObject circuitObject){
        //super(circuitObject.circuitObjectType, circuitObject.blockPos);
        //set to null so we get an error later when(if) needed, because we SHOULD always set these later.
        this.inputEdges=null;
        this.outputEdges=null;
        this.circuitObject=circuitObject;
    }

//    public AbstractActiveCircuitObject(CircuitObjectType circuitObjectType,
//                                       BlockPos blockPos,
//                                       Set<ActiveEdge> inputEdges,
//                                       Set<ActiveEdge> outputEdges){
//        super(circuitObjectType, blockPos);
//        this.inputEdges=inputEdges;
//        this.outputEdges=outputEdges;
//    }

    public Set<ActiveEdge> getInputEdges() {
        return inputEdges;
    }

    public Set<ActiveEdge> getOutputEdges() {
        return outputEdges;
    }


    //this I am kinda proud of, since I spend a few days just thinking about how it would work
    //the result is kinda simple and non-groundbreaking I guess, but it is elegant IMO.
    //what we do is we traverse the signal stream backwards, stopping if we reach a node with no inputs
    //(which is then an input itself and will be returned as such) or we enter a loop, in which case
    //we return each element in the loop. The result will be a Set of Inputs that should model which
    //inputs and "saving cells"/"clocks" influence our Circuit Object.
    //but maybe I can do better with a tree-based approach?
    //well, I was proud of it for a while, but it seems deepInputSearch Might be better..
    //see with a heavy heart gotta mark this deprecated...
    @Deprecated
    public Set<InputModel> getInputs(List<AbstractActiveCircuitObject> pathSoFar) {
        //if a node has no inputs we consider it an input itself
        if(inputEdges.size()==0){
            return new HashSet<InputModel>(
                    Arrays.asList(
                            new InputModel(
                                    pathSoFar,
                                    new ArrayList(Arrays.asList(this))
                            )
                    )
            );
        } else  {
            //if it has at least one input we check if we are in a loop
            if(pathSoFar.contains(this)){
                //inputs are everything in the loop, so we cut everything up to the repeating object
                List clonedListOfPathSoFarMadeForInput = new ArrayList<>(pathSoFar);
                while(clonedListOfPathSoFarMadeForInput.get(0)!=this){
                    clonedListOfPathSoFarMadeForInput.remove(0);
                }
                List clonedListOfPathSoFarMadeForPath = new ArrayList<>(pathSoFar);
                //path to the loop is everything up to the repeating object, so we cut everything starting
                //from the end until and including this repeating object
                while(clonedListOfPathSoFarMadeForPath.get(clonedListOfPathSoFarMadeForPath.size()-1)
                        !=this){
                    clonedListOfPathSoFarMadeForPath.remove(clonedListOfPathSoFarMadeForPath.size()-1);
                }
                clonedListOfPathSoFarMadeForPath.remove(clonedListOfPathSoFarMadeForPath.size()-1);
                return new HashSet<InputModel>(
                        Arrays.asList(
                                new InputModel(
                                        clonedListOfPathSoFarMadeForPath,
                                        clonedListOfPathSoFarMadeForInput
                                )
                        )
                );
            }
            //if we are not in a loop we return a combination of all inputs
            else {
                Set inputSet = new HashSet<InputModel>();
                for (ActiveEdge activeEdge : inputEdges){
                    List inputsSoFarCloned = new ArrayList<>(pathSoFar);
                    inputsSoFarCloned.add(this);
                    inputSet.addAll(
                            activeEdge.getSource().getInputs(inputsSoFarCloned)
                    );
                }
                return inputSet;
            }

        }
    }

    //returns a Formula that describes this CO.
    public Formula deepInputSearch(List<AbstractActiveCircuitObject> nodesWeAlreadyVisited){

        if (nodesWeAlreadyVisited.contains(this)){
            List clonedNodesWeAlreadyVisited = new ArrayList<>(nodesWeAlreadyVisited);
            while(clonedNodesWeAlreadyVisited.get(0)!=this){
                clonedNodesWeAlreadyVisited.remove(0);
            }
            return new LoopFormula(new ArrayList<>(clonedNodesWeAlreadyVisited));
        }

        if(inputEdges.size()==0){
            return new InputFormula(this);
        } else {
            Set<Formula> formulaSet = new HashSet<>();
            Set<Formula> lockOrSubtractSpotSet = new HashSet<>();
            for (ActiveEdge activeEdge : inputEdges){
                if(activeEdge.isAtLockOrSubtractPosition()){
                    List clonedNodesWeAlreadyVisited = new ArrayList<>(nodesWeAlreadyVisited);
                    clonedNodesWeAlreadyVisited.add(this);
                    lockOrSubtractSpotSet.add(activeEdge.getSource().deepInputSearch(clonedNodesWeAlreadyVisited));
                } else {
                    List clonedNodesWeAlreadyVisited = new ArrayList<>(nodesWeAlreadyVisited);
                    clonedNodesWeAlreadyVisited.add(this);
                    formulaSet.add(activeEdge.getSource().deepInputSearch(clonedNodesWeAlreadyVisited));
                }
            }
            if(lockOrSubtractSpotSet.size()==0){
                return new OrFormula(formulaSet);
            } else {
                if (this instanceof ActiveCircuitObjectRepeater){
                    return new RepeaterLockFormula(
                        new OrFormula(formulaSet),
                        new OrFormula(lockOrSubtractSpotSet)
                    );
                }
                if (this instanceof ActiveCircuitObjectComparitor){
                    return new ComparitorFormula(
                            new OrFormula(formulaSet),
                            new OrFormula(lockOrSubtractSpotSet)
                    );
                }
                throw new IllegalStateException("got a isAtLockOrSubtractPosition=true edge despite the Destination" +
                        " not being" +
                        "a Comparator or Repeater");
            }
        }
    }

    public boolean thisIsGettingPowered(){
        for (ActiveEdge activeEdge : inputEdges){
            if(activeEdge.sourceIsPoweringDestination()){
                if(activeEdge.isAtLockOrSubtractPosition==false){
                    return true;
                }
            }
        }
        return false;
    }

    //gets a state that gets fed as a style sheet to the node
    public abstract String getState();

    //gets a state that gets fed as a style sheet to all outgoing edges
    public abstract String getOutgoingEdgeState();

    //gets but doesnt apply the next update (if torch is on and redstone signal is coming in, next
    //state would be torch of.
    public abstract void queryUpdate();

    //apply the update that was query-ed in the method before
    public abstract void applyUpdate();

    //true if the CircuitObject Emits Redstone-power
    public abstract boolean outputIsOn();

    public abstract int getOutputPower();

}
