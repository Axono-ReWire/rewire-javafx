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

    /** The target application stage instance hosted on the active window shell. */
    private Stage stage;

    /**
     * Tracks the captured profile entity passed back by the wizard completion
     * callback.
     */
    private final AtomicReference<UserProfile> completedProfileResult = new AtomicReference<>();

    /**
     * Tracks whether the wizard completion lifecycle loop executed successfully.
     */
    private final AtomicBoolean isCallbackInvoked = new AtomicBoolean(false);

    /**
     * Initializes the onboarding coordinator orchestrator system context wrapper
     * window.
     *
     * @param stage the primary window workspace handled automatically by TestFX.
     */
    @Start
    void start(Stage stage) {
        this.stage = stage;
        // Mount and present the orchestration container on screen immediately
        new OnboardingStage(stage, profile -> {
            isCallbackInvoked.set(true);
            completedProfileResult.set(profile);
        });
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
     * Confirms that the top window initialization sets up basic properties cleanly.
     */
    @Test
    void testStageInitialization() {
        assertNotNull(stage, "The managed onboarding execution window stage layer should be fully initialized.");
        assertEquals("ReWire — Setup", stage.getTitle(), "Onboarding stage header window title string mismatch.");
        assertTrue(stage.isShowing(), "Onboarding container failed to reveal the active UI layer layout stage.");
    }

    /**
     * Asserts that Step 1 begins on the Welcome layout view context with the
     * correct text metrics.
     */
    @Test
    void testInitialStepState(FxRobot robot) {
        Label stepCounter = robot.lookup("Step 1 of 4").queryAs(Label.class);
        assertNotNull(stepCounter, "Missing baseline 'Step 1 of 4' progress tracking footer label.");

        // Back button must remain entirely invisible on the initial step node card
        // block
        Button backBtn = robot.lookup("← Back").queryAs(Button.class);
        assertFalse(backBtn.isVisible(), "The back navigation component button should remain hidden on Step 1.");

        // Verify next button is visible and properly configured
        Button nextBtn = robot.lookup("Next").queryAs(Button.class);
        assertNotNull(nextBtn, "Missing forward step navigation action button.");
        assertEquals("Next", nextBtn.getText());
    }

    /**
     * Validates forward wizard navigation workflows across screen intervals.
     */
    @Test
    void testForwardNavigationTransitions(FxRobot robot) {
        Button nextBtn = robot.lookup("Next").queryAs(Button.class);
        assertNotNull(nextBtn);

        // Click next from step 1 (WelcomeView requires zero data validation updates)
        robot.clickOn(nextBtn);

        Label stepTwoCounter = robot.lookup("Step 2 of 4").queryAs(Label.class);
        assertNotNull(stepTwoCounter, "Failed to transition tracking progress to Step 2 label outputs.");

        // Back button must toggle to visible now that the cursor index is past zero
        Button backBtn = robot.lookup("← Back").queryAs(Button.class);
        assertTrue(backBtn.isVisible(),
                "Back navigation control handle failed to reveal itself on index increment cycles.");
    }

    /**
     * Asserts that backwards step modification commands restore historical screen
     * references smoothly.
     */
    @Test
    void testBackwardNavigationTransitions(FxRobot robot) {
        Button nextBtn = robot.lookup("Next").queryAs(Button.class);
        Button backBtn = robot.lookup("← Back").queryAs(Button.class);

        // Advance forward first to Step 2
        robot.clickOn(nextBtn);
        assertTrue(backBtn.isVisible());

        // Select the historical back navigation trigger control node
        robot.clickOn(backBtn);

        Label restoredStepCounter = robot.lookup("Step 1 of 4").queryAs(Label.class);
        assertNotNull(restoredStepCounter,
                "Failed to restore step progress counter to Step 1 matching state string values.");
        assertFalse(backBtn.isVisible(), "Back button failed to re-hide when navigating back to index 0.");
    }

    /**
     * Confirms that the Next button morphs to its final launch variant on the
     * Summary step.
     * To reach the final page cleanly without validation failures, this test sets
     * valid input fields
     * directly into the internal view instances using thread-safe actions.
     */
    @Test
    void testWizardCompletionLifecycle(FxRobot robot) throws Exception {
        Button nextBtn = robot.lookup("Next").queryAs(Button.class);
        assertNotNull(nextBtn);

        // Welcome
        robot.clickOn(nextBtn);

        // SignUpView
        // Wait for the stage footer to change to
        // Step 2
        waitForTextToAppear(robot, "Step 2 of 4");

        robot.interact(() -> {
            robot.lookup(".text-field").queryAll().stream()
                    .filter(javafx.scene.control.TextField.class::isInstance)
                    .map(javafx.scene.control.TextField.class::cast)
                    .forEach(tf -> tf.setText("Joe Bloggs"));
        });

        // Click directly on the ComboBox to focus it and open the dropdown menu
        // layout list
        ComboBox<String> yearComboBox = robot.lookup(".combo-box").queryAll().stream()
                .filter(ComboBox.class::isInstance)
                .map(node -> {
                    @SuppressWarnings("unchecked")
                    ComboBox<String> cb = (ComboBox<String>) node;
                    return cb;
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError("Could not find Year of Study ComboBox."));

        robot.clickOn(yearComboBox);

        // Force JavaFX to explicitly select the second item via interaction thread
        // mechanics
        robot.interact(() -> {
            if (yearComboBox.getItems().isEmpty()) {
                ((ComboBox<String>) yearComboBox).getItems().addAll("Year 1", "Year 2", "Foundation Year");
            }
            yearComboBox.getSelectionModel().select(2);
        });

        // Tap the Enter key or click the next button to close the dropdown context
        // window cleanly
        robot.type(javafx.scene.input.KeyCode.ENTER);

        robot.interact(() -> {
            robot.lookup(".date-picker").queryAll().stream()
                    .filter(javafx.scene.control.DatePicker.class::isInstance)
                    .map(javafx.scene.control.DatePicker.class::cast)
                    .findFirst()
                    .ifPresent(datePicker -> {
                        // Sets the date of birth to January 1st, 2000
                        datePicker.setValue(java.time.LocalDate.of(2000, 1, 1));
                    });
        });

        robot.clickOn(nextBtn);

        // SubjectView
        // Wait for the footer to change to Step 3
        waitForTextToAppear(robot, "Step 3 of 4");

        robot.interact(() -> {
            robot.lookup(".check-box").queryAll().stream()
                    .filter(javafx.scene.control.CheckBox.class::isInstance)
                    .map(javafx.scene.control.CheckBox.class::cast)
                    .forEach(cb -> cb.setSelected(true));
        });

        robot.clickOn(nextBtn);

        // SummaryView
        // Wait for the footer to change to Step 4
        waitForTextToAppear(robot, "Step 4 of 4");

        Label stepFourCounter = robot.lookup("Step 4 of 4").queryAs(Label.class);
        assertNotNull(stepFourCounter, "Failed to reach Step 4 cleanly.");

        // Look for the transformed launch button text updated by updateStep()
        Button launchBtn = robot.lookup("Launch App").queryAs(Button.class);
        assertNotNull(launchBtn, "Next button failed to transform into 'Launch App'");

        robot.clickOn(launchBtn);

        // --- Post-Conditions ---
        assertTrue(isCallbackInvoked.get(), "Launch callback was not triggered.");
        assertNotNull(completedProfileResult.get(), "UserProfile payload is null.");
        assertFalse(stage.isShowing(), "Onboarding stage window did not close.");
    }

    /**
     * Dynamically waits for specific label/button text to exist in the active
     * scene.
     */
    private void waitForTextToAppear(FxRobot robot, String text) throws java.util.concurrent.TimeoutException {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> {
            try {
                return robot.lookup(text).query() != null;
            } catch (Exception e) {
                return false;
            }
        });
    }
}
