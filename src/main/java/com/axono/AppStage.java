package com.axono;

import com.axono.dashboard.DashboardView;
import com.axono.home.HomepageView;
import com.axono.model.UserProfile;
import com.axono.onboarding.OnboardingStage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AppStage {

    private static final String NAV_BG = "#1A6A82";

    private final Stage mainStage;
    private UserProfile profile;
    private BorderPane root;
    private Button activeNavBtn;
    private Button homeBtn;
    private Button dashBtn;

    public AppStage(Stage mainStage) {
        this.mainStage = mainStage;
        openOnboarding();
    }

    private void openOnboarding() {
        Stage onboardingStage = new Stage();
        new OnboardingStage(onboardingStage, this::onOnboardingComplete);
    }

    private void onOnboardingComplete(UserProfile profile) {
        this.profile = profile;
        buildUI();
        mainStage.show();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setTop(buildNavBar());
        showHome();

        mainStage.setScene(new Scene(root, 1100, 800));
        mainStage.setTitle("Axono ReWire");
        mainStage.setResizable(true);
    }

    // ── Nav Bar ───────────────────────────────────────────────────────────────
    private HBox buildNavBar() {
        Label logo = new Label("⚡  Axono ReWire");
        logo.setStyle(
                "-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        HBox.setHgrow(logo, Priority.ALWAYS);

        homeBtn = navButton("Home");
        dashBtn = navButton("Dashboard");

        homeBtn.setOnAction(e -> showHome());
        dashBtn.setOnAction(e -> showDashboard());

        HBox nav = new HBox(8, logo, homeBtn, dashBtn);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(12, 24, 12, 24));
        nav.setStyle("-fx-background-color: " + NAV_BG + ";");
        return nav;
    }

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

    private void setActive(Button btn) {
        if (activeNavBtn != null)
            activeNavBtn.setStyle(inactiveStyle());
        activeNavBtn = btn;
        btn.setStyle(activeStyle());
    }

    private String inactiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: white;" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-padding: 6px 16px; -fx-background-radius: 4px; -fx-cursor: hand;";
    }

    private String hoverStyle() {
        return "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white;" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-padding: 6px 16px; -fx-background-radius: 4px; -fx-cursor: hand;";
    }

    private String activeStyle() {
        return "-fx-background-color: rgba(255,255,255,0.28); -fx-text-fill: white;" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-padding: 6px 16px; -fx-background-radius: 4px; -fx-cursor: hand;";
    }

    // ── View Switching ────────────────────────────────────────────────────────
    private void showHome() {
        root.setCenter(new HomepageView());
        setActive(homeBtn);
    }

    private void showDashboard() {
        root.setCenter(new DashboardView(profile));
        setActive(dashBtn);
    }
}
