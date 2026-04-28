package com.axono.home;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.axono.database.Database;
import com.axono.ui.UIConstants;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;

/**
 * The application home page view, displayed after onboarding is complete.
 * Shows a welcome banner and a scrollable list of module topic buttons
 * for the Analogue Electronics module.
 */
public final class HomepageView extends ScrollPane {

        /** Primary brand colour hex string (local copy for this view). */
        private static final String PRIMARY = "#59BE8B";

        /** Page background colour hex string. */
        private static final String BG = "#FCFBFB";

        /** Card background colour hex string. */
        private static final String CARD = "#FFFFFF";

        /** Body text colour hex string. */
        private static final String TEXT = "#111827";

        /** Border colour hex string. */
        private static final String BORDER = "#DCDCDC";

        /** Reusable JavaFX CSS prefix for setting background colour. */
        private static final String BG_COLOR = "-fx-background-color: ";

        /**
         * Array of topic names displayed as
         * navigable buttons on the home page.
         */
        private static final String[] TOPICS = {
                        "Layouts", "Decibels", "Op-Amps", "Electromagnetism",
                        "Phasors", "Complex Impedances", "Kirchhoff's Laws",
                        "Passive Networks", "Dividers",
                        "Equivalent Networks", "Circuit Analysis",
                        "Op-Amp Bandwidths", "Poles and Zeros",
                        "Frequency Response",
                        "Step Response"
        };

        /**
         * Constructs the {@code HomepageView}
         * and builds the scrollable layout.
         */
        public HomepageView() {
                buildUI();
        }

        /**
         * Loads the curriculum from the database, organizing stages into YearGroups.
         * Each YearGroup contains a single Section with all modules for that stage.
         *
         * @return an array of YearGroup objects representing the curriculum.
         */
        private static YearGroup[] loadCurriculum() {
                List<YearGroup> years = new ArrayList<>();

                // Query to retrieve all stages ordered by level
                String stageQuery = "SELECT id, name FROM stage ORDER BY level";

                try (Connection conn = Database.getConnection();
                                PreparedStatement stageStmt = conn.prepareStatement(stageQuery);
                                ResultSet stageRs = stageStmt.executeQuery()) {

                        // Iterate through each stage
                        while (stageRs.next()) {
                                int stageId = stageRs.getInt("id");
                                String stageName = stageRs.getString("name");

                                // Load modules for this stage
                                Module[] modules = loadModulesForStage(conn, stageId);

                                // Wrap modules in a single section (no title)
                                Section section = new Section(null, modules);

                                // Add to the list of year groups
                                years.add(new YearGroup(stageName, section));
                        }

                } catch (SQLException e) {
                        e.printStackTrace();
                }

                // Convert list to array and return
                return years.toArray(new YearGroup[0]);
        }

        /**
         * Loads all modules for a given stage from the database.
         *
         * @param conn    the database connection.
         * @param stageId the ID of the stage.
         * @return an array of Module objects for the stage.
         * @throws SQLException if a database error occurs.
         */
        private static Module[] loadModulesForStage(Connection conn, int stageId)
                        throws SQLException {

                // Query to retrieve modules for the given stage
                String moduleQuery = "SELECT id, name FROM module WHERE stage_id = ?";
                List<Module> modules = new ArrayList<>();

                try (PreparedStatement stmt = conn.prepareStatement(moduleQuery)) {
                        stmt.setInt(1, stageId);

                        try (ResultSet rs = stmt.executeQuery()) {
                                // Iterate through each module
                                while (rs.next()) {
                                        int moduleId = rs.getInt("id");
                                        String moduleName = rs.getString("name");

                                        // Load topics as description for this module
                                        String desc = loadTopicsForModule(conn, moduleId);

                                        // Add module to the list
                                        modules.add(new Module(moduleName, desc));
                                }
                        }
                }

                // Convert list to array and return
                return modules.toArray(new Module[0]);
        }

        /**
         * Loads all topic names for a given module and joins them into a single string.
         *
         * @param conn     the database connection.
         * @param moduleId the ID of the module.
         * @return a comma-separated string of topic names.
         * @throws SQLException if a database error occurs.
         */
        private static String loadTopicsForModule(Connection conn, int moduleId)
                        throws SQLException {

                // Query to retrieve topics for the given module
                String topicQuery = "SELECT name FROM topic WHERE module_id = ?";
                List<String> topics = new ArrayList<>();

                try (PreparedStatement stmt = conn.prepareStatement(topicQuery)) {
                        stmt.setInt(1, moduleId);

                        try (ResultSet rs = stmt.executeQuery()) {
                                // Collect all topic names
                                while (rs.next()) {
                                        topics.add(rs.getString("name"));
                                }
                        }
                }

                // Join topics into a single string and return
                return String.join(", ", topics);
        }

        /**
         * Builds the full page layout including
         * the welcome banner and module topic list,
         * wrapping everything in a centred scrollable container.
         */
        private void buildUI() {
                VBox content = new VBox(UIConstants.SPACING_3XL);
                content.setAlignment(Pos.TOP_CENTER);
                content.setPadding(new Insets(
                                UIConstants.CONTENT_PADDING_V,
                                UIConstants.PADDING_MD,
                                UIConstants.CONTENT_PADDING_V,
                                UIConstants.PADDING_MD));
                content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
                content.setStyle(BG_COLOR + BG + ";");
                content.getChildren().addAll(buildBanner(),
                                buildModuleTopicList());

                HBox wrapper = new HBox(content);
                wrapper.setAlignment(Pos.TOP_CENTER);
                wrapper.setStyle(BG_COLOR + BG + ";");
                HBox.setHgrow(content, Priority.ALWAYS);

                setContent(wrapper);
                setFitToWidth(true);
                setBorder(Border.EMPTY);
                setStyle(BG_COLOR + BG + ";");
        }

        /**
         * Builds and returns the welcome banner containing
         * the title, subtitle, role selection buttons, and a login prompt.
         *
         * @return a {@link VBox} containing the banner elements.
         */
        private VBox buildBanner() {
                Label welcome = new Label("Welcome to Axono ReWire!");
                welcome.setStyle("-fx-font-size: 32px;"
                                + " -fx-font-weight: bold; -fx-text-fill: "
                                + TEXT + ";");

                Label subtitle = new Label(
                                "Start your engineering journey today!");
                subtitle.setStyle("-fx-font-size: 18px; -fx-text-fill: "
                                + TEXT + ";");

                HBox buttons = new HBox(UIConstants.SPACING_MD,
                                outlineButton("I'm a Student"),
                                outlineButton("I'm an Educator"));
                buttons.setAlignment(Pos.CENTER);

                Label loginPrompt = new Label(
                                "Already have an account? Log in");
                loginPrompt.setStyle("-fx-font-size: 14px; -fx-text-fill: "
                                + TEXT + ";");

                VBox banner = new VBox(UIConstants.SPACING_MD,
                                welcome, subtitle, buttons, loginPrompt);
                banner.setAlignment(Pos.CENTER);
                return banner;
        }

        /**
         * Builds and returns a scrollable list of topic buttons for the
         * Analogue Electronics module.
         *
         * @return a {@link VBox} containing the module name label and
         *         the topic list card.
         */
        private VBox buildModuleTopicList() {
                Label moduleName = new Label("Analogue Electronics");
                moduleName.setStyle("-fx-font-size: 24px;"
                                + " -fx-font-weight: bold; -fx-text-fill: "
                                + TEXT + ";");

                VBox list = new VBox(UIConstants.SPACING_SM);
                list.setPadding(new Insets(UIConstants.PADDING_MD));
                list.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);
                list.setStyle(BG_COLOR + CARD + ";"
                                + "-fx-border-color: "
                                + BORDER + ";"
                                + "-fx-border-radius: 6px;"
                                + "-fx-background-radius: 6px;");

                for (String topic : TOPICS) {
                        VBox topicButton = new VBox(
                                        UIConstants.SPACING_XS,
                                        outlineButton(topic));
                        topicButton.setStyle("-fx-font-size: 16px;"
                                        + " -fx-text-fill: "
                                        + TEXT + ";");
                        list.getChildren().add(topicButton);
                }

                VBox section = new VBox(
                                UIConstants.SPACING_XL,
                                moduleName, list);
                section.setAlignment(Pos.CENTER_LEFT);
                section.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);
                return section;
        }

        /**
         * Creates a styled outline {@link Button} with hover fill effects.
         *
         * @param text the button label.
         * @return a configured {@link Button}.
         */
        private Button outlineButton(final String text) {
                String base = "-fx-background-color: transparent;"
                                + "-fx-border-color: " + PRIMARY
                                + "; -fx-border-width: 2px;"
                                + "-fx-border-radius: 4px;"
                                + "-fx-background-radius: 4px;"
                                + "-fx-text-fill: " + PRIMARY + ";"
                                + "-fx-font-weight: bold;"
                                + "-fx-font-size: 14px; -fx-cursor: hand;";
                String hover = BG_COLOR + PRIMARY + ";"
                                + "-fx-border-color: " + PRIMARY
                                + "; -fx-border-width: 2px;"
                                + "-fx-border-radius: 4px;"
                                + "-fx-background-radius: 4px;"
                                + "-fx-text-fill: white;"
                                + "-fx-font-weight: bold; -fx-font-size: 14px;"
                                + "-fx-cursor: hand;";
                Button b = new Button(text);
                b.setStyle(base);
                b.setOnMouseEntered(e -> b.setStyle(hover));
                b.setOnMouseExited(e -> b.setStyle(base));
                return b;
        }
}
