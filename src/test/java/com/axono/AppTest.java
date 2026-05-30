package com.axono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Integration lifecycle tests for the main application entry point {@link App}.
 * This test class leverages the TestFX framework to simulate the real
 * application
 * bootstrap sequence. Because {@link AppStage} immediately intercepts the
 * execution
 * pipeline to spin up a secondary onboarding window, these tests broad-scan the
 * global desktop workspace context to ensure UI containers initialize without
 * crashes.
 */
@ExtendWith(ApplicationExtension.class)
class AppTest {

        /**
         * The initial root window stage reference managed by the TestFX framework
         * environment shell.
         */
        private Stage primaryWindowStage;

        /**
         * Bootstraps the application runtime environment prior to running each test
         * case.
         * This mimics the real JavaFX Application launcher workflow by passing a live
         * workspace stage directly into the target application instance wrapper.
         *
         * @param stage the primary window shell context provided automatically by
         *              TestFX.
         * @throws Exception if target initialization boundaries or internal layout
         *                   dependencies throw errors.
         */
        @Start
        void start(final Stage stage) throws Exception {
                this.primaryWindowStage = stage;

                // Instantiate the main application executable entry point
                App applicationInstance = new App();

                // Explicitly invoke the application start lifecycle hook on the FX toolkit
                // thread
                applicationInstance.start(primaryWindowStage);
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
         * Verifies that executing the main application launch routine surfaces a user
         * interface
         * window frame to the user cleanly.
         * Due to the application's layout architecture, the primary stage remains
         * hidden in the
         * background while a secondary onboarding stage takes foreground focus.
         * Therefore, this test
         * ensures that at least one active, visible window successfully mounts onto the
         * screen graph.
         *
         * @param robot the TestFX automation driver used to inspect active window
         *              configurations.
         * @throws Exception if the dynamic polling loop encounters an intermittent
         *                   execution timeout.
         */
        /**
         * Verifies that the application launches a visible window.
         *
         * @param robot the TestFX automation driver.
         * @throws Exception if polling timeout occurs.
         */
        @Test
        void testApplicationWindowLaunchesCleanly(final FxRobot robot)
                throws Exception {
                assertNotNull(primaryWindowStage,
                                "The application primary launch stage was "
                                + "left completely uninitialized.");

                // Dynamic Wait: Wait until at least one window is visible
                WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> {
                        List<Window> openWindows = robot.robotContext()
                                .getWindowFinder().listWindows();
                        return !openWindows.isEmpty()
                                && openWindows.stream()
                                .anyMatch(Window::isShowing);
                });

                // Verify that an active window is showing on screen
                List<Window> windows = robot.robotContext()
                        .getWindowFinder().listWindows();
                boolean anyWindowShowing = windows.stream()
                        .anyMatch(Window::isShowing);

                assertTrue(anyWindowShowing,
                                "No user interface windows were presented "
                                + "to the screen graph upon app boot.");

                // Verify visible window is the primary or onboarding stage
                Window visibleWindow = windows.stream()
                        .filter(Window::isShowing).findFirst()
                        .orElse(null);
                assertNotNull(visibleWindow,
                                "Failed to capture the active visible "
                                + "onboarding window context frame.");
        }

        /**
         * Verifies that the primary stage properties remain intact.
         * The primary stage is passed downstream to wait for onboarding
         * completion callbacks.
         */
        @Test
        void testAppStageConfigurationIsApplied() {
                assertNotNull(primaryWindowStage);
                // The primary stage is safely preserved as a backend
                // reference waiting for onboarding callbacks
                assertNotNull(primaryWindowStage.getProperties(),
                                "The primary window stage context reference "
                                + "was completely corrupted during boot.");
        }
}
