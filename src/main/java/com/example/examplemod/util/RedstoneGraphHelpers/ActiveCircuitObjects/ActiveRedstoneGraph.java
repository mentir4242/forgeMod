package com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects;

import com.example.examplemod.util.RedstoneGraph;
import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;
import com.example.examplemod.util.RedstoneGraphHelpers.ComparatorEdge;
import com.example.examplemod.util.RedstoneGraphHelpers.Edge;
import com.example.examplemod.util.RedstoneGraphHelpers.RepeaterEdge;

import java.util.HashSet;
import java.util.Set;

public class ActiveRedstoneGraph {

    Set<ActiveEdge> edges=new HashSet<>();
    Set<AbstractActiveCircuitObject> circuitObjects=new HashSet<>();

    public Set<ActiveEdge> getEdges() {
        return edges;
    }

    public Set<AbstractActiveCircuitObject> getCircuitObjects() {
        return circuitObjects;
    }

    public ActiveRedstoneGraph(RedstoneGraph redstoneGraph) {
        int id=0;
        for(CircuitObject circuitObject : redstoneGraph.getCircuitObjects()){
            circuitObjects.add(buildAbstractCircuitObjectFromCircuitObject(circuitObject, id));
            id++;
        }
        for(Edge edge : redstoneGraph.getEdges()){
            boolean isAtLockOrSubtractPosition=false;
            if(edge instanceof RepeaterEdge){
                isAtLockOrSubtractPosition=((RepeaterEdge) edge).isAtLockPossition();
            }
            if(edge instanceof ComparatorEdge){
                isAtLockOrSubtractPosition=((ComparatorEdge) edge).isAtSubtractspot();
            }
            edges.add(
                    new ActiveEdge(
                            isAtLockOrSubtractPosition,
                            findInCircuitObject(edge.getSource()),
                            findInCircuitObject(edge.getDestination()),
                            edge.getDistance(),
                            edge.getEdgeType()
                    )
            );
        }
        for(AbstractActiveCircuitObject abstractActiveCircuitObject : circuitObjects){
            abstractActiveCircuitObject.setInputEdges(
                    findAllInputEdges(abstractActiveCircuitObject)
            );
            abstractActiveCircuitObject.setOutputEdges(
                    findAllOutputEdges(abstractActiveCircuitObject)
            );
        }

    }

    private Set<ActiveEdge> findAllOutputEdges(AbstractActiveCircuitObject abstractActiveCircuitObject) {
        Set<ActiveEdge> outputEdges=new HashSet<>();
        for(ActiveEdge activeEdge : edges){
            if(activeEdge.getSource().equals(abstractActiveCircuitObject)){
                outputEdges.add(activeEdge);
            }
        }
        return outputEdges;
    }

    private Set<ActiveEdge> findAllInputEdges(AbstractActiveCircuitObject abstractActiveCircuitObject) {
        Set<ActiveEdge> inputEdges=new HashSet<>();
        for(ActiveEdge activeEdge : edges){
            if(activeEdge.getDestination().equals(abstractActiveCircuitObject)){
                inputEdges.add(activeEdge);
            }
        }
        return inputEdges;
    }

    private AbstractActiveCircuitObject findInCircuitObject(CircuitObject circuitObject){
        for (AbstractActiveCircuitObject abstractActiveCircuitObject : circuitObjects){
            if(abstractActiveCircuitObject.getCircuitObject().equals(circuitObject)){
                return abstractActiveCircuitObject;
            }
        }
        throw new IllegalStateException("it should be in there!");
    }

    private AbstractActiveCircuitObject buildAbstractCircuitObjectFromCircuitObject(CircuitObject circuitObject, int id){
        AbstractActiveCircuitObject abstractActiveCircuitObject=buildAbstractCircuitObjectFromCircuitObject(circuitObject);
        abstractActiveCircuitObject.setId(id);
        return abstractActiveCircuitObject;
    }

    private AbstractActiveCircuitObject buildAbstractCircuitObjectFromCircuitObject(CircuitObject circuitObject){
        switch (circuitObject.circuitObjectType){

            case COMPARATOR: return new ActiveCircuitObjectComparitor(circuitObject);

            case REPEATER: return new ActiveCircuitObjectRepeater(circuitObject);

            case TORCH: return new ActiveCircuitObjectTorch(circuitObject);

            case PISTON:
            case STICKY_PISTON:
            case OUTPUT: return new ActiveCircuitObjectOutput(circuitObject);

            case REDSTONE_BLOCK: return new ActiveCircuitObjectInput(circuitObject,true);
            case INPUT:
            case LEVER: return new ActiveCircuitObjectInput(circuitObject,false);
            case BUTTON: return new ActiveCircuitObjectButton(circuitObject);

            case DUMMY: return new ActiveCircuitObjectDummy(circuitObject);
        }
        throw new IllegalStateException("should be covered by the above");
    }

    public void redstoneTick(){
        //first we save all updates that would happen, then apply them at the same time. well, not the same time.
        //but after all the updates have been query'ed so we dont get cascading updates. those are bad.
        //well, not bad, but unwanted here.
        for(AbstractActiveCircuitObject abstractActiveCircuitObject : circuitObjects){
            abstractActiveCircuitObject.queryUpdate();
        }
        for(AbstractActiveCircuitObject abstractActiveCircuitObject : circuitObjects){
            abstractActiveCircuitObject.applyUpdate();
        }

        //we update dummys again because they better bet up to date^^
        //because after all they represent wire essentially
        for(AbstractActiveCircuitObject abstractActiveCircuitObject : circuitObjects){
            if(abstractActiveCircuitObject instanceof ActiveCircuitObjectDummy)
            abstractActiveCircuitObject.queryUpdate();
        }
        for(AbstractActiveCircuitObject abstractActiveCircuitObject : circuitObjects){
            if(abstractActiveCircuitObject instanceof ActiveCircuitObjectDummy)
            abstractActiveCircuitObject.applyUpdate();
        }
    }

}
