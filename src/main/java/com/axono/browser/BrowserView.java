package com.axono.browser;

import com.axono.auth.Session;
import com.axono.content.AudioItem;
import com.axono.ui.UIConstants;
import com.axono.content.ContentType;
import com.axono.content.ImageItem;
import com.axono.content.LearningContent;
import com.axono.content.LearningContentLoader;
import com.axono.content.MediaItem;
import com.axono.content.VideoItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Learning-content browser shown after login. Displays content in three
 * categorised tabs — Videos, Articles, and Quizzes — each grouping items
 * by module. Clicking a module heading opens a detail view for that module.
 */
public final class BrowserView extends VBox {

    /** Floating action button margin in pixels. */
    private static final int FAB_MARGIN = 24;

    /** Media indicator row count in grid. */
    private static final int INDICATOR_ROW_COUNT = 3;

    /** Spacing constant for content rows. */
    private static final int ROW_SPACING = 6;

    /** Spacing constant for chip icon and label. */
    private static final int CHIP_ICON_SPACING = 3;

    /** Chip icon size reduction from ICON_SIZE. */
    private static final int CHIP_ICON_REDUCTION = 6;

    /** Callback invoked when the user clicks Open on a content row. */
    private final Consumer<LearningContent> openHandler;

    /** Callback invoked when the user clicks a module heading. */
    private final Consumer<String> moduleHandler;

    /** Callback invoked when the user clicks the floating Open File button. */
    private final Runnable fileOpenHandler;

    /** All loaded learning content, cached for tab building. */
    private final List<LearningContent> allContent;

    /**
     * Constructs the {@code BrowserView}.
     *
     * @param onOpen    callback invoked with the selected content on Open.
     * @param onModule  callback invoked with the module name on heading click.
     * @param onFileOpen callback invoked when the Open File button is clicked.
     */
    public BrowserView(final Consumer<LearningContent> onOpen,
            final Consumer<String> onModule,
            final Runnable onFileOpen) {
        this.openHandler = onOpen == null ? p -> { } : onOpen;
        this.moduleHandler = onModule == null ? m -> { } : onModule;
        this.fileOpenHandler = onFileOpen == null ? () -> { } : onFileOpen;
        this.allContent = LearningContentLoader.loadAll();
        buildUI();
    }

    /**
     * Backward-compatible constructor with no module-click handler.
     *
     * @param onOpen callback invoked with the selected content on Open.
     * @param onModule callback invoked with the module name on heading click.
     */
    public BrowserView(final Consumer<LearningContent> onOpen,
            final Consumer<String> onModule) {
        this(onOpen, onModule, null);
    }

    /**
     * Backward-compatible constructor with no callbacks.
     *
     * @param onOpen callback invoked with the selected content on Open.
     */
    public BrowserView(final Consumer<LearningContent> onOpen) {
        this(onOpen, null, null);
    }

    /** Builds the banner and tab pane layout. */
    private void buildUI() {
        getStyleClass().add("bg-app");
        setAlignment(Pos.TOP_CENTER);

        VBox inner = new VBox(UIConstants.SPACING_3XL);
        inner.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        inner.setAlignment(Pos.TOP_CENTER);
        inner.setPadding(new Insets(
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H,
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H));

        inner.getChildren().add(buildBanner());

        if (allContent.isEmpty()) {
            inner.getChildren().add(buildEmptyState());
        } else {
            inner.getChildren().add(buildTabPane());
        }

        HBox wrapper = new HBox(inner);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getStyleClass().add("bg-app");
        HBox.setHgrow(inner, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        scroll.setBorder(Border.EMPTY);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        StackPane overlay = new StackPane(scroll, buildOpenFileButton());
        overlay.setAlignment(Pos.BOTTOM_RIGHT);
        VBox.setVgrow(overlay, Priority.ALWAYS);

        getChildren().add(overlay);
    }

    /**
     * Builds a floating action button anchored to the bottom-right corner.
     * Clicking it opens a file chooser for supported media/XML types.
     *
     * @return the configured floating button wrapped in a padded container.
     */
    private StackPane buildOpenFileButton() {
        Button fab = new Button("＋ Open File");
        fab.getStyleClass().add("btn-fab");
        fab.setOnAction(e -> fileOpenHandler.run());

        StackPane container = new StackPane(fab);
        StackPane.setAlignment(fab, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(fab, new Insets(0, FAB_MARGIN, FAB_MARGIN, 0));
        container.setMouseTransparent(false);
        container.setPickOnBounds(false);
        return container;
    }

    /**
     * Builds the welcome banner.
     *
     * @return a welcome banner VBox with name and prompt.
     */
    private VBox buildBanner() {
        String name = Session.isAuthenticated()
                ? Session.get().getFirstName() : "there";
        Label welcome = new Label("Welcome, " + name + "!");
        welcome.getStyleClass().add("header2");

        Label subtitle = new Label("Choose a learning resource to begin.");
        subtitle.getStyleClass().add("text-muted");
        subtitle.setStyle("-fx-font-size: " + UIConstants.FONT_SECTION + "px;");

        VBox banner = new VBox(UIConstants.SPACING_LG, welcome, subtitle);
        banner.setAlignment(Pos.CENTER);
        return banner;
    }

    /**
     * Builds a TabPane with Videos, Articles, and Quizzes tabs.
     *
     * @return the configured TabPane with three tabs.
     */
    private TabPane buildTabPane() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        List<LearningContent> videos = new ArrayList<>();
        List<LearningContent> articles = new ArrayList<>();
        List<LearningContent> quizzes = new ArrayList<>();

        for (LearningContent lc : allContent) {
            ContentType type = ContentType.of(lc);
            if (type == ContentType.VIDEO) {
                videos.add(lc);
            } else if (type == ContentType.QUIZ) {
                quizzes.add(lc);
            } else {
                articles.add(lc);
            }
        }

        tabs.getTabs().addAll(
                buildTab("Articles", articles, FontAwesomeSolid.BOOK_OPEN),
                buildTab("Quizzes", quizzes, FontAwesomeSolid.QUESTION_CIRCLE),
                buildTab("Videos", videos, FontAwesomeSolid.PLAY_CIRCLE)
        );
        return tabs;
    }

    /**
     * Builds one tab containing content grouped by module.
     *
     * @param title   the tab title.
     * @param items   the content items for this tab.
     * @param tabIcon the icon to show next to the tab title.
     * @return the configured {@link Tab}.
     */
    private Tab buildTab(final String title,
            final List<LearningContent> items,
            final FontAwesomeSolid tabIcon) {
        Tab tab = new Tab();
        FontIcon icon = new FontIcon(tabIcon);
        icon.setIconSize(ContentRowHelper.ICON_SIZE - 2);
        icon.getStyleClass().add("icon-text-dark");
        Label tabLabel = new Label(title + " (" + items.size() + ")");
        tabLabel.setStyle("-fx-font-size: 13px;");
        HBox tabHeader = new HBox(ROW_SPACING, icon, tabLabel);
        tabHeader.setAlignment(Pos.CENTER_LEFT);
        tab.setGraphic(tabHeader);

        VBox body = new VBox(UIConstants.SPACING_3XL);
        body.setPadding(new Insets(UIConstants.SPACING_3XL, 0,
                UIConstants.SPACING_3XL, 0));
        body.getStyleClass().add("bg-app");

        if (items.isEmpty()) {
            Label none = new Label("No " + title.toLowerCase()
                    + " found in the library.");
            none.getStyleClass().addAll("body-text", "text-muted");
            none.setPadding(new Insets(UIConstants.SPACING_3XL));
            body.getChildren().add(none);
        } else {
            Map<String, List<LearningContent>> byModule = groupByModule(items);
            for (Map.Entry<String, List<LearningContent>> entry
                    : byModule.entrySet()) {
                body.getChildren().add(
                        buildModuleCard(entry.getKey(), entry.getValue()));
            }
        }

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setBorder(Border.EMPTY);
        tab.setContent(scroll);
        return tab;
    }

    /**
     * Groups content by module in insertion order.
     *
     * @param items the content items to group.
     * @return a map of module names to content lists.
     */
    private Map<String, List<LearningContent>> groupByModule(
            final List<LearningContent> items) {
        Map<String, List<LearningContent>> out = new LinkedHashMap<>();
        for (LearningContent lc : items) {
            String key = lc.getModule().isEmpty() ? "Other" : lc.getModule();
            out.computeIfAbsent(key, k -> new ArrayList<>()).add(lc);
        }
        return out;
    }

    /**
     * Builds a card for a single module with a clickable heading and rows.
     *
     * @param moduleName the module name.
     * @param items      the items in the module.
     * @return the module card.
     */
    private VBox buildModuleCard(final String moduleName,
            final List<LearningContent> items) {
        Button moduleLink = new Button(moduleName);
        moduleLink.getStyleClass().add("module-link");
        moduleLink.setStyle("-fx-font-size: " + UIConstants.SPACING_3XL
                + "px;");
        moduleLink.setOnAction(e -> moduleHandler.accept(moduleName));

        VBox rows = new VBox(UIConstants.SPACING_MD);
        for (LearningContent lc : items) {
            rows.getChildren().add(buildContentRow(lc));
        }

        VBox card = new VBox(UIConstants.SPACING_LG, moduleLink, rows);
        card.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 28px 40px;");
        return card;
    }

    /**
     * Builds one content row with icon, title, topic, media indicators,
     * badge, and Open button.
     *
     * @param lc the content item.
     * @return the row HBox.
     */
    private HBox buildContentRow(final LearningContent lc) {
        return ContentRowHelper.buildContentRow(
                lc, openHandler, buildMediaIndicators(lc));
    }

    /**
     * Builds a compact horizontal row of media-type indicator chips for the
     * given content. Returns an empty {@link HBox} when there are no
     * noteworthy media items.
     *
     * @param lc the content to inspect.
     * @return the indicator row; never {@code null}.
     */
    private HBox buildMediaIndicators(final LearningContent lc) {
        int imageCount = 0;
        int audioCount = 0;
        int videoCount = 0;
        for (com.axono.content.Slide slide : lc.getSlides()) {
            for (MediaItem item : slide.getItems()) {
                if (item instanceof ImageItem) {
                    imageCount++;
                } else if (item instanceof AudioItem) {
                    audioCount++;
                } else if (item instanceof VideoItem) {
                    videoCount++;
                }
            }
        }
        HBox row = new HBox(UIConstants.SPACING_SM);
        row.setAlignment(Pos.CENTER_LEFT);
        if (imageCount > 0) {
            row.getChildren().add(mediaChip(
                    imageCount + " img", FontAwesomeSolid.IMAGE));
        }
        if (audioCount > 0) {
            row.getChildren().add(mediaChip(
                    audioCount + " audio", FontAwesomeSolid.MUSIC));
        }
        if (videoCount > 0) {
            row.getChildren().add(mediaChip(
                    videoCount + " video", FontAwesomeSolid.VIDEO));
        }
        // User-created badge
        if (lc.hasMediaAssets()) {
            row.getChildren().add(mediaChip("user", FontAwesomeSolid.USER));
        }
        return row;
    }

    /**
     * Builds a small chip label with an icon and count text.
     *
     * @param text    the chip text.
     * @param iconCode the icon to display.
     * @return a styled {@link HBox} chip.
     */
    private HBox mediaChip(final String text,
            final FontAwesomeSolid iconCode) {
        FontIcon ic = new FontIcon(iconCode);
        ic.setIconSize(ContentRowHelper.ICON_SIZE - CHIP_ICON_REDUCTION);
        ic.getStyleClass().add("icon-muted");
        Label lbl = new Label(text);
        lbl.getStyleClass().add("text-muted");
        lbl.setStyle("-fx-font-size: " + UIConstants.FONT_SMALL + "px;");
        HBox chip = new HBox(CHIP_ICON_SPACING, ic, lbl);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.getStyleClass().add("media-chip");
        return chip;
    }

    /**
     * Builds the empty-state card when no content is found.
     *
     * @return the empty state card VBox.
     */
    private VBox buildEmptyState() {
        Label heading = new Label("No learning resources found");
        heading.getStyleClass().add("text-dark");
        heading.setStyle("-fx-font-size: " + UIConstants.SPACING_3XL + "px;"
                + " -fx-font-weight: bold;");

        Label note = new Label(
                "No XML learning resources were discovered on the classpath. "
                        + "Check that learning-content/ is present in the "
                        + "application resources.");
        note.getStyleClass().add("text-muted");
        note.setStyle("-fx-font-size: " + UIConstants.FONT_BODY + "px;");
        note.setWrapText(true);

        VBox card = new VBox(UIConstants.SPACING_LG, heading, note);
        card.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 28px 40px;");
        return card;
    }
}
