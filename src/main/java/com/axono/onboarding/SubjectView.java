package com.axono.onboarding;

import com.axono.model.UserProfile;
import com.axono.ui.UIConstants;
import com.axono.ui.UITheme;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
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
         * The full curriculum data structure used
         * to render the module selection UI.
         */
        private static final YearGroup[] CURRICULUM = loadCurriculum();

        /**
         * Parses the curriculum data from the external XML file.
         *
         * @return an array of parsed {@link YearGroup} instances
         *         representing the full curriculum; never {@code null}.
         */
        private static YearGroup[] loadCurriculum() {
                try (InputStream is = SubjectView.class
                                .getResourceAsStream("/curriculum.xml")) {
                        DocumentBuilder db = createSecureDocumentBuilder();
                        Document doc = db.parse(is);
                        doc.getDocumentElement().normalize();
                        return parseYearGroups(doc);
                } catch (Exception e) {
                        e.printStackTrace();
                        return new YearGroup[0];
                }
        }

        /**
         * Creates a secure {@link DocumentBuilder}
         * with external entities disabled.
         *
         * @return a new {@link DocumentBuilder}
         *         configured with secure settings.
         * @throws ParserConfigurationException if a document builder
         *                                      cannot be created.
         */
        private static DocumentBuilder createSecureDocumentBuilder()
                        throws ParserConfigurationException {
                DocumentBuilderFactory dbf = DocumentBuilderFactory
                                .newInstance();

                String featDoctype = "http://apache.org/xml/features/"
                                + "disallow-doctype-decl";
                dbf.setFeature(featDoctype, true);

                String featGen = "http://xml.org/sax/features/"
                                + "external-general-entities";
                dbf.setFeature(featGen, false);

                String featParam = "http://xml.org/sax/features/"
                                + "external-parameter-entities";
                dbf.setFeature(featParam, false);

                String featDtd = "http://apache.org/xml/features/"
                                + "nonvalidating/load-external-dtd";
                dbf.setFeature(featDtd, false);

                dbf.setXIncludeAware(false);
                dbf.setExpandEntityReferences(false);

                return dbf.newDocumentBuilder();
        }

        /**
         * Extracts {@link YearGroup} objects from the parsed XML document.
         *
         * @param doc the DOM {@link Document} containing the curriculum XML.
         * @return an array of {@link YearGroup} instances parsed from the
         *         {@code yearGroup} elements in the document.
         */
        private static YearGroup[] parseYearGroups(final Document doc) {
                List<YearGroup> years = new ArrayList<>();
                NodeList yearNodes = doc.getElementsByTagName("yearGroup");

                for (int i = 0; i < yearNodes.getLength(); i++) {
                        Element yearEl = (Element) yearNodes.item(i);
                        String yLabel = yearEl.getAttribute("label");
                        Section[] sections = parseSections(yearEl);
                        years.add(new YearGroup(yLabel, sections));
                }
                return years.toArray(new YearGroup[0]);
        }

        /**
         * Extracts {@link Section} objects for a specific year element.
         *
         * @param yearEl the {@link Element} representing a single
         *               {@code yearGroup} in the XML document.
         * @return an array of {@link Section} instances belonging to
         *         the specified year element.
         */
        private static Section[] parseSections(final Element yearEl) {
                List<Section> sections = new ArrayList<>();
                NodeList secNodes = yearEl.getElementsByTagName("section");

                for (int j = 0; j < secNodes.getLength(); j++) {
                        Element secEl = (Element) secNodes.item(j);
                        String title = secEl.hasAttribute("title")
                                        ? secEl.getAttribute("title")
                                        : null;
                        Module[] modules = parseModules(secEl);
                        sections.add(new Section(title, modules));
                }
                return sections.toArray(new Section[0]);
        }

        /**
         * Extracts {@link Module} objects for a specific section element.
         *
         * @param secEl the {@link Element} representing a single
         *              {@code section} in the XML document.
         * @return an array of {@link Module} instances belonging to
         *         the specified section element.
         */
        private static Module[] parseModules(final Element secEl) {
                List<Module> modules = new ArrayList<>();
                NodeList modNodes = secEl.getElementsByTagName("module");

                for (int k = 0; k < modNodes.getLength(); k++) {
                        Element mEl = (Element) modNodes.item(k);
                        String name = mEl.getAttribute("name");
                        String desc = mEl.getAttribute("desc");
                        modules.add(new Module(name, desc));
                }
                return modules.toArray(new Module[0]);
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
                for (YearGroup year : CURRICULUM) {
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
