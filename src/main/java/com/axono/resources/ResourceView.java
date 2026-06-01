package com.axono.resources;

import com.axono.ui.UITheme;
import com.axono.ui.UIConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * ResourceView displays all available learning resource
 * and modules within the application.
 *
 * Users can browse module descriptions and open corresponding notes
 * via the "open notes" button.
 */
public final class ResourceView extends ScrollPane {

    private static final String BG_COLOR_STYLE = "-fx-background-color: ";

    /**
     * Constructor for ResourceView.
     */
    public ResourceView() {
        buildUI();
    }

    /**
     * Builds main layout of the page.
     * Creates the scrollable layout and page structure.
     *
     * Applies theme to match the styling of the application
     * and previous pages.
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
        content.setStyle(BG_COLOR_STYLE + UITheme.BG + ";");

        content.getChildren().addAll(
                buildHeader(),
                buildResourcesSection()
        );

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle(BG_COLOR_STYLE + UITheme.BG + ";");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);
        setBorder(Border.EMPTY);
        setStyle(BG_COLOR_STYLE + UITheme.BG + ";");
    }

    /**
     * Builds page header containing main title and navigation buttons.
     *
     * Main title "Learning Resources" with buttons for back and dashboard.
     *
     * @return VBox containing the header layout.
     */
    private VBox buildHeader() {
        Label title = new Label("Learning Resources");
        title.setStyle("-fx-font-size: 32px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + UITheme.TEXT_DARK + ";");

        HBox buttons = new HBox(UIConstants.SPACING_MD,
                outlineButton("Back"),
                outlineButton("Dashboard"));
        buttons.setAlignment(Pos.CENTER);

        VBox header = new VBox(UIConstants.PADDING_MD, title, buttons);
        header.setAlignment(Pos.CENTER);

        return header;
    }

    /**
     * Builds the main resource content boxes for all available modules.
     *
     * These are structured to display:
     * - Modules title
     * - Generic description
     * - Expandable summary
     * - Button to link to the notes
     *
     * @return VBox containing all resource boxes.
     */
    private VBox buildResourcesSection() {
        Label sectionTitle = sectionLabel("Content",
                UIConstants.FONT_SECTION);

        VBox resources = new VBox(UIConstants.SPACING_LG,
                buildResourceBox("Introduction to Engineering",
                        "Access structured notes and relevant content",
                        "This module introduces students to the physical "
                                + "principles that underpin engineering. Starting "
                                + "from Newtonian mechanics, it includes the basic "
                                + "properties of materials, the fundamental laws of "
                                + "circuit analysis, and an introduction to digital "
                                + "logic.",
                        "Open Notes"),
                buildResourceBox("Mathematics",
                        "Access structured notes and relevant content",
                        "Mathematics is the most important tool an engineer "
                                + "has for articulating engineering problems, and "
                                + "for formulating solutions to those problems. "
                                + "Mathematics lies at the heart of modelling, and "
                                + "offers unparalleled insights into the beauty of "
                                + "the natural world. This module develops fluency "
                                + "and confidence in a range of mathematical methods "
                                + "necessary for the analysis, design, and exploration "
                                + "of engineering systems.",
                        "Open Notes"),
                buildResourceBox("Programming and Digital Interfacing",
                        "Access structured notes and relevant content",
                        "This module will introduce you to the power of "
                                + "programming, and how it can be used as a tool to "
                                + "help create interactive engineered systems. We will "
                                + "start from the very beginning, looking at the basics "
                                + "of programming, before moving on to look at more "
                                + "advanced concepts and techniques, using a restricted "
                                + "subset of the C++11 programming language.",
                        "Open Notes"),
                buildResourceBox("Digital Electronics",
                        "Access structured notes and relevant content",
                        "The module will introduce tools and methodologies for "
                                + "the design and implementation of advanced digital "
                                + "circuits, covering technology and design flows "
                                + "targeting application-specific integrated circuits "
                                + "(ASICs) and field-programmable gate arrays (FPGAs). "
                                + "Techniques to improve performance will be considered "
                                + "at different levels. Technology scaling, performance "
                                + "(timing)/power/area (PPA), standard cell libraries, "
                                + "full-/semi-custom ASIC design. Timing, pipelining, "
                                + "clock domain crossing, place and route on FPGA. "
                                + "Approaches for designing testable circuits will be "
                                + "developed, including verification, fault models, "
                                + "design for testability",
                        "Open Notes"),
                buildResourceBox("Controls, Sensors and Instrumentation",
                        "Access structured notes and relevant content",
                        "This module provides an introduction to feedback "
                                + "control of linear systems, and how it can be used "
                                + "to provide stability or to obtain a particular "
                                + "response characteristic from a system. The techniques "
                                + "covered have a wide range of applications, including "
                                + "to mechanical systems such as robots, and to "
                                + "electronic systems such as audio amplifiers. Feedback "
                                + "control requires that the system under control be "
                                + "instrumented so that the controller knows what it is "
                                + "doing. We will look at a variety of different sensor "
                                + "types in this module, including optical, magnetic and "
                                + "micromechanical, and their relative characteristics "
                                + "and performance.",
                        "Open Notes"),
                buildResourceBox("Mathematics, Signals and Systems",
                        "Access structured notes and relevant content",
                        "This module introduces more advanced mathematical "
                                + "tools that are useful for modelling real-world "
                                + "engineering systems and for the analysis and "
                                + "processing of signals.",
                        "Open Notes"),
                buildResourceBox("Circuit Design",
                        "Access structured notes and relevant content",
                        "This module introduces students to analogue and digital "
                                + "design concepts, along with the appropriate role of "
                                + "Hardware Description Languages (HDLs) and simulation "
                                + "in the modern design flow. Particular emphasis will "
                                + "be placed on using HDLs for the synthesis of digital "
                                + "circuits on one side and on the development of "
                                + "appropriate testing through the use of HDL test "
                                + "benches and simulation. The properties and behaviour "
                                + "of semiconductor devices, small signal models and "
                                + "device design will be covered.",
                        "Open Notes"),
                buildResourceBox("Communication systems and Digital "
                        + "Communications",
                        "Access structured notes and relevant content",
                        "The Communication Systems module provides you with a "
                                + "detailed understanding of how wired and wireless "
                                + "communication systems work, from theoretical concepts "
                                + "through to the design of practical radio systems and "
                                + "networks. Topics include: information theory; wireless "
                                + "link design; signals, baseband and passband radio "
                                + "modulation and demodulation; transmitter and receiver "
                                + "architectures, networks and protocols.",
                        "Open Notes"),
                buildResourceBox("Digital Signal Processing",
                        "Access structured notes and relevant content",
                        "We will introduce discrete time techniques routinely "
                                + "used in Digital Signal Processing (DSP) systems, "
                                + "including the discrete time Fourier transform (DTFT), "
                                + "discrete Fourier transform (DFT) and discrete time "
                                + "convolution and correlation. The importance of data "
                                + "windows in DSP will be highlighted and a range of "
                                + "data windows will be introduced, including the raised "
                                + "cosine family (Hanning, Hamming, Blackmann) and "
                                + "orthogonal multi-taper (DPSS) windows. Frequency "
                                + "analysis of signals will be described including "
                                + "practical aspects of spectral leakage, analysis of "
                                + "stochastic signals and time-frequency analysis using "
                                + "spectrograms. Practical applications of these "
                                + "techniques will be considered using a range of "
                                + "different data modalities including biomedical, "
                                + "environmental and speech data. The difference equation "
                                + "as a key design tool in DSP will be introduced and "
                                + "its use in describing digital filters will be "
                                + "presented. The window method for Finite Impulse "
                                + "Response (FIR) filter design will be described, "
                                + "covering both theoretical and practical aspects. "
                                + "Machine learning in DSP systems will be introduced "
                                + "and the theory and application of deep Convolutional "
                                + "Neural Networks (CNN) presented, with a focus on "
                                + "image recognition including standard benchmark "
                                + "applications (MNIST, ImageNet).",
                        "Open Notes"),
                buildResourceBox("Digital Engineering",
                        "Access structured notes and relevant content",
                        "The module will introduce tools and methodologies for "
                                + "the design and implementation of advanced digital "
                                + "circuits, covering technology and design flows "
                                + "targeting application-specific integrated circuits "
                                + "(ASICs) and field-programmable gate arrays (FPGAs). "
                                + "Techniques to improve performance will be considered "
                                + "at different levels. Technology scaling, performance "
                                + "(timing)/power/area (PPA), standard cell libraries, "
                                + "full-/semi-custom ASIC design. Timing, pipelining, "
                                + "clock domain crossing, place and route on FPGA. "
                                + "Approaches for designing testable circuits will be "
                                + "developed, including verification, fault models, "
                                + "design for testability",
                        "Open Notes")
        );

        VBox section = new VBox(UIConstants.SPACING_XL,
                sectionTitle,
                cardWrap(resources));
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);

        return section;
    }

    /**
     * Builds resource boxes with the specified parameters.
     *
     * Takes the design and formatting to match application style.
     *
     * @param title the title of the resource
     * @param subheading the subheading of the resource
     * @param description the full description of the resource
     * @param buttonText the text displayed on the button
     * @return VBox containing the formatted resource box
     */
    private VBox buildResourceBox(final String title,
                                   final String subheading,
                                   final String description,
                                   final String buttonText) {

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + UITheme.TEXT_DARK + ";");

        Label subLabel = new Label(subheading);
        subLabel.setStyle("-fx-font-size: 14px;"
                + "-fx-text-fill: " + UITheme.SECONDARY + ";");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 11px;"
                + "-fx-text-fill: " + UITheme.SECONDARY + ";");
        descLabel.setWrapText(false);
        final int descLabelWidth = 650;
        descLabel.setMaxWidth(descLabelWidth);
        descLabel.setStyle("-fx-font-size: 12px;"
                + "-fx-text-fill: " + UITheme.SECONDARY + ";"
                + "-fx-cursor: hand;");

        final boolean[] expanded = {false};

        descLabel.setOnMouseClicked(e -> {
            expanded[0] = !expanded[0];

            if (expanded[0]) {
                descLabel.setWrapText(true);
            } else {
                descLabel.setWrapText(false);
            }
        });

        VBox card = new VBox(UIConstants.SPACING_MD,
                titleLabel,
                subLabel,
                descLabel
        );

        card.setFillWidth(true);
        return cardWrap(card);
    }

    /**
     * Wraps content in a styled card container.
     *
     * Takes the design and applies theme to make styling consistent.
     * Adds padding, a border and rounded corners with background colour
     * to match application.
     *
     * @param content the VBox content to wrap
     * @return VBox with card styling applied
     */
    private VBox cardWrap(final VBox content) {
        content.setPadding(new Insets(UIConstants.PADDING_MD));
        content.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        content.setStyle(BG_COLOR_STYLE + UITheme.WHITE + ";"
                + "-fx-border-color: " + UITheme.BORDER + ";"
                + "-fx-border-radius: 6px;"
                + "-fx-background-radius: 6px;");
        return content;
    }

    /**
     * Creates a styled section heading label.
     *
     * @param text the text content for the heading
     * @param size the font size in pixels
     * @return Label with section heading styling applied
     */
    private Label sectionLabel(final String text, final int size) {
        Label l = new Label(text);
        l.setStyle(String.format(
                "-fx-font-size: %dpx;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: %s;",
                size, UITheme.TEXT_DARK));
        return l;
    }

    /**
     * Creates a themed outline button.
     *
     * Uses application primary colour and hover effects to improve user
     * interaction and feedback.
     *
     * @param text the text displayed on the button
     * @return Button with outline styling applied
     */
    private Button outlineButton(final String text) {
        String base = "-fx-background-color: transparent;"
                + "-fx-border-color: " + UITheme.PRIMARY + ";"
                + "-fx-border-width: 2px;"
                + "-fx-border-radius: 4px;"
                + "-fx-background-radius: 4px;"
                + "-fx-text-fill: " + UITheme.PRIMARY + ";"
                + "-fx-font-weight: bold;"
                + "-fx-font-size: 14px;";

        String hover = BG_COLOR_STYLE + UITheme.PRIMARY + ";"
                + "-fx-text-fill: white;";

        Button b = new Button(text);
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }
}
