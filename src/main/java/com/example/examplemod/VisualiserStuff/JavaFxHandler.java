package com.example.examplemod.VisualiserStuff;

import com.example.examplemod.util.RedstoneGraph;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

@Deprecated
public class JavaFxHandler extends Application {
    private Stage primaryStage;
    boolean running;
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage=primaryStage;
    }

    public void displayGraph(RedstoneGraph redstoneGraph) throws IOException {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("GraphWindow.fxml"));

        Stage displayStage = new Stage();

        displayStage.setTitle("Custom Graph");
        displayStage.setScene(new Scene(root, 800, 600));
        //root.getChildrenUnmodifiable().add(SearchResult);

        displayStage.setMinWidth(800);
        displayStage.setMinHeight(600);
        displayStage.show();

        //drawGraph();
    }
}
