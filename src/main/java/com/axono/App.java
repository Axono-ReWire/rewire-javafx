package com.axono;

import com.axono.auth.Session;
import com.axono.database.SchemaInitializer;
import com.axono.auth.LoginView;
import com.axono.onboarding.OnboardingStage;
import com.axono.ui.ThemeManager;
import com.axono.ui.UIConstants;
import java.io.IOException;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for Axono ReWire.
 * Initialises the database schema, then shows the welcome view if not
 * authenticated, or the main app if already logged in.
 * Once a user authenticates, the primary stage is handed to
 * {@link AppStage} which builds the main browser/dashboard UI.
 */
public class App extends Application {

    /** The primary JavaFX stage, retained so we can swap scenes post-login. */
    private Stage primaryStage;

    /**
     * Initialises the database schema and presents the appropriate screen
     * based on authentication state.
     *
     * @param stage the top-level JavaFX window provided by the runtime.
     */
    @Override
    public void start(final Stage stage) {
        this.primaryStage = stage;
        if (!initSchema()) {
            return;
        }
        if (Session.isAuthenticated()) {
            showApp();
        } else {
            showWelcome();
        }
    }

    /**
     * Runs the schema initialiser. Shows a fatal-error alert if the DB
     * could not be set up.
     *
     * @return {@code true} if the schema is ready; {@code false} otherwise.
     */
    private boolean initSchema() {
        try {
            SchemaInitializer.init();
            return true;
        } catch (SQLException | IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Could not initialise the database:\n" + ex.getMessage(),
                    ButtonType.OK);
            alert.setHeaderText("Database error");
            alert.showAndWait();
            return false;
        }
    }

    /** Builds and shows the onboarding wizard on the primary stage. */
    private void showWelcome() {
        primaryStage.setTitle("ReWire — Setup");
        new OnboardingStage(primaryStage, this::onAuthSuccess, this::showLogin);
    }

    /** Builds and shows the {@link LoginView} on the primary stage. */
    void showLogin() {
        LoginView login = new LoginView(this::onAuthSuccess, this::showWelcome);
        Scene scene = new Scene(login,
                UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        String css = getClass()
                .getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        ThemeManager.register(scene);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Axono ReWire — Sign In");
        primaryStage.show();
    }

    /** Shows the main app after authentication. */
    private void showApp() {
        new AppStage(primaryStage, this::showLogin);
    }

    /** Hands the primary stage to {@link AppStage} after authentication. */
    private void onAuthSuccess() {
        showApp();
    }

    /**
     * Main method – delegates to {@link javafx.application.Application#launch}.
     *
     * @param args command-line arguments (unused).
     */
    public static void main(final String[] args) {
        launch();
    }
}
