package com.axono.browser;

import com.axono.content.ContentType;
import com.axono.content.LearningContent;
import com.axono.ui.UIConstants;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Shared helpers for building content rows used by {@link BrowserView}
 * and {@link ModuleDetailView}.
 */
final class ContentRowHelper {

    /** Icon size used for content-row type indicators. */
    static final int ICON_SIZE = 18;

    /** Spacing between title, topic, and indicator rows. */
    private static final int TEXT_SPACING = 2;

    private ContentRowHelper() { }

    /**
     * Returns the appropriate icon for a content type.
     *
     * @param type the content type.
     * @return the matching icon.
     */
    static FontAwesomeSolid iconFor(final ContentType type) {
        switch (type) {
            case VIDEO: return FontAwesomeSolid.PLAY_CIRCLE;
            case QUIZ:  return FontAwesomeSolid.QUESTION_CIRCLE;
            default:    return FontAwesomeSolid.BOOK;
        }
    }

    /**
     * Returns the CSS icon class for a content type.
     *
     * @param type the content type.
     * @return the CSS class name.
     */
    static String iconClassFor(final ContentType type) {
        switch (type) {
            case VIDEO: return "icon-secondary";
            case QUIZ:  return "icon-secondary";
            default:    return "icon-primary";
        }
    }

    /**
     * Builds a badge label for a content type.
     *
     * @param type the content type.
     * @return the badge label.
     */
    static Label buildBadge(final ContentType type) {
        String text;
        String styleClass;
        switch (type) {
            case VIDEO:
                text = "VIDEO";
                styleClass = "badge-resource";
                break;
            case QUIZ:
                text = "QUIZ";
                styleClass = "badge-quiz";
                break;
            default:
                text = "ARTICLE";
                styleClass = "badge-resource";
                break;
        }
        Label badge = new Label(text);
        badge.getStyleClass().addAll("badge", styleClass);
        return badge;
    }

    /**
     * Builds a content row with icon, title, topic, optional media indicators,
     * badge, and Open button.
     *
     * @param lc             the content item.
     * @param openHandler    callback invoked when Open is clicked.
     * @param mediaIndicators optional indicator row; omitted when empty
     *                        or null.
     * @return the row HBox.
     */
    static HBox buildContentRow(
            final LearningContent lc,
            final Consumer<LearningContent> openHandler,
            final HBox mediaIndicators) {
        ContentType type = ContentType.of(lc);

        FontIcon icon = new FontIcon(iconFor(type));
        icon.setIconSize(ICON_SIZE);
        icon.getStyleClass().add(iconClassFor(type));

        Label title = new Label(lc.getTitle());
        title.getStyleClass().add("text-dark");
        title.setStyle("-fx-font-size: " + UIConstants.FONT_LABEL + "px;"
                + " -fx-font-weight: bold;");

        Label topic = new Label(lc.getTopic());
        topic.getStyleClass().add("text-muted");
        topic.setStyle("-fx-font-size: " + UIConstants.FONT_SMALL + "px;");

        VBox text;
        if (mediaIndicators != null
                && !mediaIndicators.getChildren().isEmpty()) {
            text = new VBox(TEXT_SPACING, title, topic, mediaIndicators);
        } else {
            text = new VBox(TEXT_SPACING, title, topic);
        }
        HBox.setHgrow(text, Priority.ALWAYS);

        Label badge = buildBadge(type);
        Button open = new Button("Open");
        open.getStyleClass().add("btn-primary");
        open.setOnAction(e -> openHandler.accept(lc));

        HBox row = new HBox(UIConstants.SPACING_LG, icon, text, badge, open);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(UIConstants.SPACING_MD,
                UIConstants.SPACING_LG,
                UIConstants.SPACING_MD,
                UIConstants.SPACING_LG));
        row.getStyleClass().add("content-row");
        return row;
    }
}
