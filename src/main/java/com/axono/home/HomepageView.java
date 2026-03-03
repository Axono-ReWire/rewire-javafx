package com.axono.home;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class HomepageView extends ScrollPane {

    private static final String PRIMARY = "#59BE8B";
    private static final String BG = "#FCFBFB";
    private static final String CARD = "#FFFFFF";
    private static final String TEXT = "#111827";
    private static final String BORDER = "#DCDCDC";
    private static final String BG_COLOR = "-fx-background-color: ";

    private static final String[] TOPICS = {
            "Layouts", "Decibels", "Op-Amps", "Electromagnetism", "Phasors",
            "Complex Impedances", "Kirchhoff's Laws", "Passive Networks", "Dividers",
            "Equivalent Networks", "Circuit Analysis", "Op-Amp Bandwidths",
            "Poles and Zeros", "Frequency Response", "Step Response"
    };

    public HomepageView() {
        buildUI();
    }

    private void buildUI() {
        VBox content = new VBox(40);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(60, 20, 60, 20));
        content.setMaxWidth(800);
        content.setStyle(BG_COLOR + BG + ";");
        content.getChildren().addAll(buildBanner(), buildModuleTopicList());

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle(BG_COLOR + BG + ";");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);
        setBorder(Border.EMPTY);
        setStyle(BG_COLOR + BG + ";");
    }

    private VBox buildBanner() {
        Label welcome = new Label("Welcome to Axono ReWire!");
        welcome.setStyle(
                "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");

        Label subtitle = new Label("Start your engineering journey today!");
        subtitle.setStyle("-fx-font-size: 18px; -fx-text-fill: " + TEXT + ";");

        HBox buttons = new HBox(10,
                outlineButton("I'm a Student"),
                outlineButton("I'm an Educator"));
        buttons.setAlignment(Pos.CENTER);

        Label loginPrompt = new Label("Already have an account? Log in");
        loginPrompt.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT + ";");

        VBox banner = new VBox(10, welcome, subtitle, buttons, loginPrompt);
        banner.setAlignment(Pos.CENTER);
        return banner;
    }

    private VBox buildModuleTopicList() {
        Label moduleName = new Label("Analogue Electronics");
        moduleName.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");

        VBox list = new VBox(8);
        list.setPadding(new Insets(20));
        list.setMaxWidth(700);
        list.setStyle(
                BG_COLOR + CARD + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 6px; -fx-background-radius: 6px;");

        for (String topic : TOPICS) {
            Label lbl = new Label("• " + topic);
            lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: " + TEXT + ";");
            list.getChildren().add(lbl);
        }

        VBox section = new VBox(16, moduleName, list);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(700);
        return section;
    }

    private Button outlineButton(String text) {
        String base = "-fx-background-color: transparent;" +
                "-fx-border-color: " + PRIMARY + "; -fx-border-width: 2px;" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;" +
                "-fx-text-fill: " + PRIMARY + ";" +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
        String hover = BG_COLOR + PRIMARY + ";" +
                "-fx-border-color: " + PRIMARY + "; -fx-border-width: 2px;" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
        Button b = new Button(text);
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }
}
