package com.example.examplemod;

import com.example.examplemod.util.RedstoneGraph;
import com.example.examplemod.util.RedstoneGraphHelpers.*;
import com.example.examplemod.util.utility;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.Property;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraft.state.properties.BlockStateProperties.*;

/**
 * Redstone Circuit Diagram, renamd for easier Code reading.
 * Does most of the work transforming the minecraft world into an Graph of CircuitObjects and Connections
 */
public class RCD /*more of a graph really*/ {

    //circuitObjects aka nodes
    private Set<CircuitObject> circuitObjects;
    //redstone wire connections, aka edgeSet
    private Set<Edge> edgeSet;

    public RCD(BlockState[][][] area) {
        this(area, false);
    }

    public RCD(BlockState[][][] area, boolean ALL) {


        try {
            if (ALL) {
                setupRedstoneCircuitDiagramALL(area);
            } else {
                setupRedstoneCircuitDiagram(area);
            }
            statusReport();
            display();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.toString());
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println("this didnt work out well...");
        }
    }

    private void setupRedstoneCircuitDiagramALL(BlockState[][][] area) {
        //all signal circuitObjects are "circuitObjects" and lead into other object via edgeSet
        //those object might be new circuitObjects (repeater, comparitor, torch,...)
        //those are also getting searched (but each only once)

        //we do that in cordget, so no reason to do that
        //area = addPadding(area);

        //nodes
        Set<CircuitObject> sources = new HashSet<>();
        //you already know (Edges)
        Set<Edge> Edges = new HashSet<>();
        //find the inputs in area, inputs are market with white wool
//        List<BlockPos> inputs = findWhiteWool(area);
//        for (BlockPos blockPos : inputs) {
//            System.out.println("found an input");
//            CircuitObject c = new CircuitObject(CircuitObjectType.INPUT, blockPos);
//            sourcesToBeSearched.add(c);
//            circuitObjects.add(c);
//        }
        //find all of the redstone inputs
        Set<CircuitObject> circuitObjects = areaSearchCircuitObjects(area);
        sources.addAll(circuitObjects);

        //workhorse right here. at least in the other method it is. here it is pretty tame tbh
        for (CircuitObject startOnject : sources) {
            Set<Edge> newEdges = calculateEdges(startOnject, area);
            Edges.addAll(newEdges);
        }

        this.circuitObjects = sources;
        this.edgeSet = Edges;
    }

    public void setupRedstoneCircuitDiagram(BlockState[][][] area) {
        //all signal circuitObjects are "circuitObjects" and lead into other object via edgeSet
        //those object might be new circuitObjects (repeater, comparitor, torch,...)
        //those are also getting searched (but each only once)

        //we do that in cordget, so no reason to do that
        //area = addPadding(area);

        //nodes yet to be searched
        Set<CircuitObject> sourcesToBeSearched = new HashSet<>();
        //nodes
        Set<CircuitObject> sources = new HashSet<>();
        //you already know (Edges)
        Set<Edge> Edges = new HashSet<>();
        //find the inputs in area, inputs are market with white wool
//        List<BlockPos> inputs = findWhiteWool(area);
//        for (BlockPos blockPos : inputs) {
//            System.out.println("found an input");
//            CircuitObject c = new CircuitObject(CircuitObjectType.INPUT, blockPos);
//            sourcesToBeSearched.add(c);
//            circuitObjects.add(c);
//        }
        //find all of the redstone inputs
        Set<CircuitObject> redstoneInputDevices = areaSearchRedstoneInputs(area);
        sourcesToBeSearched.addAll(redstoneInputDevices);
        sources.addAll(redstoneInputDevices);

        //workhorse right here
        //looks from one component to what it reaches, adds all that to be searched if we have not already
        while (sourcesToBeSearched.size() != 0) {
            CircuitObject startobject = sourcesToBeSearched.iterator().next();
            sourcesToBeSearched.remove(startobject);
            Set<Edge> newEdges = calculateEdges(startobject, area);
            Edges.addAll(newEdges);
            Set<CircuitObject> potentialNewSources = new HashSet<>();
            for (Edge edge : Edges) {
                potentialNewSources.add(edge.getDestination());
            }
            //we wouldnt want to loop, so we dont visit circuitObjects twice
            potentialNewSources.removeAll(sources);
            //circuitObjects to Be searched contains only those circuitObjects that were in potentialNewSources but not in circuitObjects
            sourcesToBeSearched.addAll(potentialNewSources);
            //circuitObjects saves all circuitObjects ever
            sources.addAll(potentialNewSources);
        }

        this.circuitObjects = sources;
        this.edgeSet = Edges;
    }

    public void display(){
        System.out.println("do the display thingy");

        JavaFXWindow.sourceRedstoneGraph = new RedstoneGraph(edgeSet, circuitObjects);

        try{
            new Thread(() -> JavaFXWindow.main(new String[]{})).start();
        } catch (Exception e) {
            System.out.println("exception here, maybe because window is already running, in which case, no problem");
            e.printStackTrace();
        }

    }



    private Set<Edge> calculateEdges(CircuitObject source, BlockState[][][] area) {
        Set<Edge> edges = new HashSet<>();
        switch (source.circuitObjectType) {
            case COMPARATOR:
            case REPEATER:
                //no functional difference between repeater and comparitors found by me at this point
                return repeaterTypeObjectsEdgeSearch(source, area);
            case REDSTONE_BLOCK:
            case INPUT:
                return strongPowerSearch(area, source.blockPos, source, 15);
            case TORCH:
                //the torch hard powers the block above if it is opaque
                if (stopsRedstoneWire(area, source.blockPos.up())) {
                    edges.addAll(strongPowerSearch(area, source.blockPos.up(), source, 15));
                }
                //we could do strong power search here, but that messes up if we are on the BACKSIDE OF A REPEATER
                //welp... do we do it anyways and leave the problems for future Ludwig?
                //got it: we put it into a method that is the same, but different name, so
                //I will change it later when it comes to repeaters. OR AM I??
                edges.addAll(torchEdgeSearch(area, source.blockPos, source, 15));

                return edges;
            case LEVER:
            case BUTTON:
                //do not forget!
                Direction directionOfLever = getFacing(utility.cordGet(area, source.blockPos)).getOpposite();
                BlockPos posOfBlockLeverIsAttachedTo = source.blockPos.add(directionOfLever.getDirectionVec());
                if (stopsRedstoneWire(area, posOfBlockLeverIsAttachedTo)) {
                    edges.addAll(strongPowerSearch(area, posOfBlockLeverIsAttachedTo, source, 15));
                }
                edges.addAll(leverTypeObjectEdgeSearch(area, source.blockPos, source, 15));
                return edges;
            case OUTPUT:
            case PISTON:
            case STICKY_PISTON:
                return new HashSet<>();

            default:
                throw new IllegalStateException("Unexpected value: " + source.circuitObjectType);
        }
//        Set<BlockPos> positionsAlreadyFound = new HashSet<>(positionsToVisit);
//        edges.addAll(redstoneEdgeSearch(source, area, positionsToVisit, positionsAlreadyFound, 15));
//        return edges;
    }

    /*
     * our searchers (important helpers)
     * */

    private Set<Edge> redstoneEdgeSearch(CircuitObject source,
                                         BlockState[][][] area,
                                         Set<BlockPos> positionsToVisit,
                                         Set<BlockPos> positionsAlreadyFound,
                                         int signalPower) {
        if (signalPower == 0) {
            return new HashSet<>();
        }
        //wireYetToVisit will be used to keep track of where we need to go still
        Set<BlockPos> newPositionsToVisit = new HashSet<>();
        //in this we will collect all the edges we find along the way
        Set<Edge> edges = new HashSet<>();

        //do our thing until the signal runs out

        //do stuff for each redstone wire that is at strength equal to signal power (we make
        //sure this is true, e.g. kicking out the old ones;
        for (BlockPos startPos : positionsToVisit) {
            //startPos is the redstone wire from which we test (it is powered, we are looking for places it connects to)
            //lets start then. what we do is identical for each side, so we repeat for each horizontal direction
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                //convenience
                BlockPos pos = startPos.add(direction.getDirectionVec());
                BlockState blockStateAtPos = utility.cordGet(area, pos);
                //connect to all redstone that is right next to us
                if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WIRE)
                        && positionsAlreadyFound.contains(pos) == false) {
                    newPositionsToVisit.add(pos);
                    positionsAlreadyFound.add(pos);
                }
                //connect to any repeater right next to us, given that it points away from us
                else if (blockStateAtPos.getBlock().equals(Blocks.REPEATER) &&
                        blockStateAtPos.get(HORIZONTAL_FACING).getOpposite() == direction) {
                    edges.add(new RepeaterEdge(
                            source,
                            new CircuitObjectRepeater(pos,
                                    blockStateAtPos.get(DELAY_1_4)),
                            false,
                            15 - signalPower
                    ));
                }
                //connect to any comparitor right next to us, given that it points away from us
                else if (blockStateAtPos.getBlock().equals(Blocks.COMPARATOR) &&
                        blockStateAtPos.get(HORIZONTAL_FACING).getOpposite().equals(direction)) {
                    edges.add(new ComparatorEdge(
                            source,
                            new CircuitObjectComparitor(pos,
                                    blockStateAtPos.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                            false,
                            15 - signalPower
                    ));
                }
                //still connect to comparitor if it points sideways, this time at subtract spot
                else if (blockStateAtPos.getBlock().equals(Blocks.COMPARATOR) &&
                        sideways(blockStateAtPos.get(HORIZONTAL_FACING), direction)) {
                    edges.add(new ComparatorEdge(
                            source,
                            new CircuitObjectComparitor(pos,
                                    blockStateAtPos.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                            true,
                            15 - signalPower
                    ));
                }

                //do stuff to the blocks above pos if and only if the block above us is not Opaque
                //(Opaque blocks cut off and block all redstone signals we care about)
                if (stopsRedstoneWire(utility.cordGet(area, startPos.up())) == false) {
                    if (utility.cordGet(area, pos.up()).getBlock().equals(Blocks.REDSTONE_WIRE)
                            && positionsAlreadyFound.contains(pos.up()) == false) {
                        newPositionsToVisit.add(pos.up());
                        positionsAlreadyFound.add(pos.up());
                    }
                }

                //if redstone is beneath and a connection can be made (e.g. the block above it is
                //not opaque) then we found new redstone to connect too
                if (stopsRedstoneWire(area, startPos.down())
                        && stopsRedstoneWire(area, pos) == false
                        && utility.cordGet(area, pos.down()).getBlock().equals(Blocks.REDSTONE_WIRE)
                        && positionsAlreadyFound.contains(pos.down()) == false) {
                    newPositionsToVisit.add(pos.down());
                    positionsAlreadyFound.add(pos.down());
                }

            }


            //We check for weak powered blocks we point into
            //also piston are powered the same way, so we check those while we are at it
            //update: found a better way, no longer spagetthi code, nice
            BlockState startPosBlockState = utility.cordGet(area, startPos);
            //if there is some kind of connection, aka NOT NOTHING
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                Property directionProperty = getRedstoneDirectionPropertyByDirection(direction);
                //if the redstone Dust point in that direction for some reason, our scans begin
                if (startPosBlockState.get(directionProperty).equals(RedstoneSide.NONE) == false) {
                    if (stopsRedstoneWire(utility.cordGet(area, startPos.add(direction.getDirectionVec())))) {
                        edges.addAll(weakPowerSearch(area, startPos.add(direction.getDirectionVec()), source, signalPower));
                    }
                    //block could be a piston. not connecting if It points right at us
                    else {
                        if (isSomeKindOfPiston(utility.cordGet(area, startPos.add(direction.getDirectionVec())))
                                //not connecting if it points right at us
                                && utility.cordGet(area, startPos.add(direction.getDirectionVec())).get(FACING).equals(direction.getOpposite()) == false) {
                            edges.add(new Edge(source,
                                    buildCircuitObjectByCoord(area, startPos.add(direction.getDirectionVec())),
                                    15 - signalPower));
                        }
                        //these BUD ones dont care if they are pointing in the direction of the signal
                        if (isSomeKindOfPiston(utility.cordGet(area, startPos.down().add(direction.getDirectionVec())))) {
                            edges.add(new Edge(source,
                                    buildCircuitObjectByCoord(area, startPos.down().add(direction.getDirectionVec())),
                                    15 - signalPower));
                        }
                    }
                }
            }


            //find stuff about blocks beneath out if the block below startPose
            //only opaque blocks get "weak powered" so only that is relevant here
            if (stopsRedstoneWire(area, startPos.down())) {
                edges.addAll(weakPowerSearch(area, startPos.down(), source, signalPower));
                if (utility.cordGet(area, startPos.down()).getBlock().equals(Blocks.BLUE_WOOL)) {
                    edges.add(new Edge(source, new CircuitObject(CircuitObjectType.OUTPUT, startPos.down()),
                            15 - signalPower)
                    );
                }
            } else {
                if (isSomeKindOfPiston(utility.cordGet(area, startPos.down()))) {
                    edges.add(new Edge(source,
                            buildCircuitObjectByCoord(area, startPos.down()),
                            15 - signalPower));
                }
                if (isSomeKindOfPiston(utility.cordGet(area, startPos.down().down()))) {
                    edges.add(new Edge(source,
                            buildCircuitObjectByCoord(area, startPos.down().down()),
                            15 - signalPower));
                }
            }

        }

        edges.addAll(redstoneEdgeSearch(source, area, newPositionsToVisit, positionsAlreadyFound, --signalPower));

        return edges;
    }



    private Set<Edge> repeaterTypeObjectsEdgeSearch(CircuitObject source, BlockState[][][] area) {
        Set<Edge> edges = new HashSet<>();

        BlockPos posComparitorPointsTo = source.blockPos.add(
                utility.cordGet(area, source.blockPos).get(HORIZONTAL_FACING).getOpposite().getDirectionVec());
        BlockState blockStateComparitorPointsTo = utility.cordGet(area, posComparitorPointsTo);
        //if the repeater points into redstone, start a search at said redstone with signal 15
        if (blockStateComparitorPointsTo.getBlock().equals(Blocks.REDSTONE_WIRE)) {
            edges.addAll( redstoneEdgeSearch(source
                    , area
                    , new HashSet<>(Arrays.asList(posComparitorPointsTo))
                    , new HashSet<>(Arrays.asList(posComparitorPointsTo))
                    , 15)
            );
        }
        //if we point into an opaque block, scan that one with the help of strongPowerSearch
        else if (stopsRedstoneWire(area, posComparitorPointsTo)) {
            edges.addAll( strongPowerSearch(area, posComparitorPointsTo, source, 15));
        }
        //if we point into an repeater
        else if (blockStateComparitorPointsTo.getBlock().equals(Blocks.REPEATER)) {
            //with the same direction as us, that is our one and only edge
            if (blockStateComparitorPointsTo.get(HORIZONTAL_FACING).getDirectionVec().equals(
                    utility.cordGet(area, source.blockPos).get(HORIZONTAL_FACING).getDirectionVec()
            )) {
                edges.add(new RepeaterEdge(source,
                        new CircuitObjectRepeater(posComparitorPointsTo
                                , blockStateComparitorPointsTo.get(DELAY_1_4)),
                        false,
                        0)
                );
            }
            //else, if its sideways we "lock it"
            else if (sideways(blockStateComparitorPointsTo.get(HORIZONTAL_FACING),
                    utility.cordGet(area, source.blockPos).get(HORIZONTAL_FACING))) {
                edges.add(new RepeaterEdge(source,
                        new CircuitObjectRepeater(posComparitorPointsTo
                                , blockStateComparitorPointsTo.get(DELAY_1_4)),
                        true,
                        0)
                );
            } //if non of that that is the end, no edges to be found here

        }
        //if we point into an comparitor: (similar to repeater
        else if (blockStateComparitorPointsTo.getBlock().equals(Blocks.COMPARATOR)) {
            //if it points in the same direction as us, that is our one and only edge
            if (blockStateComparitorPointsTo.get(HORIZONTAL_FACING).getDirectionVec().equals(
                    utility.cordGet(area, source.blockPos).get(HORIZONTAL_FACING).getDirectionVec()
            )) {
                edges.add(new ComparatorEdge(source,
                        new CircuitObjectComparitor(posComparitorPointsTo,
                                blockStateComparitorPointsTo.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                        false,
                        0)
                );
            }
            //if we go into it sideways we are at its subtraction spot, still an edge
            else if (sideways(blockStateComparitorPointsTo.get(HORIZONTAL_FACING),
                    utility.cordGet(area, source.blockPos).get(HORIZONTAL_FACING))) {
                edges.add(new ComparatorEdge(source,
                        new CircuitObjectComparitor(posComparitorPointsTo,
                                blockStateComparitorPointsTo.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                        true,
                        0)
                );
            }
            //if its pointing at us, no connections to be made yet
        }

        //we could still point into pistons directly, or less directly
        if (isSomeKindOfPiston(blockStateComparitorPointsTo)
                && blockStateComparitorPointsTo.get(FACING).equals(
                utility.cordGet(area, source.blockPos).get(HORIZONTAL_FACING)) == false){
            edges.add(new Edge(source,
                    buildCircuitObjectByCoord(area, posComparitorPointsTo),
                    0));
        }

        //will power this. yes it looks weird. Yes I tried, it does
        if (isSomeKindOfPiston(utility.cordGet(area, posComparitorPointsTo.down()))){
            edges.add(new Edge(source,
                    buildCircuitObjectByCoord(area, posComparitorPointsTo.down()),
                    0));
        }
        //if its non of those we are not getting our signal much further, returning empty set
        return edges;
    }

    //finds all CircuitObjects affected by the weakPowering of a given Block (at blockpos) in an area,
    //turns them into edges
    private Set<Edge> weakPowerSearch(BlockState[][][] area, BlockPos blockPos, CircuitObject source, int signalPower) {

        Set<Edge> edges = new HashSet<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            //convenience
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = utility.cordGet(area, pos);

            //connect to any repeater right next to us, given that it points away from us
            if (blockStateAtPos.getBlock().equals(Blocks.REPEATER) &&
                    blockStateAtPos.get(HORIZONTAL_FACING).getOpposite().equals(direction)) {
                edges.add(new RepeaterEdge(
                        source,
                        new CircuitObjectRepeater(pos,
                                blockStateAtPos.get(DELAY_1_4)),
                        false,
                        15 - signalPower
                ));
            }
            //connect to any comparitor right next to us, given that it points away from us
            else if (blockStateAtPos.getBlock().equals(Blocks.COMPARATOR) &&
                    blockStateAtPos.get(HORIZONTAL_FACING).getOpposite().equals(direction)) {
                edges.add(new ComparatorEdge(
                        source,
                        new CircuitObjectComparitor(pos,
                                blockStateAtPos.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                        false,
                        15 - signalPower
                ));
            }
        }

        //looking for torches or wallTorches we power
        //also, pistons
        for (Direction direction : Direction.values()) {
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = utility.cordGet(area, pos);


            if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_TORCH)
                    && direction.equals(Direction.UP)) {
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.TORCH, pos), 15 - signalPower));
                //debug
            } else if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WALL_TORCH)
                    && direction.equals(blockStateAtPos.get(HORIZONTAL_FACING))) {
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.TORCH, pos), 15 - signalPower));
            }

            //looking for pistons. This is tricky
            //piston checks, yay!
            else {
                if (isSomeKindOfPiston(blockStateAtPos)
                        && blockStateAtPos.get(FACING).equals(direction.getOpposite()) == false) {
                    edges.add(new Edge(source,
                            buildCircuitObjectByCoord(area, pos),
                            15 - signalPower));
                }
                if (isSomeKindOfPiston(utility.cordGet(area, pos.down()))) {
                    //indirect edge
                    edges.add(new Edge(source,
                            buildCircuitObjectByCoord(area, pos.down()),
                            15 - signalPower,
                            EdgeType.powering));
                }
            }

        }
        return edges;
    }

    private Set<Edge> strongPowerSearch(BlockState[][][] area, BlockPos blockPos, CircuitObject source, int signalPower) {

        Set<Edge> edges = new HashSet<>();
        Set<BlockPos> positionsToVisit = new HashSet<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            //convenience
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = utility.cordGet(area, pos);

            //connect to any repeater right next to us, given that it points away from us
            if (blockStateAtPos.getBlock().equals(Blocks.REPEATER) &&
                    blockStateAtPos.get(HORIZONTAL_FACING).getOpposite().equals(direction)) {
                edges.add(new RepeaterEdge(
                        source,
                        new CircuitObjectRepeater(pos,
                                blockStateAtPos.get(DELAY_1_4)),
                        false,
                        15 - signalPower
                ));
            }
            //connect to any comparitor right next to us, given that it points away from us
            else if (blockStateAtPos.getBlock().equals(Blocks.COMPARATOR) &&
                    blockStateAtPos.get(HORIZONTAL_FACING).getOpposite().equals(direction)) {
                edges.add(new ComparatorEdge(
                        source,
                        new CircuitObjectComparitor(pos,
                                blockStateAtPos.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                        false,
                        15 - signalPower
                ));
            }
        }

        for (Direction direction : Direction.values()) {
            //pos is where we look for connections. Dont use blockpos here, that would be an annoying bug
            //to track down. So dont do it.
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = utility.cordGet(area, pos);

            if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WIRE)) {
                positionsToVisit.add(pos);
            } else if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_TORCH)
                    && direction.equals(Direction.UP)) {
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.TORCH, pos), 15 - signalPower));
            } else if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WALL_TORCH)
                    && direction.equals(blockStateAtPos.get(HORIZONTAL_FACING))) {
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.TORCH, pos), 15 - signalPower));
            }

            //looking for pistons. This is tricky
            //piston checks, yay!
            else {
                if (isSomeKindOfPiston(blockStateAtPos)
                        && blockStateAtPos.get(FACING).equals(direction.getOpposite()) == false) {
                    edges.add(new Edge(source,
                            buildCircuitObjectByCoord(area, pos),
                            15 - signalPower));
                }
                if (isSomeKindOfPiston(utility.cordGet(area, pos.down()))) {
                    //indirect edge
                    edges.add(new Edge(source,
                            buildCircuitObjectByCoord(area, pos.down()),
                            15 - signalPower,
                            EdgeType.powering));
                }
            }
        }

        Set<BlockPos> positionsAlreadyFound = new HashSet<>(positionsToVisit);
        edges.addAll(redstoneEdgeSearch(source, area, positionsToVisit, positionsAlreadyFound, signalPower));

        return edges;
    }

    private Set<Edge> torchEdgeSearch(BlockState[][][] area, BlockPos blockPos, CircuitObject source, int signalPower) {

        //doesnt include above us.
        Set<Edge> edges = new HashSet<>();
        Set<BlockPos> positionsToVisit = new HashSet<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {

            //convenience
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = utility.cordGet(area, pos);

            //connect to any repeater right next to us, given that it points away from us
            if (blockStateAtPos.getBlock().equals(Blocks.REPEATER) &&
                    blockStateAtPos.get(HORIZONTAL_FACING).getOpposite().equals(direction)) {
                edges.add(new RepeaterEdge(
                        source,
                        new CircuitObjectRepeater(pos,
                                blockStateAtPos.get(DELAY_1_4)),
                        false,
                        15 - signalPower
                ));
            }
            //connect to any comparitor right next to us, given that it points away from us
            else if (blockStateAtPos.getBlock().equals(Blocks.COMPARATOR) &&
                    blockStateAtPos.get(HORIZONTAL_FACING).getOpposite().equals(direction)) {
                edges.add(new ComparatorEdge(
                        source,
                        new CircuitObjectComparitor(pos,
                                blockStateAtPos.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                        false,
                        15 - signalPower
                ));
            }
        }

        for (Direction direction : Direction.values()) {
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = utility.cordGet(area, pos);

            if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WIRE)) {
                positionsToVisit.add(pos);
            }

            //looking for pistons. This is tricky
            //piston checks, yay!
            else {
                if (isSomeKindOfPiston(blockStateAtPos)
                        && blockStateAtPos.get(FACING).equals(direction.getOpposite()) == false) {
                    edges.add(new Edge(source,
                            buildCircuitObjectByCoord(area, pos),
                            15 - signalPower));
                }
                if (isSomeKindOfPiston(utility.cordGet(area, pos.down()))) {
                    //indirect edge
                    edges.add(new Edge(source,
                            buildCircuitObjectByCoord(area, pos.down()),
                            15 - signalPower,
                            EdgeType.both)); //torches always update those pistons
                }
            }
        }

        //we must do this because in redstoneEdgeSearch() we loop through positionsToVisit while changing
        //positionsAlreadyFound.
        Set<BlockPos> positionsAlreadyFound = new HashSet<>(positionsToVisit);
        edges.addAll(redstoneEdgeSearch(source, area, positionsToVisit, positionsAlreadyFound, signalPower));

        return edges;
    }

    private Set<Edge> leverTypeObjectEdgeSearch(BlockState[][][] area, BlockPos blockPos, CircuitObject source, int signalPower) {

        Set<Edge> edges = new HashSet<>();
        Set<BlockPos> positionsToVisit = new HashSet<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            //convenience
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = utility.cordGet(area, pos);

            //connect to any repeater right next to us, given that it points away from us
            if (blockStateAtPos.getBlock().equals(Blocks.REPEATER) &&
                    blockStateAtPos.get(HORIZONTAL_FACING).getOpposite().equals(direction)) {
                edges.add(new RepeaterEdge(
                        source,
                        new CircuitObjectRepeater(pos,
                                blockStateAtPos.get(DELAY_1_4)),
                        false,
                        15 - signalPower
                ));
            }
            //connect to any comparitor right next to us, given that it points away from us
            else if (blockStateAtPos.getBlock().equals(Blocks.COMPARATOR) &&
                    blockStateAtPos.get(HORIZONTAL_FACING).getOpposite().equals(direction)) {
                edges.add(new ComparatorEdge(
                        source,
                        new CircuitObjectComparitor(pos,
                                blockStateAtPos.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                        false,
                        15 - signalPower
                ));
            }
        }

        for (Direction direction : Direction.values()) {
            //pos is where we look for connections. Dont use blockpos here, that would be an annoying bug
            //to track down. So dont do it.
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = utility.cordGet(area, pos);

            if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WIRE)) {
                positionsToVisit.add(pos);
            }
            //this cant happen in the lever case, but also doesnt entail bad behaviour since
            //torched cant be attacked in this way to levers. So do we keep it in or out?
            //a just question
            else if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_TORCH)
                    && direction.equals(Direction.UP)) {
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.TORCH, pos), 15 - signalPower));
            } else if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WALL_TORCH)
                    && direction.equals(blockStateAtPos.get(HORIZONTAL_FACING))) {
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.TORCH, pos), 15 - signalPower));
            }

            //looking for pistons. This is tricky
            //piston checks, yay!
            else {
                if (isSomeKindOfPiston(blockStateAtPos)
                        && blockStateAtPos.get(FACING).equals(direction.getOpposite()) == false) {
                    edges.add(new Edge(source,
                            buildCircuitObjectByCoord(area, pos),
                            15 - signalPower));
                }
                if (isSomeKindOfPiston(utility.cordGet(area, pos.down()))) {
                    //indirect edge
                    edges.add(new Edge(source,
                            buildCircuitObjectByCoord(area, pos.down()),
                            15 - signalPower,
                            EdgeType.powering));
                }
            }
        }


        Set<BlockPos> positionsAlreadyFound = new HashSet<>(positionsToVisit);
        edges.addAll(redstoneEdgeSearch(source, area, positionsToVisit, positionsAlreadyFound, signalPower));

        return edges;
    }


    /*
     * useful helper methods are helpfull
     * */

    //copied from an internal method. I love it when its easy
    //but not that easy since its protected so I have to copy it here for it to work
    //so for the purposes of work I did on this thesis I guess this wasn't me
    //unless digging through unmapped methods is work, in which case yes, I found this
    private static Direction getFacing(BlockState blockState) {
        switch ((AttachFace) blockState.get(FACE)) {
            case CEILING:
                return Direction.DOWN;
            case FLOOR:
                return Direction.UP;
            default:
                return blockState.get(HORIZONTAL_FACING);
        }
    }

    private static List<BlockPos> findWhiteWool(BlockState[][][] area) {
        List<BlockPos> blockPosList = new ArrayList<>();
        for (int i = 0; i < area.length; i++) {
            for (int j = 0; j < area[0].length; j++) {
                for (int k = 0; k < area[0][0].length; k++) {
                    BlockState blockState = area[i][j][k];
                    if (blockState.getBlock() == Blocks.WHITE_WOOL) {
                        blockPosList.add(new BlockPos(i, j, k));
                    }
                }
            }
        }
        return blockPosList;
    }

    public static boolean stopsRedstoneWire(BlockState[][][] area, BlockPos blockPos) {
        return stopsRedstoneWire(utility.cordGet(area, blockPos));
    }

    //todo: make this better and more reliable
    //right now we are more or less guessing (given, its a good guess, but its not perfect as seen
    //in the exceptions at the start
    public static boolean stopsRedstoneWire(BlockState blockState) {
        //all blocks that disoboey "Opaque means you stop redstone, and only opaque does that"
        //are added as "exceptions" here. Maybe there is a smarter way to figure this out
        Block block = blockState.getBlock();
        if(block.equals(Blocks.PISTON)
        ||block.equals(Blocks.STICKY_PISTON)
        ||block.equals(Blocks.GLOWSTONE)){
            return false;
        }
        if(block.equals(Blocks.SLIME_BLOCK)){
            return true;
        }
        //most blocks follow "it doesnt stop redstone unless its opaque"
        return blockState.isOpaqueCube(new IBlockReader() {
                                           @Nullable
                                           @Override
                                           public TileEntity getTileEntity(BlockPos pos) {
                                               return null;
                                           }

                                           @Override
                                           public BlockState getBlockState(BlockPos pos) {
                                               return blockState;
                                           }

                                           @Override
                                           public FluidState getFluidState(BlockPos pos) {
                                               return null;
                                           }
                                       },
                new BlockPos(0, 0, 0));
    }

    private void test(BlockState[][][] area) {
        try {
            System.out.println("  normal: \n");
            setupRedstoneCircuitDiagram(area);
            statusReport();
            System.out.println("\n alternative: \n ");
            setupRedstoneCircuitDiagramALL(area);
            statusReport();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.toString());
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println("this didnt work out well...");
            return;
        }
    }

    private Set<CircuitObject> areaSearchRedstoneInputs(BlockState[][][] area) {
        Set<CircuitObject> redstoneInputs = new HashSet<>();
        for (int i = 0; i < area.length; i++) {
            for (int j = 0; j < area[0].length; j++) {
                for (int k = 0; k < area[0][0].length; k++) {
                    BlockState blockState = area[i][j][k];
                    try {
                        redstoneInputs.add(buildRedstoneInputByCoord(area, new BlockPos(i, j, k)));
                    } catch (IllegalStateException e) {
                    }
                }
            }
        }
        return redstoneInputs;
    }

    private Set<CircuitObject> areaSearchCircuitObjects(BlockState[][][] area) {
        Set<CircuitObject> redstoneComponents = new HashSet<>();
        for (int i = 0; i < area.length; i++) {
            for (int j = 0; j < area[0].length; j++) {
                for (int k = 0; k < area[0][0].length; k++) {
                    BlockState blockState = area[i][j][k];
                    try {
                        CircuitObject circuitObject = buildCircuitObjectByCoord(area, new BlockPos(i, j, k));
                        //System.out.println("found something: "+circuitObject.toString());
                        redstoneComponents.add(circuitObject);
                    } catch (IllegalStateException e) {
                    }
                }
            }
        }
        return redstoneComponents;
    }

    private Property getRedstoneDirectionPropertyByDirection(Direction direction) {
        if (direction.equals(Direction.NORTH)) {
            return REDSTONE_NORTH;
        } else if (direction.equals(Direction.EAST)) {
            return REDSTONE_EAST;
        } else if (direction.equals(Direction.SOUTH)) {
            return REDSTONE_SOUTH;
        } else if (direction.equals(Direction.WEST)) {
            return REDSTONE_WEST;
        }
        throw new IllegalStateException("invelid direction for this: " + direction.toString());
    }

    private boolean isSomeKindOfPiston(BlockState blockState) {
        return blockState.getBlock().equals(Blocks.PISTON) || blockState.getBlock().equals(Blocks.STICKY_PISTON);
    }

    public boolean isSticky(BlockState blockState) {
        if (blockState.getBlock().equals(Blocks.PISTON))
            return false;
        else if (blockState.getBlock().equals(Blocks.STICKY_PISTON))
            return true;
        throw new IllegalStateException("not a Piston at all!");
    }

    private CircuitObject buildCircuitObjectByCoord(BlockState[][][] area, BlockPos blockPos) {
        BlockState blockStateAtPos = utility.cordGet(area, blockPos);
        Block blockAtPos = blockStateAtPos.getBlock();
        if (blockAtPos.equals(Blocks.PISTON)) {
            return new CircuitObject(CircuitObjectType.PISTON, blockPos);
        } else if (blockAtPos.equals(Blocks.STICKY_PISTON)) {
            return new CircuitObject(CircuitObjectType.STICKY_PISTON, blockPos);
        } else if (blockStateAtPos.getBlock().equals(Blocks.REPEATER)) {
            return new CircuitObjectRepeater(area, blockPos);
        } else if (blockStateAtPos.getBlock().equals(Blocks.WHITE_WOOL)) {
            return new CircuitObject(CircuitObjectType.INPUT, blockPos);
        } else if (blockStateAtPos.getBlock().equals(Blocks.BLUE_WOOL)) {
            return new CircuitObject(CircuitObjectType.OUTPUT, blockPos);
        } else if (blockStateAtPos.getBlock().equals(Blocks.COMPARATOR)) {
            return new CircuitObjectComparitor(area, blockPos);
        } else if (blockStateAtPos.getBlock().equals(Blocks.LEVER)) {
            return new CircuitObject(CircuitObjectType.LEVER, blockPos);
        } else if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_BLOCK)) {
            return new CircuitObject(CircuitObjectType.REDSTONE_BLOCK, blockPos);
        } else if (blockStateAtPos.getBlock() instanceof AbstractButtonBlock) {
            return new CircuitObject(CircuitObjectType.BUTTON, blockPos);
        } else if (blockStateAtPos.isIn(Blocks.REDSTONE_TORCH)
        || blockStateAtPos.isIn(Blocks.REDSTONE_WALL_TORCH)  ) {
            return new CircuitObject(CircuitObjectType.TORCH, blockPos);
        } else {
            throw new IllegalStateException("not implemented yet I guess");
        }
    }

    private CircuitObject buildRedstoneInputByCoord(BlockState[][][] area, BlockPos blockPos) {
        BlockState blockStateAtPos = utility.cordGet(area, blockPos);
        Block blockAtPos = blockStateAtPos.getBlock();
        if (blockStateAtPos.getBlock().equals(Blocks.WHITE_WOOL)) {
            return new CircuitObject(CircuitObjectType.INPUT, blockPos);
        } else if (blockStateAtPos.getBlock().equals(Blocks.LEVER)) {
            return new CircuitObject(CircuitObjectType.LEVER, blockPos);
        } else if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_BLOCK)) {
            return new CircuitObject(CircuitObjectType.REDSTONE_BLOCK, blockPos);
        } else if (blockStateAtPos.getBlock() instanceof AbstractButtonBlock) {
            return new CircuitObject(CircuitObjectType.BUTTON, blockPos);
        } else if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_TORCH)
                || blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WALL_TORCH)) {
            return new CircuitObject(CircuitObjectType.TORCH, blockPos);
        } else {
            throw new IllegalStateException("not implemented yet I guess");
        }
    }

    private void statusReport() {
        System.out.println("we have " + circuitObjects.size() + " circuitObjects and " + edgeSet.size() + " edgeSet");
        System.out.println("those would be: ");
        for (CircuitObject source : circuitObjects) {
            System.out.println(source.toString());
        }
        for (Edge edge : edgeSet) {
            System.out.println(edge.toString());
        }
    }

    private boolean sideways(Direction direction1, Direction direction2) {
        return (direction1 == direction2 || direction1.getOpposite() == direction2) == false;
    }

    private boolean isBlock(BlockState[][][] area, BlockPos pos, Block block) {
        return utility.cordGet(area, pos).getBlock().equals(block);
    }

    private boolean debugThingy(BlockState[][][] area, BlockPos blockPos) {
        System.out.println("name: " + utility.cordGet(area, blockPos).getBlock().getRegistryName().toString());
        System.out.println("isSolid: " + utility.cordGet(area, blockPos).isSolid());
        System.out.println("canProvidePower: " + utility.cordGet(area, blockPos).canProvidePower());
        boolean isFullBlock = utility.cordGet(area, blockPos).isNormalCube(new IBlockReader() {
            @Nullable
            @Override
            public TileEntity getTileEntity(BlockPos pos) {
                return null;
            }

            @Override
            public BlockState getBlockState(BlockPos pos) {
                return utility.cordGet(area, blockPos);
            }

            @Override
            public FluidState getFluidState(BlockPos pos) {
                return null;
            }
        }, blockPos);

        System.out.println("isFullBlock: " + isFullBlock);
        return isFullBlock && utility.cordGet(area, blockPos).isSolid();
    }

    //utility classes for our help

    //we may not need this. But maybe we do. But maybe we wont. Who knows :D
    public class StrongPowerResult {
        Set<Edge> edges;
        Set<BlockPos> redstonePositions;

        public StrongPowerResult(Set<Edge> edges, Set<BlockPos> redstonePositions) {
            this.edges = edges;
            this.redstonePositions = redstonePositions;
        }

        public Set<Edge> getEdges() {
            return edges;
        }

        public Set<BlockPos> getRedstonePositions() {
            return redstonePositions;
        }
    }
}

