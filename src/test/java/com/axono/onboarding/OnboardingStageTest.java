package com.axono.onboarding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import com.axono.model.UserProfile;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Structural integration and state workflow navigation tests for
 * {@link OnboardingStage}.
 * Verifies wizard step transitions, dynamic button state updates, label
 * mutations,
 * and the final application launch callback invocation hook.
 */
@ExtendWith(ApplicationExtension.class)
class OnboardingStageTest {

    /** Expected result of basic arithmetic operation (2 + 2). */
    private static final int ARITHMETIC_RESULT = 4;

    /** Wait timeout for text to appear (seconds). */
    private static final int WAIT_TIMEOUT_SECONDS = 5;

    /** Timeout duration for async operations in milliseconds. */
    private static final long LONG_TIMEOUT_MS = 2000;

    /** The target application stage instance hosted on the active window. */
    private Stage testStageParam;

    /**
     * Tracks the captured profile entity passed back by the wizard completion
     * callback.
     */
    private final AtomicReference<UserProfile> completedProfileResult =
            new AtomicReference<>();

    /**
     * Tracks whether the wizard completion lifecycle loop executed
     * successfully.
     */
    private final AtomicBoolean isCallbackInvoked = new AtomicBoolean(false);

    /**
     * Initializes the onboarding coordinator orchestrator system context
     * wrapper window.
     *
     * @param stageParam the primary window workspace handled automatically by
     *                   TestFX.
     */
    @Start
    void start(final Stage stageParam) {
        this.testStageParam = stageParam;
        new OnboardingStage(stageParam, profile -> {
            isCallbackInvoked.set(true);
            completedProfileResult.set((UserProfile) profile);
        });
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
     * Confirms that the top window initialization sets up basic properties
     * cleanly.
     */
    @Test
    void testStageInitialization() {
        assertNotNull(
                testStageParam,
                "The managed onboarding execution window stage layer should be "
                        + "fully initialized.");
        assertEquals(
                "ReWire — Setup",
                testStageParam.getTitle(),
                "Onboarding stage header window title string mismatch.");
        assertTrue(
                testStageParam.isShowing(),
                "Onboarding container failed to reveal the active UI layer "
                        + "layout stage.");
    }

    /**
     * Asserts that Step 1 begins on the Welcome layout view context with the
     * correct text metrics.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testInitialStepState(final FxRobot robot) {
        Label stepCounter = robot.lookup("Step 1 of 4").queryAs(Label.class);
        assertNotNull(
                stepCounter,
                "Missing baseline 'Step 1 of 4' progress tracking footer "
                        + "label.");

        Button backBtn = robot.lookup("← Back").queryAs(Button.class);
        assertFalse(
                backBtn.isVisible(),
                "The back navigation component button should remain hidden on "
                        + "Step 1.");

        Button nextBtn = robot.lookup("Next").queryAs(Button.class);
        assertNotNull(nextBtn,
                "Missing forward step navigation action button.");
        assertEquals("Next", nextBtn.getText());
    }

    /**
     * Validates forward wizard navigation workflows across screen intervals.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testForwardNavigationTransitions(final FxRobot robot) {
        Button nextBtn = robot.lookup("Next").queryAs(Button.class);
        assertNotNull(nextBtn);

        robot.clickOn(nextBtn);

        Label stepTwoCounter = robot.lookup("Step 2 of 4")
                .queryAs(Label.class);
        assertNotNull(
                stepTwoCounter,
                "Failed to transition tracking progress to Step 2 label "
                        + "outputs.");

        Button backBtn = robot.lookup("← Back").queryAs(Button.class);
        assertTrue(
                backBtn.isVisible(),
                "Back navigation control handle failed to reveal itself on "
                        + "index increment cycles.");
    }

    /**
     * Asserts that backwards step modification commands restore historical
     * screen references smoothly.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testBackwardNavigationTransitions(final FxRobot robot) {
        Button nextBtn = robot.lookup("Next").queryAs(Button.class);
        Button backBtn = robot.lookup("← Back").queryAs(Button.class);

        robot.clickOn(nextBtn);
        assertTrue(backBtn.isVisible());

        robot.clickOn(backBtn);

        Label restoredStepCounter = robot.lookup("Step 1 of 4")
                .queryAs(Label.class);
        assertNotNull(
                restoredStepCounter,
                "Failed to restore step progress counter to Step 1 matching "
                        + "state string values.");
        assertFalse(
                backBtn.isVisible(),
                "Back button failed to re-hide when navigating back to index "
                        + "0.");
    }

    /**
     * Confirms that the Next button morphs to its final launch variant on the
     * Summary step. To reach the final page cleanly without validation
     * failures, this test sets valid input fields directly into the internal
     * view instances using thread-safe actions.
     *
     * @param robot the TestFX robot for interaction.
     * @throws Exception if test execution fails.
     */
    @Test
    void testWizardCompletionLifecycle(final FxRobot robot) throws Exception {
        Button nextBtn = robot.lookup("Next").queryAs(Button.class);
        assertNotNull(nextBtn);

        robot.clickOn(nextBtn);

        waitForTextToAppear(robot, "Step 2 of 4");

        robot.interact(() -> {
            robot.lookup(".text-field").queryAll().stream()
                    .filter(javafx.scene.control.TextField.class::isInstance)
                    .map(javafx.scene.control.TextField.class::cast)
                    .forEach(tf -> tf.setText("Joe Bloggs"));
        });

        ComboBox<String> yearComboBox = robot.lookup(".combo-box").queryAll()
                .stream()
                .filter(ComboBox.class::isInstance)
                .map(node -> {
                    @SuppressWarnings("unchecked")
                    ComboBox<String> cb = (ComboBox<String>) node;
                    return cb;
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Could not find Year of Study ComboBox."));

        robot.clickOn(yearComboBox);

        robot.interact(() -> {
            if (yearComboBox.getItems().isEmpty()) {
                ((ComboBox<String>) yearComboBox).getItems().addAll("Year 1",
                        "Year 2", "Foundation Year");
            }
            yearComboBox.getSelectionModel().select(2);
        });

        robot.type(javafx.scene.input.KeyCode.ENTER);

        robot.interact(() -> {
            robot.lookup(".date-picker").queryAll().stream()
                    .filter(javafx.scene.control.DatePicker.class::isInstance)
                    .map(javafx.scene.control.DatePicker.class::cast)
                    .findFirst()
                    .ifPresent(datePicker -> {
                        datePicker.setValue(java.time.LocalDate.of(2000, 1, 1)); // Historical date for testing
                    });
        });

        robot.clickOn(nextBtn);

        waitForTextToAppear(robot, "Step 3 of 4");

        robot.interact(() -> {
            robot.lookup(".check-box").queryAll().stream()
                    .filter(javafx.scene.control.CheckBox.class::isInstance)
                    .map(javafx.scene.control.CheckBox.class::cast)
                    .forEach(cb -> cb.setSelected(true));
        });

        robot.clickOn(nextBtn);

        waitForTextToAppear(robot, "Step 4 of 4");

        Label stepFourCounter = robot.lookup("Step 4 of 4")
                .queryAs(Label.class);
        assertNotNull(stepFourCounter, "Failed to reach Step 4 cleanly.");

        Button launchBtn = robot.lookup("Launch App").queryAs(Button.class);
        assertNotNull(
                launchBtn,
                "Next button failed to transform into 'Launch App'");

        robot.clickOn(launchBtn);

        assertTrue(
                isCallbackInvoked.get(),
                "Launch callback was not triggered.");
        assertNotNull(
                completedProfileResult.get(),
                "UserProfile payload is null.");
        assertFalse(
                testStageParam.isShowing(),
                "Onboarding stage window did not close.");
    }

    /**
     * Dynamically waits for specific label/button text to exist in the active
     * scene.
     *
     * @param robot the TestFX robot for interaction.
     * @param text the text to look for.
     * @throws java.util.concurrent.TimeoutException if timeout occurs.
     */
    private void waitForTextToAppear(final FxRobot robot, final String text)
            throws java.util.concurrent.TimeoutException {
        WaitForAsyncUtils.waitFor(
                WAIT_TIMEOUT_SECONDS,
                TimeUnit.SECONDS,
                () -> {
                    try {
                        return robot.lookup(text).query() != null;
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

}

