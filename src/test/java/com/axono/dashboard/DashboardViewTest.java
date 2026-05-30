package com.axono.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.axono.model.UserProfile;
import com.axono.ui.UIConstants;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Unit and structural integration tests for the {@link DashboardView} layout.
 * Uses TestFX explicit text selectors and state interactions to query the live
 * window context.
 */
@ExtendWith(ApplicationExtension.class)
class DashboardViewTest {

    /** The dashboard view container component instance under test. */
    private DashboardView dashboardView;

    /** The user profile data model injected into the view instance framework. */
    private UserProfile testProfile;

    /**
     * Initializes a sample testing data model, constructs the
     * {@link DashboardView},
     * and presents the parent wrapper container inside a live JavaFX testing stage
     * scene graph.
     *
     * @param stage the automated window management Stage instance provided by
     *              TestFX.
     */
    /**
     * Initializes the test stage with a test profile and DashboardView.
     *
     * @param stage the primary JavaFX stage for testing.
     */
    @Start
    void start(final Stage stage) {
        testProfile = new UserProfile();
        testProfile.setName("Joe");
        testProfile.setYearOfStudy("Year 1");
        testProfile.setInstitution("University of York");
        // Initialize with no subjects to evaluate the default dashboard layout baseline
        testProfile.setSubjects(new ArrayList<>());

        this.dashboardView = new DashboardView(testProfile);

        Scene scene = new Scene(
                new StackPane(dashboardView),
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
     * Confirms that the main dashboard layout shell instantiates safely.
     */
    @Test
    void testInitialization() {
        assertNotNull(dashboardView,
                "DashboardView structural shell instance should be fully initialized.");
    }

    /**
     * Verifies that the welcome header parses the user profile name correctly
     * and renders primary window action management button handles.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testWelcomeBannerAndActionButtons(final FxRobot robot) {
        assertNotNull(dashboardView);

        // Target lookups directly using the exact expected rendered text combinations
        Label welcomeLabel = robot.lookup("Welcome, Joe!").queryAs(Label.class);
        Button profileBtn = robot.lookup("Profile").queryAs(Button.class);
        Button logoutBtn = robot.lookup("Logout").queryAs(Button.class);

        assertNotNull(welcomeLabel,
                "Personalized welcome title banner is missing from the layout graph.");
        assertNotNull(profileBtn,
                "Missing the 'Profile' configuration action button item.");
        assertNotNull(logoutBtn,
                "Missing the 'Logout' termination session button item.");
    }

    /**
     * Validates that the progress row section falls back safely to its empty state
     * notice string when the profile does not have any active module subjects
     * tracking.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testProgressSectionFallbackWithNoSubjects(final FxRobot robot) {
        // Assert foundational labels exist
        Label dashboardTitle = robot.lookup("Your Learning Dashboard")
                .queryAs(Label.class);
        Label progressSubTitle = robot.lookup("Your Progress")
                .queryAs(Label.class);

        // Assert fallback label renders explicitly when subjects array is empty
        Label placeholderLabel = robot.lookup("No modules selected.")
                .queryAs(Label.class);

        assertNotNull(dashboardTitle,
                "Missing primary 'Your Learning Dashboard' title layout label.");
        assertNotNull(progressSubTitle,
                "Missing secondary section tracking title layout label.");
        assertNotNull(placeholderLabel,
                "Missing default empty message fallback response text.");
    }

    /**
     * Validates that updating the profile data model updates the layout
     * dynamically, generating progressive percentage card items for every
     * injected string.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testProgressSectionPopulatesDynamicRows(final FxRobot robot) {
        // Assign subjects and force view updates inside the safe FX thread loop
        // context
        robot.interact(() -> {
            testProfile.setSubjects(new ArrayList<>(Arrays.asList(
                    "Engineering Mathematics", "Analogue Electronics")));
            // Re-instantiate layout parameters to process the new subject lists
            // inside the scene graph
            dashboardView.setContent(
                    new HBox(new DashboardView(testProfile)));
        });

        // Query structural components off the live rewritten canvas setup layout
        // tree
        Label engMathLabel = robot.lookup("Engineering Mathematics")
                .queryAs(Label.class);
        Label analogueElecLabel = robot.lookup("Analogue Electronics")
                .queryAs(Label.class);

        // Find how many percentage markers exist (should match 2 active row metrics)
        long completeLabelCount = robot.lookup("0% Complete").queryAll()
                .stream()
                .filter(Label.class::isInstance)
                .count();

        assertNotNull(engMathLabel,
                "Dynamic text generator failed to construct row entry for "
                + "Engineering Mathematics.");
        assertNotNull(analogueElecLabel,
                "Dynamic text generator failed to construct row entry for "
                + "Analogue Electronics.");
        assertEquals(2, completeLabelCount,
                "Should output exactly 2 tracking rows holding '0% Complete' "
                + "fields.");
    }

    /**
     * Verifies that placeholder topic metrics and action card blocks exist
     * within the layout graph structure.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testRecommendedTopicsCardsAndNavigationText(final FxRobot robot) {
        // Verify primary header definitions inside the layout card section block
        Label recommendationHeading = robot.lookup("Recommended Topics")
                .queryAs(Label.class);
        Label topicEmoji = robot.lookup("📘").queryAs(Label.class);
        Label topicTitle = robot.lookup("Electronics").queryAs(Label.class);

        // Verify nested child module card entries built by helper actions
        Label analyticCardText = robot.lookup("Continue: Analogue Electronics")
                .queryAs(Label.class);
        Button resumeBtn = robot.lookup("Resume Lesson").queryAs(Button.class);

        Label exploreCardText = robot.lookup("Explore Other Topics")
                .queryAs(Label.class);
        Button browseBtn = robot.lookup("Browse All").queryAs(Button.class);

        assertNotNull(recommendationHeading,
                "Missing the parent 'Recommended Topics' layout heading.");
        assertNotNull(topicEmoji,
                "Missing thematic card visual icon indicator.");
        assertNotNull(topicTitle,
                "Missing subject group card label placeholder title.");
        assertNotNull(analyticCardText,
                "Analogue Electronics card component went missing.");
        assertNotNull(resumeBtn,
                "Missing operational 'Resume Lesson' navigation handle button.");
        assertNotNull(exploreCardText,
                "Alternative discovery topic layout block card went missing.");
        assertNotNull(browseBtn,
                "Missing operational 'Browse All' context navigation handle "
                + "button.");
    }

    /**
     * Intercepts individual style sheets to verify mouse event handlers
     * accurately transition design characteristics during execution.
     *
     * @param robot the TestFX robot for interaction.
     */
    @Test
    void testButtonHoverStyleTransitions(final FxRobot robot) {
        // Target specific functional control component
        Button testButton = robot.lookup("Resume Lesson").queryAs(Button.class);
        assertNotNull(testButton);

        // Keep hold of baseline styles tracking values
        String originalStyleString = testButton.getStyle();

        // Simulate user dragging the pointer onto the component container bounds
        robot.moveTo(testButton);
        String activeHoverStyleString = testButton.getStyle();

        // Pull pointer away out of the operational perimeter framework boundary
        // bounds
        robot.moveBy(250, 250);
        String postExitRestoredStyleString = testButton.getStyle();

        // Assert inline styles adjusted into hover mappings and dropped clean on
        // exit
        assertTrue(activeHoverStyleString.contains("white")
                || !activeHoverStyleString.equals(originalStyleString),
                "Inline style properties failed to apply background alterations "
                + "when entering button footprint coordinates.");
        assertEquals(originalStyleString, postExitRestoredStyleString,
                "Button inline rules failed to restore baseline aesthetics on "
                + "cursor exit.");
    }

