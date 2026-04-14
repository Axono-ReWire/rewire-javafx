package com.axono;

import com.axono.dashboard.DashboardView;
import com.axono.home.HomepageView;
import com.axono.model.UserProfile;
import com.axono.onboarding.OnboardingStage;
import com.axono.results.ResultsPage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import com.axono.ui.UITheme;

/**
 * Constructs and manages the main application window for Axono ReWire.
 * Responsible for launching the onboarding wizard, building the navigation
 * bar, and switching between the Home, Dashboard, and Results views.
 */
public final class AppStage {

    private static final String NAV_BG = "#FFFFFF"; // Background colour hex code for the navigation bar.
    private static final String NAV_BTN_BORDER = "; -fx-border-color: " + UITheme.BORDER
            + "; -fx-border-width: 2px; -fx-border-radius: 4px;";

    /** The primary JavaFX {@link Stage} owned by this class. */
    private final Stage mainStage;

    private UserProfile profile; // User profile populated during onboarding.
    private BorderPane root;
    private Button activeNavBtn;
    private Button homeBtn;
    private Button dashBtn;
    private Button resultsBtn;

    /**
     * Creates an {@code AppStage}, opens the onboarding wizard, and
     * shows the main window once onboarding is complete.
     *
     * @param mainStage the primary JavaFX stage to attach the main UI to.
     */
    public AppStage(Stage mainStage) {
        this.mainStage = mainStage;
        openOnboarding();
    }

    /**
     * Opens the onboarding wizard in a new {@link Stage}.
     * Registers {@link #onOnboardingComplete} as the completion callback.
     */
    private void openOnboarding() {
        Stage onboardingStage = new Stage();
        new OnboardingStage(onboardingStage, this::onOnboardingComplete);
    }

    /**
     * Called by the onboarding wizard when the user finishes setup.
     * Stores the completed profile, builds the main UI, and shows the window.
     *
     * @param profile the {@link UserProfile} collected during onboarding.
     */
    private void onOnboardingComplete(UserProfile profile) {
        this.profile = profile;
        buildUI();
        mainStage.show();
    }

    /**
     * Constructs the root {@link BorderPane}, attaches the navigation bar,
     * shows the home view, and configures the main stage scene.
     */
    private void buildUI() {
        root = new BorderPane();
        root.setTop(buildNavBar());
        showHome();

        mainStage.setScene(new Scene(root, 1100, 800));
        mainStage.setTitle("Axono ReWire");
        mainStage.setResizable(true);
    }

    /**
     * Builds and returns the top navigation bar containing the app logo
     * and navigation buttons.
     *
     * @return an {@link HBox} configured as the navigation bar.
     */
    private HBox buildNavBar() {
        Label logo = new Label("Axono ReWire");
        logo.setStyle(
                "-fx-text-fill: " + UITheme.PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        HBox.setHgrow(logo, Priority.ALWAYS);

        homeBtn = navButton("Home");

        dashBtn = navButton("Dashboard");
        resultsBtn = navButton("Results (temp)");

        homeBtn.setOnAction(e -> showHome());
        dashBtn.setOnAction(e -> showDashboard());
        resultsBtn.setOnAction(e -> showResults());

        HBox nav = new HBox(8, logo, homeBtn, dashBtn, resultsBtn);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(12, 24, 12, 24));
        nav.setStyle("-fx-background-color: " + NAV_BG + ";");
        return nav;
    }

    /**
     * Creates a styled navigation {@link Button} with hover effects.
     * The button does not show an active style until {@link #setActive} is called.
     *
     * @param text the label text for the button.
     * @return the configured navigation {@link Button}.
     */
    private Button navButton(String text) {
        Button b = new Button(text);
        b.setStyle(inactiveStyle());
        b.setOnMouseEntered(e -> {
            if (b != activeNavBtn)
                b.setStyle(hoverStyle());
        });
        b.setOnMouseExited(e -> {
            if (b != activeNavBtn)
                b.setStyle(inactiveStyle());
        });
        return b;
    }

    /**
     * Marks the given button as the active navigation item, resetting
     * the previously active button to its inactive style.
     *
     * @param btn the {@link Button} to mark as active.
     */

    private void setActive(Button btn) {
        if (activeNavBtn != null)
            activeNavBtn.setStyle(inactiveStyle());
        activeNavBtn = btn;
        btn.setStyle(activeStyle());
    }

    /**
     * Returns the inline CSS string for a navigation button in its
     * default (inactive) state.
     *
     * @return CSS style string.
     */
    private String inactiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: " + UITheme.TEXT_MUTED + ";" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-padding: 6px 16px; -fx-background-radius: 4px; -fx-cursor: hand;" +
                NAV_BTN_BORDER;
    }

    /**
     * Returns the inline CSS string for a navigation button in its
     * hovered state.
     *
     * @return CSS style string.
     */
    private String hoverStyle() {
        return "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: " + UITheme.TEXT_DARK + ";" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-padding: 6px 16px; -fx-background-radius: 4px; -fx-cursor: hand;" +
                NAV_BTN_BORDER;
    }

    /**
     * Returns the inline CSS string for a navigation button in its
     * active (selected) state.
     *
     * @return CSS style string.
     */
    private String activeStyle() {
        return "-fx-background-color: rgba(255,255,255,0.28); -fx-text-fill: " + UITheme.TEXT_DARK + ";" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-padding: 6px 16px; -fx-background-radius: 4px; -fx-cursor: hand;" +
                NAV_BTN_BORDER;
    }

    /**
     * Replaces the centre pane with the {@link HomepageView} and
     * marks the home button as active.
     */
    private void showHome() {
        root.setCenter(new HomepageView());
        setActive(homeBtn);
    }

    /**
     * Replaces the centre pane with the {@link DashboardView} using
     * the current user profile, and marks the dashboard button as active.
     */
    private void showDashboard() {
        root.setCenter(new DashboardView(profile));
        setActive(dashBtn);
    }

    /**
     * Replaces the centre pane with the {@link ResultsPage} and
     * marks the results button as active.
     */
    private void showResults() {
        root.setCenter(new ResultsPage());
        setActive(resultsBtn);
    }

}
