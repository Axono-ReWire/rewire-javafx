package com.axono.player.module;

import com.axono.content.TextItem;
import com.axono.ui.UIConstants;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Renders one or more {@link TextItem}s as a vertically stacked block of
 * labels. The module is purely visual — {@link #onEnter()} and
 * {@link #onExit()} are no-ops because text has no playback state.
 */
public final class TextModule implements MediaModule {

    /** The rendered view, built once during construction. */
    private final VBox view;

    /**
     * Constructs a {@code TextModule} that displays the given items in
     * the order supplied.
     *
     * @param items the text items to render; an empty list produces an
     *              empty (but non-null) view.
     */
    public TextModule(final List<TextItem> items) {
        this.view = new VBox(UIConstants.SPACING_MD);
        for (TextItem item : items) {
            view.getChildren().add(buildLabel(item.getText()));
        }
    }

    /**
     * Builds a wrapping body label with the project's standard text style.
     *
     * @param text the label content.
     * @return the configured {@link Label}.
     */
    private static Label buildLabel(final String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.getStyleClass().add("text-dark");
        l.setStyle("-fx-font-size: 15px;");
        return l;
    }

    @Override
    public Node getView() {
        return view;
    }

    @Override
    public void onEnter() {
        // No playback state.
    }

    @Override
    public void onExit() {
        // No playback state.
    }
}
