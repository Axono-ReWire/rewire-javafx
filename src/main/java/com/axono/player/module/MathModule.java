package com.axono.player.module;

import com.axono.content.MathItem;
import com.axono.ui.UIConstants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

/**
 * Renders a list of {@link MathItem}s as vertically stacked LaTeX images.
 * Each item is rendered with JLaTeXMath into a JavaFX {@link ImageView}.
 * When rendering fails, a plain label falls back to the raw LaTeX source.
 */
public final class MathModule implements MediaModule {

    /** Font size used for LaTeX rendering. */
    private static final float FONT_SIZE = 18.0f;

    /** Horizontal padding added around rendered LaTeX, in pixels. */
    private static final int PADDING_PX = 8;

    /** Rendered view. */
    private final VBox view;

    /**
     * Constructs a {@code MathModule} that renders the given math items.
     *
     * @param items the math items to render in order.
     */
    public MathModule(final List<MathItem> items) {
        this.view = new VBox(UIConstants.SPACING_MD);
        for (MathItem item : items) {
            view.getChildren().add(buildNode(item.getLatex()));
        }
    }

    /**
     * Renders a LaTeX string to a JavaFX node. Falls back to a plain label
     * if JLaTeXMath throws an exception.
     *
     * @param latex the LaTeX source.
     * @return an {@link ImageView} of the rendered formula, or a fallback
     *         {@link Label}.
     */
    private static Node buildNode(final String latex) {
        try {
            TeXFormula formula = new TeXFormula(latex);
            TeXIcon icon = formula.createTeXIcon(
                    TeXConstants.STYLE_DISPLAY, FONT_SIZE);
            icon.setInsets(new java.awt.Insets(
                    PADDING_PX, PADDING_PX, PADDING_PX, PADDING_PX));

            BufferedImage buf = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = buf.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
            icon.paintIcon(new javax.swing.JLabel(), g2, 0, 0);
            g2.dispose();

            ImageView iv = new ImageView(SwingFXUtils.toFXImage(buf, null));
            iv.setPreserveRatio(true);
            return iv;
        } catch (Exception ex) {
            return buildFallbackLabel(latex);
        }
    }

    /**
     * Builds a plain-text fallback label when rendering fails.
     *
     * @param latex the raw LaTeX string to display.
     * @return a styled {@link Label}.
     */
    private static Label buildFallbackLabel(final String latex) {
        Label l = new Label(latex);
        l.setWrapText(true);
        l.getStyleClass().add("text-dark");
        l.setStyle("-fx-font-size: 14px; -fx-font-family: monospace;");
        return l;
    }

    @Override
    public Node getView() {
        return view;
    }

    @Override
    public void onEnter() {
        // Math images render passively.
    }

    @Override
    public void onExit() {
        // No playback state.
    }
}
