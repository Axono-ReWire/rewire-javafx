package com.axono.answer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point used to launch the {@link AnswerView} in
 * isolation with the canned test data from {@link TestAnswerView}.
 */
public final class ViewAnswerView extends Application {

    /** Width of the test scene in pixels. */
    private static final int SCENE_WIDTH = 900;

    /** Height of the test scene in pixels. */
    private static final int SCENE_HEIGHT = 900;

    @Override
    public void start(final Stage ansStage) {

        TestAnswerView test = new TestAnswerView();
        AnswerView testAnswerView = new AnswerView(test.getQuestions());

        Scene answerSceneTest = new Scene(testAnswerView, SCENE_WIDTH, SCENE_HEIGHT);
        ansStage.setScene(answerSceneTest);
        ansStage.setTitle("TEST");
        ansStage.show();
    }

    /**
     * JavaFX application launcher.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(final String[] args) {
        launch(args);
    }
}