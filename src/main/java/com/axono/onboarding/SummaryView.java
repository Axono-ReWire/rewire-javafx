package com.axono.onboarding;

import com.axono.auth.UserProfile;
import com.axono.ui.UIConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Final step of the onboarding wizard, displaying a summary card of the
 * user's entered profile details before they launch the application.
 */
public final class SummaryView extends StackPane {

        /** The user profile whose data is displayed in the summary. */
        private final UserProfile profile;

        /** Label displaying the user's name. */
        private Label name;

        /** Label displaying the user's selected year of study. */
        private Label yearOfStudy;

        /** Label displaying the user's chosen username. */
        private Label username;

        /**
         * Label displaying the user's selected modules
         * as a comma-separated list.
         */
        private Label modules;

        /**
         * Constructs the {@code SummaryView} for
         * the given profile and builds the UI.
         *
         * @param userProfile the {@link UserProfile}
         *                    whose data will be summarised.
         */
        public SummaryView(final UserProfile userProfile) {
                this.profile = userProfile;
                getStyleClass().add("bg-transparent");
                setPadding(new Insets(UIConstants.SPACING_6XL));
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
                heading.getStyleClass().add("text-primary");
                heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

                Separator sep = new Separator();
                VBox.setMargin(sep, new Insets(UIConstants.SPACING_XL,
                                0, UIConstants.SPACING_XL, 0));

                name = valueLabel();
                yearOfStudy = valueLabel();
                username = valueLabel();
                modules = valueLabel();
                modules.setWrapText(true);

                VBox card = new VBox(0,
                                icon, heading, sep,
                                row("Name", name),
                                row("Username", username),
                                row("Year of Study", yearOfStudy),
                                row("Modules", modules));
                card.setAlignment(Pos.TOP_CENTER);
                card.setMaxWidth(UIConstants.SUMMARY_CARD_WIDTH);
                card.getStyleClass().add("card");
                card.setStyle("-fx-padding: 30px 44px;");

                setAlignment(Pos.CENTER);
                getChildren().add(card);
        }

        /**
         * Builds a labelled key-value row for the summary card.
         *
         * @param key the field name to display on the left.
         * @param val the {@link Label} containing the field value
         *            on the right.
         * @return an {@link HBox} row containing the key and value labels.
         */
        private HBox row(final String key, final Label val) {
                Label k = new Label(key + ":");
                k.getStyleClass().add("text-muted");
                k.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                k.setMinWidth(UIConstants.NAV_BTN_WIDTH);

                HBox row = new HBox(UIConstants.SPACING_2XL, k, val);
                row.setAlignment(Pos.CENTER_LEFT);
                VBox.setMargin(row, new Insets(0, 0,
                                UIConstants.SPACING_LG, 0));
                return row;
        }

        /**
         * Creates a placeholder value {@link Label}
         * initialised with an em dash.
         *
         * @return a styled {@link Label} used as a summary value field.
         */
        private Label valueLabel() {
                Label l = new Label("—");
                l.getStyleClass().add("text-dark");
                l.setStyle("-fx-font-size: 14px;");
                return l;
        }

        /**
         * Refreshes all summary labels from the current state
         * of the user profile.
         * Should be called immediately before the summary step is displayed.
         */
        public void refresh() {
                String fullName = profile.getFullName();
                name.setText(fullName.isEmpty() ? "—" : fullName);
                yearOfStudy.setText(profile.getYearOfStudy());
                username.setText(profile.getUsername().isEmpty()
                                ? "—" : profile.getUsername());
                var subjects = profile.getSubjects();
                modules.setText(subjects
                                .isEmpty() ? "—"
                                                : String.join(", ", subjects));
        }
}
