package com.axono.onboarding;

import com.axono.model.UserProfile;
import com.axono.ui.UITheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ProfileView extends StackPane {

    private final UserProfile profile;
    private TextField nameField;
    private ComboBox<String> yearCombo;
    private TextField institutionField;

    public ProfileView(UserProfile profile) {
        this.profile = profile;
        buildUI();
    }

    private void buildUI() {
        setStyle("-fx-background-color: " + UITheme.BG + ";");
        setPadding(new Insets(30));

        Label heading = new Label("Your Profile");
        heading.setStyle("-fx-text-fill: " + UITheme.PRIMARY + ";" +
                "-fx-font-size: 20px; -fx-font-weight: bold;");
        VBox.setMargin(heading, new Insets(0, 0, 16, 0));

        nameField = styledField("e.g. Alex Johnson");
        institutionField = styledField("e.g. University of York");

        yearCombo = new ComboBox<>();
        yearCombo.getItems().addAll(
                "— Select —", "Year 1", "Year 2", "Year 3", "Year 4 / Masters");
        yearCombo.getSelectionModel().selectFirst();
        yearCombo.setPrefWidth(400);
        yearCombo.setPrefHeight(36);
        yearCombo.setStyle("-fx-font-size: 14px;");

        VBox card = new VBox(4,
                heading,
                fieldLabel("Full Name *"), nameField, gap(8),
                fieldLabel("Year of Study *"), yearCombo, gap(8),
                fieldLabel("Institution (optional)"), institutionField);
        card.setMaxWidth(500);
        card.setStyle(UITheme.CARD_STYLE);

        setAlignment(Pos.CENTER);
        getChildren().add(card);
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + UITheme.TEXT_DARK + "; -fx-font-size: 14px;");
        VBox.setMargin(l, new Insets(4, 0, 2, 0));
        return l;
    }

    private TextField styledField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefSize(400, 36);
        f.setStyle("-fx-font-size: 14px;" +
                "-fx-border-color: " + UITheme.BORDER + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;");
        return f;
    }

    private Region gap(double h) {
        Region r = new Region();
        r.setPrefHeight(h);
        return r;
    }

    public boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            warn("Please enter your full name.");
            nameField.requestFocus();
            return false;
        }
        if (yearCombo.getSelectionModel().getSelectedIndex() == 0) {
            warn("Please select your year of study.");
            return false;
        }
        return true;
    }

    public void saveData() {
        profile.setName(nameField.getText().trim());
        profile.setYearOfStudy(yearCombo.getValue());
        profile.setInstitution(institutionField.getText().trim());
    }

    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText("Required Field");
        a.showAndWait();
    }
}
