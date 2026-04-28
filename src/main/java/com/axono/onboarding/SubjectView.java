package com.axono.onboarding;

import com.axono.model.UserProfile;
import com.axono.ui.UIConstants;
import com.axono.ui.UITheme;
import com.axono.database.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.Border;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// import javax.xml.parsers.DocumentBuilder;
// import javax.xml.parsers.DocumentBuilderFactory;
// import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Onboarding step that presents a scrollable curriculum of engineering modules
 * grouped by year and section. The user selects the modules they are studying
 * using checkboxes, and the selection is saved to the {@link UserProfile}.
 */
public final class SubjectView extends StackPane {

        /** The user profile to populate when {@link #saveData()} is called. */
        private final UserProfile profile;

        /**
         * Flat list of all {@link CheckBox} controls
         * rendered in the curriculum grid.
         */
        private final List<CheckBox> checkboxes = new ArrayList<>();

        /** Reusable JavaFX CSS prefix for setting text colour. */
        private static final String FX_TEXT_FILL = "-fx-text-fill: ";

        /**
         * Immutable static data model representing
         * a single teachable module.
         */
        private static class Module {

                /** The display name of the module. */
                private final String name;

                /** A short description of the module's content. */
                private final String desc;

                /**
                 * Constructs a {@code Module}
                 * with the given name and description.
                 *
                 * @param moduleName the module display name.
                 * @param moduleDesc the short module description.
                 */
                Module(final String moduleName, final String moduleDesc) {
                        this.name = moduleName;
                        this.desc = moduleDesc;
                }

                /** @return the display name of this module. */
                String getName() {
                        return name;
                }

                /** @return the short description of this module. */
                String getDesc() {
                        return desc;
                }
        }

        /**
         * A named group of {@link Module} objects
         * within a year (e.g. "Core Modules").
         */
        private static class Section {
                /**
                 * The section heading (e.g. "Core Modules", "Option Modules")
                 * or {@code null}.
                 */
                private final String title;

                /** The modules belonging to this section. */
                private final Module[] modules;

                /**
                 * Constructs a {@code Section} with
                 * the given title and modules.
                 *
                 * @param sectionTitle   the section heading,
                 *                       or {@code null} for untitled sections.
                 * @param sectionModules the modules in this section.
                 */
                Section(final String sectionTitle,
                                final Module... sectionModules) {
                        this.title = sectionTitle;
                        this.modules = sectionModules;
                }

                /** @return the section heading, or {@code null}. */
                String getTitle() {
                        return title;
                }

                /** @return the modules in this section. */
                Module[] getModules() {
                        return modules.clone();
                }
        }

        /**
         * Academic year group containing one or more {@link Section}s.
         */
        private static class YearGroup {
                /** The year label (e.g. "Year 1", "Foundation Year"). */
                private final String label;

                /** The sections belonging to this year group. */
                private final Section[] sections;

                /**
                 * Constructs a {@code YearGroup} with
                 * the given label and sections.
                 *
                 * @param yearLabel    the year group label.
                 * @param yearSections the sections within this year group.
                 */
                YearGroup(final String yearLabel,
                                final Section... yearSections) {
                        this.label = yearLabel;
                        this.sections = yearSections;
                }

                /** @return the year group label. */
                String getLabel() {
                        return label;
                }

                /** @return the sections in this year group. */
                Section[] getSections() {
                        return sections.clone();
                }
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
         * Constructs the {@code SubjectView} for the given user profile and
         * renders the module selection UI.
         *
         * @param userProfile the {@link UserProfile} to populate on save.
         */
        public SubjectView(final UserProfile userProfile) {
                this.profile = userProfile;
                buildUI();
        }

        /**
         * Builds the full module selection layout including the heading,
         * Select All button, and scrollable curriculum list.
         */
        private void buildUI() {
                setStyle("-fx-background-color: " + UITheme.BG + ";");
                setPadding(new Insets(UIConstants.PADDING_MD));

                Label heading = new Label("Select Your Modules");
                heading.setStyle(FX_TEXT_FILL + UITheme.PRIMARY
                                + ";" + "-fx-font-size: 20px;"
                                + "-fx-font-weight: bold;");
                HBox.setHgrow(heading, Priority.ALWAYS);

                Button selectAll = new Button("Select All");
                selectAll.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
                selectAll.setOnAction(e -> {
                        boolean anyUnchecked = checkboxes.stream().anyMatch(
                                        c -> !c.isSelected());
                        checkboxes.forEach(c -> c.setSelected(anyUnchecked));
                });

                HBox topRow = new HBox(UIConstants.SPACING_MD,
                                heading, selectAll);
                topRow.setAlignment(Pos.CENTER_LEFT);

                Label sub = new Label("Choose the modules you're studying:");
                sub.setStyle(FX_TEXT_FILL
                                + UITheme.TEXT_MUTED
                                + "; -fx-font-size: 14px;");

                VBox content = new VBox(UIConstants.SPACING_2XL);

                // Load from database at runtime
                YearGroup[] curriculum = loadCurriculum();

                for (YearGroup year : curriculum) {
                        content.getChildren().add(buildYearBlock(year));
                }

                ScrollPane scroll = new ScrollPane(content);
                scroll.setFitToWidth(true);
                scroll.setStyle("-fx-background: transparent; "
                                + "-fx-background-color: transparent;");
                scroll.setBorder(Border.EMPTY);
                VBox.setVgrow(scroll, Priority.ALWAYS);

                VBox card = new VBox(UIConstants.SPACING_MD,
                                topRow, sub, scroll);
                card.setMaxWidth(UIConstants.SUBJECT_MAX_WIDTH);
                card.setStyle(UITheme.CARD_STYLE + "-fx-padding: 24px 36px;");

                setAlignment(Pos.CENTER);
                getChildren().add(card);
        }

        /**
         * Builds a {@link VBox} block representing a single academic year,
         * including its label, separator, and section grids.
         *
         * @param year the {@link YearGroup} to render.
         * @return a {@link VBox} containing the year's UI elements.
         */
        private VBox buildYearBlock(final YearGroup year) {
                Label yearLabel = new Label(year.getLabel());
                yearLabel.setStyle(FX_TEXT_FILL + UITheme.PRIMARY
                                + "; -fx-font-size: 15px;"
                                + "-fx-font-weight: bold;");

                Separator sep = new Separator();
                VBox.setMargin(sep, new Insets(2, 0,
                                UIConstants.SPACING_XS, 0));

                VBox block = new VBox(UIConstants.SPACING_SM, yearLabel, sep);

                for (Section section : year.getSections()) {
                        if (section.getTitle() != null) {
                                Label sectionLabel = new Label(section
                                                .getTitle()
                                                .toUpperCase());
                                sectionLabel.setStyle(FX_TEXT_FILL
                                                + UITheme.TEXT_MUTED
                                                + "; -fx-font-size: 10px;"
                                                + "-fx-font-weight: bold;");
                                VBox.setMargin(sectionLabel, new Insets(
                                                UIConstants.SPACING_XS,
                                                0, 2, 0));
                                block.getChildren().add(sectionLabel);
                        }
                        block.getChildren().add(buildGrid(section
                                        .getModules()));
                }
                return block;
        }

        /**
         * Builds a two-column {@link GridPane}
         * of checkbox cells for the given modules.
         * Each cell contains a {@link CheckBox}
         * and a description label, and highlights
         * when selected.
         *
         * @param modules the array of {@link Module} objects to render.
         * @return a {@link GridPane} containing one cell per module.
         */
        private GridPane buildGrid(final Module[] modules) {
                String normalStyle = "-fx-background-color: " + UITheme.BG
                                + ";" + "-fx-border-color: " + UITheme.BORDER
                                + ";" + "-fx-border-radius: 4px;"
                                + "-fx-background-radius: 4px;"
                                + "-fx-padding: 8px 10px;";
                String selectedStyle = "-fx-background-color: #E8F7FB;"
                                + "-fx-border-color: " + UITheme.SECONDARY
                                + "; -fx-border-width: 2px;"
                                + "-fx-border-radius: 4px;"
                                + "-fx-background-radius: 4px;"
                                + "-fx-padding: 8px 10px;";

                GridPane grid = new GridPane();
                grid.setHgap(UIConstants.SPACING_SM);
                grid.setVgap(UIConstants.SPACING_XS);

                ColumnConstraints c1 = new ColumnConstraints();
                c1.setPercentWidth(UIConstants.SUBJECT_ICON_SIZE);
                ColumnConstraints c2 = new ColumnConstraints();
                c2.setPercentWidth(UIConstants.SUBJECT_ICON_SIZE);
                grid.getColumnConstraints().addAll(c1, c2);

                for (int i = 0; i < modules.length; i++) {
                        Module m = modules[i];

                        CheckBox cb = new CheckBox(m.getName());
                        cb.setStyle("-fx-font-size: 13px; "
                                        + FX_TEXT_FILL
                                        + UITheme.TEXT_DARK + ";");
                        cb.setWrapText(true);

                        Label desc = new Label(m.getDesc());
                        desc.setStyle("-fx-font-size: 11px; -fx-text-fill: "
                                        + UITheme.TEXT_MUTED + ";");
                        desc.setWrapText(true);

                        VBox cell = new VBox(2, cb, desc);
                        cell.setStyle(normalStyle);
                        cb.selectedProperty().addListener((obs,
                                        old,
                                        sel) -> cell.setStyle(sel
                                                        ? selectedStyle
                                                        : normalStyle));

                        checkboxes.add(cb);
                        grid.add(cell, i % 2, i / 2);
                }
                return grid;
        }

        /**
         * Validates that the user has selected at least one module.
         * Displays a warning alert if no modules are selected.
         *
         * @return {@code true} if at least
         *         one checkbox is selected; {@code false}
         *         otherwise.
         */
        public boolean validateInput() {
                boolean any = checkboxes.stream().anyMatch(
                                CheckBox::isSelected);
                if (!any) {
                        Alert a = new Alert(Alert.AlertType.WARNING,
                                        "Please select at least one module.",
                                        ButtonType.OK);
                        a.setHeaderText("Required");
                        a.showAndWait();
                }
                return any;
        }

        /**
         * Saves the names of all selected modules into the associated
         * {@link UserProfile}.
         * Must only be called after
         * {@link #validateInput()} returns {@code true}.
         */
        public void saveData() {
                List<String> selected = new ArrayList<>();
                checkboxes.stream()
                                .filter(CheckBox::isSelected)
                                .map(CheckBox::getText)
                                .forEach(selected::add);
                profile.setSubjects(selected);
        }
}
