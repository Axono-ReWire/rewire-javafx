package com.axono.signup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import com.axono.model.UserProfile;
import com.axono.ui.UIConstants;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Unit and structural integration tests for the {@link SignUpView} view.
 * Uses TestFX to manage the JavaFX scene graph lifecycle and simulate user
 * interactions using explicit node selectors and thread-safe operations.
 */
@ExtendWith(ApplicationExtension.class)
class SignUpViewTest {

    /** Expected result of basic arithmetic operation (2 + 2). */
    private static final int ARITHMETIC_RESULT = 4;

    /** Minimum age for account creation in years. */
    private static final long MINIMUM_AGE_YEARS = 20;

    /** The signup view component instance under test. */
    private SignUpView signUpView;

    /** The target user profile model injected into the signup context. */
    private UserProfile testProfile;

    /**
     * Initializes a data model instance, builds the {@link SignUpView}, and
     * mounts the view inside a JavaFX Stage wrapper before executing tests.
     *
     * @param stage the automated Stage provided by TestFX.
     */
    @Start
    void start(final Stage stage) {
        testProfile = new UserProfile();
        testProfile.setName("");
        testProfile.setYearOfStudy("");
        testProfile.setInstitution("");
        testProfile.setSubjects(new ArrayList<>());

        this.signUpView = new SignUpView(testProfile);

        Scene scene = new Scene(
                new StackPane(signUpView),
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
     * Verifies that the SignUpView component and its inner child form blocks
     * load correctly into the JavaFX scene layout graph.
     *
     * @param robot the TestFX robot for interaction.
     * @throws Exception if test execution fails.
     */
    @Test
    void testInitialization(final FxRobot robot) throws Exception {
        assertNotNull(
                signUpView,
                "SignUpView instance should be fully initialized.");

        VBox inputCard = WaitForAsyncUtils
                .asyncFx(() -> signUpView.signupinput()).get();

        assertNotNull(
                inputCard,
                "The input form card block should not be null.");
    }

    /**
     * Scans the full view hierarchy to ensure all required fields and
     * user-facing instructional form labels exist on the layout tree.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testFormLabelsRender(final FxRobot robot) {
        assertNotNull(
                signUpView,
                "SignUpView instance should be fully initialized.");

        Label signUpTitle = robot.lookup("Sign up").queryAs(Label.class);
        Label firstNameLabel = robot.lookup("First Name")
                .queryAs(Label.class);
        Label lastNameLabel = robot.lookup("Last Name").queryAs(Label.class);
        Label dobLabel = robot.lookup("Date of Birth (dd/mm/yyyy)")
                .queryAs(Label.class);
        Label usernameLabel = robot.lookup("Username").queryAs(Label.class);
        Label yearOfStudyLabel = robot.lookup("Year of Study")
                .queryAs(Label.class);
        Label passwordLabel = robot.lookup("Password").queryAs(Label.class);
        Label confirmPasswordLabel = robot.lookup("Confirm Password")
                .queryAs(Label.class);

        assertNotNull(
                signUpTitle,
                "Missing the main 'Sign up' page header label.");
        assertNotNull(
                firstNameLabel,
                "Missing the form helper 'First Name' label.");
        assertNotNull(
                lastNameLabel,
                "Missing the form helper 'Last Name' label.");
        assertNotNull(
                dobLabel,
                "Missing the form helper 'Date of Birth (dd/mm/yyyy)' label.");
        assertNotNull(
                usernameLabel,
                "Missing the form helper 'Username' label.");
        assertNotNull(
                yearOfStudyLabel,
                "Missing the form helper 'Year of Study' label.");
        assertNotNull(
                passwordLabel,
                "Missing the form helper 'Password' label.");
        assertNotNull(
                confirmPasswordLabel,
                "Missing the form helper 'Confirm Password' label.");
    }

    /**
     * Confirms that input parameter checking passes successfully when all
     * mandatory data fields are completely populated with valid data entries.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testValidateInputSuccess(final FxRobot robot) {
        populateValidFormFields(robot);

        boolean validationResult = signUpView.validateInput();

        assertTrue(
                validationResult,
                "Validation should pass completely when all data parameters "
                        + "are valid.");
    }

    /**
     * Verifies that entering completely valid information and executing
     * saveData() transfers the parameters straight into the underlying
     * UserProfile target instance fields.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testSaveDataPersistsToProfile(final FxRobot robot) {
        populateValidFormFields(robot);

        signUpView.saveData();

        assertEquals(
                "Joe",
                testProfile.getName(),
                "Profile name should track the First Name field.");
        assertEquals(
                "Year 1",
                testProfile.getYearOfStudy(),
                "Profile year of study should match selection choice.");
        assertEquals(
                "Bloggs",
                testProfile.getInstitution(),
                "Profile institution should track the Last Name field.");
    }

    /**
     * Validates missing input protection rules. Executes validation
     * asynchronously via Platform.runLater to prevent test thread blocking,
     * then simulates a real button click to handle and dismiss the resulting
     * warning alert dialog.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testValidateInputFailureHandlesAlertDismissal(final FxRobot robot) {
        AtomicBoolean validationOutcome = new AtomicBoolean(true);

        Platform.runLater(() -> {
            validationOutcome.set(signUpView.validateInput());
        });

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("OK");

        assertFalse(
                validationOutcome.get(),
                "Validation must fail when required fields remain empty.");
    }

    /**
     * Utility method that uses the robot to fetch components out of the live
     * scene graph and populate them with valid registration profiles.
     *
     * @param robot the TestFX robot for interaction.
     */
    @SuppressWarnings("unchecked")
    private void populateValidFormFields(final FxRobot robot) {
        TextField fNameField = robot.lookup(".text-field")
                .match(node -> node instanceof TextField
                        && "First name".equals(
                                ((TextField) node).getPromptText()))
                .queryAs(TextField.class);

        TextField lNameField = robot.lookup(".text-field")
                .match(node -> node instanceof TextField
                        && "Last name".equals(
                                ((TextField) node).getPromptText()))
                .queryAs(TextField.class);

        TextField userField = robot.lookup(".text-field")
                .match(node -> node instanceof TextField
                        && "Username".equals(
                                ((TextField) node).getPromptText()))
                .queryAs(TextField.class);

        DatePicker dobPicker = robot.lookup(".date-picker")
                .queryAs(DatePicker.class);
        ComboBox<String> studyCombo = robot.lookup(".combo-box")
                .queryAs(ComboBox.class);

        Object[] passFields = robot.lookup(".password-field").queryAll()
                .toArray();

        assertNotNull(
                fNameField,
                "First Name field missing from visible scene graph.");
        assertNotNull(
                lNameField,
                "Last Name field missing from visible scene graph.");
        assertNotNull(
                userField,
                "Username field missing from visible scene graph.");
        assertNotNull(
                dobPicker,
                "DatePicker missing from visible scene graph.");
        assertNotNull(
                studyCombo,
                "Year of Study ComboBox missing from visible scene graph.");
        assertTrue(
                passFields.length >= 2,
                "Missing mandatory Password and Confirm Password fields.");

        robot.interact(() -> {
            fNameField.setText("Joe");
            lNameField.setText("Bloggs");
            userField.setText("joebloggs99");

            dobPicker.setValue(LocalDate.now().minusYears(MINIMUM_AGE_YEARS));

            studyCombo.getSelectionModel().select("Year 1");

            ((PasswordField) passFields[0]).setText("SecurePass123");
            ((PasswordField) passFields[1]).setText("SecurePass123");
        });
    }

}

