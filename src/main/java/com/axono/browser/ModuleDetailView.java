package com.axono.browser;

import com.axono.content.ContentType;
import com.axono.content.LearningContent;
import com.axono.ui.UIConstants;
import com.axono.content.LearningContentLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
 * Detailed view for a single module, showing all its resources with
 * filter buttons (All, Articles, Videos, Quizzes). Accessed by clicking
 * a module heading in {@link BrowserView}.
 */
public final class ModuleDetailView extends VBox {

    /** The module being displayed. */
    private final String moduleName;

    /** Callback invoked when the user clicks Open on a content row. */
    private final Consumer<LearningContent> openHandler;

    /** Callback invoked when the user clicks the Back button. */
    private final Runnable backHandler;

    /** All content in this module. */
    private final List<LearningContent> moduleContent;

    /** Container for the filtered content list. */
    private VBox listContainer;

    /** Currently active filter. */
    private ContentType activeFilter;

    /**
     * All filter buttons, so we can update their active state on
     * click.
     */
    private final java.util.List<Button> filterButtons =
            new java.util.ArrayList<>();

    /**
     * Constructs a {@code ModuleDetailView}.
     *
     * @param module      the module name to display.
     * @param onOpen      called with the selected content when Open is clicked.
     * @param onBack      called when the Back button is clicked.
     */
    public ModuleDetailView(final String module,
            final Consumer<LearningContent> onOpen,
            final Runnable onBack) {
        this.moduleName = module;
        this.openHandler = onOpen == null ? lc -> { } : onOpen;
        this.backHandler = onBack == null ? () -> { } : onBack;
        this.activeFilter = null;

        List<LearningContent> all = LearningContentLoader.loadAll();
        moduleContent = new ArrayList<>();
        for (LearningContent lc : all) {
            if (moduleName.equals(lc.getModule())) {
                moduleContent.add(lc);
            }
        }

        buildUI();
    }

    /** Builds the full detail view layout. */
    private void buildUI() {
        getStyleClass().add("bg-app");
        setAlignment(Pos.TOP_CENTER);

        VBox inner = new VBox(UIConstants.SPACING_3XL);
        inner.setMaxWidth(UIConstants.CONTENT_MAX_WIDTH);
        inner.setAlignment(Pos.TOP_LEFT);
        inner.setPadding(new Insets(
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H,
                UIConstants.PADDING_CONTENT_V,
                UIConstants.PADDING_CONTENT_H));

        inner.getChildren().addAll(
                buildHeader(),
                buildFilterBar(),
                buildListContainer()
        );

        HBox wrapper = new HBox(inner);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getStyleClass().add("bg-app");
        HBox.setHgrow(inner, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        scroll.setBorder(Border.EMPTY);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().add(scroll);
        refreshList();
    }

    /**
     * Builds the header with Back button and module title.
     *
     * @return the header {@link HBox}.
     */
    private HBox buildHeader() {
        Button back = new Button("← Back to Browser");
        back.getStyleClass().add("btn-back");
        back.setStyle("-fx-font-size: " + UIConstants.FONT_CAPTION + "px;");
        back.setOnAction(e -> backHandler.run());

        Label title = new Label(moduleName);
        title.getStyleClass().add("text-dark");
        title.setStyle("-fx-font-size: " + UIConstants.FONT_BANNER + "px;"
                + " -fx-font-weight: bold;");

        Label count = new Label(moduleContent.size() + " items");
        count.getStyleClass().add("text-muted");
        count.setStyle("-fx-font-size: " + UIConstants.FONT_BODY + "px;");

        VBox titleBox = new VBox(UIConstants.SPACING_MD, back, title, count);
        return new HBox(titleBox);
    }

    /**
     * Builds the filter button bar (All | Articles | Videos | Quizzes).
     *
     * @return the filter bar {@link HBox}.
     */
    private HBox buildFilterBar() {
        Button allBtn = filterButton("All", null);
        Button articleBtn = filterButton("Articles", ContentType.ARTICLE);
        Button videoBtn = filterButton("Videos", ContentType.VIDEO);
        Button quizBtn = filterButton("Quizzes", ContentType.QUIZ);
        filterButtons.addAll(java.util.Arrays.asList(
                allBtn, articleBtn, videoBtn, quizBtn));

        HBox bar = new HBox(UIConstants.SPACING_MD,
                allBtn, articleBtn, videoBtn, quizBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    /**
     * Creates a styled filter toggle button.
     *
     * @param label  the button text.
     * @param filter the content type to filter by, or null for "All".
     * @return the configured button.
     */
    private Button filterButton(final String label,
            final ContentType filter) {
        Button btn = new Button(label);
        btn.getStyleClass().add("filter-btn");
        if (activeFilter == filter) {
            btn.getStyleClass().add("filter-btn-active");
        }

        btn.setOnAction(e -> {
            activeFilter = filter;
            for (Button fb : filterButtons) {
                fb.getStyleClass().remove("filter-btn-active");
            }
            btn.getStyleClass().add("filter-btn-active");
            refreshList();
        });
        return btn;
    }

    /**
     * Builds the container that holds the filtered content list.
     *
     * @return the list container {@link VBox}.
     */
    private VBox buildListContainer() {
        listContainer = new VBox(UIConstants.SPACING_MD);
        listContainer.getStyleClass().add("bg-app");
        return listContainer;
    }

    /** Clears and repopulates the list based on the active filter. */
    private void refreshList() {
        listContainer.getChildren().clear();

        List<LearningContent> visible = new ArrayList<>();
        for (LearningContent lc : moduleContent) {
            if (activeFilter == null || ContentType.of(lc) == activeFilter) {
                visible.add(lc);
            }
        }

        if (visible.isEmpty()) {
            Label none = new Label("No items match this filter.");
            none.getStyleClass().add("text-muted");
            none.setStyle("-fx-font-size: " + UIConstants.FONT_BODY + "px;");
            none.setPadding(new Insets(UIConstants.PADDING_CONTENT_H));
            listContainer.getChildren().add(none);
        } else {
            VBox card = new VBox(UIConstants.SPACING_MD);
            card.setMaxWidth(UIConstants.SECTION_MAX_WIDTH);
            card.getStyleClass().add("card");
            card.setStyle("-fx-padding: 28px 40px;");
            for (LearningContent lc : visible) {
                card.getChildren().add(buildContentRow(lc));
            }
            listContainer.getChildren().add(card);
        }
    }

    /**
     * Builds a single content row with icon, title, topic, badge, Open button.
     *
     * @param lc the content item.
     * @return the row HBox.
     */
    private HBox buildContentRow(final LearningContent lc) {
        return ContentRowHelper.buildContentRow(lc, openHandler, null);
    }
}
