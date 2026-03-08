package com.axono.signup;

//import java.beans.EventHandler;

import com.axono.model.UserProfile;
import com.axono.ui.UITheme;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.*;
import java.time.*;
import java.time.chrono.*;

public class SignUpView extends ScrollPane {
    private static final String BG_COLOR_STYLE = "-fx-background-color: ";

    private final UserProfile profile;
    private TextField FirstnameIn;
    private TextField LastnameIn;
    private TextField UserIn;
    private ComboBox<String> YearIn;
    private PasswordField PassIn = new PasswordField();
    private DatePicker DobIn = new DatePicker();

    public SignUpView(UserProfile profile) {
        this.profile = profile;
        buildUI();
    }

    private void buildUI() {
        VBox content = new VBox(40);
        FirstnameIn = styledField("First name");
        LastnameIn = styledField("Last name");
        UserIn = styledField("Username");
        YearIn = new ComboBox<>();
        YearIn.getItems().addAll("Select", "Foundation", "Year 1", "Year 2", "Year 3", "Year 4", "Post Graduate");
        YearIn.getSelectionModel().selectFirst();
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(60, 20, 60, 20));
        content.setMaxWidth(800);
        content.setStyle(BG_COLOR_STYLE + UITheme.BG + ";");
        content.getChildren().addAll(
                signupcon(),
                signupinput());

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle(BG_COLOR_STYLE + UITheme.BG + ";");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);
        setBorder(Border.EMPTY);
        setStyle(BG_COLOR_STYLE + UITheme.BG + ";");
    }

    private VBox signupcon() {
        Label thisLabel = new Label("Sign up");
        thisLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + UITheme.TEXT_DARK + ";");
        VBox.setMargin(thisLabel, new Insets(0, 0, 16, 0));
        VBox banner = new VBox(20, thisLabel);
        banner.setAlignment(Pos.CENTER);
        return banner;
    }

    public VBox signupinput() {

        Label DobLab = new Label("dd/mm/yyyy");
        VBox sd = new VBox(6,
                InLabel("First Name"), FirstnameIn, gap(8),
                InLabel("Last Name"), LastnameIn, gap(8),
                InLabel("Date of Birth (dd/mm/yyyy"), DobIn,
                InLabel("Username"), UserIn, gap(8),
                InLabel("Year of Study"), YearIn,
                InLabel("Password"), PassIn);
        sd.setMaxWidth(500);
        sd.setStyle(UITheme.CARD_STYLE);
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                LocalDate i = DobIn.getValue();
                DobLab.setText("" + i);
            }

        };
        DobIn.setShowWeekNumbers(true);
        DobIn.setOnAction(event);

        return sd;

    }

    private Label InLabel(String text) {
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
        if (FirstnameIn.getText().trim().isEmpty()) {
            warn("Please enter First name");
            FirstnameIn.requestFocus();
            return false;
        }
        if (LastnameIn.getText().trim().isEmpty()) {
            warn("Please enter Last name");
            LastnameIn.requestFocus();
            return false;
        }
        if (UserIn.getText().trim().isEmpty()) {
            warn("Please enter Username");
            UserIn.requestFocus();
            return false;
        }
        if (YearIn.getSelectionModel().getSelectedIndex() == 0) {
            warn("Please select Year of Study");
            return false;
        }

        if (DobIn.getValue() == null) {
            warn("Please select Date of Birth");
            return false;
        }

        if (PassIn.getText().isEmpty()) {
            warn("Please enter Password");
            return false;
        }

        return true;
    }

    public void saveData() {
        profile.setName(FirstnameIn.getText().trim());
        profile.setYearOfStudy(YearIn.getValue());
        profile.setInstitution(LastnameIn.getText().trim());
    }

    private void warn(String msg) {
        Alert req = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        req.setHeaderText(("Required Field"));
        req.showAndWait();
    }
    /*
     * Label sectionLabel(String text, int size) {
     * Label l = new Label(text);
     * l.setStyle(String.format(
     * "-fx-font-size: %dpx; -fx-font-weight: bold; -fx-text-fill: %s;", size,
     * UITheme.TEXT_DARK));
     * return l;
     * }
     */

}
