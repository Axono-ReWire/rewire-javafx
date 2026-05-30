package com.axono.onboarding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.axono.ui.UIConstants;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Unit and structural integration tests for the {@link WelcomeView} wizard
 * card.
 * Uses TestFX explicit text selectors to reliably query and verify elements off
 * the live
 * scene graph on the JavaFX Application Thread.
 */
@ExtendWith(ApplicationExtension.class)
class WelcomeViewTest {

    /** The welcome layout step component instance under test. */
    private WelcomeView welcomeView;

    /**
     * Initializes the JavaFX Stage window context and mounts the
     * {@link WelcomeView} layout inside a testing scene wrapper before
     * executing tests.
     *
     * @param stage the automated Stage provided by TestFX.
     */
    @Start
    void start(final Stage stage) {
        this.welcomeView = new WelcomeView();

        Scene scene = new Scene(
                new StackPane(welcomeView),
                UIConstants.WINDOW_WIDTH,
                UIConstants.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * A basic sanity check to ensure the JUnit 5 test runner
     * is configured and executing properly.
     */
    @Test
    void sampleTest() {
        assertEquals(4, 2 + 2);
    }

    /**
     * Verifies that the primary parent WelcomeView layout shell instantiates
     * safely.
     */
    @Test
    void testInitialization() {
        assertNotNull(welcomeView,
                "WelcomeView structural instance should be fully initialized.");
    }

    /**
     * Asserts that all core introductory titles, logos, and instructional
     * labels render natively with their exact text contents using clean,
     * thread-safe text lookups.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testWelcomeCardElementsRender(final FxRobot robot) {
        assertNotNull(welcomeView);

        Label logoPlaceholder = robot.lookup("Axono Logo Placeholder")
                .queryAs(Label.class);
        Label mainTitle = robot.lookup("Welcome to ReWire!")
                .queryAs(Label.class);
        Label bodyMessage = robot.lookup(
                "The all-in-one platform for mastering all things "
                        + "Engineering.")
                .queryAs(Label.class);
        Label instructionHint = robot.lookup(
                "Click Next to set up your profile and get started.")
                .queryAs(Label.class);

        assertNotNull(
                logoPlaceholder,
                "Missing the 'Axono Logo Placeholder' graphical text label "
                        + "layout component.");
        assertNotNull(
                mainTitle,
                "Missing the primary 'Welcome to ReWire!' header title label.");
        assertNotNull(
                bodyMessage,
                "Missing the central onboarding platform descriptive body "
                        + "message text.");
        assertNotNull(
                instructionHint,
                "Missing the structural layout workflow guidance hint text "
                        + "label.");
    }

}
