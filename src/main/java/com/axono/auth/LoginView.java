package com.axono.auth;

import com.axono.ui.UIConstants;
import java.sql.SQLException;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Root authentication view shown on application launch. Provides a
 * username/password login form with inline validation, and a Sign Up
 * link that launches the existing onboarding wizard.
 *
 * <p>On successful login or signup, the {@link Session} is populated and
 * the {@code onAuthSuccess} callback is invoked so the host can swap the
 * scene to the main app UI.
 */
public final class LoginView extends StackPane {


    /** Callback fired after a successful login or signup. */
    private final Runnable onAuthSuccess;

    /** Callback fired when the user clicks the Sign Up link. */
    private final Runnable onSignup;

    /** Stateless auth service for login + signup operations. */
    private final AuthService authService = new AuthService();

    /** Username input field. */
    private TextField usernameField;

    /** Password input field. */
    private PasswordField passwordField;

    /** Inline error message, hidden by default. */
    private Label errorLabel;

    /**
     * Constructs a {@code LoginView}.
     *
     * @param onAuthSuccessCallback callback invoked once {@link Session}
     *                              has been populated with a valid user.
     * @param onSignupCallback      callback invoked when Sign Up is clicked.
     */
    public LoginView(final Runnable onAuthSuccessCallback,
            final Runnable onSignupCallback) {
        this.onAuthSuccess = onAuthSuccessCallback;
        this.onSignup = onSignupCallback == null ? () -> { } : onSignupCallback;
        getStyleClass().add("grad-back");
        setPadding(new Insets(UIConstants.SPACING_6XL));
        getChildren().add(buildCard());
    }

    /**
     * Constructs a {@code LoginView} without a signup navigation callback.
     *
     * @param onAuthSuccessCallback callback invoked once {@link Session}
     *                              has been populated with a valid user.
     */
    public LoginView(final Runnable onAuthSuccessCallback) {
        this(onAuthSuccessCallback, null);
    }

    /**
     * Builds the centred card containing the form fields and buttons.
     *
     * @return the login card VBox.
     */
    private VBox buildCard() {
        Label title = new Label("Welcome to Axono ReWire");
        title.getStyleClass().add("text-dark");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitle = new Label("Log in to continue.");
        subtitle.getStyleClass().add("text-muted");
        subtitle.setStyle("-fx-font-size: 14px;");

        usernameField = styledField("Username");
        passwordField = styledPasswordField("Password");

        errorLabel = new Label();
        errorLabel.getStyleClass().add("text-error");
        errorLabel.setStyle("-fx-font-size: 13px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        VBox card = new VBox(UIConstants.SPACING_LG,
                title, subtitle,
                gap(UIConstants.SPACING_MD),
                fieldLabel("Username"), usernameField,
                fieldLabel("Password"), passwordField,
                errorLabel,
                buildLoginButton(),
                buildSignupLink());
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(UIConstants.FORM_MAX_WIDTH);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 28px 40px;");
        return card;
    }

    /**
     * Creates a small label used immediately above a form field.
     *
     * @param text the label text.
     * @return a styled {@link Label}.
     */
    private Label fieldLabel(final String text) {
        Label l = new Label(text);
        l.getStyleClass().add("text-dark");
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }

    /**
     * Builds the primary Login button.
     *
     * @return the configured {@link Button}.
     */
    private Button buildLoginButton() {
        Button login = new Button("Log In");
        login.setMaxWidth(Double.MAX_VALUE);
        login.getStyleClass().add("btn-primary");
        login.setOnAction(e -> handleLogin());
        HBox.setHgrow(login, Priority.ALWAYS);
        return login;
    }

    /**
     * Builds the row containing the "Sign up" link.
     *
     * @return the sign-up link HBox.
     */
    private HBox buildSignupLink() {
        Label prompt = new Label("Don't have an account?");
        prompt.getStyleClass().add("text-muted");
        prompt.setStyle("-fx-font-size: 13px;");

        Hyperlink signup = new Hyperlink("Sign up");
        signup.getStyleClass().addAll("text-primary", "bold");
        signup.setOnAction(e -> handleSignup());

        HBox row = new HBox(UIConstants.SPACING_SM, prompt, signup);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** Reads form input, calls AuthService.login, routes on success. */
    private void handleLogin() {
        clearError();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showError("Please enter your username");
            usernameField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }

        try {
            Optional<User> user = authService.login(username, password);
            if (user.isPresent()) {
                Session.set(user.get());
                onAuthSuccess.run();
            } else {
                showError("Invalid username or password");
                passwordField.clear();
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    /** Opens the onboarding wizard in a new stage for signup. */
    private void handleSignup() {
        onSignup.run();
    }

    /**
     * Displays the given error message in the inline error label.
     *
     * @param msg the error text.
     */
    private void showError(final String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /** Hides the inline error label and clears its text. */
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Creates a styled text input field with a prompt.
     *
     * @param prompt the prompt text.
     * @return the configured {@link TextField}.
     */
    private TextField styledField(final String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefHeight(UIConstants.FIELD_HEIGHT);
        return f;
    }

    /**
     * Creates a styled password input field with a prompt.
     *
     * @param prompt the prompt text.
     * @return the configured {@link PasswordField}.
     */
    private PasswordField styledPasswordField(final String prompt) {
        PasswordField p = new PasswordField();
        p.setPromptText(prompt);
        p.setPrefHeight(UIConstants.FIELD_HEIGHT);
        return p;
    }

    /**
     * Creates an invisible spacer region of the given height.
     *
     * @param h the preferred height in pixels.
     * @return a {@link Region} acting as a vertical spacer.
     */
    private Region gap(final double h) {
        Region r = new Region();
        r.setPrefHeight(h);
        return r;
    }
}
