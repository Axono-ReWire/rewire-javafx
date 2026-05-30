package com.axono.onboarding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.axono.model.UserProfile;
import com.axono.ui.UIConstants;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Unit and structural integration tests for the {@link SubjectView} view layer.
 * Verifies interactive checkbox selection states, select-all macro features,
 * and data mapping into the active UserProfile.
 */
@ExtendWith(ApplicationExtension.class)
class SubjectViewTest {

    /** Expected result of basic arithmetic operation (2 + 2). */
    private static final int ARITHMETIC_RESULT = 4;

    /** The subject view layout container instance under test. */
    private SubjectView subjectView;

    /** The user profile data model injected into the view instance wrapper. */
    private UserProfile testProfile;

    /**
     * Initializes a testing user profile data model, constructs the
     * {@link SubjectView}, and presents the parent wrapper container inside a
     * live JavaFX window context stage.
     *
     * @param stage the automated window management Stage instance provided by
     *              TestFX.
     */
    @Start
    void start(final Stage stage) {
        testProfile = new UserProfile();
        this.subjectView = new SubjectView(testProfile);

        Scene scene = new Scene(
                new StackPane(subjectView),
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
     * Confirms that the main subject layout view shell instantiates safely.
     */
    @Test
    void testInitialization() {
        assertNotNull(
                subjectView,
                "SubjectView structural shell instance should be fully "
                        + "initialized.");
    }

    /**
     * Asserts that basic structural headers and user layout instructions render
     * natively with their exact expected text content matches.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testCoreLabelsRender(final FxRobot robot) {
        assertNotNull(subjectView);

        Label primaryTitle = robot.lookup("Select Your Modules")
                .queryAs(Label.class);
        Label descriptionLabel = robot.lookup(
                "Choose the modules you're studying:").queryAs(Label.class);

        assertNotNull(
                primaryTitle,
                "Missing primary 'Select Your Modules' title layout label.");
        assertNotNull(
                descriptionLabel,
                "Missing the onboarding section user instructional guide "
                        + "string.");
    }

    /**
     * Verifies that the macro control actions loop updates all discoverable
     * checkbox elements inside the panel to a uniform positive selection
     * configuration.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testSelectAllButtonBehavior(final FxRobot robot) {
        Button selectAllBtn = robot.lookup("Select All").queryAs(Button.class);
        assertNotNull(
                selectAllBtn,
                "Missing 'Select All' operational macro action handle button.");

        List<CheckBox> boxes = robot.lookup(".check-box").queryAll().stream()
                .filter(CheckBox.class::isInstance)
                .map(CheckBox.class::cast)
                .collect(Collectors.toList());

        assertFalse(
                boxes.isEmpty(),
                "The rendered curriculum UI layout contains zero checkboxes. "
                        + "Verify testcurriculum.xml exists.");

        robot.clickOn(selectAllBtn);
        for (CheckBox box : boxes) {
            assertTrue(
                    box.isSelected(),
                    "Checkbox element failed to check: " + box.getText());
        }

        robot.clickOn(selectAllBtn);
        for (CheckBox box : boxes) {
            assertFalse(
                    box.isSelected(),
                    "Checkbox element failed to clear: " + box.getText());
        }
    }

    /**
     * Validates that input safety routines flag an empty selection matrix
     * negatively before allowing validation parameters to proceed.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testInputValidationFailsWhenEmpty(final FxRobot robot) {
        robot.interact(() -> {
            robot.lookup(".check-box").queryAll().stream()
                    .filter(CheckBox.class::isInstance)
                    .map(CheckBox.class::cast)
                    .forEach(cb -> cb.setSelected(false));
        });

        robot.interact(() -> {
            boolean isValid = subjectView.validateInput();
            assertFalse(
                    isValid,
                    "Input validation should return false when zero module "
                            + "categories are selected.");
        });

        robot.clickOn(".button");
    }

    /**
     * Asserts that selected choices serialize perfectly into target domain
     * attributes when running field state persistence pipelines.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testSaveDataPersistsToProfile(final FxRobot robot) {
        List<CheckBox> boxes = robot.lookup(".check-box").queryAll().stream()
                .filter(CheckBox.class::isInstance)
                .map(CheckBox.class::cast)
                .collect(Collectors.toList());

        assertFalse(
                boxes.isEmpty(),
                "No module checkbox controls discovered in active scene "
                        + "graph.");

        CheckBox firstTarget = boxes.get(0);
        String firstTargetName = firstTarget.getText();

        robot.interact(() -> firstTarget.setSelected(true));

        robot.interact(() -> subjectView.saveData());

        List<String> storedSubjects = testProfile.getSubjects();
        assertNotNull(
                storedSubjects,
                "UserProfile subjects collection initialized to null value map "
                        + "parameters.");
        assertEquals(
                1,
                storedSubjects.size(),
                "UserProfile should register exactly 1 active selected course "
                        + "module entry.");
        assertTrue(
                storedSubjects.contains(firstTargetName),
                "UserProfile subject array collection is missing item label: "
                        + firstTargetName);
    }

}

