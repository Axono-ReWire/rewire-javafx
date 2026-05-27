package com.axono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import com.axono.model.UserProfile;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Structural and view-navigation lifecycle tests for {@link AppStage}.
 * Bypasses the modal onboarding stage via reflection to verify navigation
 * structures,
 * active CSS button highlights, and view layout transitions.
 */
@ExtendWith(ApplicationExtension.class)
class AppStageTest {

    private Stage mainStage;
    private AppStage appStage;
    private UserProfile mockProfile;

    /**
     * Prepares the main stage container. The Onboarding window will open
     * automatically,
     * but the execution flow is intercepted in testing.
     */
    @Start
    void start(Stage stage) {
        this.mainStage = stage;

        // Setup a mock profile to feed into the completion callback
        mockProfile = new UserProfile();
        mockProfile.setName("Test Engineer");
        mockProfile.setYearOfStudy("Year 3");
        mockProfile.setInstitution("Axono University");

        // Instantiate the system under test (opens Onboarding internally)
        appStage = new AppStage(mainStage);
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
     * Helper method that uses reflection to invoke the private
     * onOnboardingComplete() callback.
     * This forces AppStage to build its main navigation layout immediately on the
     * FX thread.
     */
    private void completeOnboardingBypass(FxRobot robot) throws Exception {
        robot.interact(() -> {
            try {
                Method callbackMethod = AppStage.class.getDeclaredMethod("onOnboardingComplete", UserProfile.class);
                callbackMethod.setAccessible(true);
                callbackMethod.invoke(appStage, mockProfile);
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke onboarding complete callback via reflection", e);
            }
        });

        // Block until the primary window renders and gains its structural title
        // property
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> "Axono ReWire".equals(mainStage.getTitle()));
    }

    @Test
    void testMainUiAssemblyPostOnboarding(FxRobot robot) throws Exception {
        // Complete the onboarding wall via reflection bypass
        completeOnboardingBypass(robot);

        assertTrue(mainStage.isShowing(), "The main application stage should be made visible.");
        assertEquals("Axono ReWire", mainStage.getTitle(), "Main stage window title string mismatch.");

        assertNotNull(mainStage.getScene(), "Main stage scene graph container should be populated.");
        assertTrue(mainStage.getScene().getRoot() instanceof BorderPane,
                "The root node should be an instance of BorderPane.");
    }

    @Test
    void testDefaultViewInitialization(FxRobot robot) throws Exception {
        completeOnboardingBypass(robot);

        Button homeBtn = robot.lookup("Home").queryAs(Button.class);
        Button dashBtn = robot.lookup("Dashboard").queryAs(Button.class);

        // Verify Home button is active (uses non-transparent background alpha)
        assertTrue(homeBtn.getStyle().contains("rgba(255,255,255,0.28)"),
                "The Home button should possess the active background CSS highlight style.");

        // Verify Dashboard button remains inactive (uses completely transparent
        // background styling)
        assertTrue(dashBtn.getStyle().contains("transparent"),
                "The Dashboard button should default to an inactive transparent style color state.");

        // Confirm the current view loaded in the center of the root panel is the
        // HomepageView
        BorderPane rootPane = (BorderPane) mainStage.getScene().getRoot();
        assertEquals("HomepageView", rootPane.getCenter().getClass().getSimpleName(),
                "The initial center display node pane should be the HomepageView.");
    }

    @Test
    void testNavigationViewTransitions(FxRobot robot) throws Exception {
        completeOnboardingBypass(robot);

        Button homeBtn = robot.lookup("Home").queryAs(Button.class);
        Button dashBtn = robot.lookup("Dashboard").queryAs(Button.class);
        BorderPane rootPane = (BorderPane) mainStage.getScene().getRoot();

        // Click the Dashboard Nav Trigger Control
        robot.clickOn(dashBtn);

        // Dashboard view should replace the central pane node context
        assertEquals("DashboardView", rootPane.getCenter().getClass().getSimpleName(),
                "The central panel view node layout failed to transition over into a DashboardView structure.");

        // Active CSS state metrics must swap instantly between components
        assertTrue(dashBtn.getStyle().contains("rgba(255,255,255,0.28)"),
                "Dashboard button failed to gain the active highlight.");
        assertTrue(homeBtn.getStyle().contains("transparent"),
                "Home button failed to drop its active style state context.");

        // Click the Results Nav Trigger Control
        Button resultsBtn = robot.lookup("Results (temp)").queryAs(Button.class);
        robot.clickOn(resultsBtn);

        assertEquals("ResultsPage", rootPane.getCenter().getClass().getSimpleName(),
                "The central layout workspace failed to route active view frames into a ResultsPage context container.");
        assertTrue(resultsBtn.getStyle().contains("rgba(255,255,255,0.28)"),
                "Results button failed to gain active highlight states.");
    }
}
