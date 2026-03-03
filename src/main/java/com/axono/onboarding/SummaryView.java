package com.axono.onboarding;

import com.axono.model.UserProfile;
import com.axono.ui.UITheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

public class SummaryView extends StackPane {

    private static final String TEXT_FILL_STYLE = "-fx-text-fill: ";
    private final UserProfile profile;
    private Label nameVal;
    private Label yearVal;
    private Label instVal;
    private Label subjectsVal;

    public SummaryView(UserProfile profile) {
        this.profile = profile;
        buildUI();
    }

    private void buildUI() {
        setStyle("-fx-background-color: " + UITheme.BG + ";");
        setPadding(new Insets(30));

        Label icon = new Label("✅");
        icon.setStyle("-fx-font-size: 40px;");

        Label heading = new Label("You're all set!");
        heading.setStyle(TEXT_FILL_STYLE + UITheme.PRIMARY + ";" +
                "-fx-font-size: 20px; -fx-font-weight: bold;");

        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(12, 0, 12, 0));

        nameVal = valueLabel();
        yearVal = valueLabel();
        instVal = valueLabel();
        subjectsVal = valueLabel();
        subjectsVal.setWrapText(true);

        VBox card = new VBox(0,
                icon, heading, sep,
                row("Name", nameVal),
                row("Year of Study", yearVal),
                row("Institution", instVal),
                row("Modules", subjectsVal));
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(520);
        card.setStyle(UITheme.CARD_STYLE + "-fx-padding: 30px 44px;");

        setAlignment(Pos.CENTER);
        getChildren().add(card);
    }

    private HBox row(String key, Label val) {
        Label k = new Label(key + ":");
        k.setStyle(TEXT_FILL_STYLE + UITheme.TEXT_MUTED + ";" +
                "-fx-font-size: 13px; -fx-font-weight: bold;");
        k.setMinWidth(130);

        HBox row = new HBox(16, k, val);
        row.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(row, new Insets(0, 0, 10, 0));
        return row;
    }

    private Label valueLabel() {
        Label l = new Label("—");
        l.setStyle(TEXT_FILL_STYLE + UITheme.TEXT_DARK + "; -fx-font-size: 14px;");
        return l;
    }

    public void refresh() {
        nameVal.setText(profile.getName().isEmpty() ? "—" : profile.getName());
        yearVal.setText(profile.getYearOfStudy());
        instVal.setText(profile.getInstitution().isEmpty() ? "Not specified" : profile.getInstitution());
        var subs = profile.getSubjects();
        subjectsVal.setText(subs.isEmpty() ? "—" : String.join(", ", subs));
    }
}
