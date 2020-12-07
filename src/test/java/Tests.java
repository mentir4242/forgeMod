import com.example.examplemod.JavaFXWindow;
import com.example.examplemod.util.RedstoneGraph;
import com.example.examplemod.util.RedstoneGraphHelpers.*;
import com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects.ActiveCircuitObjectInput;
import com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel.Formula;
import com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel.InputFormula;
import com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel.OrFormula;
import com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel.TruthTable.GenericTruthTable;
import com.example.examplemod.util.utility;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Tests {
    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "javafx");
        System.setProperty("org.graphstream.debug", "true");

        testJavaFxWindow3();
    }

    private static void TruthTableTest(){
        InputFormula inputFormula1 = new InputFormula(
                new ActiveCircuitObjectInput(
                        new CircuitObject(
                                CircuitObjectType.INPUT,
                                new BlockPos(0,0,0)
                        ),
                        true
                ).setIdReturn(0)
        );
        InputFormula inputFormula2 = new InputFormula(
                new ActiveCircuitObjectInput(
                        new CircuitObject(
                                CircuitObjectType.INPUT,
                                new BlockPos(1,0,0)
                        ),
                        true
                ).setIdReturn(1)
        );
        InputFormula inputFormula3 = new InputFormula(
                new ActiveCircuitObjectInput(
                        new CircuitObject(
                                CircuitObjectType.INPUT,
                                new BlockPos(2,0,0)
                        ),
                        true
                ).setIdReturn(2)
        );
        OrFormula orFormula1 = new OrFormula(
                new HashSet<Formula>(
                        Arrays.asList(inputFormula1, inputFormula2, inputFormula3)
                )
        );

        GenericTruthTable genericTruthTable = new GenericTruthTable(orFormula1);
        System.out.println(genericTruthTable.toString());
    }

    private static void stringtest(){
        for (int i = 0; i < 5000; i++) {
            System.out.println(i+" "+utility.getStringRepresentationOfInt(i));
        }
    }

    private static void testJavaFxWindow2(){

        Set<Edge> edges=new HashSet<>();
        Set<CircuitObject> circuitObjects = new HashSet<>();
        CircuitObject button = new CircuitObject(CircuitObjectType.BUTTON, new BlockPos(0,0,0));
        CircuitObject repeater = new CircuitObjectRepeater(new BlockPos(1,1,1),1);
        CircuitObject repeater2 = new CircuitObjectRepeater(new BlockPos(1,2,1),1);
        CircuitObject repeater3 = new CircuitObjectRepeater(new BlockPos(1,3,1),1);
        circuitObjects.add(button);
        circuitObjects.add(repeater);
        circuitObjects.add(repeater2);
        circuitObjects.add(repeater3);

        edges.add(new RepeaterEdge(button,repeater,false, 3));
        edges.add(new RepeaterEdge(button,repeater2,false, 3));
        edges.add(new RepeaterEdge(button,repeater3,false, 3));

        RedstoneGraph redstoneGraph= new RedstoneGraph(edges, circuitObjects);

        JavaFXWindow.sourceRedstoneGraph = redstoneGraph;

        JavaFXWindow.main(new String[]{});
    }

    private static void testJavaFxWindow3(){

        Set<Edge> edges=new HashSet<>();
        Set<CircuitObject> circuitObjects = new HashSet<>();
        CircuitObject torch = new CircuitObject(CircuitObjectType.TORCH, new BlockPos(0,0,0));
        CircuitObject repeater = new CircuitObjectRepeater(new BlockPos(1,1,1),1);
        CircuitObject repeater2 = new CircuitObjectRepeater(new BlockPos(1,2,1),3);
        CircuitObject repeater3 = new CircuitObjectRepeater(new BlockPos(1,3,1),1);
        circuitObjects.add(torch);
        circuitObjects.add(repeater);
        circuitObjects.add(repeater2);
        circuitObjects.add(repeater3);

        edges.add(new RepeaterEdge(torch,repeater,false, 3));
        edges.add(new RepeaterEdge(repeater,repeater2,false, 3));
        edges.add(new RepeaterEdge(repeater2,repeater3,false, 3));
        edges.add(new Edge(repeater3,torch, 3));

        RedstoneGraph redstoneGraph= new RedstoneGraph(edges, circuitObjects);

        JavaFXWindow.sourceRedstoneGraph = redstoneGraph;

        JavaFXWindow.main(new String[]{});
    }

    private static void testJavaFxWindow4(){

        Set<Edge> edges=new HashSet<>();
        Set<CircuitObject> circuitObjects = new HashSet<>();
        CircuitObject torch = new CircuitObject(CircuitObjectType.TORCH, new BlockPos(0,0,0));
        CircuitObject comparator = new CircuitObjectComparitor(new BlockPos(1,1,1),false);
        CircuitObject comparator2 = new CircuitObjectComparitor(new BlockPos(1,2,1),false);
        CircuitObject comparator3 = new CircuitObjectComparitor(new BlockPos(1,3,1),false);
        circuitObjects.add(torch);
        circuitObjects.add(comparator);
        circuitObjects.add(comparator2);
        circuitObjects.add(comparator3);

        edges.add(new RepeaterEdge(torch,comparator,false, 13));
        edges.add(new RepeaterEdge(comparator,comparator2,false, 6));
        edges.add(new RepeaterEdge(comparator2,comparator3,false, 6));
        edges.add(new Edge(comparator3,torch, 5));

        RedstoneGraph redstoneGraph= new RedstoneGraph(edges, circuitObjects);

        JavaFXWindow.sourceRedstoneGraph = redstoneGraph;

        JavaFXWindow.main(new String[]{});
    }

//    public static void testJavaFxWindow(){
//
//        Graph graph = new SingleGraph("myGraph");
//        graph.addNode("A").setAttribute("ui.label", "A");
//        graph.addNode("B").setAttribute("ui.label", "B");
//        Node node = graph.addNode("C");
//        node.setAttribute("ui.label", "C");
//        node.setAttribute("ui.class", "REPEATER");
//        graph.addEdge("AB", "A", "B").setAttribute("layout.weight", 2);
//        graph.addEdge("CB", "C", "B").setAttribute("layout.weight", 2);
//        graph.addEdge("AC", "A", "C").setAttribute("layout.weight", 2);
//
//
//        graph.setAttribute("ui.stylesheet", "url('file://stylesheetMC')");
//
//        //startGraphStatic.display();
//
//        JavaFXWindow.startGraphStatic = graph;
//        JavaFXWindow.main(new String[]{});
//    }

    public static void testMiddleCoordinate() {
        //current plan for making more readable:
        // 1. find middle of destinations
        // 2. of that middle, build the middle between that and out source
        // 3. make that a new Circuit object in the middle
        // 4 connection source and and all outgoing Edges to that.

        Set<BlockPos> blockPosses=new HashSet<>();
        blockPosses.add(new BlockPos(3,1,0));
        blockPosses.add(new BlockPos(3,2,0));
        blockPosses.add(new BlockPos(3,3,0));
        BlockPos sourceBlockpos=new BlockPos(0,0,0);

        //not reinventing the wheel here, add them all up then devide by total amount to get middle
        int outgoingEdgesSize = blockPosses.size();
        BlockPos middleOfDestinations = new BlockPos(0, 0, 0);
        for (BlockPos blockPos : blockPosses) {
            middleOfDestinations.add(blockPos);
        }

        System.out.println("all added up: "+middleOfDestinations);

        middleOfDestinations = new BlockPos(
                middleOfDestinations.getX() / outgoingEdgesSize,
                middleOfDestinations.getY() / outgoingEdgesSize,
                middleOfDestinations.getZ() / outgoingEdgesSize
        );

        System.out.println("middleOfDestinations " + middleOfDestinations);

        //weightedMiddle is the middle of circuitObjects Blockpos and middleOfDestinations
        int xInTheWeightedMiddle = (sourceBlockpos.getX() + middleOfDestinations.getX()) / 2;
        int yInTheWeightedMiddle = (sourceBlockpos.getY() + middleOfDestinations.getY()) / 2;
        int zInTheWeightedMiddle = (sourceBlockpos.getZ() + middleOfDestinations.getZ()) / 2;
        BlockPos weightedMiddleOfItAll = new BlockPos(
                xInTheWeightedMiddle,
                yInTheWeightedMiddle,
                zInTheWeightedMiddle
        );

        System.out.println("weightedMiddleOfItAll " + weightedMiddleOfItAll);

        CircuitObject newBridgeBetweenSourceAndDestinations = new CircuitObject(
                CircuitObjectType.DUMMY,
                weightedMiddleOfItAll
        );
    }


//public static void main(String[] args) {
//    JavaFXWindow.main(args);
//}
}


