package com.axono.dashboard;

import com.axono.model.UserProfile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class DashboardView extends ScrollPane {

    private static final String PRIMARY = "#59BE8B";
    private static final String SECONDARY = "#1A6A82";
    private static final String BG = "#FCFBFB";
    private static final String CARD = "#FFFFFF";
    private static final String TEXT = "#111827";
    private static final String BORDER = "#DCDCDC";
    private static final String BG_COLOR_STYLE = "-fx-background-color: ";

    private final UserProfile profile;

    public DashboardView(UserProfile profile) {
        this.profile = profile;
        buildUI();
    }

    private void buildUI() {
        VBox content = new VBox(40);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(60, 20, 60, 20));
        content.setMaxWidth(800);
        content.setStyle(BG_COLOR_STYLE + BG + ";");
        content.getChildren().addAll(
                buildBanner(),
                buildProgressSection(),
                buildRecommendedTopics());

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle(BG_COLOR_STYLE + BG + ";");
        HBox.setHgrow(content, Priority.ALWAYS);

        setContent(wrapper);
        setFitToWidth(true);
        setBorder(Border.EMPTY);
        setStyle(BG_COLOR_STYLE + BG + ";");
    }

    private VBox buildBanner() {
        Label welcome = new Label("Welcome, " + profile.getName() + "!");
        welcome.setStyle(
                "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");

        HBox buttons = new HBox(10, outlineButton("Profile"), outlineButton("Logout"));
        buttons.setAlignment(Pos.CENTER);

        VBox banner = new VBox(20, welcome, buttons);
        banner.setAlignment(Pos.CENTER);
        return banner;
    }

    private VBox buildProgressSection() {
        Label pageTitle = sectionLabel("Your Learning Dashboard", 28);
        Label subTitle = sectionLabel("Your Progress", 22);

        VBox entries = new VBox(10);
        var subjects = profile.getSubjects();
        if (subjects.isEmpty()) {
            entries.getChildren().add(bodyLabel("No modules selected."));
        } else {
            for (String subject : subjects) {
                Label name = bodyLabel(subject);
                name.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(name, Priority.ALWAYS);

                StackPane bar = progressBar(0);

                Label pct = new Label("0% Complete");
                pct.setStyle("-fx-text-fill: " + SECONDARY + "; -fx-font-size: 13px;");

                HBox row = new HBox(12, name, bar, pct);
                row.setAlignment(Pos.CENTER_LEFT);
                entries.getChildren().add(row);
            }
        }

        VBox section = new VBox(16, pageTitle, subTitle, cardWrap(entries));
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(700);
        return section;
    }

    private VBox buildRecommendedTopics() {
        Label title = sectionLabel("Recommended Topics", 22);

        Label icon = new Label("📘");
        icon.setStyle("-fx-font-size: 26px;");
        Label topic = new Label("Electronics");
        topic.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + SECONDARY + ";");

        HBox header = new HBox(8, icon, topic);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox moduleList = new VBox(12,
                buildModuleCard("Continue: Analogue Electronics", "Resume Lesson"),
                buildModuleCard("Explore Other Topics", "Browse All"));

        VBox section = new VBox(20, title, cardWrap(new VBox(20, header, moduleList)));
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(700);
        return section;
    }

    private VBox buildModuleCard(String title, String btnText) {
        Label label = bodyLabel(title);
        label.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");
        return cardWrap(new VBox(10, label, outlineButton(btnText)));
    }

    private VBox cardWrap(VBox content) {
        content.setPadding(new Insets(20));
        content.setMaxWidth(700);
        content.setStyle(
                BG_COLOR_STYLE + CARD + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 6px; -fx-background-radius: 6px;");
        return content;
    }

    private StackPane progressBar(double percent) {
        StackPane track = new StackPane();
        track.setPrefSize(160, 8);
        track.setStyle(BG_COLOR_STYLE + "#E5E7EB; -fx-background-radius: 4px;");

        double w = Math.max(4, 160 * percent / 100.0);
        StackPane fill = new StackPane();
        fill.setPrefSize(w, 8);
        fill.setStyle(BG_COLOR_STYLE + PRIMARY + "; -fx-background-radius: 4px;");
        fill.setTranslateX(-(160 - w) / 2);

        track.getChildren().add(fill);
        return track;
    }

    private Label sectionLabel(String text, int size) {
        Label l = new Label(text);
        l.setStyle(String.format(
                "-fx-font-size: %dpx; -fx-font-weight: bold; -fx-text-fill: %s;", size, TEXT));
        return l;
    }

    private Label bodyLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT + ";");
        return l;
    }

    private Button outlineButton(String text) {
        String base = "-fx-background-color: transparent;" +
                "-fx-border-color: " + PRIMARY + "; -fx-border-width: 2px;" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;" +
                "-fx-text-fill: " + PRIMARY + ";" +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
        String hover = BG_COLOR_STYLE + PRIMARY + ";" +
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
