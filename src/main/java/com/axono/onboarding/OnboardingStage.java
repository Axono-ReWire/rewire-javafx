package com.axono.onboarding;

import com.axono.auth.AuthException;
import com.axono.auth.AuthService;
import com.axono.ui.UIConstants;
import com.axono.auth.Session;
import com.axono.auth.User;
import com.axono.auth.UserProfile;
import com.axono.player.UserModuleRepository;
import java.sql.SQLException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
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
     * Callback invoked after signup has been persisted to the database
     * and {@link Session} has been populated with the new user.
     */
    private final Runnable onComplete;

    /**
     * Callback invoked when the user clicks "Sign In" on the welcome view.
     */
    private final Runnable onSignIn;

    /** Auth service used to persist the new user on wizard completion. */
    private final AuthService authService = new AuthService();

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

    /** HBox containing Back and Next buttons for steps 1-3. */
    private HBox centerButtonsBox;

    /** HBox containing Sign In and Sign Up buttons for welcome step. */
    private HBox welcomeButtonsBox;

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
            final Runnable completionHandler) {
        this(onboardingStage, completionHandler, () -> {
        });
    }

    /**
     * Constructs the onboarding wizard, initialises all step views,
     * builds the UI, and shows the stage.
     *
     * @param onboardingStage   the {@link Stage}
     *                          to use for the onboarding window.
     * @param completionHandler callback to invoke with the completed profile
     *                          when onboarding finishes.
     * @param signInHandler     callback to invoke when user clicks Sign In
     *                          on the welcome view.
     */
    public OnboardingStage(
            final Stage onboardingStage,
            final Runnable completionHandler,
            final Runnable signInHandler) {
        this.stage = onboardingStage;
        this.onComplete = completionHandler;
        this.onSignIn = signInHandler;
        signupView = new SignUpView(profile, this::closeStage);
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
        root.getStyleClass().add("grad-back");
        root.setTop(buildHeader());
        root.setCenter(steps[0]);
        root.setBottom(buildFooter());

        Scene scene = new Scene(root,
                UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        String css = getClass()
                .getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
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
        HBox dotsRow = new HBox(UIConstants.SPACING_MD);
        dotsRow.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(dotsRow, Priority.NEVER);
        for (int i = 0; i < steps.length; i++) {
            Circle dot = new Circle(UIConstants.SPACING_SM,
                    Color.web("#399386"));
            dots.add(dot);
            dotsRow.getChildren().add(dot);
        }

        HBox header = new HBox(logo, spacer, dotsRow);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("navbar");
        header.setPadding(new Insets(
                UIConstants.PADDING_NAV_V,
                UIConstants.PADDING_NAV_H,
                UIConstants.PADDING_NAV_V,
                UIConstants.PADDING_NAV_H));
        return header;
    }

    /**
     * Builds and returns the onboarding window footer containing the Back
     * and Next navigation buttons and the step counter label.
     * For the welcome view (step 0), shows "Sign In" and "Sign Up" buttons
     * instead.
     *
     * @return a styled {@link StackPane} to be placed
     *         at the bottom of the window.
     */
    private StackPane buildFooter() {
        stepLabel = new Label();
        stepLabel.getStyleClass().add("text-muted");
        stepLabel.setStyle("-fx-font-size: " + UIConstants.FONT_SMALL + "px;");

        backButton = new Button("← Back");
        backButton.getStyleClass().add("btn-outline");
        backButton.setPrefSize(UIConstants.NAV_BTN_WIDTH,
                UIConstants.NAV_BTN_HEIGHT);
        nextButton = new Button("Next →");
        nextButton.getStyleClass().add("btn-primary");
        nextButton.setPrefSize(UIConstants.NAV_BTN_WIDTH,
                UIConstants.NAV_BTN_HEIGHT);
        backButton.setOnAction(e -> goBack());
        nextButton.setOnAction(e -> goNext());

        Button signInButton = new Button("Sign In");
        signInButton.getStyleClass().add("btn-outline");
        signInButton.setPrefSize(UIConstants.NAV_BTN_WIDTH,
                UIConstants.NAV_BTN_HEIGHT);
        signInButton.setOnAction(e -> handleSignIn());

        Button signUpButton = new Button("Sign Up");
        signUpButton.getStyleClass().add("btn-primary");
        signUpButton.setPrefSize(UIConstants.NAV_BTN_WIDTH,
                UIConstants.NAV_BTN_HEIGHT);
        signUpButton.setOnAction(e -> goNext());

        centerButtonsBox = new HBox(UIConstants.SPACING_LG,
                backButton, nextButton);
        centerButtonsBox.setAlignment(Pos.CENTER);
        centerButtonsBox.setMaxWidth(Region.USE_PREF_SIZE);

        welcomeButtonsBox = new HBox(UIConstants.SPACING_LG,
                signInButton, signUpButton);
        welcomeButtonsBox.setAlignment(Pos.CENTER);
        welcomeButtonsBox.setMaxWidth(Region.USE_PREF_SIZE);

        StackPane footer = new StackPane(stepLabel, centerButtonsBox,
                welcomeButtonsBox);

        StackPane.setAlignment(stepLabel, Pos.CENTER_LEFT);
        StackPane.setAlignment(centerButtonsBox, Pos.CENTER);
        StackPane.setAlignment(welcomeButtonsBox, Pos.CENTER);

        footer.setPadding(new Insets(
                UIConstants.PADDING_NAV_V,
                UIConstants.PADDING_NAV_H,
                UIConstants.PADDING_NAV_V,
                UIConstants.PADDING_NAV_H));
        footer.getStyleClass().add("panel-footer");

        updateFooterVisibility();
        return footer;
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
     * Returns control to the login view.
     * Called when the user clicks the back button on the signup step.
     */
    private void closeStage() {
        onSignIn.run();
    }

    /**
     * Updates all step-dependent UI elements: progress dot colours,
     * the step counter label, Back button visibility, and the Next
     * button label and colour.
     */
    private void updateStep() {
        for (int i = 0; i < dots.size(); i++) {
            dots.get(i).setFill(Color.web(
                    i <= currentStep ? "#399386" : "#DEE2E6"));
        }
        stepLabel.setText("Step " + (currentStep + 1)
                + " of " + steps.length);
        backButton.setVisible(currentStep > 0);

        boolean lastStep = (currentStep == steps.length - 1);
        nextButton.setText(lastStep ? "Launch App" : "Next →");
        updateFooterVisibility();
    }

    /**
     * Updates footer button visibility based on current step.
     * Shows Sign In/Sign Up buttons for welcome view, Back/Next for others.
     */
    private void updateFooterVisibility() {
        boolean isWelcome = (currentStep == 0);
        if (centerButtonsBox != null && welcomeButtonsBox != null) {
            centerButtonsBox.setVisible(!isWelcome);
            centerButtonsBox.setManaged(!isWelcome);
            welcomeButtonsBox.setVisible(isWelcome);
            welcomeButtonsBox.setManaged(isWelcome);
        }
    }

    /**
     * Handles the Sign In button action. Invokes the sign-in callback.
     */
    private void handleSignIn() {
        onSignIn.run();
    }

    /**
     * Persists the new user via {@link AuthService}, populates the
     * {@link Session}, saves selected modules, closes the wizard,
     * and fires {@link #onComplete}. If signup fails (e.g. duplicate
     * username), routes back to step 1 and surfaces the error inline.
     */
    private void launchApp() {
        try {
            User user = authService.signup(
                    profile.getUsername(),
                    profile.getPassword(),
                    profile.getFirstName(),
                    profile.getLastName(),
                    profile.getYearOfStudy());
            Session.set(user);
            UserModuleRepository.saveUserModules(user.getId(),
                    profile.getSubjects());
            onComplete.run();
        } catch (AuthException ex) {
            currentStep = 1;
            root.setCenter(steps[1]);
            updateStep();
            signupView.showSignupError(ex.getMessage());
        } catch (SQLException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Database error: " + ex.getMessage(), ButtonType.OK);
            alert.setHeaderText("Could not create account");
            alert.showAndWait();
        }
    }
}
