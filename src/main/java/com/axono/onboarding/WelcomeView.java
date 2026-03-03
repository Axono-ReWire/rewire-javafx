package com.axono.onboarding;

import com.axono.ui.UITheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class WelcomeView extends StackPane {

    private static final String TEXT_FILL_STYLE = "-fx-text-fill: ";

    public WelcomeView() {
        setStyle("-fx-background-color: " + UITheme.BG + ";");
        setPadding(new Insets(30));

        Label icon = new Label("📐");
        icon.setStyle("-fx-font-size: 52px;");

        Label title = new Label("Welcome to axono");
        title.setStyle(TEXT_FILL_STYLE + UITheme.PRIMARY + ";" +
                "-fx-font-size: 20px; -fx-font-weight: bold;");

        Label body = new Label(
                "Your all-in-one platform for mastering distributed systems,\n" +
                        "digital communications, networking, and signal processing.");
        body.setStyle(TEXT_FILL_STYLE + UITheme.TEXT_MUTED + "; -fx-font-size: 14px;");
        body.setTextAlignment(TextAlignment.CENTER);
        body.setWrapText(true);

        Label hint = new Label("Click Next to set up your profile →");
        hint.setStyle(TEXT_FILL_STYLE + UITheme.ACCENT + "; -fx-font-size: 12px;");

        VBox card = new VBox(14, icon, title, body, hint);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(460);
        card.setStyle(UITheme.CARD_STYLE + "-fx-padding: 44px 60px;");

        setAlignment(Pos.CENTER);
        getChildren().add(card);
    }
}
