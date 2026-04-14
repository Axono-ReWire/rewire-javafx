package com.axono.signup;

import com.axono.model.UserProfile;
import com.axono.ui.UITheme;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.event.*;
import java.time.*;

public class SignUpView extends ScrollPane {
    private static final String BG_COLOR_STYLE = "-fx-background-color: ";

    private final UserProfile profile;
    private TextField firstName;
    private TextField lastName;
    private TextField username;
    private ComboBox<String> yearOfStudy;
    private PasswordField password = new PasswordField();
    private PasswordField passcheck = new PasswordField();
    private DatePicker dateOfBirth = new DatePicker();
    private String passwordstr;
    private String passcheckstr;

    public SignUpView(UserProfile profile) {
        this.profile = profile;
        buildUI();
    }

    private void buildUI() {
        VBox content = new VBox(40); // creates box for user inputs
        firstName = styledField("First name");
        lastName = styledField("Last name");
        username = styledField("Username");
        yearOfStudy = new ComboBox<>();
        yearOfStudy.getItems().addAll("Select", "Foundation", "Year 1", "Year 2", "Year 3", "Year 4", "Post Graduate");
        yearOfStudy.getSelectionModel().selectFirst();
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

        Label dateOfBirthLabel = new Label("dd/mm/yyyy");
        VBox sd = new VBox(7,
                createLabel("First Name"), firstName, gap(8),
                createLabel("Last Name"), lastName, gap(8),
                createLabel("Date of Birth (dd/mm/yyyy)"), dateOfBirth,
                createLabel("Username"), username, gap(8),
                createLabel("Year of Study"), yearOfStudy,
                createLabel("Password"), password,
                createLabel("Confirm Password"), passcheck);
        sd.setMaxWidth(500);
        sd.setStyle(UITheme.CARD_STYLE);
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                LocalDate i = dateOfBirth.getValue();
                dateOfBirthLabel.setText("" + i);
            }

        };
        dateOfBirth.setShowWeekNumbers(true);
        dateOfBirth.setOnAction(event);

        return sd;

    }

    private Label createLabel(String text) {
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
        passwordstr = password.getText();
        passcheckstr = passcheck.getText();
        if (firstName.getText().trim().isEmpty()) {
            warn("Please enter First name");
            firstName.requestFocus();
            return false;
        }
        if (lastName.getText().trim().isEmpty()) {
            warn("Please enter Last name");
            lastName.requestFocus();
            return false;
        }
        if (username.getText().trim().isEmpty()) {
            warn("Please enter Username");
            username.requestFocus();
            return false;
        }
        if (yearOfStudy.getSelectionModel().getSelectedIndex() == 0) {
            warn("Please select Year of Study");
            return false;
        }

        if (dateOfBirth.getValue() == null) {
            warn("Please select Date of Birth");
            return false;
        }

        if (dobcheck.isBefore(dobirthLD)) {
            warn("You must be over the age of 13");
            return false;
        }

        if (password.getText().isEmpty()) {
            warn("Please enter Password");
            return false;
        }
        if (!passwordstr.equals(passcheckstr)) {
            warn("Passwords do not match");
            return false;
        }

        return true;
    }

    public void saveData() {
        profile.setName(firstName.getText().trim());
        profile.setYearOfStudy(yearOfStudy.getValue());
        profile.setInstitution(lastName.getText().trim());
    }

    private void warn(String msg) {
        Alert req = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        req.setHeaderText(("Required Field"));
        req.showAndWait();
    }
}
