package com.axono.signup;

import java.time.LocalDate;

import com.axono.model.UserProfile;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Onboarding step that collects user registration details including name,
 * date of birth, username, year of study, and password. Validates all fields
 * before allowing the user to proceed and saves data to a
 * {@link UserProfile}.
 */
public final class SignUpView extends ScrollPane {

    /** Minimum age requirement for users to sign up. */
    private static final long MINIMUM_AGE = 13;

    /** Bottom margin applied to the sign-up title, in pixels. */
    private static final int TITLE_BOTTOM_MARGIN = 16;

    /** Spacing used in the banner VBox, in pixels. */
    private static final int BANNER_SPACING = 16;

    /** The user profile to populate when {@link #saveData()} is called. */
    private final UserProfile profile;

    /** Input field for the user's first name. */
    private TextField firstName;

    /** Input field for the user's last name. */
    private TextField lastName;

    /** Input field for the user's chosen username. */
    private TextField username;

    /** Dropdown for the user's year of study selection. */
    private ComboBox<String> yearOfStudy;

    /** Masked input field for the user's password. */
    private PasswordField password = new PasswordField();

    /** Masked input field for confirming the user's password. */
    private PasswordField passcheck = new PasswordField();

    /** Date picker for the user's date of birth. */
    private DatePicker dateOfBirth = new DatePicker();

    /**
     * Constructs the {@code SignUpView} for the given user profile.
     *
     * @param userProfile the {@link UserProfile} to populate on save.
     */
    public SignUpView(final UserProfile userProfile) {
        this.profile = userProfile;
        buildUI();
    }

    /**
     * Builds the full scrollable sign-up layout including the header
     * banner and the input form.
     */
    private void buildUI() {
        VBox content = new VBox();
        firstName = styledField("First name");
        lastName = styledField("Last name");
        username = styledField("Username");
        yearOfStudy = new ComboBox<>();
        yearOfStudy.getItems().addAll(
                "Select", "Foundation", "Year 1", "Year 2",
                "Year 3", "Year 4", "Post Graduate");
        yearOfStudy.getSelectionModel().selectFirst();

        content.getStyleClass().add("signupcon");

        content.getChildren().addAll(
                signupcon(),
                signupinput());

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getStyleClass().add("grad-back");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);
        setBorder(Border.EMPTY);

    }

    /**
     * Builds and returns the sign-up heading banner.
     *
     * @return a {@link VBox} containing the "Sign up" title label.
     */
    private VBox signupcon() {
        Label titleSU = new Label("Sign up");
        titleSU.getStyleClass().add("header2");
        VBox.setMargin(titleSU, new Insets(0, 0,
                TITLE_BOTTOM_MARGIN, 0));
        VBox banner = new VBox(BANNER_SPACING, titleSU);
        banner.setAlignment(Pos.CENTER);
        return banner;
    }

    /**
     * Builds and returns the input form card containing
     * all registration fields and the date-of-birth picker action handler.
     *
     * @return a {@link VBox} card containing all input controls.
     */
    public VBox signupinput() {

        Label dateOfBirthLabel = new Label("dd/mm/yyyy");
        VBox sd = new VBox(
                new Label("First"), firstName,
                new Label("Last Name"), lastName,
                new Label("Date of Birth (dd/mm/yyyy)"), dateOfBirth,
                new Label("Username"), username,
                new Label("Year of Study"), yearOfStudy,
                new Label("Password"), password,
                new Label("Confirm Password"), passcheck);
        sd.getStyleClass().add("sd");
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                LocalDate i = dateOfBirth.getValue();
                dateOfBirthLabel.setText("" + i);
            }

        };
        dateOfBirth.setShowWeekNumbers(true);
        dateOfBirth.setOnAction(event);

        return sd;

    }

    /**
     * Creates a styled {@link TextField} with the given prompt text.
     *
     * @param prompt the placeholder text shown when the field is empty.
     * @return a configured {@link TextField}.
     */
    private TextField styledField(final String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("textbox-s");
        return f;
    }

    /**
     * Creates an invisible spacer {@link Region} of the specified height.
     *
     * @param h the preferred height in pixels.
     * @return a {@link Region} acting as a vertical spacer.
     */
    private Region gap(final double h) {
        Region r = new Region();
        r.setPrefHeight(h);
        return r;
    }

    /**
     * Validates all sign-up form fields.
     * Displays a warning alert for the first invalid field found and returns
     * {@code false} without saving any data.
     *
     * @return {@code true} if all fields are valid; {@code false} otherwise.
     */
    public boolean validateInput() {
        String passwordstr = password.getText();
        String passcheckstr = passcheck.getText();
        LocalDate dobirthLD = dateOfBirth.getValue();
        LocalDate dobcheck = LocalDate.now().minusYears(MINIMUM_AGE);
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

    /**
     * Saves the validated form data into the associated {@link UserProfile}.
     * Must only be called after {@link #validateInput()} returns {@code true}.
     */
    public void saveData() {
        profile.setName(firstName.getText().trim());
        profile.setYearOfStudy(yearOfStudy.getValue());
        profile.setInstitution(lastName.getText().trim());
    }

    /**
     * Displays a warning {@link Alert} dialog with the given message.
     *
     * @param msg the warning message to display to the user.
     */
    public void warn(final String msg) {
        Alert req = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        req.setHeaderText(("Required Field"));
        req.showAndWait();
        req.initOwner(null);
    }
}
