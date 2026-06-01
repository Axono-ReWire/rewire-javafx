package com.axono.onboarding;

import com.axono.ui.UIConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * First step of the onboarding wizard, displays a branded welcome card
 * that introduces Axono ReWire to the user.
 */
public final class WelcomeView extends StackPane {

        /** Size of the logo icon in pixels. */
        private static final int LOGO_ICON_SIZE = 52;

        /**
         * Constructs the {@code WelcomeView}, building and displaying
         * the welcome card layout.
         */
        public WelcomeView() {
                getStyleClass().add("bg-transparent");
                setPadding(new Insets(UIConstants.SPACING_6XL));

                FontIcon icon = new FontIcon(FontAwesomeSolid.GRADUATION_CAP);
                icon.setIconSize(LOGO_ICON_SIZE);
                icon.getStyleClass().add("icon-primary");

                Label title = new Label("Welcome to ReWire!");
                title.getStyleClass().add("text-primary");
                title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

                Label body = new Label("The all-in-one platform"
                                + " for mastering all things Engineering.");
                body.getStyleClass().add("text-muted");
                body.setStyle("-fx-font-size: " + UIConstants.FONT_BODY
                        + "px;");
                body.setTextAlignment(TextAlignment.CENTER);
                body.setWrapText(true);

                Label hint = new Label("Click Next to set up your profile"
                                + " and get started.");
                hint.getStyleClass().add("text-secondary");
                hint.setStyle("-fx-font-size: 12px;");

                VBox card = new VBox(UIConstants.SPACING_LG,
                                icon, title, body, hint);
                card.setAlignment(Pos.CENTER);
                card.setMaxWidth(UIConstants.WELCOME_CARD_WIDTH);
                card.getStyleClass().add("card");
                card.setStyle("-fx-padding: 44px 60px;");

                setAlignment(Pos.CENTER);
                getChildren().add(card);
        }
}
