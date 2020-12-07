import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
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
import static net.minecraft.state.properties.BlockStateProperties.COMPARATOR_MODE;

@Deprecated
public class SimpleScanner {
    //sources aka nodes
    private Set<CircuitObject> sources;
    //redstone wire connections, aka edgeSet
    private Set<Edge> edgeSet;

    //oh boy this will be a mess. or it was. Its better now
    public SimpleScanner(BlockState[][][] area) {
        try{
            setupRedstoneCircuitDiagram(area);
        } catch (Exception e){
            System.out.println( e.getMessage() );
            System.out.println(e.toString());
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println("this didnt work out well...");
            return;
        }
    }


    public Set<CircuitObject> getSources() {
        return sources;
    }

    public Set<Edge> getEdgeSet() {
        return edgeSet;
    }


    private void setupRedstoneCircuitDiagram(BlockState[][][] area){
        //all signal sources are "sources" and lead into other object via edgeSet
        //those object might be new sources (repeater, comparitor, torch,...)
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
        List<BlockPos> inputs = findWhiteWool(area);
        for (BlockPos blockPos : inputs) {
            System.out.println("found an input");
            CircuitObject c = new CircuitObject(CircuitObjectType.input, blockPos);
            sourcesToBeSearched.add(c);
            sources.add(c);
        }

        //workhorse right here
        //looks from one component to what it reaches, adds all that to be searched if we have not already
        while (sourcesToBeSearched.size() != 0) {
            CircuitObject startobject = sourcesToBeSearched.iterator().next();
            sourcesToBeSearched.remove(startobject);
            Set<Edge> newEdges = calculateEdges(startobject, area);
            Edges.addAll(newEdges);
            Set<CircuitObject> potentialNewSources = new HashSet<>();
            for (Edge edge : Edges) {
                potentialNewSources.add(edge.destination);
            }
            //we wouldnt want to loop, so we dont visit sources twice
            potentialNewSources.removeAll(sources);
            //sources to Be searched contains only those sources that were in potentialNewSources but not in sources
            sourcesToBeSearched.addAll(potentialNewSources);
            //sources saves all sources ever
            sources.addAll(potentialNewSources);
        }

        this.sources = sources;
        this.edgeSet = Edges;
    }


    private Set<Edge> calculateEdges(CircuitObject source, BlockState[][][] area) {
        Set<BlockPos> positionsToVisit = new HashSet<>();
        Set<Edge> edges = new HashSet<>();
        switch (source.circuitObjectType) {
            case Comparator:
                //find all Edges that start at this Comparator
            {//convenience stuff
                BlockPos posComparitorPointsTo = source.blockPos.add(
                        cordGet(area, source.blockPos).get(HORIZONTAL_FACING).getOpposite().getDirectionVec());
                BlockState blockStateComparitorPointsTo = cordGet(area, posComparitorPointsTo);
                //if the repeater points into redstone, start a search at said redstone with signal 15
                if (blockStateComparitorPointsTo.getBlock().equals(Blocks.REDSTONE_WIRE)) {
                    Set<BlockPos> blockPos = new HashSet<>(Arrays.asList(posComparitorPointsTo));
                    return redstoneEdgeSearch(source
                            , area
                            , new HashSet<>(Arrays.asList(posComparitorPointsTo))
                            , new HashSet<>(Arrays.asList(posComparitorPointsTo))
                            , 15);
                }
                //if we point into an opaque block, scan that one with the help of strongPowerSearch
                else if (isOpaqueCube(area, posComparitorPointsTo)) {
                    return strongPowerSearch(area, posComparitorPointsTo, source, 15);
                }
                //if we point into an repeater
                else if (blockStateComparitorPointsTo.getBlock().equals(Blocks.REPEATER)) {
                    //with the same direction as us, that is our one and only edge
                    if (blockStateComparitorPointsTo.get(HORIZONTAL_FACING).getDirectionVec().equals(
                            cordGet(area, source.blockPos).get(HORIZONTAL_FACING).getDirectionVec()
                    )) {
                        edges.add(new ComparitorEdge(source,
                                new CircuitObjectRepeater(posComparitorPointsTo
                                        , blockStateComparitorPointsTo.get(DELAY_1_4)),
                                false,
                                0)
                        );
                        return edges;
                    }
                    //else, if its sideways we "lock it"
                    else if (sideways(blockStateComparitorPointsTo.get(HORIZONTAL_FACING),
                            cordGet(area, source.blockPos).get(HORIZONTAL_FACING))) {
                        edges.add(new ComparitorEdge(source,
                                new CircuitObjectRepeater(posComparitorPointsTo
                                        , blockStateComparitorPointsTo.get(DELAY_1_4)),
                                true,
                                0)
                        );
                        return edges;
                    } else
                        //if non of that that is the end, no edges for us
                        return new HashSet<>();
                }
                //if we point into an comparitor: (similar to repeater
                else if (blockStateComparitorPointsTo.getBlock().equals(Blocks.COMPARATOR)) {
                    //if it points in the same direction as us, that is our one and only edge
                    if (blockStateComparitorPointsTo.get(HORIZONTAL_FACING).getDirectionVec().equals(
                            cordGet(area, source.blockPos).get(HORIZONTAL_FACING).getDirectionVec()
                    )) {
                        edges.add(new ComparitorEdge(source,
                                new CircuitObjectComparitor(posComparitorPointsTo,
                                        blockStateComparitorPointsTo.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                                false,
                                0)
                        );
                        return edges;
                    }
                    //if we go into it sideways we are at its subtraction spot, still an edge
                    else if (sideways(blockStateComparitorPointsTo.get(HORIZONTAL_FACING),
                            cordGet(area, source.blockPos).get(HORIZONTAL_FACING))) {
                        edges.add(new ComparitorEdge(source,
                                new CircuitObjectComparitor(posComparitorPointsTo,
                                        blockStateComparitorPointsTo.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                                true,
                                0)
                        );
                        return edges;
                    }
                    //if its pointing at us, no connections to be made, return empty set
                    else {
                        return new HashSet<>();
                    }
                } else
                    //if its non of those we are not getting our signal much further, returning empty set
                    return new HashSet<>();
            }
            case Repeater:
                //find all Edges that start at this repeater
            {//convenience stuff
                BlockPos posRepeaterPointsTo = source.blockPos.add(
                        cordGet(area, source.blockPos).get(HORIZONTAL_FACING).getOpposite().getDirectionVec());
                BlockState blockStateRepeaterPointsTo = cordGet(area, posRepeaterPointsTo);
                //if the repeater points into redstone, start a search at said redstone with signal 15
                if (blockStateRepeaterPointsTo.getBlock().equals(Blocks.REDSTONE_WIRE)) {
                    Set<BlockPos> blockPos = new HashSet<>(Arrays.asList(posRepeaterPointsTo));
                    return redstoneEdgeSearch(source
                            , area
                            , new HashSet<BlockPos>(Arrays.asList(posRepeaterPointsTo))
                            , new HashSet<BlockPos>(Arrays.asList(posRepeaterPointsTo))
                            , 15);
                }
                //if we point into an opaque block, scan that one with the help of strongPowerSearch
                else if (isOpaqueCube(area, posRepeaterPointsTo)) {
                    return strongPowerSearch(area, posRepeaterPointsTo, source, 15);
                }
                //if we point into an repeater
                else if (blockStateRepeaterPointsTo.getBlock().equals(Blocks.REPEATER)) {
                    //with the same direction as us, that is our one and only edge
                    if (blockStateRepeaterPointsTo.get(HORIZONTAL_FACING).getDirectionVec().equals(
                            cordGet(area, source.blockPos).get(HORIZONTAL_FACING).getDirectionVec()
                    )) {
                        edges.add(new RepeaterEdge(source,
                                new CircuitObjectRepeater(posRepeaterPointsTo
                                        , blockStateRepeaterPointsTo.get(DELAY_1_4)),
                                false,
                                0)
                        );
                        return edges;
                    }
                    //else, if its sideways we "lock it"
                    else if (sideways(blockStateRepeaterPointsTo.get(HORIZONTAL_FACING),
                            cordGet(area, source.blockPos).get(HORIZONTAL_FACING))) {
                        edges.add(new RepeaterEdge(source,
                                new CircuitObjectRepeater(posRepeaterPointsTo
                                        , blockStateRepeaterPointsTo.get(DELAY_1_4)),
                                true,
                                0)
                        );
                        return edges;
                    } else
                        //if non of that that is the end, no edges for us
                        return new HashSet<>();
                }
                //if we point into an comparitor: (similar to repeater
                else if (blockStateRepeaterPointsTo.getBlock().equals(Blocks.COMPARATOR)) {
                    //if it points in the same direction as us, that is our one and only edge
                    if (blockStateRepeaterPointsTo.get(HORIZONTAL_FACING).getDirectionVec().equals(
                            cordGet(area, source.blockPos).get(HORIZONTAL_FACING).getDirectionVec()
                    )) {
                        edges.add(new ComparitorEdge(source,
                                new CircuitObjectComparitor(posRepeaterPointsTo,
                                        blockStateRepeaterPointsTo.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                                false,
                                0)
                        );
                        return edges;
                    }
                    //if we go into it sideways we are at its subtraction spot, still an edge
                    else if (sideways(blockStateRepeaterPointsTo.get(HORIZONTAL_FACING),
                            cordGet(area, source.blockPos).get(HORIZONTAL_FACING))) {
                        edges.add(new RepeaterEdge(source,
                                new CircuitObjectComparitor(posRepeaterPointsTo,
                                        blockStateRepeaterPointsTo.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                                true,
                                0)
                        );
                        return edges;
                    }
                    //if its pointing at us, no connections to be made, return empty set
                    else {
                        return new HashSet<>();
                    }
                } else
                    //if its non of those we are not getting our signal much further, returning empty set
                    return new HashSet<>();
            }
            case RedstoneBlock:
            case input:
                return strongPowerSearch(area, source.blockPos, source, 15);
            case torch:
                //the torch hard powers the block above if it is opaque
                if(isOpaqueCube(area, source.blockPos.up())){
                    edges.addAll(strongPowerSearch(area,source.blockPos.up(),source,15));
                }
                //we could do strong power search here, but that messes up if we are on the BACKSIDE OF A REPEATER
                //welp... do we do it anyways and leave the problems for future Ludwig?
                //got it: we put it into a method that is the same, but different name, so
                //I will change it later when it comes to repeaters. OR AM I??
                edges.addAll(torchPowerSearch(area, source.blockPos,source,15));

                break;
            case Lever:
                Direction directionOfLever = getFacing(cordGet(area, source.blockPos));
                BlockPos posOfBlockLeverIsAttachedTo = source.blockPos.add(directionOfLever.getDirectionVec());
                if(isOpaqueCube(area, posOfBlockLeverIsAttachedTo)){
                    edges.addAll(strongPowerSearch(area, posOfBlockLeverIsAttachedTo,source, 15));
                }
                edges.addAll(leverPowerSearch(area, source.blockPos,source, 15));
                return edges;
            case output:
            case piston:
            case stickyPiston:
                return new HashSet<>();

        }
        edges.addAll(redstoneEdgeSearch(source, area, positionsToVisit, positionsToVisit, 15));
        return edges;
    }


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
                BlockState blockStateAtPos = cordGet(area, pos);
                //connect to all redstone that is right next to us
                if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WIRE)
                        && positionsAlreadyFound.contains(pos) == false) {
                    newPositionsToVisit.add(pos);
                    positionsAlreadyFound.add(pos);
                }
                //connect to any repeater right next to us, given that it points away from us
                else if (blockStateAtPos.getBlock().equals(Blocks.REPEATER) &&
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
                    edges.add(new ComparitorEdge(
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
                    edges.add(new ComparitorEdge(
                            source,
                            new CircuitObjectComparitor(pos,
                                    blockStateAtPos.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                            true,
                            15 - signalPower
                    ));
                }

                //todo: weak power blocks we point into
//                else if (stopsRedstoneWire(blockStateAtPos
//                        && cordGet(area, startPos).get(R)
//                        )){
//
//                }

                //do stuff to the blocks above pos if and only if the block above us is not Opaque
                //(Opaque blocks cut off and block all redstone signals we care about)
                if (isOpaqueCube(cordGet(area, startPos.up())) == false) {
                    if ( cordGet(area, pos.up()).getBlock().equals(Blocks.REDSTONE_WIRE)
                            && positionsAlreadyFound.contains(pos.up()) == false) {
                        newPositionsToVisit.add(pos.up());
                        positionsAlreadyFound.add(pos.up());
                    }
                }

                //if redstone is beneath and a connection can be made (e.g. the block above it is
                //not opaque) then we found new redstone to connect too
                if (isOpaqueCube(area, startPos.down())
                        && isOpaqueCube(area, pos) == false
                        && cordGet(area, pos.down()).getBlock().equals(Blocks.REDSTONE_WIRE)
                        && positionsAlreadyFound.contains(pos.down()) == false) {
                    newPositionsToVisit.add(pos.down());
                    positionsAlreadyFound.add(pos.down());
                }

            }



            //hardcoded until I find a better way to do this. We check for weak powered blocks we
            //point into
            BlockState startPosBlockState = cordGet(area, startPos);
            //if there is some kind of connection, aka NOT NOTHING
            if(startPosBlockState.get(REDSTONE_NORTH).equals(RedstoneSide.SIDE)
                    && isOpaqueCube(cordGet(area, startPos.north()))){
                edges.addAll(weakPowerSearch(area, startPos.north(),source,signalPower));
            }
            if(startPosBlockState.get(REDSTONE_EAST).equals(RedstoneSide.SIDE)
                    && isOpaqueCube(cordGet(area, startPos.east()))){
                edges.addAll(weakPowerSearch(area, startPos.east(),source,signalPower));
            }
            if(startPosBlockState.get(REDSTONE_SOUTH).equals(RedstoneSide.SIDE)
                    && isOpaqueCube(cordGet(area, startPos.south()))){
                edges.addAll(weakPowerSearch(area, startPos.south(),source,signalPower));
            }
            if(startPosBlockState.get(REDSTONE_WEST).equals(RedstoneSide.SIDE)
                    && isOpaqueCube(cordGet(area, startPos.west()))){
                edges.addAll(weakPowerSearch(area, startPos.west(),source,signalPower));
            }

//            //DEBUG
//            System.out.println("blockPos: "+startPos);
//
//            System.out.println("North: "+startPosBlockState.get(REDSTONE_NORTH));
//            System.out.println("East: "+startPosBlockState.get(REDSTONE_EAST));
//            System.out.println("Sout: "+startPosBlockState.get(REDSTONE_SOUTH));
//            System.out.println("West: "+startPosBlockState.get(REDSTONE_WEST));
//            //DEBUG END

            //find stuff about blocks beneath out if the block below startPose
            //only opaque blocks get "weak powered" so only that is relevant here
            if (isOpaqueCube(area, startPos.down())) {
                edges.addAll(weakPowerSearch(area, startPos.down(), source, signalPower));
                if(cordGet(area, startPos.down()).getBlock().equals(Blocks.BLUE_WOOL)){
                    edges.add(new Edge(source,new CircuitObject(CircuitObjectType.output,startPos.down()),
                            15-signalPower)
                    );
                }
            }
        }

        edges.addAll(redstoneEdgeSearch(source, area, newPositionsToVisit, positionsAlreadyFound, --signalPower));

        return edges;
    }

    //finds all CircuitObjects affected by the weakPowering of a given Block (at blockpos) in an area,
    //turns them into edges
    private Set<Edge> weakPowerSearch(BlockState[][][] area, BlockPos blockPos, CircuitObject source, int signalPower) {

        Set<Edge> edges = new HashSet<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            //convenience
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = cordGet(area, pos);

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
                edges.add(new ComparitorEdge(
                        source,
                        new CircuitObjectComparitor(pos,
                                blockStateAtPos.get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT)),
                        false,
                        15 - signalPower
                ));
            }
        }

        //looking for torches or wallTorches we power
        for (Direction direction : Direction.values()) {
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = cordGet(area, pos);


            if(blockStateAtPos.getBlock().equals(Blocks.REDSTONE_TORCH)
                    && direction.equals(Direction.UP)){
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.torch,pos),15-signalPower));
                //debug
            }

            else if(blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WALL_TORCH)
                    && direction.equals(blockStateAtPos.get(HORIZONTAL_FACING))){
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.torch,pos),15-signalPower));
            }

        }

        //looking for pistons. This is tricky, because wheter a piston is soft or hard powered depends on where the
        //redstone goes into the block, which is not trivial to find out
        //so how do we do this?
        //maybe blockwise, calling redstone wire a circuit object?

        return edges;
    }

    private Set<Edge> strongPowerSearch(BlockState[][][] area, BlockPos blockPos, CircuitObject source, int signalPower) {

        Set<Edge> edges = new HashSet<>();
        Set<BlockPos> positionsToVisit = new HashSet<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            //convenience
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = cordGet(area, pos);

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
                edges.add(new ComparitorEdge(
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
            BlockState blockStateAtPos = cordGet(area, pos);

            if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WIRE)) {
                positionsToVisit.add(pos);
            }

            else if(blockStateAtPos.getBlock().equals(Blocks.REDSTONE_TORCH)
                    && direction.equals(Direction.UP)){
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.torch,pos),15-signalPower));
            }

            else if(blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WALL_TORCH)
                    && direction.equals(blockStateAtPos.get(HORIZONTAL_FACING))){
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.torch,pos),15-signalPower));
            }
        }

        edges.addAll(redstoneEdgeSearch(source, area, positionsToVisit, positionsToVisit, signalPower));

        return edges;
    }

    private Set<Edge> torchPowerSearch(BlockState[][][] area, BlockPos blockPos, CircuitObject source, int signalPower) {

        //doesnt include above us.
        Set<Edge> edges = new HashSet<>();
        Set<BlockPos> positionsToVisit = new HashSet<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {

            //convenience
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = cordGet(area, pos);

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
                edges.add(new ComparitorEdge(
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
            BlockState blockStateAtPos = cordGet(area, pos);

            if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WIRE)) {
                positionsToVisit.add(pos);
            }
        }

        //todo: get pistons in there, they are powered in weird and misterious ways

        //we must do this because in redstoneEdgeSearch() we loop through positionsToVisit while changing
        //positionsAlreadyFound.
        Set<BlockPos> positionsAlreadyFound = new HashSet<>(positionsToVisit);
        edges.addAll(redstoneEdgeSearch(source, area, positionsToVisit, positionsAlreadyFound, signalPower));

        return edges;
    }

    //no difference to strongPowerSearch yet, but those may come in the future, who knows.
    //maybe pistons react in different ways to powered blocks or powered levers
    private Set<Edge> leverPowerSearch(BlockState[][][] area, BlockPos blockPos, CircuitObject source, int signalPower) {

        Set<Edge> edges = new HashSet<>();
        Set<BlockPos> positionsToVisit = new HashSet<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            //convenience
            BlockPos pos = blockPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = cordGet(area, pos);

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
                edges.add(new ComparitorEdge(
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
            BlockState blockStateAtPos = cordGet(area, pos);

            if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WIRE)) {
                positionsToVisit.add(pos);
            }
            //this cant happen in the lever case, but also doesnt entail bad behaviour since
            //torched cant be attacked in this way to levers. So do we keep it in or out?
            //a just question
            else if(blockStateAtPos.getBlock().equals(Blocks.REDSTONE_TORCH)
                    && direction.equals(Direction.UP)){
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.torch,pos),15-signalPower));
            }

            else if(blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WALL_TORCH)
                    && direction.equals(blockStateAtPos.get(HORIZONTAL_FACING))){
                edges.add(new Edge(source, new CircuitObject(CircuitObjectType.torch,pos),15-signalPower));
            }
        }

        edges.addAll(redstoneEdgeSearch(source, area, positionsToVisit, positionsToVisit, signalPower));

        return edges;
    }



    //small, useful utility methods

    //copied from an internal method. I love it when its easy
    //but not that easy since its protected so I have to copy it here for it to work
    //so for the purposes of work I did on this thesis I guess this wasn't me
    //unless digging through unmapped methods is work, in which case yes, I found this
    protected static Direction getFacing(BlockState blockState) {
        switch((AttachFace)blockState.get(FACE)) {
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

    private static boolean isOpaqueCube(BlockState[][][] area, BlockPos blockPos) {
        return cordGet(area, blockPos).isOpaqueCube(new IBlockReader() {
                                                        @Nullable
                                                        @Override
                                                        public TileEntity getTileEntity(BlockPos pos) {
                                                            return null;
                                                        }

                                                        @Override
                                                        public BlockState getBlockState(BlockPos pos) {
                                                            return cordGet(area, blockPos);
                                                        }

                                                        @Override
                                                        public FluidState getFluidState(BlockPos pos) {
                                                            return null;
                                                        }
                                                    },
                blockPos);
    }

    private static boolean isOpaqueCube(BlockState blockState) {
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

    private static BlockState cordGet(BlockState[][][] area, BlockPos pos) {
        try {
            return area[pos.getX()][pos.getY()][pos.getZ()];
        }
        catch (ArrayIndexOutOfBoundsException e){
            //tried getting something out of area, everything around us is air
            return Blocks.AIR.getDefaultState();
        }
    }

    public void statusReport() {
        System.out.println("we have " + sources.size() + " sources and " + edgeSet.size() + " edgeSet");
        System.out.println("those would be: ");
        for (CircuitObject source : sources) {
            System.out.println(source.descibeYourself());
        }
        for (Edge edge : edgeSet) {
            System.out.println("edge from " + edge.source.descibeYourself() +
                    " to " + edge.destination.descibeYourself() +
                    " distance: " + edge.distance
            );
        }
    }

    private boolean sideways(Direction direction1, Direction direction2) {
        return (direction1 == direction2 || direction1.getOpposite() == direction2) == false;
    }

    //methods not in use

    private boolean isBlock(BlockState[][][] area, BlockPos pos, Block block) {
        return cordGet(area, pos).getBlock().equals(block);
    }

    private CircuitObject build(BlockState[][][] area, BlockPos blockPos) {
        BlockState blockState = (cordGet(area, blockPos));
        if (blockState.getBlock().equals(Blocks.REPEATER)) {
            return new CircuitObjectRepeater(area, blockPos);
        } else if (blockState.getBlock().equals(Blocks.WHITE_WOOL)) {
            return new CircuitObject(CircuitObjectType.input, blockPos);
        } else if (blockState.getBlock().equals(Blocks.BLUE_WOOL)) {
            return new CircuitObject(CircuitObjectType.output, blockPos);
        } else if (blockState.getBlock().equals(Blocks.COMPARATOR)) {
            return new CircuitObjectComparitor(area, blockPos);
        } else {
            throw new IllegalStateException("not implemented yet I guess");
        }
    }

    private boolean debugThingy(BlockState[][][] area, BlockPos blockPos) {
        System.out.println("name: " + cordGet(area, blockPos).getBlock().getRegistryName().toString());
        System.out.println("isSolid: " + cordGet(area, blockPos).isSolid());
        System.out.println("canProvidePower: " + cordGet(area, blockPos).canProvidePower());
        boolean isFullBlock = cordGet(area, blockPos).isNormalCube(new IBlockReader() {
            @Nullable
            @Override
            public TileEntity getTileEntity(BlockPos pos) {
                return null;
            }

            @Override
            public BlockState getBlockState(BlockPos pos) {
                return cordGet(area, blockPos);
            }

            @Override
            public FluidState getFluidState(BlockPos pos) {
                return null;
            }
        }, blockPos);

        System.out.println("isFullBlock: " + isFullBlock);
        return isFullBlock && cordGet(area, blockPos).isSolid();
    }

    //utility classes for our help

    enum CircuitObjectType {
        Comparator,
        Repeater,
        torch,
        piston,
        stickyPiston,
        input, /*white wool*/
        output, /*blue wool*/
        RedstoneBlock,
        Lever,
//        //might want this for rewrite. Maybe not. Who knows?
//        RedstoneWire
    }

    enum EdgeType {
        powering,
        updating,
        //having both can be functionally different from having one of each (update order nightmares)
        both
    }

    public class Edge {
        EdgeType edgeType=EdgeType.both;
        CircuitObject source;
        CircuitObject destination;
        int distance;

        public Edge(CircuitObject source, CircuitObject destination, int distance) {
            this.source = source;
            this.destination = destination;
            this.distance = distance;
        }

        public void setEdgeType(EdgeType edgeType) {
            this.edgeType = edgeType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return distance == edge.distance &&
                    source.equals(edge.source) &&
                    destination.equals(edge.destination);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, destination, distance);
        }
    }

    public class RepeaterEdge extends Edge {
        boolean isAtLockPossition; /*true if input is at lock-possition*/

        public RepeaterEdge(CircuitObject source, CircuitObject destination, boolean isAtLockPossition, int distance) {
            super(source, destination, distance);
            this.isAtLockPossition = isAtLockPossition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            RepeaterEdge that = (RepeaterEdge) o;
            return isAtLockPossition == that.isAtLockPossition;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), isAtLockPossition);
        }
    }

    public class ComparitorEdge extends Edge {
        boolean isAtSubtractspot; /*true if the edge goes into the subtract possition*/

        public ComparitorEdge(CircuitObject source, CircuitObject destination,
                              boolean isAtSubtractspot, int distance) {
            super(source, destination, distance);
            this.isAtSubtractspot = isAtSubtractspot;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            ComparitorEdge that = (ComparitorEdge) o;
            return isAtSubtractspot == that.isAtSubtractspot;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), isAtSubtractspot);
        }
    }

    public class CircuitObject {
        final CircuitObjectType circuitObjectType;
        final BlockPos blockPos;

        //        public CircuitObject(CircuitObjectType circuitObjectType) {
//            this.circuitObjectType = circuitObjectType;
//        }
        public CircuitObject(CircuitObjectType circuitObjectType, BlockPos blockPos) {
            this.circuitObjectType = circuitObjectType;
            this.blockPos = blockPos;
        }

        public String descibeYourself() {
            return (String.format("CircuitObject, pos: %d,%d,%d  type: %s", blockPos.getX(), blockPos.getY(), blockPos.getZ(), circuitObjectType.name()));
        }

        @Override
        public int hashCode() {
            return Objects.hash(circuitObjectType, blockPos.getX(), blockPos.getY(), blockPos.getY());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CircuitObject circuitObject = (CircuitObject) o;
            return circuitObject.circuitObjectType.equals(this.circuitObjectType) &&
                    this.blockPos.equals(circuitObject.blockPos);
        }
    }

    public class CircuitObjectRepeater extends CircuitObject {
        final int delay; /*1 to 4 depending on repeater setting, 1 being fastest*/

        public CircuitObjectRepeater(BlockPos blockPos, int state) {
            super(CircuitObjectType.Repeater, blockPos);
            this.delay = state;
        }

        public CircuitObjectRepeater(BlockState[][][] area, BlockPos blockPos) {
            super(CircuitObjectType.Repeater, blockPos);
            this.delay = cordGet(area, blockPos).get(DELAY_1_4);
        }


    }

    public class CircuitObjectComparitor extends CircuitObject {
        boolean subtracksMode;/*you already know, false if off*/

        public CircuitObjectComparitor(BlockPos blockPos, boolean subtracksMode) {
            super(CircuitObjectType.Comparator, blockPos);
            this.subtracksMode = subtracksMode;
        }

        public CircuitObjectComparitor(BlockState[][][] area, BlockPos blockPos) {
            super(CircuitObjectType.Comparator, blockPos);
            this.subtracksMode = cordGet(area, blockPos).get(COMPARATOR_MODE).equals(ComparatorMode.SUBTRACT);
        }

    }

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
