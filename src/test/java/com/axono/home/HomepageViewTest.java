package com.axono.home;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.axono.ui.UIConstants;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Unit and structural integration tests for the {@link HomepageView} layout.
 * Leverages TestFX explicit selectors and method reference patterns to test the
 * UI
 * on the JavaFX Application Thread.
 */
@ExtendWith(ApplicationExtension.class)
class HomepageViewTest {

    /** The homepage view instance under test. */
    private HomepageView homepageView;

    /**
     * Initializes the JavaFX Stage window context and mounts the
     * {@link HomepageView} inside a testing scene wrapper before executing tests.
     *
     * @param stage the automated Stage provided by TestFX.
     */
    @Start
    void start(Stage stage) {
        this.homepageView = new HomepageView();

        Scene scene = new Scene(new StackPane(homepageView), UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
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
     * Confirms that the main homepage layout structure instantiates safely.
     */
    @Test
    void testInitialization() {
        assertNotNull(homepageView, "HomepageView structural instance should be fully initialized.");
    }

    /**
     * Asserts that foundational structural text headings and description strings
     * are loaded and discoverable on the live window canvas.
     */
    @Test
    void testBannerLabelsRender(FxRobot robot) {
        assertNotNull(homepageView);

        // Query labeled string elements right off the visible scene graph
        Label mainHeader = robot.lookup("Welcome to Axono ReWire!").queryAs(Label.class);
        Label subHeader = robot.lookup("Start your engineering journey today!").queryAs(Label.class);
        Label loginText = robot.lookup("Already have an account? Log in").queryAs(Label.class);

        assertNotNull(mainHeader, "Missing the main banner 'Welcome to Axono ReWire!' header title.");
        assertNotNull(subHeader, "Missing the introductory sub-heading description message.");
        assertNotNull(loginText, "Missing the account fallback login navigation prompt string.");
    }

    /**
     * Verifies that the primary onboarding role selection buttons are initialized
     * and visible inside the header layout area.
     */
    @Test
    void testRoleSelectionButtonsExist(FxRobot robot) {
        assertNotNull(homepageView);

        Button studentBtn = robot.lookup("I'm a Student").queryAs(Button.class);
        Button educatorBtn = robot.lookup("I'm an Educator").queryAs(Button.class);

        assertNotNull(studentBtn, "Missing the 'I'm a Student' action navigation item button.");
        assertNotNull(educatorBtn, "Missing the 'I'm an Educator' action navigation item button.");
    }

    /**
     * Verifies that the target subject module name heading displays prominently
     * above the scrollable interactive options area.
     */
    @Test
    void testModuleHeaderLabel(FxRobot robot) {
        assertNotNull(homepageView);

        Label moduleHeader = robot.lookup("Analogue Electronics").queryAs(Label.class);
        assertNotNull(moduleHeader, "Missing the 'Analogue Electronics' main list section category label.");
    }

    /**
     * Iterates over every individual topic option inside the internal layout
     * definitions array
     * to ensure the application dynamically constructs interactive controls for
     * every string entry.
     */
    @Test
    void testDynamicTopicButtonsPopulate(FxRobot robot) {
        assertNotNull(homepageView);

        // Explicit list of all required topics defined inside the application view
        // logic component context
        String[] expectedTopics = {
                "Layouts", "Decibels", "Op-Amps", "Electromagnetism",
                "Phasors", "Complex Impedances", "Kirchhoff's Laws",
                "Passive Networks", "Dividers", "Equivalent Networks",
                "Circuit Analysis", "Op-Amp Bandwidths", "Poles and Zeros",
                "Frequency Response", "Step Response"
        };

        // Assert that every single individual button item is fully rendered and
        // selectable on the active tree
        for (String topicText : expectedTopics) {
            Button topicButton = robot.lookup(topicText).queryAs(Button.class);
            assertNotNull(topicButton, "Dynamic button went missing for expected topic entry: " + topicText);
        }
    }

    /**
     * Captures and evaluates inline style string metrics to verify that mouse hover
     * events dynamically transition component design characteristics on entering
     * layout coordinates.
     */
    @Test
    void testButtonHoverStyleTransitions(FxRobot robot) {
        assertNotNull(homepageView);

        // Fetch a control handle directly off the active canvas node tree
        Button actionButton = robot.lookup("I'm a Student").queryAs(Button.class);
        assertNotNull(actionButton, "Target button must exist to verify interactive cursor styling.");

        // Capture initial baseline design rules tracking characteristics
        String baselineStyle = actionButton.getStyle();

        // Simulate a physical user dragging the pointer onto the target footprint area
        // coordinates
        robot.moveTo(actionButton);
        String activeHoverStyle = actionButton.getStyle();

        // Pull the cursor clear of the element dimensions completely to fire standard
        // exit hooks
        robot.moveBy(200, 200);
        String restoredStyle = actionButton.getStyle();

        // Assert inline styles adjusted into functional highlight states and dropped
        // cleanly on exit
        assertTrue(activeHoverStyle.contains("white") || !activeHoverStyle.equals(baselineStyle),
                "Inline styles failed to activate background modifications when cursor entered button bounds.");
        assertEquals(baselineStyle, restoredStyle,
                "Button inline styles failed to restore baseline aesthetics on cursor exit.");
    }
}