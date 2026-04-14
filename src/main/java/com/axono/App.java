package com.axono;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for Axono ReWire.
 * Launches the primary stage via {@link AppStage}.
 */
public class App extends Application {

    /**
     * Initialises and shows the primary application stage.
     *
     * @param primaryStage the top-level JavaFX window provided by the runtime.
     */
    @Override
    public void start(final Stage primaryStage) {
        new AppStage(primaryStage);
    }

    /**
     * Main method – delegates to {@link javafx.application.Application#launch}.
     *
     * @param args command-line arguments (unused).
     */
    public static void main(String[] args) {
        launch();
    }

}