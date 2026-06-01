package com.axono.player;

import com.axono.auth.Session;
import com.axono.auth.User;
import com.axono.ui.UIConstants;
import com.axono.content.MediaAsset;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Full-screen view for creating a new learning resource. Presents a simple
 * form-based editor divided into two sections:
 * <ol>
 *   <li><b>Metadata</b> — title, module, topic, author, description.</li>
 *   <li><b>Slides</b> — an expandable list of slides, each of which can hold
 *       any number of text, image, audio, or video items.</li>
 * </ol>
 *
 * <p>When the user clicks <i>Save</i>, the content specification is serialised
 * to XML, copied media files are placed in the user-data directory, and the
 * record is persisted to the {@code user_content} database table via
 * {@link ContentCreationService} and {@link UserContentRepository}.</p>
 *
 * <p>The {@code onSaved} callback is fired after a successful save so the
 * caller (e.g. {@link com.axono.AppStage}) can navigate back to the browser
 * and reload its content list.</p>
 */
public final class ContentCreationView extends VBox {

    // ── Constants ────────────────────────────────────────────────────────────

    /** Minimum acceptable title length. */
    private static final int MIN_TITLE_LEN = 2;

    /** Minimum width for type label in item rows. */
    private static final int TYPE_LABEL_MIN_WIDTH = 60;

    /** Preferred row count for description text area. */
    private static final int DESC_AREA_ROW_COUNT = 3;

    /** Preferred row count for text/math input areas. */
    private static final int TEXT_AREA_ROW_COUNT = 2;

    /** Number of answer options required per quiz question. */
    private static final int OPTION_COUNT = 4;


    // ── State ────────────────────────────────────────────────────────────────

    /** Invoked when the user wishes to cancel and return without saving. */
    private final Runnable onCancel;

    /** Invoked after a successful save. */
    private final Runnable onSaved;

    /** Reference to the owning JavaFX window (needed for FileChooser). */
    private final Stage ownerStage;

    // ── Metadata fields ──────────────────────────────────────────────────────

    /** Lesson title input field. */
    private final TextField titleField = new TextField();

    /** Module name input field. */
    private final TextField moduleField = new TextField();

    /** Topic name input field. */
    private final TextField topicField = new TextField();

    /** Author name input field. */
    private final TextField authorField = new TextField();

    /** Lesson description text area. */
    private final TextArea descArea = new TextArea();

    // ── Slides ───────────────────────────────────────────────────────────────

    /** Live mutable list of slide editors, one per slide. */
    private final List<SlideEditor> slideEditors = new ArrayList<>();

    /** Container that holds slide editor boxes. */
    private final VBox slidesBox = new VBox(10);

    // ── Quiz toggle ──────────────────────────────────────────────────────────

    /** Toggles quiz creation mode; each slide must then include a question. */
    private final CheckBox isQuizToggle = new CheckBox("Mark as Quiz");

    /** Save button kept as a field so its label updates with quiz mode. */
    private Button saveBtn;

    // ── Media tracking ───────────────────────────────────────────────────────

    /** Assets accumulated across all slide editors. */
    private final List<MediaAsset> pendingAssets = new ArrayList<>();

    /** Base directory for this content's media files; assigned on first use. */
    private Path baseDir;

    /** Unique id for this content (assigned on construction). */
    private final String contentId = ContentCreationService.newId();

    // ── Construction ─────────────────────────────────────────────────────────

    /**
     * Constructs a {@code ContentCreationView}.
     *
     * @param owner the stage used as the owner for file dialogs.
     * @param cancelCallback callback to return to the previous view without
     *                       saving.
     * @param savedCallback callback to invoke after successful save.
     */
    public ContentCreationView(final Stage owner,
            final Runnable cancelCallback,
            final Runnable savedCallback) {
        this.ownerStage = owner;
        this.onCancel = cancelCallback == null ? () -> { } : cancelCallback;
        this.onSaved = savedCallback == null ? () -> { } : savedCallback;
        buildUI();
    }

    /** Assembles the complete editor layout. */
    private void buildUI() {
        getStyleClass().add("bg-app");

        VBox inner = new VBox(UIConstants.SPACING_3XL);
        inner.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        inner.setPadding(new Insets(
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H,
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H));

        inner.getChildren().addAll(
                buildHeader(),
                buildMetadataSection(),
                new Separator(),
                buildSlidesSection(),
                buildActionRow()
        );

        HBox wrapper = new HBox(inner);
        wrapper.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(inner, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        scroll.setBorder(Border.EMPTY);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        isQuizToggle.selectedProperty().addListener((obs, oldVal, on) -> {
            slideEditors.forEach(se -> se.setQuizMode(on));
            saveBtn.setText(on ? "Save Quiz" : "Save Lesson");
        });

        getChildren().add(scroll);
    }

    // ── Section builders ─────────────────────────────────────────────────────

    /**
     * Builds the page header with title and cancel button.
     *
     * @return the header HBox.
     */
    private HBox buildHeader() {
        Label heading = new Label("Create New Lesson");
        heading.getStyleClass().add("text-dark");
        heading.setStyle("-fx-font-size: " + UIConstants.FONT_BANNER + "px;"
                + " -fx-font-weight: bold;");

        Button cancelBtn = new Button("← Back");
        cancelBtn.getStyleClass().add("btn-back");
        cancelBtn.setOnAction(e -> onCancel.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(UIConstants.SPACING_LG, heading, spacer,
                cancelBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    /**
     * Builds the metadata form section.
     *
     * @return the metadata form VBox.
     */
    private VBox buildMetadataSection() {
        Label sectionLabel = sectionHeading("Lesson Details");

        styleField(titleField, "Title");
        styleField(moduleField, "Module (e.g. 2.Mathematics)");
        styleField(topicField, "Topic (e.g. Integration)");
        styleField(authorField, "Author");

        descArea.setPromptText("Description (optional)");
        descArea.setPrefRowCount(DESC_AREA_ROW_COUNT);
        descArea.setWrapText(true);

        isQuizToggle.getStyleClass().add("text-dark");
        isQuizToggle.setStyle(
                "-fx-font-size: " + UIConstants.FONT_BODY + "px;");

        VBox form = new VBox(UIConstants.SPACING_XL,
                fieldRow("Title *", titleField),
                fieldRow("Module", moduleField),
                fieldRow("Topic", topicField),
                fieldRow("Author", authorField),
                fieldRow("Description", descArea),
                isQuizToggle);
        form.getStyleClass().add("card");
        form.setStyle("-fx-padding: 28px 40px;");

        return new VBox(UIConstants.SPACING_LG, sectionLabel, form);
    }

    /**
     * Builds the slides section with an "Add Slide" button and slide list.
     *
     * @return the slides section VBox.
     */
    private VBox buildSlidesSection() {
        Label sectionLabel = sectionHeading("Slides");

        Button addSlideBtn = new Button("+ Add Slide");
        addSlideBtn.getStyleClass().add("btn-primary");
        addSlideBtn.setOnAction(e -> addSlide());

        slidesBox.getChildren().clear();
        addSlide(); // Start with one empty slide

        HBox addRow = new HBox(addSlideBtn);
        addRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(UIConstants.SPACING_LG, sectionLabel, slidesBox,
                addRow);
    }

    /**
     * Builds the Save/Cancel button row at the bottom.
     *
     * @return the action button row HBox.
     */
    private HBox buildActionRow() {
        saveBtn = new Button("Save Lesson");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setStyle(saveBtn.getStyle()
                + " -fx-font-size: " + UIConstants.FONT_BODY + "px;"
                + " -fx-padding: 10px 24px;");
        saveBtn.setOnAction(e -> handleSave());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-outline");
        cancelBtn.setOnAction(e -> onCancel.run());

        HBox row = new HBox(UIConstants.SPACING_LG, saveBtn, cancelBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ── Slide management ─────────────────────────────────────────────────────

    /** Adds a new slide editor to the list. */
    private void addSlide() {
        int number = slideEditors.size() + 1;
        SlideEditor editor = new SlideEditor(number, this::removeSlide);
        editor.setQuizMode(isQuizToggle.isSelected());
        slideEditors.add(editor);
        slidesBox.getChildren().add(editor.getView());
    }

    /**
     * Removes a slide editor from the list and renumbers remaining slides.
     *
     * @param editor the editor to remove.
     */
    private void removeSlide(final SlideEditor editor) {
        int idx = slideEditors.indexOf(editor);
        if (idx < 0) {
            return;
        }
        slideEditors.remove(idx);
        slidesBox.getChildren().remove(editor.getView());
        // Renumber
        for (int i = 0; i < slideEditors.size(); i++) {
            slideEditors.get(i).setNumber(i + 1);
        }
    }

    // ── Save handler ─────────────────────────────────────────────────────────

    /** Validates, serialises, and persists the content. */
    private void handleSave() {
        String title = titleField.getText().trim();
        if (title.length() < MIN_TITLE_LEN) {
            showError("Please enter a title of at least "
                    + MIN_TITLE_LEN + " characters.");
            return;
        }
        if (slideEditors.isEmpty()) {
            showError("Add at least one slide before saving.");
            return;
        }
        User user = Session.get();
        if (user == null) {
            showError("No user is signed in. Please log in and try again.");
            return;
        }
        boolean isQuiz = isQuizToggle.isSelected();
        if (isQuiz && !validateQuizSlides()) {
            return;
        }
        String moduleName = moduleField.getText().trim();
        String topicName = topicField.getText().trim();
        baseDir = ContentCreationService.getBaseDir(
                moduleName.isEmpty() ? "General" : moduleName,
                topicName, contentId);

        List<MediaAsset> assets = new ArrayList<>();
        List<ContentCreationService.SlideSpec> slides =
                buildSlideSpecs(assets, isQuiz);
        if (slides == null) {
            return; // error already shown inside buildSlideSpecs
        }
        ContentCreationService.ContentSpec spec =
                new ContentCreationService.ContentSpec(
                contentId, title,
                authorField.getText().trim(),
                descArea.getText().trim(),
                moduleName, topicName);
        try {
            ContentCreationService.save(spec, baseDir, slides, assets,
                    user.getId(), isQuiz);
            String kind = isQuiz ? "Quiz" : "Lesson";
            showInfo(kind + " \"" + title + "\" saved successfully!");
            onSaved.run();
        } catch (SQLException ex) {
            showError("Could not save: " + ex.getMessage());
        }
    }

    /**
     * Validates that every slide has a complete question when in quiz mode.
     *
     * @return {@code true} if all slides are valid; shows an error and returns
     *         {@code false} on the first failure.
     */
    private boolean validateQuizSlides() {
        for (int i = 0; i < slideEditors.size(); i++) {
            SlideEditor se = slideEditors.get(i);
            int slideNum = i + 1;
            ContentCreationService.QuestionSpec qs = se.getQuestionSpec();
            if (qs.questionText().isBlank()) {
                showError("Slide " + slideNum + ": question text is required.");
                return false;
            }
            for (String opt : qs.options()) {
                if (opt.isBlank()) {
                    showError("Slide " + slideNum
                            + ": all 4 answer options are required.");
                    return false;
                }
            }
            if (qs.correctAnswerIndex() < 1) {
                showError("Slide " + slideNum + ": select a correct answer.");
                return false;
            }
        }
        return true;
    }

    /**
     * Iterates all slide editors, copies any media files into the content's
     * base directory, and builds the ordered
     * {@link ContentCreationService.SlideSpec} list needed by
     * {@link ContentCreationService#save}.
     *
     * @param outAssets mutable list that receives each copied
     *                  {@link MediaAsset}; populated as a side-effect.
     * @param isQuiz    when {@code true} each slide gets its question spec.
     * @return the ordered slide specs, or {@code null} if a file-copy error
     *         occurred (the error alert has already been shown).
     */
    private List<ContentCreationService.SlideSpec> buildSlideSpecs(
            final List<MediaAsset> outAssets, final boolean isQuiz) {
        List<ContentCreationService.SlideSpec> slides = new ArrayList<>();
        for (SlideEditor se : slideEditors) {
            List<ContentCreationService.ItemSpec> specs = new ArrayList<>();
            for (ItemEditor ie : se.items()) {
                if (ie.isMediaFile()) {
                    try {
                        String assetId = ContentCreationService.newId();
                        MediaAsset asset = ContentCreationService.addMediaFile(
                                ie.getFile(), baseDir, assetId);
                        outAssets.add(asset);
                        String relPath = asset.getRelativePath();
                        String assetType = asset.getMediaType();
                        String altText = "IMAGE".equals(assetType)
                                ? ie.getAlt() : "";
                        specs.add(new ContentCreationService.ItemSpec(
                                assetType, relPath, altText));
                    } catch (IOException ex) {
                        showError("Could not copy media file: "
                                + ie.getFile().getName()
                                + "\n" + ex.getMessage());
                        return null;
                    }
                } else {
                    specs.add(new ContentCreationService.ItemSpec(
                            ie.getType(), ie.getValue(),
                            ie.getAlt()));
                }
            }
            ContentCreationService.QuestionSpec qs =
                    isQuiz ? se.getQuestionSpec() : null;
            slides.add(new ContentCreationService.SlideSpec(specs, qs));
        }
        return slides;
    }

    // ── UI helpers ───────────────────────────────────────────────────────────

    /**
     * Creates a styled section heading label.
     *
     * @param text the heading text.
     * @return the styled label.
     */
    private static Label sectionHeading(final String text) {
        Label l = new Label(text);
        l.getStyleClass().add("text-dark");
        l.setStyle("-fx-font-size: " + UIConstants.SPACING_3XL + "px;"
                + " -fx-font-weight: bold;");
        return l;
    }

    /**
     * Creates a form field row with label and control.
     *
     * @param labelText the label text.
     * @param control the control node (TextField, TextArea, etc.).
     * @return the form field row HBox.
     */
    private static HBox fieldRow(final String labelText,
            final javafx.scene.Node control) {
        Label lbl = new Label(labelText);
        lbl.setMinWidth(UIConstants.NAV_BTN_WIDTH);
        lbl.getStyleClass().add("text-accent");
        lbl.setStyle("-fx-font-size: " + UIConstants.FONT_BODY + "px;");
        HBox row = new HBox(UIConstants.SPACING_LG, lbl, control);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(control, Priority.ALWAYS);
        return row;
    }

    /**
     * Applies standard styling to a text field.
     *
     * @param field the field to style.
     * @param prompt the placeholder text.
     */
    private static void styleField(final TextField field,
            final String prompt) {
        field.setPromptText(prompt);
    }

    /**
     * Shows an error alert to the user.
     *
     * @param message the error message.
     */
    private void showError(final String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message,
                ButtonType.OK);
        a.setHeaderText("Error");
        a.showAndWait();
    }

    /**
     * Shows an information alert to the user.
     *
     * @param message the information message.
     */
    private void showInfo(final String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message,
                ButtonType.OK);
        a.setHeaderText("Saved");
        a.showAndWait();
    }

    // ── Inner: SlideEditor ───────────────────────────────────────────────────

    /**
     * Editor for a single slide. Holds an ordered list of {@link ItemEditor}s
     * and provides buttons to add new items.
     */
    private final class SlideEditor {

        /** Container node returned to the parent view. */
        private final VBox view;

        /** Slide number label, updated when slides are renumbered. */
        private final Label numberLabel;

        /** Ordered list of item editors on this slide. */
        private final List<ItemEditor> items = new ArrayList<>();

        /** Container for item editor rows. */
        private final VBox itemsBox = new VBox(UIConstants.SPACING_MD);

        /** Callback to request removal of this slide. */
        private final java.util.function.Consumer<SlideEditor> onRemove;

        /** Quiz question text field. */
        private TextField questionField;

        /** Answer option text fields (OPTION_COUNT entries). */
        private TextField[] optionFields;

        /** Toggle group for selecting the correct answer radio button. */
        private ToggleGroup correctToggle;

        /** Optional explanation text field. */
        private TextField explanationField;

        /** Quiz question section; shown/hidden by setQuizMode(). */
        private VBox quizSection;

        SlideEditor(final int number,
                final java.util.function.Consumer<SlideEditor>
                onRemoveCallback) {
            this.onRemove = onRemoveCallback;
            this.numberLabel = new Label("Slide " + number);
            numberLabel.getStyleClass().add("text-dark");
            numberLabel.setStyle(
                    "-fx-font-size: " + UIConstants.FONT_LABEL + "px;"
                    + " -fx-font-weight: bold;");

            Button removeBtn = new Button("Remove Slide");
            removeBtn.getStyleClass().add("btn-danger");
            removeBtn.setOnAction(e -> onRemove.accept(this));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox slideHeader = new HBox(UIConstants.SPACING_LG,
                    numberLabel, spacer, removeBtn);
            slideHeader.setAlignment(Pos.CENTER_LEFT);

            // Add-item buttons
            Button addText = addItemBtn("+ Text", "TEXT");
            Button addImage = addItemBtn("+ Image", "IMAGE");
            Button addAudio = addItemBtn("+ Audio", "AUDIO");
            Button addVideo = addItemBtn("+ Video", "VIDEO");
            Button addMath = addItemBtn("+ Math (LaTeX)", "MATH");

            HBox addRow = new HBox(UIConstants.SPACING_MD,
                    addText, addImage, addAudio, addVideo, addMath);
            addRow.setAlignment(Pos.CENTER_LEFT);

            quizSection = buildQuizSection();
            view = new VBox(UIConstants.SPACING_MD,
                    slideHeader, new Separator(),
                    itemsBox, addRow, quizSection);
            view.getStyleClass().add("card");
            view.setStyle("-fx-padding: 28px 40px;");
        }

        /**
         * Creates a styled button for adding an item of a given type.
         *
         * @param label the button label.
         * @param type the item type (TEXT, IMAGE, AUDIO, VIDEO, MATH).
         * @return the styled button.
         */
        private Button addItemBtn(final String label, final String type) {
            Button btn = new Button(label);
            btn.getStyleClass().add("btn-outline-sm");
            btn.setOnAction(e -> addItem(type));
            return btn;
        }

        /**
         * Adds a new item of the given type to this slide.
         *
         * @param type the item type (TEXT, IMAGE, AUDIO, VIDEO, MATH).
         */
        private void addItem(final String type) {
            ItemEditor editor;
            if ("IMAGE".equals(type) || "AUDIO".equals(type)
                    || "VIDEO".equals(type)) {
                editor = pickFile(type);
                if (editor == null) {
                    return; // user cancelled
                }
            } else {
                editor = new ItemEditor(type, "", "");
            }
            items.add(editor);
            itemsBox.getChildren().add(editor.getRow(this));
        }

        /**
         * Opens a file chooser for the user to select a media file.
         *
         * @param type the media type (IMAGE, AUDIO, VIDEO).
         * @return an ItemEditor with the selected file, or {@code null} if
         *         the user cancelled.
         */
        private ItemEditor pickFile(final String type) {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select " + type.charAt(0)
                    + type.substring(1).toLowerCase() + " File");
            if ("IMAGE".equals(type)) {
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                        "Images",
                        "*.png", "*.jpg", "*.jpeg", "*.gif",
                        "*.heic", "*.webp", "*.bmp"));
            } else if ("VIDEO".equals(type)) {
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                        "Videos",
                        "*.mp4", "*.webm", "*.mov", "*.mkv",
                        "*.avi", "*.m4v", "*.mpg", "*.mpeg"));
            } else {
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                        "Audio",
                        "*.mp3", "*.wav", "*.ogg", "*.flac",
                        "*.aac", "*.m4a", "*.opus"));
            }
            File f = fc.showOpenDialog(ownerStage);
            if (f == null) {
                return null;
            }
            return new ItemEditor(type, f.getAbsolutePath(), "");
        }

        /**
         * Builds the quiz question section (initially hidden).
         *
         * @return the VBox containing the question, options, and explanation.
         */
        private VBox buildQuizSection() {
            Label quizLabel = new Label("Quiz Question");
            quizLabel.getStyleClass().add("text-dark");
            quizLabel.setStyle("-fx-font-size: " + UIConstants.FONT_LABEL
                    + "px; -fx-font-weight: bold;");

            questionField = new TextField();
            questionField.setPromptText("Question text…");

            correctToggle = new ToggleGroup();
            String[] optLabels = {"A", "B", "C", "D"};
            optionFields = new TextField[OPTION_COUNT];
            VBox optionsBox = new VBox(UIConstants.SPACING_SM);
            for (int i = 0; i < OPTION_COUNT; i++) {
                optionFields[i] = new TextField();
                optionFields[i].setPromptText("Option " + optLabels[i] + "…");
                RadioButton rb = new RadioButton();
                rb.setToggleGroup(correctToggle);
                rb.setUserData(i + 1);
                Label optLabel = new Label("Option " + optLabels[i]);
                optLabel.setMinWidth(UIConstants.NAV_BTN_WIDTH);
                optLabel.getStyleClass().add("text-accent");
                optLabel.setStyle("-fx-font-size: " + UIConstants.FONT_BODY
                        + "px;");
                HBox optRow = new HBox(UIConstants.SPACING_MD,
                        rb, optionFields[i]);
                optRow.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(optionFields[i], Priority.ALWAYS);
                HBox fullRow = new HBox(UIConstants.SPACING_LG,
                        optLabel, optRow);
                fullRow.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(optRow, Priority.ALWAYS);
                optionsBox.getChildren().add(fullRow);
            }

            explanationField = new TextField();
            explanationField.setPromptText("Explanation (optional)…");

            VBox section = new VBox(UIConstants.SPACING_MD, new Separator(),
                    quizLabel, fieldRow("Question", questionField),
                    optionsBox, fieldRow("Explanation", explanationField));
            section.setVisible(false);
            section.setManaged(false);
            return section;
        }

        /**
         * Shows or hides the quiz question section on this slide.
         *
         * @param on {@code true} to show; {@code false} to hide.
         */
        void setQuizMode(final boolean on) {
            quizSection.setVisible(on);
            quizSection.setManaged(on);
        }

        /**
         * Reads the current quiz question fields and returns a
         * {@link ContentCreationService.QuestionSpec}. The caller is
         * responsible for validation before using this result.
         *
         * @return the question spec built from current field values.
         */
        ContentCreationService.QuestionSpec getQuestionSpec() {
            List<String> opts = new ArrayList<>();
            for (TextField tf : optionFields) {
                opts.add(tf.getText().trim());
            }
            int correctIdx = 0;
            if (correctToggle.getSelectedToggle() != null) {
                correctIdx = (int) correctToggle.getSelectedToggle()
                        .getUserData();
            }
            return new ContentCreationService.QuestionSpec(
                    questionField.getText().trim(), opts,
                    correctIdx, explanationField.getText().trim());
        }

        /**
         * Removes an item from this slide.
         *
         * @param editor the item editor to remove.
         */
        void removeItem(final ItemEditor editor) {
            items.remove(editor);
            itemsBox.getChildren().remove(editor.getRow(null));
        }

        /**
         * Updates the slide number display label.
         *
         * @param n the new slide number.
         */
        void setNumber(final int n) {
            numberLabel.setText("Slide " + n);
        }

        /**
         * Returns the slide editor view node.
         *
         * @return the slide VBox.
         */
        VBox getView() {
            return view;
        }

        /**
         * Returns the list of item editors on this slide.
         *
         * @return the item editor list.
         */
        List<ItemEditor> items() {
            return items;
        }
    }

    // ── Inner: ItemEditor ────────────────────────────────────────────────────

    /**
     * Represents a single content item on a slide. Holds type, value (text
     * or file path), and optional alt text.
     */
    private static final class ItemEditor {

        /** The item type: TEXT, IMAGE, AUDIO, VIDEO, or MATH. */
        private final String type;

        /**
         * For text/math: the content. For media: the absolute file path of
         * the selected file.
         */
        private String value;

        /** Alt text for images. */
        private String alt;

        /** The cached row node (lazy, built on first getRow() call). */
        private HBox rowNode;

        /** Optional text/math input area, present for TEXT and MATH. */
        private TextArea textArea;

        /** Optional alt-text input, present for IMAGE. */
        private TextField altField;

        ItemEditor(final String itemType, final String itemValue,
                final String altText) {
            this.type = itemType;
            this.value = itemValue;
            this.alt = altText == null ? "" : altText;
        }

        /**
         * Returns the row HBox for this item.
         *
         * @param slide the parent slide (used for removal; may be {@code null}
         *              to just return the cached row).
         * @return the constructed row node.
         */
        HBox getRow(final SlideEditor slide) {
            if (rowNode != null) {
                return rowNode;
            }
            rowNode = buildRow(slide);
            return rowNode;
        }

        /**
         * Builds the row node for this item editor.
         *
         * @param slide the parent slide editor (may be {@code null} to just
         *              return the cached row).
         * @return the item row HBox.
         */
        private HBox buildRow(final SlideEditor slide) {
            Label typeLabel = new Label(type);
            typeLabel.setMinWidth(TYPE_LABEL_MIN_WIDTH);
            typeLabel.getStyleClass().add("text-primary");
            typeLabel.setStyle(
                    "-fx-font-size: " + UIConstants.FONT_CAPTION + "px;"
                    + " -fx-font-weight: bold;");

            javafx.scene.Node contentNode =
                    ("TEXT".equals(type) || "MATH".equals(type))
                    ? buildTextOrMathNode()
                    : buildMediaFileNode();
            HBox.setHgrow(contentNode, Priority.ALWAYS);

            Button removeBtn = new Button("✕");
            removeBtn.getStyleClass().add("btn-danger-icon");
            if (slide != null) {
                removeBtn.setOnAction(e -> slide.removeItem(this));
            }
            HBox row = new HBox(UIConstants.SPACING_LG,
                    typeLabel, contentNode, removeBtn);
            row.setAlignment(Pos.TOP_LEFT);
            row.setPadding(new Insets(UIConstants.SPACING_MD));
            row.getStyleClass().add("bg-app");
            row.setStyle("-fx-background-radius: 4px;");
            return row;
        }

        /**
         * Builds the editor node for a TEXT or MATH item — a {@link TextArea}
         * whose content is kept in sync with {@link #value}.
         *
         * @return the configured {@link TextArea}.
         */
        private javafx.scene.Node buildTextOrMathNode() {
            textArea = new TextArea(value);
            textArea.setPromptText("TEXT".equals(type) ? "Enter text…"
                    : "Enter LaTeX…");
            textArea.setPrefRowCount(TEXT_AREA_ROW_COUNT);
            textArea.setWrapText(true);
            textArea.textProperty().addListener((obs, o, n) -> value = n);
            return textArea;
        }

        /**
         * Builds the display node for an IMAGE, AUDIO, or VIDEO item — a
         * filename label, plus an alt-text field for images.
         *
         * @return a {@link Label} for non-image media, or a {@link VBox}
         *         containing the label and alt-text field for images.
         */
        private javafx.scene.Node buildMediaFileNode() {
            String displayName = value.isEmpty()
                    ? "(no file)" : new File(value).getName();
            Label fileLabel = new Label(displayName);
            fileLabel.getStyleClass().add("text-muted");
            fileLabel.setStyle("-fx-font-size: "
                    + UIConstants.FONT_CAPTION + "px;");
            fileLabel.setWrapText(true);
            if ("IMAGE".equals(type)) {
                altField = new TextField(alt);
                altField.setPromptText("Alt text (optional)");
                altField.textProperty().addListener((obs, o, n) -> alt = n);
                return new VBox(UIConstants.SPACING_SM, fileLabel, altField);
            }
            return fileLabel;
        }

        /**
         * Returns the item type.
         *
         * @return the item type (TEXT, IMAGE, AUDIO, VIDEO, MATH).
         */
        String getType() {
            return type;
        }

        /**
         * Returns the item value (text content or file path).
         *
         * @return the item value.
         */
        String getValue() {
            if (textArea != null) {
                return textArea.getText().trim();
            }
            return value;
        }

        /**
         * Returns the alt text for this item (images only).
         *
         * @return the alt text; may be empty.
         */
        String getAlt() {
            if (altField != null) {
                return altField.getText().trim();
            }
            return alt;
        }

        /**
         * Checks if this item is a media file (image, audio, or video).
         *
         * @return {@code true} if this is a media file item.
         */
        boolean isMediaFile() {
            return "IMAGE".equals(type) || "AUDIO".equals(type)
                    || "VIDEO".equals(type);
        }

        /**
         * Returns the file object for this media item.
         *
         * @return the File object from the value path.
         */
        File getFile() {
            return new File(value);
        }
    }
}
