package com.axono.onboarding;

import com.axono.model.UserProfile;
import com.axono.ui.UITheme;
import javafx.stage.Stage;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.ArrayList;
import java.util.List;

public class OnboardingStage {

    private final Stage stage;
    private final Consumer<UserProfile> onComplete;
    private final UserProfile profile = new UserProfile();
    private int currentStep = 0;

    private final ProfileView profileView;
    private final SubjectView subjectView;
    private final SummaryView summaryView;
    private final Node[] steps;

    private BorderPane root;
    private Button backBtn;
    private Button nextBtn;
    private Label stepLabel;
    private List<Circle> dots;

    public OnboardingStage(Stage stage, Consumer<UserProfile> onComplete) {
        this.stage = stage;
        this.onComplete = onComplete;
        profileView = new ProfileView(profile);
        subjectView = new SubjectView(profile);
        summaryView = new SummaryView(profile);
        steps = new Node[] { new WelcomeView(), profileView, subjectView, summaryView };
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG + ";");
        root.setTop(buildHeader());
        root.setCenter(steps[0]);
        root.setBottom(buildFooter());

        stage.setScene(new Scene(root, 660, 540));
        stage.setTitle("EngineerStudy — Setup");
        stage.setResizable(false);
        stage.show();
        syncState();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private HBox buildHeader() {
        Label logo = new Label("⚙  EngineerStudy");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        HBox.setHgrow(logo, Priority.ALWAYS);

        dots = new ArrayList<>();
        HBox dotsRow = new HBox(8);
        dotsRow.setAlignment(Pos.CENTER_RIGHT);
        for (int i = 0; i < steps.length; i++) {
            Circle dot = new Circle(6, Color.web(UITheme.ACCENT));
            dots.add(dot);
            dotsRow.getChildren().add(dot);
        }

        HBox header = new HBox(logo, dotsRow);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + UITheme.PRIMARY + ";");
        header.setPadding(new Insets(14, 24, 14, 24));
        return header;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private HBox buildFooter() {
        stepLabel = new Label();
        stepLabel.setStyle("-fx-text-fill: " + UITheme.TEXT_MUTED + "; -fx-font-size: 12px;");
        HBox.setHgrow(stepLabel, Priority.ALWAYS);

        backBtn = navButton("← Back", UITheme.BG, UITheme.TEXT_DARK);
        nextBtn = navButton("Next →", UITheme.ACCENT, UITheme.WHITE);
        backBtn.setOnAction(e -> goBack());
        nextBtn.setOnAction(e -> goNext());

        HBox footer = new HBox(10, stepLabel, backBtn, nextBtn);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 24, 12, 24));
        footer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + UITheme.BORDER + ";" +
                        "-fx-border-width: 1 0 0 0;");
        return footer;
    }

    private Button navButton(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setPrefSize(130, 38);
        String base = String.format(
                "-fx-background-color: %s; -fx-text-fill: %s;" +
                        "-fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-background-radius: 6px; -fx-cursor: hand;",
                bg, fg);
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(base + "-fx-opacity: 0.88;"));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    private void goNext() {
        if (currentStep == 1 && !profileView.validateInput())
            return;
        if (currentStep == 2 && !subjectView.validateInput())
            return;
        if (currentStep == 1)
            profileView.saveData();
        if (currentStep == 2)
            subjectView.saveData();

        if (currentStep == steps.length - 1) {
            launchApp();
            return;
        }

        currentStep++;
        if (currentStep == steps.length - 1)
            summaryView.refresh();
        root.setCenter(steps[currentStep]);
        syncState();
    }

    private void goBack() {
        if (currentStep > 0) {
            currentStep--;
            root.setCenter(steps[currentStep]);
            syncState();
        }
    }

    private void syncState() {
        for (int i = 0; i < dots.size(); i++)
            dots.get(i).setFill(Color.web(i <= currentStep ? UITheme.ACCENT : "#4A6FA5"));

        stepLabel.setText("Step " + (currentStep + 1) + " of " + steps.length);
        backBtn.setVisible(currentStep > 0);

        boolean isLast = (currentStep == steps.length - 1);
        String color = isLast ? UITheme.SUCCESS : UITheme.ACCENT;
        nextBtn.setText(isLast ? "Launch App ✓" : "Next →");
        nextBtn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-background-radius: 6px; -fx-cursor: hand;",
                color));
    }

    private void launchApp() {
        stage.close();
        onComplete.accept(profile);
    }

}
