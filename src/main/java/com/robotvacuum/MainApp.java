package com.robotvacuum;

import com.robotvacuum.controller.SimulationController;
import com.robotvacuum.model.SimulationModel;
import com.robotvacuum.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * BZ 214 Visual Programming
 * Robot Vacuum Cleaning Simulation
 * Main application entry point.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SimulationModel model = new SimulationModel();
        MainView view = new MainView(model);
        SimulationController controller = new SimulationController(model, view);

        view.setController(controller);

        Scene scene = new Scene(view.getRoot(), 1280, 780);
        scene.getStylesheets().add(
            getClass().getResource("/com/robotvacuum/style.css").toExternalForm()
        );

        primaryStage.setTitle("Robot Süpürge Simülasyonu");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();

        controller.initialize();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
