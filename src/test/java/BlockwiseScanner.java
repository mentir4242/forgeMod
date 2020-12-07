import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraft.state.properties.BlockStateProperties.*;
import static net.minecraft.state.properties.BlockStateProperties.COMPARATOR_MODE;

@Deprecated
public class BlockwiseScanner {

    //circuitObjects aka nodes
    private Set<CircuitObject> circuitObjects;
    //redstone wire connections, aka edgeSet
    private Set<Edge> edgeSet;

    //oh boy this will be a mess. or it was. Its better now
    public BlockwiseScanner(BlockState[][][] area) {
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

    public Set<CircuitObject> getCircuitObjects() {
        return circuitObjects;
    }

    public Set<Edge> getEdgeSet() {
        return edgeSet;
    }


    private void setupRedstoneCircuitDiagram(BlockState[][][] area){
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
        List<BlockPos> sourcesPos = findRedstoneObjects(area);
        for (BlockPos blockPos : sourcesPos) {
            System.out.println("found a redstone Object");
            CircuitObject c = buildCircuitObject(cordGet(area, blockPos));
            sources.add(c);
        }

        //workhorse right here
        //looks from one component to what it reaches, adds all that to be searched if we have not already
        for (CircuitObject circuitObject : sources) {
            Set<Edge> newEdges = calculateEdges(circuitObject, area);
            Edges.addAll(newEdges);
        }

        this.circuitObjects = sources;
        this.edgeSet = Edges;
    }

    private CircuitObject buildCircuitObject(BlockState blockState){
        throw new IllegalStateException("this was not a CircuitObject!");
    }

    private Set<Edge> calculateEdges(CircuitObject source, BlockState[][][] area) {
        Set<BlockPos> positionsToVisit = new HashSet<>();
        Set<Edge> edges = new HashSet<>();
        switch (source.circuitObjectType) {
            case RedstoneWire:
                break;
            case Comparator:
                //find all Edges that start at this Comparator
            {//convenience stuff
                BlockPos posComparitorPointsTo = source.blockPos.add(
                        cordGet(area, source.blockPos).get(HORIZONTAL_FACING).getOpposite().getDirectionVec());
                BlockState blockStateComparitorPointsTo = cordGet(area, posComparitorPointsTo);
                //if the repeater points into redstone, start a search at said redstone with signal 15
                if (blockStateComparitorPointsTo.getBlock().equals(Blocks.REDSTONE_WIRE)) {
                    edges.addAll( redstoneBlockwiseEdgeSearch(source
                            , area
                            , new HashSet<>(Arrays.asList(posComparitorPointsTo))
                            , new HashSet<>(Arrays.asList(posComparitorPointsTo))
                            , 15));
                }
                //if we point into an opaque block, scan that one with the help of strongPowerSearch
                else if (isOpaqueCube(area, posComparitorPointsTo)) {
                    edges.addAll( strongPowerSearch(area, posComparitorPointsTo, source, 15));
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
                    }
                        //if non of that that is the end, no edges for us

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
                    }
                    //if its pointing at us, no connections to be made, edges.addAll( empty set

                }
                    //if its non of those we are not getting our signal much further, edges.addAll(ing empty set
                    //todo: indirect edges
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
                    edges.addAll( redstoneBlockwiseEdgeSearch(source
                            , area
                            , new HashSet<BlockPos>(Arrays.asList(posRepeaterPointsTo))
                            , new HashSet<BlockPos>(Arrays.asList(posRepeaterPointsTo))
                            , 15));
                }
                //if we point into an opaque block, scan that one with the help of strongPowerSearch
                else if (isOpaqueCube(area, posRepeaterPointsTo)) {
                    edges.addAll( strongPowerSearch(area, posRepeaterPointsTo, source, 15));
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
                    }
                        //if non of that that is the end, no edges for us
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
                    }
                    //if its pointing at us, no connections to be made, return empty set

                }
                    //if its non of those we are not getting our signal much further, returning empty set
                    //todo: update edges search
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
        edges.addAll(redstoneBlockwiseEdgeSearch(source, area, positionsToVisit, positionsToVisit, 15));
        return edges;
    }

    //not good practive btw
    private Set<Edge> redstoneBlockwiseEdgeSearch(CircuitObject source, BlockState[][][] area, Set<BlockPos> posToVisit, Set<BlockPos> posAlreadyVisited, int signalStrength){
        return redstoneBlockwiseEdgeSearch(area, source);
    }

    private Set<Edge> redstoneBlockwiseEdgeSearch(BlockState[][][] area, CircuitObject source){
        BlockPos startPos = source.blockPos;
        Set<BlockPos> newPositionsToVisit = new HashSet<>();
        Set<Edge> edges = new HashSet<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            //convenience
            BlockPos pos = startPos.add(direction.getDirectionVec());
            BlockState blockStateAtPos = cordGet(area, pos);
            //connect to all redstone that is right next to us
            if (blockStateAtPos.getBlock().equals(Blocks.REDSTONE_WIRE)
                    ) {
                edges.add(new Edge(
                        source,
                        new CircuitObject(CircuitObjectType.RedstoneWire,pos),
                        1
                ));
            }
            //connect to any repeater right next to us, given that it points away from us
            else if (blockStateAtPos.getBlock().equals(Blocks.REPEATER) &&
                    blockStateAtPos.get(HORIZONTAL_FACING).getOpposite().equals(direction)) {
                edges.add(new RepeaterEdge(
                        source,
                        new CircuitObjectRepeater(pos,
                                blockStateAtPos.get(DELAY_1_4)),
                        false,
                        0
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
                        0
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
                        0
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
                        ) {
                    edges.add(new Edge(source,new CircuitObject(CircuitObjectType.RedstoneWire,pos.up()) ,1));
                }
            }

            //if redstone is beneath and a connection can be made (e.g. the block above it is
            //not opaque) then we found new redstone to connect too
            if (isOpaqueCube(area, startPos.down())
                    && isOpaqueCube(area, pos) == false
                    && cordGet(area, pos.down()).getBlock().equals(Blocks.REDSTONE_WIRE)
                    ) {
                edges.add(new Edge(source,new CircuitObject(CircuitObjectType.RedstoneWire,pos.down()) ,1));
            }

        }
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

        edges.addAll(redstoneBlockwiseEdgeSearch(source, area, positionsToVisit, positionsToVisit, signalPower));

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

        //we must do this because in redstoneBlockwiseEdgeSearch() we loop through positionsToVisit while changing
        //positionsAlreadyFound.
        Set<BlockPos> positionsAlreadyFound = new HashSet<>(positionsToVisit);
        edges.addAll(redstoneBlockwiseEdgeSearch(source, area, positionsToVisit, positionsAlreadyFound, signalPower));

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

        edges.addAll(redstoneBlockwiseEdgeSearch(source, area, positionsToVisit, positionsToVisit, signalPower));

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

    private static List<BlockPos> findRedstoneObjects(BlockState[][][] area) {
        List<BlockPos> blockPosList = new ArrayList<>();
        for (int i = 0; i < area.length; i++) {
            for (int j = 0; j < area[0].length; j++) {
                for (int k = 0; k < area[0][0].length; k++) {
                    BlockState blockState = area[i][j][k];
                    if ( isBlockwiseCircuitObject( blockState.getBlock())) {
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

    private void statusReport() {
        System.out.println("we have " + circuitObjects.size() + " circuitObjects and " + edgeSet.size() + " edgeSet");
        System.out.println("those would be: ");
        for (CircuitObject source : circuitObjects) {
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

    public static boolean isBlockwiseCircuitObject(BlockState[][][] area, BlockPos blockPos){
        return isBlockwiseCircuitObject(cordGet(area, blockPos).getBlock());
    }

    public static boolean isBlockwiseCircuitObject(BlockState blockState){
        return isBlockwiseCircuitObject(blockState.getBlock());
    }

    public static boolean isBlockwiseCircuitObject(Block block){
        return(block.equals(Blocks.REDSTONE_WIRE)
                || block.equals(Blocks.REDSTONE_WALL_TORCH)
                || block.equals(Blocks.REDSTONE_TORCH)
                || block.equals(Blocks.REPEATER)
                || block.equals(Blocks.WHITE_WOOL)
                || block.equals(Blocks.BLUE_WOOL)
                || block.equals(Blocks.LEVER)
                || block.equals(Blocks.PISTON)
                || block.equals(Blocks.STICKY_PISTON)
                || block.equals(Blocks.COMPARATOR));
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
        //might want this for rewrite. Maybe not. Who knows?
        RedstoneWire
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




