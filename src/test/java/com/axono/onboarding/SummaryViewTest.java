package com.axono.onboarding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.axono.model.UserProfile;
import com.axono.ui.UIConstants;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Structural and state integration tests for the {@link SummaryView} final
 * step.
 * Verifies default placeholder rendering and data reflection mechanics
 * following
 * state variations in the underlying UserProfile model.
 */
@ExtendWith(ApplicationExtension.class)
class SummaryViewTest {

    /** The UI view component under test. */
    private SummaryView summaryView;

    /** The injected data profile wrapper container. */
    private UserProfile testProfile;

    /**
     * Initializes a testing profile model data entity, constructs the
     * {@link SummaryView}, and maps it inside a testing frame stage wrapper
     * layout.
     *
     * @param stage the automation window lifecycle hook provided by TestFX.
     */
    @Start
    void start(final Stage stage) {
        testProfile = new UserProfile();
        this.summaryView = new SummaryView(testProfile);

        Scene scene = new Scene(
                new StackPane(summaryView),
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
     * Asserts that the layout initializes safely without null errors.
     */
    @Test
    void testInitialization() {
        assertNotNull(summaryView,
                "SummaryView instance should be fully initialized.");
    }

    /**
     * Validates that standard boilerplate titles and static text headers
     * populate the view space.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testStaticComponentsRender(final FxRobot robot) {
        assertNotNull(summaryView);

        Label completionIcon = robot.lookup("✅").queryAs(Label.class);
        Label mainHeading = robot.lookup("You're all set!")
                .queryAs(Label.class);

        assertNotNull(completionIcon,
                "Missing structural finish verification check emoji label.");
        assertNotNull(mainHeading,
                "Missing central card confirmation alert welcome title.");
    }

    /**
     * Confirms that structural layout key descriptions are mapped explicitly
     * across data table positions.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testRowKeysRender(final FxRobot robot) {
        assertNotNull(summaryView);

        assertNotNull(robot.lookup("Name:").queryAs(Label.class),
                "Missing 'Name:' row metadata indicator.");
        assertNotNull(robot.lookup("Year of Study:").queryAs(Label.class),
                "Missing 'Year of Study:' row metadata indicator.");
        assertNotNull(robot.lookup("Institution:").queryAs(Label.class),
                "Missing 'Institution:' row metadata indicator.");
        assertNotNull(robot.lookup("Modules:").queryAs(Label.class),
                "Missing 'Modules:' row metadata indicator.");
    }

    /**
     * Verifies that the view initializes properties to an em dash default
     * placeholder before explicit data reflection triggers are invoked.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testDefaultValueStateLookups(final FxRobot robot) {
        assertNotNull(summaryView);

        // Multiple fields initialize to an em-dash string literal value ("—")
        int dashOccurrences = robot.lookup("—").queryAll().size();

        // At minimum, Name, Year of Study, and Modules fields should show the
        // dash placeholder
        org.junit.jupiter.api.Assertions.assertTrue(dashOccurrences >= 3,
                "Expected default em-dash placeholders to occupy unpopulated "
                + "details spaces.");
    }

    /**
     * Modifies model properties and executes a component refresh call on the
     * safe thread context to confirm data transfers smoothly onto visible
     * layout labels.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testRefreshReflectsPopulatedProfile(final FxRobot robot) {
        assertNotNull(summaryView);

        // Mutate field settings inside the standalone structural context model
        testProfile.setName("Joe Bloggs");
        testProfile.setYearOfStudy("Year 1");
        testProfile.setInstitution("University of York");
        testProfile.setSubjects(
                Arrays.asList("Engineering Mathematics",
                        "Introduction to Engineering", "Analogue Electronics"));

        // Perform the view level update execution cleanly within safe FX thread
        // lines
        robot.interact(() -> summaryView.refresh());

        // Locate elements directly off the live canvas by their fresh data
        // contents
        Label updatedName = robot.lookup("Joe Bloggs").queryAs(Label.class);
        Label updatedYear = robot.lookup("Year 1").queryAs(Label.class);
        Label updatedInst = robot.lookup("University of York")
                .queryAs(Label.class);
        Label updatedMods = robot.lookup(
                "Engineering Mathematics, Introduction to Engineering, "
                + "Analogue Electronics")
                .queryAs(Label.class);

        assertNotNull(updatedName,
                "UI labels failed to display the updated profile name string "
                + "field value.");
        assertNotNull(updatedYear,
                "UI labels failed to display the updated profile year of study "
                + "value.");
        assertNotNull(updatedInst,
                "UI labels failed to display the updated profile academic "
                + "institution name.");
        assertNotNull(updatedMods,
                "UI labels failed to map list values into a single "
                + "comma-separated text string summary block.");
    }

    /**
     * Checks validation corner-cases where optional properties evaluate to
     * empty maps to ensure the class safely switches strings to alternate
     * layout flags.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testRefreshHandlesBlankOptionalFields(final FxRobot robot) {
        assertNotNull(summaryView);

        // Leave properties entirely blank or empty inside the collection matrix
        testProfile.setName("");
        testProfile.setYearOfStudy("Foundation Year");
        // Triggers "Not specified" business logic branch rules
        testProfile.setInstitution("");
        testProfile.setSubjects(Collections.emptyList());

        robot.interact(() -> summaryView.refresh());

        // Assert that specific fallback labels are safely discoverable on the
        // layout context tree
        Label missingInstFallback = robot.lookup("Not specified")
                .queryAs(Label.class);
        assertNotNull(missingInstFallback,
                "SummaryView missing the standard 'Not specified' safety label "
                + "fallback text message.");

        // Assert blank name values recover cleanly back to baseline placeholder
        // elements
        int dashOccurrences = robot.lookup("—").queryAll().size();
        org.junit.jupiter.api.Assertions.assertTrue(dashOccurrences >= 2,
                "Missing dashboard fallbacks for unpopulated profile names or "
                + "empty subjects collections.");
    }

