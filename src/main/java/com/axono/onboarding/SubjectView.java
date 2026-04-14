package com.axono.onboarding;

import com.axono.model.UserProfile;
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
         * Flat list of all {@link CheckBox} controls rendered in the curriculum grid.
         */
        private final List<CheckBox> checkboxes = new ArrayList<>();

        /** Constant label string used for core module section headers. */
        private static final String CORE_MODULES = "Core Modules";

        /** Reusable JavaFX CSS prefix for setting text colour. */
        private static final String FX_TEXT_FILL = "-fx-text-fill: ";

        /**
         * Immutable static data model representing a single teachable module.
         */
        private static class Module {

                /** The display name of the module. */
                final String name;

                /** A short description of the module's content. */
                final String desc;

                /**
                 * Constructs a {@code Module} with the given name and description.
                 *
                 * @param name the module display name.
                 * @param desc the short module description.
                 */
                Module(final String name, final String desc) {
                        this.name = name;
                        this.desc = desc;
                }
        }

        /**
         * A named group of {@link Module} objects within a year (e.g. "Core Modules").
         */
        private static class Section {
                final String title;
                final Module[] modules;

                /**
                 * Constructs a {@code Section} with the given title and modules.
                 *
                 * @param title   the section heading, or {@code null} for untitled sections.
                 * @param modules the modules in this section.
                 */
                Section(final String title, final Module... modules) {
                        this.title = title;
                        this.modules = modules;
                }
        }

        /**
         * Academic year group containing one or more {@link Section}s.
         */
        private static class YearGroup {
                // ** The year label (e.g. "Year 1", "Foundation Year"). */
                final String label;

                /** The sections belonging to this year group. */
                final Section[] sections;

                /**
                 * Constructs a {@code YearGroup} with the given label and sections.
                 *
                 * @param label    the year group label.
                 * @param sections the sections within this year group.
                 */
                YearGroup(final String label, final Section... sections) {
                        this.label = label;
                        this.sections = sections;
                }
        }

        /**
         * The full curriculum data structure used to render the module selection UI.
         */
        private static final YearGroup[] CURRICULUM = buildCurriculum();

        /**
         * Builds and returns the complete static curriculum data structure
         * covering Foundation Year through Year 4.
         *
         * @return an array of {@link YearGroup} objects representing the curriculum.
         */
        private static YearGroup[] buildCurriculum() {
                return new YearGroup[] {

                                new YearGroup("Foundation Year",
                                                new Section(CORE_MODULES,
                                                                new Module("Mathematics 1",
                                                                                "Algebra, calculus, and mathematical reasoning"),
                                                                new Module("Physics 1",
                                                                                "Mechanics, waves, and thermodynamics basics"),
                                                                new Module("Skills for Engineering and the Physical Sciences 1",
                                                                                "Lab skills, academic writing, and scientific methods"),
                                                                new Module("Mathematics 2",
                                                                                "Trigonometry, vectors, and differential equations"),
                                                                new Module("Physics 2",
                                                                                "Electromagnetism, optics, and quantum fundamentals"),
                                                                new Module("Skills for Engineering and the Physical Sciences 2",
                                                                                "Advanced lab techniques and data analysis"))),

                                new YearGroup("Year 1",
                                                new Section(CORE_MODULES,
                                                                new Module("Introduction to Engineering",
                                                                                "Engineering principles, design process, and professional practice"),
                                                                new Module("Mathematics",
                                                                                "Calculus, linear algebra, and complex numbers"),
                                                                new Module("Analogue Electronics",
                                                                                "Op-amps, filters, and circuit analysis")),
                                                new Section("Option Modules",
                                                                new Module("Digital Electronics",
                                                                                "Logic gates, flip-flops, and digital circuit design"),
                                                                new Module("Materials and Mechanics",
                                                                                "Stress, strain, and material properties"))),

                                new YearGroup("Year 2",
                                                new Section(CORE_MODULES,
                                                                new Module("Mathematics, Signals and Systems",
                                                                                "Fourier transforms, Laplace, and LTI systems"),
                                                                new Module("Control, Sensors and Instrumentation",
                                                                                "PID control, feedback loops, and sensor interfacing")),
                                                new Section("Option Modules",
                                                                new Module("Acoustics and Studio Recording",
                                                                                "Room acoustics, microphones, and recording techniques"),
                                                                new Module("Audio Technology & Psychoacoustics",
                                                                                "Hearing perception, audio codecs, and masking"),
                                                                new Module("Manufacturing and Fabrication",
                                                                                "CNC, 3D printing, and production processes"),
                                                                new Module("Circuit Design",
                                                                                "PCB layout, simulation, and mixed-signal design"),
                                                                new Module("Electromagnetism",
                                                                                "Maxwell's equations, wave propagation, and antennas"),
                                                                new Module("Software Design",
                                                                                "OOP, design patterns, and software architecture"),
                                                                new Module("Semiconductor Physics and Devices",
                                                                                "BJTs, MOSFETs, and semiconductor band theory"),
                                                                new Module("Electrical Circuits and Systems",
                                                                                "AC/DC analysis, network theorems, and power"),
                                                                new Module("Data Analysis and Numerical Methods",
                                                                                "Regression, interpolation, and numerical integration"),
                                                                new Module("Thermodynamics and Fluid Dynamics",
                                                                                "Heat transfer, entropy, and fluid mechanics"))),

                                new YearGroup("Year 3",
                                                new Section(null,
                                                                new Module("Audio App Development and Marketing",
                                                                                "Building and launching audio software products"),
                                                                new Module("Multimedia Sound Design",
                                                                                "Synthesis, sound design, and spatial audio"),
                                                                new Module("Principles of Microengineering",
                                                                                "MEMS, microfabrication, and miniaturised systems"),
                                                                new Module("Applications of Electromagnetics",
                                                                                "Antenna design, RF systems, and EMC"),
                                                                new Module("Communications Systems and Digital Communications",
                                                                                "Modulation, channel coding, and link budgets"),
                                                                new Module("Innovation Management",
                                                                                "R&D strategy, IP, and technology commercialisation"),
                                                                new Module("Robotics Design and Construction",
                                                                                "Actuators, sensors, and robot system integration"),
                                                                new Module("Biomedical Engineering",
                                                                                "Biosignals, medical devices, and clinical instrumentation"),
                                                                new Module("Renewable Power Generation",
                                                                                "Wind, hydro, and grid integration"),
                                                                new Module("Digital Signal Processing",
                                                                                "FIR/IIR filters, FFT, and real-time DSP algorithms"),
                                                                new Module("Mobile Communications and Internet Protocols",
                                                                                "4G/5G, TCP/IP stack, and network protocols"),
                                                                new Module("Photonics and Nanoelectronics",
                                                                                "Lasers, optical fibres, and nanoscale devices"),
                                                                new Module("Advanced Sensors and Instrumentation",
                                                                                "Smart sensors, calibration, and data acquisition"),
                                                                new Module("Digital Engineering",
                                                                                "HDL, FPGAs, and digital system design"),
                                                                new Module("Cloud and Distributed Computing",
                                                                                "Microservices, containers, and distributed architectures"),
                                                                new Module("Photovoltaics and Solar Thermal Technology",
                                                                                "Solar cells, PV systems, and thermal collectors"),
                                                                new Module("Electric Powertrain Design",
                                                                                "EV motors, inverters, and drivetrain systems"),
                                                                new Module("Medical Physics",
                                                                                "Radiation, imaging physics, and therapeutic applications"),
                                                                new Module("Product Design, Development and Commercialization",
                                                                                "User-centred design, prototyping, and go-to-market"),
                                                                new Module("Introduction to Clinical Engineering and Physiological Systems",
                                                                                "Clinical devices, body systems, and healthcare technology"),
                                                                new Module("Micro-mechanical and Microfluidic Devices and Systems",
                                                                                "Lab-on-chip, microfluidics, and MEMS fabrication"),
                                                                new Module("Robot Kinematics and Dynamics",
                                                                                "Forward/inverse kinematics, Jacobians, and motion planning"),
                                                                new Module("Mechanical Design and Kinematics",
                                                                                "Mechanism design, gears, and dynamic analysis"),
                                                                new Module("Astrobiology",
                                                                                "Origin of life, extremophiles, and planetary habitability"),
                                                                new Module("Fuel Cell and Battery Technologies",
                                                                                "Electrochemistry, cell types, and energy storage"),
                                                                new Module("Critical Evaluation of Renewable Energy Systems",
                                                                                "Lifecycle analysis, policy, and system comparison"))),

                                new YearGroup("Year 4",
                                                new Section(null,
                                                                new Module("Modelling and Analysing Sound and Music Signals",
                                                                                "Spectral analysis, MIDI, and audio feature extraction"),
                                                                new Module("Systems Programming for ARM",
                                                                                "Embedded C, memory-mapped I/O, and RTOS"),
                                                                new Module("Machine Learning and Computational Intelligence",
                                                                                "Neural networks, SVMs, and deep learning"),
                                                                new Module("Power Electronics",
                                                                                "Converters, inverters, and switching circuits"),
                                                                new Module("Electric Vehicle Technologies",
                                                                                "Battery management, charging infrastructure, and EV design"),
                                                                new Module("Medical Imaging and Physics",
                                                                                "MRI, CT, ultrasound, and image reconstruction"),
                                                                new Module("Future Healthcare and Computer Aided Diagnosis",
                                                                                "AI diagnostics, telemedicine, and health informatics"),
                                                                new Module("Information Theory, Wireless and Optical Transmission",
                                                                                "Shannon entropy, channel capacity, and fibre optics"),
                                                                new Module("Sustainability in Engineering Management",
                                                                                "ESG, circular economy, and sustainable design"),
                                                                new Module("Machine Vision and Human Machine Interaction",
                                                                                "Computer vision, gesture recognition, and HCI"),
                                                                new Module("Research Topics in Nanotechnology: Advanced Data Storage and Spintronics",
                                                                                "Spintronic devices, magnetic storage, and nanotechnology"),
                                                                new Module("Advanced Control",
                                                                                "State-space, robust, and optimal control systems"),
                                                                new Module("Smart Grid",
                                                                                "Grid topology, demand response, and smart metering"),
                                                                new Module("Scientific Supercomputing",
                                                                                "HPC clusters, parallel algorithms, and GPU computing"),
                                                                new Module("Emerging Trends in Microengineering",
                                                                                "Next-gen MEMS, bioMEMS, and emerging fabrication"),
                                                                new Module("Practical Skills in Virtual Anatomy and Morphology",
                                                                                "3D anatomy, virtual dissection, and morphological analysis"),
                                                                new Module("Advanced Project Management- Agile, Scrum and Six Sigma",
                                                                                "Agile methodology, sprints, and quality management"),
                                                                new Module("Sound Interactions in the Metaverse",
                                                                                "Spatial audio, VR acoustics, and immersive sound"),
                                                                new Module("Leadership in Engineering Businesses",
                                                                                "Team leadership, strategy, and engineering management"),
                                                                new Module("Circular Economy and Sustainability in Engineering Management",
                                                                                "Waste reduction, resource loops, and sustainable operations"),
                                                                new Module("Immersive and Interactive Audio",
                                                                                "Binaural audio, ambisonics, and interactive sound systems"),
                                                                new Module("Statistical Techniques for Data Analysis and Machine Learning",
                                                                                "Bayesian inference, hypothesis testing, and ML statistics"),
                                                                new Module("Critical Evaluation of Intelligent Robots",
                                                                                "Robot ethics, performance benchmarking, and autonomy"),
                                                                new Module("Fuel Cell and Battery Technologies",
                                                                                "Electrochemistry, cell types, and energy storage")))
                };
        }

        /**
         * Constructs the {@code SubjectView} for the given user profile and
         * renders the module selection UI.
         *
         * @param profile the {@link UserProfile} to populate on save.
         */
        public SubjectView(UserProfile profile) {
                this.profile = profile;
                buildUI();
        }

        /**
         * Builds the full module selection layout including the heading,
         * Select All button, and scrollable curriculum list.
         */
        private void buildUI() {
                setStyle("-fx-background-color: " + UITheme.BG + ";");
                setPadding(new Insets(20));

                Label heading = new Label("Select Your Modules");
                heading.setStyle(FX_TEXT_FILL + UITheme.PRIMARY + ";" +
                                "-fx-font-size: 20px; -fx-font-weight: bold;");
                HBox.setHgrow(heading, Priority.ALWAYS);

                Button selectAll = new Button("Select All");
                selectAll.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
                selectAll.setOnAction(e -> {
                        boolean anyUnchecked = checkboxes.stream().anyMatch(c -> !c.isSelected());
                        checkboxes.forEach(c -> c.setSelected(anyUnchecked));
                });

                HBox topRow = new HBox(10, heading, selectAll);
                topRow.setAlignment(Pos.CENTER_LEFT);

                Label sub = new Label("Choose the modules you're studying:");
                sub.setStyle(FX_TEXT_FILL + UITheme.TEXT_MUTED + "; -fx-font-size: 14px;");

                VBox content = new VBox(20);
                for (YearGroup year : CURRICULUM) {
                        content.getChildren().add(buildYearBlock(year));
                }

                ScrollPane scroll = new ScrollPane(content);
                scroll.setFitToWidth(true);
                scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
                scroll.setBorder(Border.EMPTY);
                VBox.setVgrow(scroll, Priority.ALWAYS);

                VBox card = new VBox(10, topRow, sub, scroll);
                card.setMaxWidth(620);
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
                Label yearLabel = new Label(year.label);
                yearLabel.setStyle(FX_TEXT_FILL + UITheme.PRIMARY +
                                "; -fx-font-size: 15px; -fx-font-weight: bold;");

                Separator sep = new Separator();
                VBox.setMargin(sep, new Insets(2, 0, 6, 0));

                VBox block = new VBox(8, yearLabel, sep);

                for (Section section : year.sections) {
                        if (section.title != null) {
                                Label sectionLabel = new Label(section.title.toUpperCase());
                                sectionLabel.setStyle(FX_TEXT_FILL + UITheme.TEXT_MUTED +
                                                "; -fx-font-size: 10px; -fx-font-weight: bold;");
                                VBox.setMargin(sectionLabel, new Insets(6, 0, 2, 0));
                                block.getChildren().add(sectionLabel);
                        }
                        block.getChildren().add(buildGrid(section.modules));
                }
                return block;
        }

        /**
         * Builds a two-column {@link GridPane} of checkbox cells for the given modules.
         * Each cell contains a {@link CheckBox} and a description label, and highlights
         * when selected.
         *
         * @param modules the array of {@link Module} objects to render.
         * @return a {@link GridPane} containing one cell per module.
         */
        private GridPane buildGrid(final Module[] modules) {
                String normalStyle = "-fx-background-color: " + UITheme.BG + ";" +
                                "-fx-border-color: " + UITheme.BORDER + ";" +
                                "-fx-border-radius: 4px; -fx-background-radius: 4px;" +
                                "-fx-padding: 8px 10px;";
                String selectedStyle = "-fx-background-color: #E8F7FB;" +
                                "-fx-border-color: " + UITheme.SECONDARY + "; -fx-border-width: 2px;" +
                                "-fx-border-radius: 4px; -fx-background-radius: 4px;" +
                                "-fx-padding: 8px 10px;";

                GridPane grid = new GridPane();
                grid.setHgap(8);
                grid.setVgap(6);

                ColumnConstraints c1 = new ColumnConstraints();
                c1.setPercentWidth(50);
                ColumnConstraints c2 = new ColumnConstraints();
                c2.setPercentWidth(50);
                grid.getColumnConstraints().addAll(c1, c2);

                for (int i = 0; i < modules.length; i++) {
                        Module m = modules[i];

                        CheckBox cb = new CheckBox(m.name);
                        cb.setStyle("-fx-font-size: 13px; " + FX_TEXT_FILL + UITheme.TEXT_DARK + ";");
                        cb.setWrapText(true);

                        Label desc = new Label(m.desc);
                        desc.setStyle("-fx-font-size: 11px; -fx-text-fill: " + UITheme.TEXT_MUTED + ";");
                        desc.setWrapText(true);

                        VBox cell = new VBox(2, cb, desc);
                        cell.setStyle(normalStyle);
                        cb.selectedProperty().addListener(
                                        (obs, old, sel) -> cell.setStyle(sel ? selectedStyle : normalStyle));

                        checkboxes.add(cb);
                        grid.add(cell, i % 2, i / 2);
                }
                return grid;
        }

        /**
         * Validates that the user has selected at least one module.
         * Displays a warning alert if no modules are selected.
         *
         * @return {@code true} if at least one checkbox is selected; {@code false}
         *         otherwise.
         */
        public boolean validateInput() {
                boolean any = checkboxes.stream().anyMatch(CheckBox::isSelected);
                if (!any) {
                        Alert a = new Alert(Alert.AlertType.WARNING,
                                        "Please select at least one module.", ButtonType.OK);
                        a.setHeaderText("Required");
                        a.showAndWait();
                }
                return any;
        }

        /**
         * Saves the names of all selected modules into the associated
         * {@link UserProfile}.
         * Must only be called after {@link #validateInput()} returns {@code true}.
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
