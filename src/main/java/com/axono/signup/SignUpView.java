package com.axono.signup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.axono.database.Database;
import com.axono.model.UserProfile;
import com.axono.ui.UIConstants;
import com.axono.ui.UITheme;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class SignUpView extends ScrollPane {

    private static final String BG_COLOR_STYLE = "-fx-background-color: ";
    private static final long MINIMUM_AGE = 13;

    private final UserProfile profile;

    private TextField firstName;
    private TextField lastName;
    private TextField username;
    private ComboBox<String> yearOfStudy;
    private PasswordField password = new PasswordField();
    private PasswordField passcheck = new PasswordField();
    private DatePicker dateOfBirth = new DatePicker();

    public SignUpView(final UserProfile userProfile) {
        this.profile = userProfile;
        buildUI();
    }

    private void buildUI() {
        VBox content = new VBox(UIConstants.SPACING_3XL);

        firstName = styledField("First name");
        lastName = styledField("Last name");
        username = styledField("Username");

        yearOfStudy = new ComboBox<>();
        yearOfStudy.getItems().addAll(
                "Select", "Foundation", "Year 1", "Year 2",
                "Year 3", "Year 4", "Post Graduate");
        yearOfStudy.getSelectionModel().selectFirst();

        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(
                UIConstants.CONTENT_PADDING_V,
                UIConstants.PADDING_MD,
                UIConstants.CONTENT_PADDING_V,
                UIConstants.PADDING_MD));
        content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        content.setStyle(BG_COLOR_STYLE + UITheme.BG + ";");

        content.getChildren().addAll(
                signupHeader(),
                signupForm());

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle(BG_COLOR_STYLE + UITheme.BG + ";");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);
        setBorder(Border.EMPTY);
        setStyle(BG_COLOR_STYLE + UITheme.BG + ";");
    }

    private VBox signupHeader() {
        Label title = new Label("Sign up");
        title.setStyle("-fx-font-size: 32px;"
                + "-fx-font-weight: bold; -fx-text-fill: "
                + UITheme.TEXT_DARK + ";");

        VBox banner = new VBox(title);
        banner.setAlignment(Pos.CENTER);
        return banner;
    }

    private VBox signupForm() {

        VBox form = new VBox(UIConstants.FORM_ROW_SPACING,
                createLabel("First Name"), firstName,
                createLabel("Last Name"), lastName,
                createLabel("Date of Birth"), dateOfBirth,
                createLabel("Username"), username,
                createLabel("Year of Study"), yearOfStudy,
                createLabel("Password"), password,
                createLabel("Confirm Password"), passcheck);

        form.setMaxWidth(UIConstants.FORM_MAX_WIDTH);
        form.setStyle(UITheme.CARD_STYLE);

        dateOfBirth.setOnAction(e -> {
        });

        return form;
    }

    private Label createLabel(final String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: "
                + UITheme.TEXT_DARK + "; -fx-font-size: 14px;");
        return l;
    }

    private TextField styledField(final String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefSize(UIConstants.FIELD_PREF_WIDTH,
                UIConstants.FIELD_PREF_HEIGHT);
        return f;
    }

    private Region gap(final double h) {
        Region r = new Region();
        r.setPrefHeight(h);
        return r;
    }

    public boolean validateInput() {

        if (firstName.getText().trim().isEmpty()) {
            warn("Please enter First name");
            return false;
        }

        if (lastName.getText().trim().isEmpty()) {
            warn("Please enter Last name");
            return false;
        }

        if (username.getText().trim().isEmpty()) {
            warn("Please enter Username");
            return false;
        }

        if (usernameExists(username.getText().trim())) {
            warn("Username already exists");
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

        if (LocalDate.now().minusYears(MINIMUM_AGE)
                .isBefore(dateOfBirth.getValue())) {
            warn("You must be over 13");
            return false;
        }

        if (password.getText().isEmpty()) {
            warn("Enter password");
            return false;
        }

        if (!password.getText().equals(passcheck.getText())) {
            warn("Passwords do not match");
            return false;
        }

        return true;
    }

    public void saveData() {

        profile.setName(firstName.getText().trim());
        profile.setYearOfStudy(yearOfStudy.getValue());

        String sql = "INSERT INTO user " +
                "(username, password, firstname, lastname, date_of_birth, stage_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.getText().trim());
            stmt.setString(2, password.getText()); // ⚠️ plain text
            stmt.setString(3, firstName.getText().trim());
            stmt.setString(4, lastName.getText().trim());
            stmt.setString(5, dateOfBirth.getValue().toString());
            stmt.setInt(6, getStageId(yearOfStudy.getValue()));

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            warn("Database error while saving user");
        }
    }

    private int getStageId(String stageName) {

        String sql = "SELECT id FROM stage WHERE name = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, stageName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private boolean usernameExists(String username) {

        String sql = "SELECT 1 FROM user WHERE username = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void warn(final String msg) {
        Alert req = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        req.setHeaderText("Required Field");
        req.showAndWait();
    }
}
