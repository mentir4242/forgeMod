package com.example.examplemod.util;

import com.example.examplemod.util.RedstoneGraphHelpers.*;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/*
if one want to be fancy, generate an Instance of this, put all the boolean to what one fancys, then let if run
If default setting shall be used, "doTheThingyByYourself()" does all the work for you
 */
public class MakeGraphLookBetterUtility {
    private Set<Edge> edges;
    private Set<CircuitObject> circuitObjects;
    public boolean cleanUpEdges = true;
    public boolean fixAtoMultipleOtherCircuitObjects = true;

    public MakeGraphLookBetterUtility(RedstoneGraph redstoneGraph){
        this.edges=new HashSet<>( );
        this.edges.addAll(redstoneGraph.getEdges());
        this.circuitObjects=new HashSet<>();
        this.circuitObjects.addAll( redstoneGraph.getCircuitObjects());
    }

    //modifies the startGraphStatic based on the boolean above set or unset. Each of them
    //wil typicly turn on or off a method call in here
    public void makeGraphLookPrettyAuto(){
        if(cleanUpEdges){
            cleanUpEdges();
        }
        if(fixAtoMultipleOtherCircuitObjects){
            fixAtoMultipleOtherCircuitObjects();
        }
    }

    public RedstoneGraph returnGraph(){
        return new RedstoneGraph(edges, circuitObjects);
    }

    public static RedstoneGraph doTheThingyByYourself(RedstoneGraph redstoneGraph){
        MakeGraphLookBetterUtility makeGraphLookBetterUtility = new MakeGraphLookBetterUtility(redstoneGraph);
        makeGraphLookBetterUtility.makeGraphLookPrettyAuto();
        return makeGraphLookBetterUtility.returnGraph();
    }

    //attemps to make the case "A has edges to multiple other CircuitObjects" more readable
    //ways to do that in a good way still WIP
    //only compatible with fixed positions right now
    public void fixAtoMultipleOtherCircuitObjects(){

        Set<CircuitObject> Dummys= new HashSet<>();

        for (CircuitObject circuitObject : circuitObjects){
            Set<Edge> outgoingEdges = findAllOutgoingEdges(circuitObject);
            //edgeLimit is the limit above which we intervene and attempt to make
            //the Situation more readable. Best value for that still todo
            int edgeLimit = 2;

            if(outgoingEdges.size() > edgeLimit){
                //current plan for making more readable:
                // 1. find middle of destinations
                // 2. of that middle, build the middle between that and out source
                // 3. make that a new Circuit object in the middle
                // 4 connection source and and all outgoing Edges to that.

                //not reinventing the wheel here, add them all up then devide by total amount to get middle
                int outgoingEdgesSize = outgoingEdges.size();
                BlockPos middleOfDestinations = new BlockPos(0,0,0);
                for (Edge edge : outgoingEdges){
                    middleOfDestinations= middleOfDestinations.add(edge.getDestination().getBlockPos());
                }

                middleOfDestinations = new BlockPos(
                        middleOfDestinations.getX()/outgoingEdgesSize,
                        middleOfDestinations.getY()/outgoingEdgesSize,
                        middleOfDestinations.getZ()/outgoingEdgesSize
                );

                System.out.println("middleOfDestinations "+middleOfDestinations);

                //weightedMiddle is the middle of circuitObjects Blockpos and middleOfDestinations
                int xInTheWeightedMiddle = (circuitObject.getBlockPos().getX()+middleOfDestinations.getX() )/2;
                int yInTheWeightedMiddle = (circuitObject.getBlockPos().getY()+middleOfDestinations.getY() )/2;
                int zInTheWeightedMiddle = (circuitObject.getBlockPos().getZ()+middleOfDestinations.getZ() )/2;
                BlockPos weightedMiddleOfItAll=new BlockPos(
                        xInTheWeightedMiddle,
                        yInTheWeightedMiddle,
                        zInTheWeightedMiddle
                );

                System.out.println("weightedMiddleOfItAll "+weightedMiddleOfItAll);

                CircuitObject newBridgeBetweenSourceAndDestinations = new CircuitObject(
                        CircuitObjectType.DUMMY,
                        weightedMiddleOfItAll,
                        true
                );

                for(Edge edge : outgoingEdges){
                    edge.setSource(newBridgeBetweenSourceAndDestinations);
                    edge.setDistance(edge.getDistance()/2);
                }

                Edge newEdgeFromSourceToBridge = new Edge(
                        circuitObject,
                        newBridgeBetweenSourceAndDestinations,
                        1
                );

                Dummys.add(newBridgeBetweenSourceAndDestinations);

                edges.add(newEdgeFromSourceToBridge);
            }
        }

        circuitObjects.addAll(Dummys);
    }

    private boolean allEdgesAreSOMETHING(Collection<Edge> edges, EdgeType edgeType){
        for (Edge edge : edges){
            if(edge.getEdgeType()!=edgeType){
                return false;
            }
        }
        return true;
    }

    //find all edge in the edges that go to a different CircuitObject (notably not those that go to itself
    private Set<Edge> findAllOutgoingEdges(CircuitObject circuitObject){
        Set<Edge> outgoingEdges=new HashSet<>();
        for (Edge edge : edges){
            if(edge.getSource().equals(circuitObject)
            && edge.getDestination().equals(circuitObject)==false){
                outgoingEdges.add(edge);
            }
        }
        return outgoingEdges;
    }


    //deletes duplicate edges we dont need, that is Edges that both go from
    //A to B in the same way (but with different distance. Does this by taking each edge, comparing
    //it to the Rest and keeping only the shortest distance if two are identical (except for distance)
    public void cleanUpEdges(){
            Set<Edge> newEdges = new HashSet<>();
            while (edges.size() != 0) {
                Edge masterEdge = edges.iterator().next();
                //we only keep one Edge from start to End point, and we
                //remove all in the End.
                Set<Edge> edgesToBeRemoved = new HashSet<>();
                edges.remove(masterEdge);
                int i=0;
                for (Edge edge : edges) {
                    //if another edge goes from the same object to the same object in the same way,
                    //they are equivalent to us except for distance. shorter distance overrides longer distance
                    //so we keep the edge with the short distance
                    if (masterEdge.getSource() .equals( edge.getSource())
                            && masterEdge.getDestination() .equals( edge.getDestination())) {
                        //repeater Edges need to be at the same position to be considered equal for this
                        if (masterEdge instanceof RepeaterEdge
                                && edge instanceof RepeaterEdge
                                && ((RepeaterEdge) masterEdge).isAtLockPossition() ==
                                ((RepeaterEdge) edge).isAtLockPossition()) {
                            masterEdge.setDistance(Math.min(masterEdge.getDistance(), edge.getDistance()));
                            edgesToBeRemoved.add(edge);
                        }
                        //comparator edges need to be at the same spot to be considered Equal
                        else if (masterEdge instanceof ComparatorEdge
                                && edge instanceof ComparatorEdge
                                && (((ComparatorEdge) masterEdge).isAtSubtractspot() ==
                                ((ComparatorEdge) edge).isAtSubtractspot())) {
                            masterEdge.setDistance(Math.min(masterEdge.getDistance(), edge.getDistance()));
                            edgesToBeRemoved.add(edge);
                        }
                        //normale edges just need the same EdgeType. This is a bit of trickery,
                        //since technically this is also true for comparator and RepeaterEdges,
                        //but those can only ever have EdgeType both.
                        else if (masterEdge.getClass().equals(Edge.class)
                                && edge.getClass().equals(Edge.class)
                                &&   masterEdge.getEdgeType() == edge.getEdgeType()) {
                            masterEdge.setDistance(Math.min(masterEdge.getDistance(), edge.getDistance()));
                            edgesToBeRemoved.add(edge);
                        }
                    }
                }
                newEdges.add(masterEdge);
                edges.removeAll(edgesToBeRemoved);
            }
            edges = newEdges;
            //System.out.println("cleanup done");

    }

}
