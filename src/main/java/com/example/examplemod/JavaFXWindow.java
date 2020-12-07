package com.example.examplemod;

import com.example.examplemod.util.MakeGraphLookBetterUtility;
import com.example.examplemod.util.RedstoneGraph;
import com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects.AbstractActiveCircuitObject;
import com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects.ActiveCircuitObjectButton;
import com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects.ActiveCircuitObjectInput;
import com.example.examplemod.util.RedstoneGraphHelpers.ActiveCircuitObjects.ActiveRedstoneGraph;
import com.example.examplemod.util.RedstoneGraphHelpers.CircuitObject;
import com.example.examplemod.util.RedstoneGraphHelpers.Edge;
import com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel.Formula;
import com.example.examplemod.util.RedstoneGraphHelpers.FormulaModel.TruthTable.GenericTruthTable;
import com.example.examplemod.util.utility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.graphstream.graph.Graph;
import org.graphstream.stream.Sink;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.GraphRenderer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class JavaFXWindow extends Application {
    public static JavaFXWindow instance;
    public static RedstoneGraph sourceRedstoneGraph;
    private static Stage primaryStageInstance;
    public Label selectedNodeLabel;
    public ChoiceBox styleChoiceBox;
    public Button switchInputButton;
    public Button uselssButtonA;
    public Button uselssButtonB;
    public Button uselssButtonC;
    public Button drawGraphButton;
    public Button truthTableButton;
    public CheckBox autoLayoutCheckBox;
    public boolean autoLayoutCheckBoxDefault = false;
    public CheckBox activeGraphCheckBox;
    public boolean activeGraphCheckBoxDefault = true;
    public CheckBox fixDoubleEdgesCheckBox;
    public boolean fixDoubleEdgesCheckBoxDefault = true;
    public CheckBox DoDummyThingsCheckBox;
    public boolean DoDummyThingsCheckBoxDefault = true;
    //    public Graph graph;
//    public RedstoneGraph sourceRedstoneGraph;
    public AnchorPane graphAnchorPane;
    public ToggleButton toggleRedstoneAutoTicksButton;
    public Button labelSwitchButton;
    private Graph graph;
    private Viewer inUseViewer;
    private String selectedNodeId;
    private RedstoneGraph inUseRedstoneGraph;
    private boolean labelVisible = false;
    private ViewerPipe fromViewer;
    //    private Graph graph; //dont need that rn, just a test mostly
    private ActiveVisualiser activeVisualiser;
    private Stage primaryStage;

    public static void main(String[] args) {
        try {
            Application.launch(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception, show stage-instance");
            Platform.runLater(() -> instance.openAgain());
            //Platform.runLater(() -> primaryStageInstance.show());
            System.out.println("later");
            //throw e;
        }
        System.out.println("done with main");
    }

    private void switchLabelVisibility() {
        if (activeVisualiser == null) {
            System.out.println("no graph here, dont know why");
            return;
        }
        labelVisible = !labelVisible;
        if (labelVisible) {
            activeVisualiser.showLabels();
            uselssButtonB.setText("Hide Label");
        } else {
            activeVisualiser.hideLabels();
            uselssButtonB.setText("Show Label");
        }
    }

    void openAgain() {
        primaryStageInstance.show();
        setdefaultSelectionsForButtonsAndStuff();
        drawGraph();
    }

    void setdefaultSelectionsForButtonsAndStuff() {
        autoLayoutCheckBox.setSelected(autoLayoutCheckBoxDefault);
        activeGraphCheckBox.setSelected(activeGraphCheckBoxDefault);
        //dont think we need people to choose here.
        activeGraphCheckBox.setVisible(false);
        DoDummyThingsCheckBox.setSelected(DoDummyThingsCheckBoxDefault);
        fixDoubleEdgesCheckBox.setSelected(fixDoubleEdgesCheckBoxDefault);
        styleChoiceBox.setValue("MC-style active");
        uselssButtonB.setText("Show Label");
    }

    @FXML
    void initialize() {
        System.out.println("ini running");

        drawGraphButton.setOnAction(event -> actionDrawButton());
        uselssButtonA.setOnAction(event -> actionUselssButtonA());
        uselssButtonC.setOnAction(event -> actionRedstoneTick());
        uselssButtonB.setOnAction(event -> switchLabelVisibility());
        switchInputButton.setOnAction(event -> actionSwitchInput());
        truthTableButton.setOnAction(event -> actionTruthTable());
        labelSwitchButton.setOnAction(event -> actionLabelSwitch());
//        labelSwitchButton.setOnAction(event -> inUseRedstoneGraph);
        uselssButtonB.setText("Show Label");
//        uselssButtonD.setOnAction(event -> toggleAutoRedstoneTick());
//        uselssButtonD.setText("Auto redstone ticking on");
        autoLayoutCheckBox.setSelected(autoLayoutCheckBoxDefault);
        activeGraphCheckBox.setSelected(activeGraphCheckBoxDefault);
        //dont think we need people to choose here.
        activeGraphCheckBox.setVisible(false);
        DoDummyThingsCheckBox.setSelected(DoDummyThingsCheckBoxDefault);
        fixDoubleEdgesCheckBox.setSelected(fixDoubleEdgesCheckBoxDefault);

        ObservableList<String> styleChoices =
                FXCollections.observableArrayList("MC-style active", "MC-style", "Logic Circuit");
        styleChoiceBox.setItems(styleChoices);
        styleChoiceBox.setValue("MC-style active");

        styleChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue observable, Object oldValue, Object newValue) -> {
                    if (graph == null) {
                        return;
                    }
                    String sheetName = (String) newValue;
                    graph.removeAttribute("ui.stylesheet");
                    graph.setAttribute("ui.stylesheet", getStylesheetUrl(sheetName));
                    System.out.println("Set stylesheet to: " + sheetName);
                }
        );


        Thread tickerThread = new Thread(() -> {
            while (true) {
                try {
                    sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                if (toggleRedstoneAutoTicksButton.isSelected()) {
                    actionRedstoneTick();
                }
            }
        });
        tickerThread.start();

        Thread updatePumpThread = new Thread(() -> {
            while (true) {
                try {
                    sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                pumpUpdates();
            }
        });
        updatePumpThread.start();

        drawGraph();



        instance = this;
    }

    private void actionLabelSwitch() {
        if(activeVisualiser!=null){
            activeVisualiser.labelsAsFormulasSwitch();
        }
    }

    private void actionTruthTable() {

        if(activeVisualiser!=null && selectedNodeId!=null) {
            for (AbstractActiveCircuitObject abstractActiveCircuitObject : activeVisualiser.getActiveRedstoneGraph().getCircuitObjects()) {
                if (abstractActiveCircuitObject.toString().equals(selectedNodeId)) {
                    selectedNodeLabel.setText(abstractActiveCircuitObject.getCircuitObject().toString());
                    //System.out.println(abstractActiveCircuitObject.getCircuitObject().toString());
                    Formula inputFormula = abstractActiveCircuitObject.deepInputSearch(new ArrayList<>());
                    System.out.println("setSelectedNode mit Node "+selectedNodeId+", formula:");
                    System.out.println(inputFormula.toString());
                    System.out.println("or in simple terms:");
                    System.out.println(inputFormula.simplifyThis().toString());
                    System.out.println("or in boolisch terms:");
                    Formula inputFormula2 = abstractActiveCircuitObject.deepInputSearch(new ArrayList<>());
                    inputFormula2=inputFormula2.simplifyThisBoolisch();
                    System.out.println(inputFormula2.toString());
                    System.out.println("all the inputs:");
                    inputFormula2.getAllInputs().forEach((Integer)->{
                        System.out.print(utility.getStringRepresentationOfInt(Integer)+", ");
                    });
                    GenericTruthTable genericTruthTable = new GenericTruthTable(inputFormula2);
                    System.out.println(genericTruthTable.toString());
                }
            }
        }
    }

    private void actionSwitchInput() {
        if (selectedNodeId != null && activeVisualiser != null) {
            for (AbstractActiveCircuitObject abstractActiveCircuitObject : activeVisualiser.getActiveRedstoneGraph().getCircuitObjects()) {
                if (abstractActiveCircuitObject.toString().equals(selectedNodeId)) {
                    if (abstractActiveCircuitObject instanceof ActiveCircuitObjectInput) {
                        ((ActiveCircuitObjectInput) abstractActiveCircuitObject).setOnState(
                                !((ActiveCircuitObjectInput) abstractActiveCircuitObject).isOnState()
                                //dont miss the ! here, it inverts it all
                        );
                    } else if (abstractActiveCircuitObject instanceof ActiveCircuitObjectButton){
                        ((ActiveCircuitObjectButton) abstractActiveCircuitObject).press();
                    }
                }
            }
            //activeVisualiser.updateGraph();
        }
    }

    String getStylesheetUrl() {
        return getStylesheetUrl((String) styleChoiceBox.getValue());
    }


    String getStylesheetUrl(String mySheetName) {
        String sheetName = mySheetName;
        switch (sheetName) {
            case "MC-style active":
                sheetName = "stylesheetMC-Style_active";
                break;
            case "MC-style":
                sheetName = "stylesheetMC-Style";
                break;
            case "Logic Circuit":
                sheetName = "Circuit_Diagram";
                break;
            default:
                throw new IllegalStateException("no stylesheet with this name");
        }
        String url = "url('file://" + sheetName + "')";
        return url;
    }

    synchronized void pumpUpdates() {
        //System.out.print(".");
        if (this.inUseViewer == null) {
            return;
        }
        if (fromViewer != null)
            fromViewer.pump();
        //System.out.print("!");
    }

    void setSelectedNode(String nodeId) {
        if (activeVisualiser == null) {

        } else {
            if(selectedNodeId!=null){
                activeVisualiser.unselectSelectedNode(selectedNodeId);
            }

            for (AbstractActiveCircuitObject abstractActiveCircuitObject : activeVisualiser.getActiveRedstoneGraph().getCircuitObjects()) {
                if (abstractActiveCircuitObject.toString().equals(nodeId)) {
                    selectedNodeLabel.setText(abstractActiveCircuitObject.getCircuitObject().toString());
                    //System.out.println(abstractActiveCircuitObject.getCircuitObject().toString());
//                    Formula inputFormula = abstractActiveCircuitObject.deepInputSearch(new ArrayList<>());
//                    System.out.println("setSelectedNode mit Node "+nodeId+", formula:");
//                    System.out.println(inputFormula.toString());
//                    System.out.println("or in simple terms:");
//                    System.out.println(inputFormula.simplifyThis().toString());
//                    System.out.println("or in boolisch terms:");
//                    Formula inputFormula2 = abstractActiveCircuitObject.deepInputSearch(new ArrayList<>());
//                    inputFormula2=inputFormula2.simplifyThisBoolisch();
//                    System.out.println(inputFormula2.toString());
//                    System.out.println("all the inputs:");
//                    inputFormula2.getAllInputs().forEach((Integer)->{
//                        System.out.println(utility.getStringRepresentationOfInt(Integer)+", ");
//                    });
                }
            }
            //selectedNodeLabel.setText(name);
            //System.out.println(name);
            selectedNodeId = nodeId;
            activeVisualiser.setSelectedNode(nodeId);
        }
    }

    private synchronized void actionRedstoneTick() {
        if (activeVisualiser != null) {
            //System.out.println("do the redstone Ticking");
            activeVisualiser.redstoneTick();
        }
    }

    private void actionActiveVisualiserTellMeAboutYou() {
        System.out.println("asking the activeVisualiser to tell me about him");
    }

    void actionDrawButton() {
        drawGraph();
    }

    void actionUselssButtonA() {
        System.out.println("printing out source graph: ");
        for (CircuitObject circuitObject : sourceRedstoneGraph.getCircuitObjects()) {
            System.out.println(circuitObject);
        }
        for (Edge edge : sourceRedstoneGraph.getEdges()) {
            System.out.println(edge);
        }
        System.out.println("das wars. ");
    }

    //also sets the inUseRedstoneGraph to what we brew up here. That is implied, lets hope I dont forget that
    private Graph makeGraphFromSettings() {
        RedstoneGraph clonedGraph = sourceRedstoneGraph.clone();

        MakeGraphLookBetterUtility makeGraphLookBetterUtility = new MakeGraphLookBetterUtility(clonedGraph);
        //set setting accoring to plan
        makeGraphLookBetterUtility.fixAtoMultipleOtherCircuitObjects = DoDummyThingsCheckBox.isSelected();
        makeGraphLookBetterUtility.cleanUpEdges = fixDoubleEdgesCheckBox.isSelected();
        //turn these settings into effect
        makeGraphLookBetterUtility.makeGraphLookPrettyAuto();

        RedstoneGraph currentRedstoneGraph = makeGraphLookBetterUtility.returnGraph();

        inUseRedstoneGraph = currentRedstoneGraph;

        Graph graph;
        if (activeGraphCheckBox.isSelected()) {
            this.activeVisualiser = new ActiveVisualiser(
                    new ActiveRedstoneGraph(currentRedstoneGraph)
            );
            graph = activeVisualiser.getGraph();
        } else {
            graph = Visualiser.getGraphFromRedStoneGraph(currentRedstoneGraph);
        }
        graph.setAttribute("ui.stylesheet", "url('file://stylesheetMC-Style_active')");
        graph.setAttribute("ui.antialias");
        return graph;
    }

    void drawGraph() {
        Graph graph = makeGraphFromSettings();
        displayGraph(graph);
    }

    void displayGraph(Graph graph) {
        this.graph = graph;
        FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        inUseViewer = viewer;
        fromViewer = inUseViewer.newViewerPipe();
        //fromViewer.addViewerListener(this);
        fromViewer.addSink(new Sink() {
            @Override
            public void graphAttributeAdded(String sourceId, long timeId, String attribute, Object value) { }

            @Override
            public void graphAttributeChanged(String sourceId, long timeId, String attribute, Object oldValue, Object newValue) { }

            @Override
            public void graphAttributeRemoved(String sourceId, long timeId, String attribute) {            }

            @Override
            public void nodeAttributeAdded(String sourceId, long timeId, String nodeId, String attribute, Object value) {
                //System.out.println("nodeAttributeAdded");
                if (attribute.equals("ui.clicked") && (boolean) value == true) {
                    Platform.runLater(() -> setSelectedNode(nodeId));
                    //System.out.println("ui.clicked, true");
                }
                if (attribute.equals("ui.clicked") && (boolean) value == false) {
                    //System.out.println("ui.clicked, false");
                }
                if (attribute.equals("ui.selected") && (boolean) value == true) {
                    //System.out.println("ui.selected, true");
                }
                if (attribute.equals("ui.selected") && (boolean) value == false) {
                    //System.out.println("ui.selected, false");
                }
            }

            @Override
            public void nodeAttributeChanged(String sourceId, long timeId, String nodeId, String attribute, Object oldValue, Object newValue) {
                //System.out.println("nodeAttributeChanged");
            }

            @Override
            public void nodeAttributeRemoved(String sourceId, long timeId, String nodeId, String attribute) {
                //System.out.println("nodeAttributeRemoved");
            }

            @Override
            public void edgeAttributeAdded(String sourceId, long timeId, String edgeId, String attribute, Object value) { }

            @Override
            public void edgeAttributeChanged(String sourceId, long timeId, String edgeId, String attribute, Object oldValue, Object newValue) { }

            @Override
            public void edgeAttributeRemoved(String sourceId, long timeId, String edgeId, String attribute) { }

            @Override
            public void nodeAdded(String sourceId, long timeId, String nodeId) { }

            @Override
            public void nodeRemoved(String sourceId, long timeId, String nodeId) { }

            @Override
            public void edgeAdded(String sourceId, long timeId, String edgeId, String fromNodeId, String toNodeId, boolean directed) { }

            @Override
            public void edgeRemoved(String sourceId, long timeId, String edgeId) { }

            @Override
            public void graphCleared(String sourceId, long timeId) { }

            @Override
            public void stepBegins(String sourceId, long timeId, double step) { }
        });


        if (autoLayoutCheckBox.isSelected()) {
            viewer.enableAutoLayout();
        }
        GraphRenderer renderer = new FxGraphRenderer();
        //FxDefaultView view = (FxDefaultView) viewer.addView(FxViewer.DEFAULT_VIEW_ID, renderer);
        FxViewPanel pane = (FxViewPanel) viewer.addDefaultView(true, renderer);

        graphAnchorPane.getChildren().clear();
        graphAnchorPane.getChildren().addAll(pane);
    }

//    public JavaFXWindow(Graph graph) {
//        //this.graph=graph;
//        launch();
//    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        System.out.println("start running");
        Platform.setImplicitExit(false);

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("GraphWindow.fxml"));

        primaryStage.setTitle("Graph Window");
        primaryStage.setScene(new Scene(root, 950, 660));
        primaryStage.setMinWidth(950);
        primaryStage.setMinHeight(660);
//        primaryStage.setMaxWidth(950);
//        primaryStage.setMaxHeight(660);
        //root.getChildrenUnmodifiable().add(SearchResult);

        // primaryStage.setMinWidth(800);
        //primaryStage.setMinHeight(600);
        this.primaryStage = primaryStage;
        primaryStageInstance = primaryStage;
        primaryStage.show();

        //drawGraph();

    }

    @Override
    public void stop() {
        primaryStage.hide();
    }

    //    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Graph");
    //StackPane root = new StackPane();
    //primaryStage.setScene(new Scene(root, 600, 400));

//        Graph graph = new MultiGraph("myGraph");
//        graph.addNode("A");
//        graph.addNode("B");
//        graph.addNode("C");
//        graph.addEdge("AB", "A", "B");
//        graph.addEdge("CB", "C", "B");

//        FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
//        viewer.enableAutoLayout();
//        GraphRenderer renderer = new FxGraphRenderer();
//        //FxDefaultView view = (FxDefaultView) viewer.addView(FxViewer.DEFAULT_VIEW_ID, renderer);
//        FxViewPanel pane = (FxViewPanel)viewer.addDefaultView(true,renderer);
//
//        StackPane graphPane = new StackPane();
//        graphPane.getChildren().addAll(pane);


//        Scene scene = new Scene(graphPane, 800, 600);
//        primaryStage.setScene(scene);
//        System.out.println("second before");
//        primaryStage.show();
//        System.out.println("should be seen here I guess");
    //graph.display();
//    }
}
