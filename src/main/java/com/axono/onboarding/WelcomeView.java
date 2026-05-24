package com.axono.onboarding;

//import com.axono.ui.UITheme;
import com.axono.ui.UIConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * First step of the onboarding wizard, displays a branded welcome card
 * that introduces Axono ReWire to the user.
 */
public final class WelcomeView extends StackPane {

        /** Reusable JavaFX CSS prefix for setting text colour. */
        private static final String TEXT_FILL_STYLE = "-fx-text-fill: ";

        /**
         * Constructs the {@code WelcomeView}, building and displaying
         * the welcome card layout.
         */
        public WelcomeView() {

                Label icon = new Label("Axono Logo Placeholder");
                icon.getStyleClass().add("header");

                Label title = new Label("Welcome to ReWire!");
                title.getStyleClass().add("subheader");

                Label body = new Label("The all-in-one platform"
                                + " for mastering all things Engineering.");
                body.setTextAlignment(TextAlignment.CENTER);
                body.setWrapText(true);

                Label hint = new Label("Click Next to set up your profile"
                                + " and get started.");

                VBox card = new VBox(
                                icon, title, body, hint);
                card.getStyleClass().add("card");

                getChildren().add(card);
        }
}
