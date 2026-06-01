package com.axono.player.module;

import com.axono.player.Assets;
import com.axono.content.ImageItem;
import com.axono.ui.UIConstants;
import java.util.List;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

/**
 * Renders a list of {@link ImageItem}s as a column of images.
 * Each image is resolved via {@link Assets#resolve(String)} (classpath-rooted
 * under {@code /assets/}); if the resource cannot be found, the alt text is
 * shown instead so the slide always renders something.
 *
 * <p>SVG files are rendered via a {@link WebView} because JavaFX's built-in
 * {@link Image} loader does not support the SVG format. All other raster
 * formats use a standard {@link ImageView}.</p>
 *
 * <p>An optional {@code maxImageHeight} constructor argument caps the rendered
 * height of each image. When zero the module uses the full-width, scene-height
 * binding suited to presentation slides.</p>
 */
public final class ImageSetModule implements MediaModule {

    /** Fraction of the container width used as max width (constrained). */
    private static final double CONSTRAINED_WIDTH_FRACTION = 0.9;

    /** Rendered container, populated during construction. */
    private final VBox view;

    /** Maximum image height in pixels; {@code 0} means unconstrained. */
    private final double maxImageHeight;

    /**
     * Constructs an unconstrained {@code ImageSetModule} (presentation mode).
     *
     * @param items the image items to render in order.
     */
    public ImageSetModule(final List<ImageItem> items) {
        this(items, 0);
    }

    /**
     * Constructs an {@code ImageSetModule} with an optional height cap.
     *
     * @param items  the image items to render in order.
     * @param height maximum rendered height in pixels; {@code 0} for
     *               unconstrained (full presentation sizing).
     */
    public ImageSetModule(final List<ImageItem> items,
            final double height) {
        this.maxImageHeight = height;
        this.view = new VBox(UIConstants.SPACING_LG);
        this.view.setAlignment(Pos.CENTER);
        for (ImageItem item : items) {
            Node node = buildNode(item);
            view.getChildren().add(node);
            bindToParent(node);
        }
    }

    /**
     * Binds the size of a media node to the parent {@link #view} width so
     * images and SVGs resize dynamically when the app window is resized.
     * When {@link #maxImageHeight} is set, a width-fraction + fixed-height cap
     * is applied instead of the full-screen presentation binding.
     *
     * @param node the node just added to {@link #view}.
     */
    private void bindToParent(final Node node) {
        if (node instanceof ImageView) {
            ImageView iv = (ImageView) node;
            if (maxImageHeight > 0) {
                iv.setFitHeight(maxImageHeight);
                iv.fitWidthProperty().bind(
                        view.widthProperty().multiply(
                                CONSTRAINED_WIDTH_FRACTION));
            } else {
                iv.sceneProperty().addListener((obs, o, scene) -> {
                    if (scene != null) {
                        iv.fitWidthProperty().bind(view.widthProperty());
                        iv.fitHeightProperty().bind(scene.heightProperty()
                                .subtract(UIConstants.THUMBNAIL_HEIGHT));
                    } else {
                        iv.fitWidthProperty().unbind();
                        iv.fitHeightProperty().unbind();
                    }
                });
            }
        } else if (node instanceof WebView) {
            WebView wv = (WebView) node;
            wv.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            wv.sceneProperty().addListener((obs, o, scene) -> {
                if (scene != null) {
                    wv.prefWidthProperty().bind(view.widthProperty());
                    wv.prefWidthProperty().addListener(
                            (pw, pwOld, pwNew) -> {
                                if (pwNew.doubleValue() > 0) {
                                    syncSvgHeight(wv);
                                }
                            });
                } else {
                    wv.prefWidthProperty().unbind();
                }
            });
        }
    }

    /**
     * Builds the JavaFX node for a single image item, falling back to an
     * alt-text label if the asset cannot be resolved.
     *
     * @param item the image item.
     * @return either an {@link ImageView}, a {@link WebView} (for SVG),
     *         or a fallback {@link Label}.
     */
    private static Node buildNode(final ImageItem item) {
        String url = Assets.resolve(item.getSrc());
        if (url != null) {
            if (url.toLowerCase().endsWith(".svg")) {
                return buildSvgView(url);
            }
            return buildImageView(url);
        }
        return buildAltLabel(item.getAlt());
    }

    /**
     * Builds an {@link ImageView} from an asset URL string.
     * The caller is responsible for binding {@code fitWidthProperty()} to the
     * parent container so the image scales responsively.
     *
     * @param url the external-form URL of the image resource.
     * @return the configured {@link ImageView}.
     */
    private static ImageView buildImageView(final String url) {
        Image image = new Image(url);
        ImageView iv = new ImageView(image);
        iv.setPreserveRatio(true);
        return iv;
    }

    /**
     * Builds a {@link WebView} for rendering an SVG asset.
     * JavaFX's {@link Image} loader does not support SVG, so a WebView
     * is used as a lightweight in-process SVG renderer.
     * The caller binds preferred dimensions to the parent container width.
     *
     * @param url the external-form URL of the SVG resource.
     * @return a configured {@link WebView}.
     */
    private static WebView buildSvgView(final String url) {
        WebView wv = new WebView();
        String html = "<html><head><style>"
                + "html,body{margin:0;padding:0;width:100%;height:100%;"
                + "overflow:hidden;background:transparent;}"
                + "img{width:100%;height:auto;display:block;}"
                + "</style></head><body>"
                + "<img src='" + url + "'/>"
                + "</body></html>";
        wv.getEngine().loadContent(html);
        wv.getEngine().getLoadWorker().stateProperty().addListener(
                (obs, old, state) -> {
                    if (state == Worker.State.SUCCEEDED) {
                        syncSvgHeight(wv);
                    }
                });
        return wv;
    }

    /**
     * Queries the rendered content height of a {@link WebView} via JavaScript
     * and sets it as the node's preferred height. Called after the SVG page
     * finishes loading and again on every width change so the WebView always
     * shows the full image without clipping.
     *
     * @param wv the WebView whose height to synchronise.
     */
    private static void syncSvgHeight(final WebView wv) {
        Object h = wv.getEngine().executeScript("document.body.scrollHeight");
        if (h instanceof Number && ((Number) h).intValue() > 0) {
            wv.setPrefHeight(((Number) h).doubleValue());
        }
    }

    /**
     * Builds a fallback label showing alt text when an image cannot be
     * loaded. The label is styled like a muted caption.
     *
     * @param alt the alt text; may be empty.
     * @return a styled {@link Label}.
     */
    private static Label buildAltLabel(final String alt) {
        String shown = alt.isEmpty() ? "[image]" : alt;
        Label l = new Label(shown);
        l.setWrapText(true);
        l.getStyleClass().add("text-muted");
        l.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");
        return l;
    }

    @Override
    public Node getView() {
        return view;
    }

    @Override
    public void onEnter() {
        // Images render passively once attached to the scene graph.
    }

    @Override
    public void onExit() {
        // No playback state to release.
    }
}
