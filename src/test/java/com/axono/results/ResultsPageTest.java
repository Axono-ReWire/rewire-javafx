package com.axono.results;

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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Unit and structural integration tests for the {@link ResultsPage} view.
 * Uses TestFX to manage the JavaFX scene graph lifecycle and simulate user
 * interactions
 * using explicit node selectors and thread-safe operations.
 */
@ExtendWith(ApplicationExtension.class)
class ResultsPageTest {

    /** Expected result of basic arithmetic operation (2 + 2). */
    private static final int ARITHMETIC_RESULT = 4;

    /** Mouse move offset for hover testing. */
    private static final int HOVER_OFFSET = 300;

    /** The main results view component instance under test. */
    private ResultsPage resultsPage;

    /**
     * Initializes the JavaFX Stage window context and mounts the
     * {@link ResultsPage} inside a testing scene wrapper before executing
     * tests.
     *
     * @param stage the automated Stage provided by TestFX.
     */
    @Start
    void start(final Stage stage) {
        this.resultsPage = new ResultsPage();

        Scene scene = new Scene(
                new StackPane(resultsPage),
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
        assertEquals(ARITHMETIC_RESULT, 2 + 2);
    }

    /**
     * Verifies that the ResultsPage view instance successfully builds and is
     * initialized without evaluating to null.
     */
    @Test
    void testInitialisation() {
        assertNotNull(
                resultsPage,
                "ResultsPage view instance should be fully initialized.");
    }

    /**
     * Asserts that critical structural labels and headings inside the banner
     * render natively with their expected text content using clean text
     * lookups.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testCoreLabelsRender(final FxRobot robot) {
        assertNotNull(resultsPage, "ResultsPage instance was null.");

        Label mainHeader = robot.lookup("Results").queryAs(Label.class);
        Label congratsMsg = robot.lookup(
                "Congratulations on completing the quiz!")
                .queryAs(Label.class);
        Label breakdownHeader = robot.lookup("Results Breakdown")
                .queryAs(Label.class);
        Label summaryHeader = robot.lookup("Summary").queryAs(Label.class);

        assertNotNull(
                mainHeader,
                "Missing the main 'Results' banner page title.");
        assertNotNull(
                congratsMsg,
                "Missing the celebratory user sub-header text.");
        assertNotNull(
                breakdownHeader,
                "Missing the 'Results Breakdown' section title header.");
        assertNotNull(
                summaryHeader,
                "Missing the 'Summary' section title header.");
    }

    /**
     * Verifies that the specific score parameters accompanied by their
     * respective icon identifiers populate properly inside the score card
     * block.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testScoreSectionCategories(final FxRobot robot) {
        assertNotNull(resultsPage, "ResultsPage instance was null.");

        Label percentageRow = robot.lookup("Percentage:")
                .queryAs(Label.class);
        Label resultsRow = robot.lookup("Results:").queryAs(Label.class);

        assertNotNull(percentageRow,
                "The score card is missing the 'Percentage:' row indicator.");
        assertNotNull(resultsRow,
                "The score card is missing the 'Results:' row indicator.");
    }

    /**
     * Verifies that the metric category labels populate safely within the
     * lower summary overview layout block.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testSummarySectionCategories(final FxRobot robot) {
        assertNotNull(resultsPage, "ResultsPage instance was null.");

        Label timeRow = robot.lookup("Time taken:").queryAs(Label.class);
        Label feedbackRow = robot.lookup("Feedback:").queryAs(Label.class);
        Label questionReviewRow = robot.lookup("Question review:")
                .queryAs(Label.class);

        assertNotNull(
                timeRow,
                "The summary block is missing the 'Time taken:' row category.");
        assertNotNull(
                feedbackRow,
                "The summary block is missing the 'Feedback:' row category.");
        assertNotNull(
                questionReviewRow,
                "The summary block is missing the 'Question review:' row "
                        + "category.");
    }

    /**
     * Asserts that all navigation action controls instantiated in the container
     * are loaded and discoverable on the live layout canvas.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testActionButtonsExist(final FxRobot robot) {
        assertNotNull(resultsPage, "ResultsPage instance was null.");

        Button saveBtn = robot.lookup("Save Results").queryAs(Button.class);
        Button retakeBtn = robot.lookup("Retake quiz").queryAs(Button.class);
        Button selectAnotherBtn = robot.lookup("Select another quiz")
                .queryAs(Button.class);

        assertNotNull(saveBtn,
                "Missing 'Save Results' operational button component.");
        assertNotNull(retakeBtn,
                "Missing 'Retake quiz' operational button component.");
        assertNotNull(selectAnotherBtn,
                "Missing 'Select another quiz' operational button component.");
    }

    /**
     * Simulates mouse pointer actions using an automated FxRobot to hover over
     * layout action buttons, validating that the button dynamically mutates its
     * styles properly during hover events.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testButtonHoverStyleStateChanges(final FxRobot robot) {
        assertNotNull(resultsPage, "ResultsPage instance was null.");

        Button actionButton = robot.lookup("Save Results")
                .queryAs(Button.class);
        assertNotNull(
                actionButton,
                "Target action button must exist to verify hover styles.");

        String initialStyle = actionButton.getStyle();

        robot.moveTo(actionButton);
        String hoverStyle = actionButton.getStyle();

        robot.moveBy(HOVER_OFFSET, HOVER_OFFSET);
        String restoredStyle = actionButton.getStyle();

        assertTrue(
                hoverStyle.contains("white")
                        || !hoverStyle.equals(initialStyle),
                "Button inline styles failed to transition when mouse entered "
                        + "its layout bounds.");
        assertEquals(
                initialStyle,
                restoredStyle,
                "Button inline styles failed to reset back to original "
                        + "parameters upon mouse exit.");
    }

}

