package com.axono.onboarding;

import com.axono.model.UserProfile;
import com.axono.signup.SignUpView;
import com.axono.ui.UITheme;
import com.axono.ui.UIConstants;
import javafx.stage.Stage;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.ArrayList;
import java.util.List;

/**
 * Multi-step onboarding wizard that guides a new user through profile setup.
 * Manages a sequence of four steps: Welcome, Sign Up, Subject Selection,
 * and Summary. Fires a completion callback with the populated
 * {@link UserProfile} when the user clicks "Launch App".
 */
public final class OnboardingStage {

    /** The JavaFX {@link Stage} that hosts the onboarding wizard. */
    private final Stage stage;

    /**
     * Callback invoked with the completed {@link UserProfile} when
     * the user finishes onboarding.
     */
    private final Consumer<UserProfile> onComplete;

    /** The user profile being built across all onboarding steps. */
    private final UserProfile profile = new UserProfile();

    /** Zero-based index of the currently displayed onboarding step. */
    private int currentStep = 0;

    /** The sign-up form view (step 1). */
    private final SignUpView signupView;

    /** The module selection view (step 2). */
    private final SubjectView subjectView;

    /** The profile summary view (step 3). */
    private final SummaryView summaryView;

    /** Array of all onboarding step nodes in display order. */
    private final Node[] steps;

    /** Root layout of the onboarding window. */
    private BorderPane root;

    /** Button that navigates to the previous step. */
    private Button backButton;

    /** Button that navigates to the next step or launches the app. */
    private Button nextButton;

    /** Label showing the current step number (e.g. "Step 2 of 4"). */
    private Label stepLabel;

    /** Progress indicator dots displayed in the header, one per step. */
    private List<Circle> dots;

    /**
     * Constructs the onboarding wizard, initialises all step views,
     * builds the UI, and shows the stage.
     *
     * @param onboardingStage   the {@link Stage}
     *                          to use for the onboarding window.
     * @param completionHandler callback to invoke with the completed profile
     *                          when onboarding finishes.
     */
    public OnboardingStage(
            final Stage onboardingStage,
            final Consumer<UserProfile> completionHandler) {
        this.stage = onboardingStage;
        this.onComplete = completionHandler;
        signupView = new SignUpView(profile);
        subjectView = new SubjectView(profile);
        summaryView = new SummaryView(profile);
        steps = new Node[] {
                new WelcomeView(), signupView, subjectView, summaryView
        };
        buildUI();
    }

    /**
     * Assembles the root {@link BorderPane} with a header, footer, and
     * the first step as the centre, then shows the stage.
     */
    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + UITheme.BG + ";");
        root.setTop(buildHeader());
        root.setCenter(steps[0]);
        root.setBottom(buildFooter());

        stage.setScene(new Scene(root,
                UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT));
        stage.setTitle("ReWire — Setup");
        stage.show();
        updateStep();
    }

    /**
     * Builds and returns the onboarding window header containing the app
     * logo and step progress indicator dots.
     *
     * @return a styled {@link HBox} to be placed at the top of the window.
     */
    private HBox buildHeader() {
        Label logo = new Label("Axono - ReWire");
        logo.setStyle("-fx-text-fill: white;"
                + "-fx-font-size: 22px; -fx-font-weight: bold;");
        HBox.setHgrow(logo, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        dots = new ArrayList<>();
        HBox dotsRow = new HBox(UIConstants.SPACING_SM);
        dotsRow.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(dotsRow, Priority.NEVER);
        for (int i = 0; i < steps.length; i++) {
            Circle dot = new Circle(UIConstants.SPACING_XS,
                    Color.web(UITheme.TERTIARY));
            dots.add(dot);
            dotsRow.getChildren().add(dot);
        }

        HBox header = new HBox(logo, spacer, dotsRow);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + UITheme.PRIMARY + ";");
        header.setPadding(new Insets(
                UIConstants.PADDING_SM,
                UIConstants.PADDING_LG,
                UIConstants.PADDING_SM,
                UIConstants.PADDING_LG));
        return header;
    }

    /**
     * Builds and returns the onboarding window footer containing the Back
     * and Next navigation buttons and the step counter label.
     *
     * @return a styled {@link StackPane} to be placed
     *         at the bottom of the window.
     */
    private StackPane buildFooter() {
        stepLabel = new Label();
        stepLabel.setStyle("-fx-text-fill: "
                + UITheme.TEXT_MUTED + "; -fx-font-size: 12px;");

        backButton = navButton(
                "← Back", UITheme.SECONDARY_OPTION, UITheme.TEXT_DARK);
        nextButton = navButton("Next →", UITheme.SECONDARY, UITheme.WHITE);
        backButton.setOnAction(e -> goBack());
        nextButton.setOnAction(e -> goNext());

        HBox centerButtons = new HBox(UIConstants.SPACING_MD,
                backButton, nextButton);
        centerButtons.setAlignment(Pos.CENTER);

        centerButtons.setMaxWidth(Region.USE_PREF_SIZE);

        StackPane footer = new StackPane(stepLabel, centerButtons);

        StackPane.setAlignment(stepLabel, Pos.CENTER_LEFT);

        footer.setPadding(new Insets(
                UIConstants.PADDING_SM,
                UIConstants.PADDING_LG,
                UIConstants.PADDING_SM,
                UIConstants.PADDING_LG));
        footer.setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: " + UITheme.BORDER + ";"
                        + "-fx-border-width: 1 0 0 0;");
        return footer;
    }

    /**
     * Creates a styled navigation {@link Button} with the given label,
     * background colour, and foreground (text) colour.
     *
     * @param text the button label.
     * @param bg   the hex background colour string.
     * @param fg   the hex text colour string.
     * @return a configured navigation {@link Button}.
     */
    private Button navButton(
            final String text,
            final String bg,
            final String fg) {
        Button b = new Button(text);
        b.setPrefSize(UIConstants.WIZARD_BTN_WIDTH,
                UIConstants.WIZARD_BTN_HEIGHT);
        String base = String.format(
                "-fx-background-color: %s; -fx-text-fill: %s;"
                        + "-fx-font-weight: bold; -fx-font-size: 13px;"
                        + "-fx-background-radius: 6px; -fx-cursor: hand;",
                bg, fg);
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(base + "-fx-opacity: 0.88;"));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    /**
     * Handles the Next button action. Validates the current step if required,
     * saves its data, then advances to the next step or launches the app
     * if on the final step.
     */
    private void goNext() {
        if (currentStep == 1 && !signupView.validateInput()) {
            return;
        } else if (currentStep == 2 && !subjectView.validateInput()) {
            return;
        } else if (currentStep == 1) {
            signupView.saveData();
        } else if (currentStep == 2) {
            subjectView.saveData();
        } else if (currentStep == steps.length - 1) {
            launchApp();
            return;
        }

        currentStep++;
        if (currentStep == steps.length - 1) {
            summaryView.refresh();
        }
        root.setCenter(steps[currentStep]);
        updateStep();
    }

    /**
     * Handles the Back button action. Decrements the current step index
     * and updates the displayed view.
     */
    private void goBack() {
        if (currentStep > 0) {
            currentStep--;
            root.setCenter(steps[currentStep]);
            updateStep();
        }
    }

    /**
     * Updates all step-dependent UI elements: progress dot colours,
     * the step counter label, Back button visibility, and the Next
     * button label and colour.
     */
    private void updateStep() {
        for (int i = 0; i < dots.size(); i++) {
            dots.get(i).setFill(Color.web(
                    i <= currentStep ? UITheme.TERTIARY : UITheme.BORDER));
        }
        stepLabel.setText("Step " + (currentStep + 1)
                + " of " + steps.length);
        backButton.setVisible(currentStep > 0);

        boolean lastStep = (currentStep == steps.length - 1);
        String color = lastStep ? UITheme.PRIMARY : UITheme.TERTIARY;
        nextButton.setText(lastStep ? "Launch App" : "Next");
        nextButton.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white;"
                        + "-fx-font-weight: bold; -fx-font-size: 13px;"
                        + "-fx-background-radius: 6px; -fx-cursor: hand;",
                color));
    }

    /**
     * Closes the onboarding stage and fires the {@link #onComplete}
     * callback with the collected {@link UserProfile}.
     */
    private void launchApp() {
        stage.close();
        onComplete.accept(profile);
    }
}
