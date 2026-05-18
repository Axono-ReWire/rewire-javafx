package com.axono.answer;

import javafx.application.Application;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewAnswerView extends Application {

    @Override

    public void start(Stage ansStage) {

        TestAnswerView test = new TestAnswerView();
        AnswerView testAnswerView = new AnswerView(test.getQuestions());

        Scene answerSceneTest = new Scene(testAnswerView, 900, 900);
        ansStage.setScene(answerSceneTest);
        ansStage.setTitle("TEST");
        ansStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
