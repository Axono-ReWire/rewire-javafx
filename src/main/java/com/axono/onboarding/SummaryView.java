package com.axono.onboarding;

import com.axono.model.UserProfile;
import com.axono.ui.UITheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

/**
 * Final step of the onboarding wizard, displaying a summary card of the
 * user's entered profile details before they launch the application.
 */
public final class SummaryView extends StackPane {

    private static final String TEXT_FILL_STYLE = "-fx-text-fill: ";
    private final UserProfile profile;
    private Label name;
    private Label yearOfStudy;
    private Label institution;
    private Label modules;

    /**
     * Constructs the {@code SummaryView} for the given profile and builds the UI.
     *
     * @param profile the {@link UserProfile} whose data will be summarised.
     */
    public SummaryView(UserProfile profile) {
        this.profile = profile;
        setStyle("-fx-background-color: " + UITheme.BG + ";");
        setPadding(new Insets(30));
        buildUI();
    }

    /**
     * Builds the summary card layout, initialising all value labels
     * with placeholder dashes.
     */
    private void buildUI() {
        Label icon = new Label("✅");
        icon.setStyle("-fx-font-size: 40px;");

        Label heading = new Label("You're all set!");
        heading.setStyle(TEXT_FILL_STYLE + UITheme.PRIMARY + ";" +
                "-fx-font-size: 20px; -fx-font-weight: bold;");

        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(12, 0, 12, 0));

        name = valueLabel();
        yearOfStudy = valueLabel();
        institution = valueLabel();
        modules = valueLabel();
        modules.setWrapText(true);

        VBox card = new VBox(0,
                icon, heading, sep,
                row("Name", name),
                row("Year of Study", yearOfStudy),
                row("Institution", institution),
                row("Modules", modules));
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(520);
        card.setStyle(UITheme.CARD_STYLE + "-fx-padding: 30px 44px;");

        setAlignment(Pos.CENTER);
        getChildren().add(card);
    }

    /**
     * Builds a labelled key-value row for the summary card.
     *
     * @param key the field name to display on the left.
     * @param val the {@link Label} containing the field value on the right.
     * @return an {@link HBox} row containing the key and value labels.
     */
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

    /**
     * Creates a placeholder value {@link Label} initialised with an em dash.
     *
     * @return a styled {@link Label} used as a summary value field.
     */
    private Label valueLabel() {
        Label l = new Label("—");
        l.setStyle(TEXT_FILL_STYLE + UITheme.TEXT_DARK + "; -fx-font-size: 14px;");
        return l;
    }

    /**
     * Refreshes all summary labels from the current state of the user profile.
     * Should be called immediately before the summary step is displayed.
     */
    public void refresh() {
        name.setText(profile.getName().isEmpty() ? "—" : profile.getName());
        yearOfStudy.setText(profile.getYearOfStudy());
        institution.setText(profile.getInstitution().isEmpty() ? "Not specified" : profile.getInstitution());
        var subjects = profile.getSubjects();
        modules.setText(subjects.isEmpty() ? "—" : String.join(", ", subjects));
    }
}
