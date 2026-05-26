package com.axono.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.axono.model.UserProfile;
import com.axono.ui.UIConstants;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class DashboardViewTest {

    private DashboardView dashboardView;
    private UserProfile testProfile;

    @Start
    void start(Stage stage) {
        testProfile = new UserProfile();
        testProfile.setName("Joe");
        testProfile.setYearOfStudy("Year 1");
        testProfile.setInstitution("University of York");
        testProfile.setSubjects(List.of("Engineering Mathematics", "Analogue Electronics"));

        this.dashboardView = new DashboardView(testProfile);

        Scene scene = new Scene(new StackPane(dashboardView), UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void sampleTest() {
        assertEquals(4, 2 + 2);
    }

    @Test
    void testWelcomeBannerRenders() {
        assertNotNull(dashboardView);
        boolean hasWelcomeMessage = false;

        for (javafx.scene.Node node : dashboardView.lookupAll("")) {
            if (node instanceof javafx.scene.control.Label) {
                String labelText = ((javafx.scene.control.Label) node).getText();

                if (labelText != null && labelText.contains("Joe")) {
                    hasWelcomeMessage = true;
                    break;
                }
            }
        }

        assertTrue(hasWelcomeMessage, "The dashboard should contain a text label personalized with 'Joe'");
    }
}
