package com.axono.onboarding;

import java.time.LocalDate;

import com.axono.auth.UserProfile;
import com.axono.ui.UIConstants;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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

    /** The user profile to populate when {@link #saveData()} is called. */
    private final UserProfile profile;

    /** Callback invoked when the user clicks the Back button. */
    private final Runnable backHandler;

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
     * Constructs the {@code SignUpView} for the given user profile with
     * an optional back button handler.
     *
     * @param userProfile the {@link UserProfile} to populate on save.
     * @param onBack      the {@link Runnable} to invoke when the back button
     *                    is clicked (may be null; if so, button is disabled).
     */
    public SignUpView(final UserProfile userProfile, final Runnable onBack) {
        this.profile = userProfile;
        this.backHandler = onBack == null ? () -> { } : onBack;
        buildUI();
    }

    /**
     * Backward-compatible constructor for {@code SignUpView} without
     * a back button handler.
     *
     * @param userProfile the {@link UserProfile} to populate on save.
     */
    public SignUpView(final UserProfile userProfile) {
        this(userProfile, null);
    }

    /**
     * Builds the full scrollable sign-up layout including the header
     * banner and the input form.
     */
    private void buildUI() {
        VBox content = new VBox(UIConstants.SPACING_8XL);
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
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H,
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H));
        content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        content.getStyleClass().add("bg-transparent");
        content.getChildren().addAll(
                buildHeader(),
                buildForm());

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getStyleClass().add("bg-transparent");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);
        setBorder(Border.EMPTY);
        getStyleClass().add("bg-transparent");
    }

    /**
     * Builds and returns the sign-up heading banner with optional back button.
     *
     * @return a {@link VBox} containing the back button and "Sign up" title.
     */
    private VBox buildHeader() {
        // Back button
        Button back = new Button("← Back to Login");
        back.getStyleClass().add("btn-back");
        back.setOnAction(e -> backHandler.run());

        // Title label
        Label thisLabel = new Label("Sign up");
        thisLabel.getStyleClass().add("text-dark");
        thisLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        VBox.setMargin(thisLabel, new Insets(0, 0,
                UIConstants.SPACING_2XL, 0));

        // Banner with back button and title
        VBox banner = new VBox(UIConstants.SPACING_3XL, back, thisLabel);
        banner.setAlignment(Pos.CENTER);
        return banner;
    }

    /**
     * Builds and returns the input form card containing
     * all registration fields and the date-of-birth picker action handler.
     *
     * @return a {@link VBox} card containing all input controls.
     */
    public VBox buildForm() {

        Label dateOfBirthLabel = new Label("dd/mm/yyyy");
        VBox sd = new VBox(UIConstants.SPACING_MD,
                createLabel("First Name"), firstName,
                gap(UIConstants.SPACING_MD),
                createLabel("Last Name"), lastName,
                gap(UIConstants.SPACING_MD),
                createLabel("Date of Birth (dd/mm/yyyy)"), dateOfBirth,
                createLabel("Username"), username,
                gap(UIConstants.SPACING_MD),
                createLabel("Year of Study"), yearOfStudy,
                createLabel("Password"), password,
                createLabel("Confirm Password"), passcheck);
        sd.setMaxWidth(UIConstants.FORM_MAX_WIDTH);
        sd.getStyleClass().add("card");
        sd.setStyle("-fx-padding: 28px 40px;");
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
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
     * Creates a styled form label with the given text.
     *
     * @param text the label text to display.
     * @return a configured {@link Label}.
     */
    private Label createLabel(final String text) {
        Label l = new Label(text);
        l.getStyleClass().add("text-dark");
        l.setStyle("-fx-font-size: 14px;");
        VBox.setMargin(l, new Insets(
                UIConstants.SPACING_XS, 0, 2, 0));
        return l;
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
        f.setPrefSize(UIConstants.SIGNUP_IMG_WIDTH,
                UIConstants.FIELD_HEIGHT);
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
        profile.setFirstName(firstName.getText().trim());
        profile.setLastName(lastName.getText().trim());
        profile.setUsername(username.getText().trim());
        profile.setPassword(password.getText());
        profile.setYearOfStudy(yearOfStudy.getValue());
    }

    /**
     * Displays a server-side signup error (e.g. duplicate username)
     * as a warning dialog. Called by {@link OnboardingStage}
     * when {@link com.axono.auth.AuthService#signup} rejects the input.
     *
     * @param msg the error message to show.
     */
    public void showSignupError(final String msg) {
        warn(msg);
        username.requestFocus();
    }

    /**
     * Displays a warning {@link Alert} dialog with the given message.
     *
     * @param msg the warning message to display to the user.
     */
    private void warn(final String msg) {
        Alert req = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        req.setHeaderText(("Required Field"));
        req.showAndWait();
    }
}
