package com.axono;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        new AppStage(primaryStage);
    }

    public static void main(String[] args) {
        launch();
    }

}