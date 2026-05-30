package com.axono.onboarding;

import com.axono.model.UserProfile;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Final step of the onboarding wizard, displaying a summary card of the
 * user's entered profile details before they launch the application.
 */
public final class SummaryView extends StackPane {

        /** Spacing between items in the summary card VBox, in pixels. */
        private static final int CARD_SPACING = 12;

        /** The user profile whose data is displayed in the summary. */
        private final UserProfile profile;

        /** Label displaying the user's name. */
        private Label name;

        /** Label displaying the user's selected year of study. */
        private Label yearOfStudy;

        /** Label displaying the user's institution. */
        private Label institution;

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

                buildUI();
        }

        /**
         * Builds the summary card layout, initialising all value labels
         * with placeholder dashes.
         */
        private void buildUI() {
                Label icon = new Label("\u2705");
                icon.getStyleClass().add("subheader");

                Label heading = new Label("You're all set!");
                heading.getStyleClass().add("header");

                name = valueLabel();
                yearOfStudy = valueLabel();
                institution = valueLabel();
                modules = valueLabel();
                modules.setWrapText(true);

                VBox card = new VBox(CARD_SPACING,
                                icon, heading,
                                row("Name", name),
                                row("Year of Study", yearOfStudy),
                                row("Institution", institution),
                                row("Modules", modules));
                card.getStyleClass().add("card3");

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
                HBox row = new HBox(k, val);
                row.setStyle("-fx-alignment:center; -fx-spacing:4px;");

                return row;
        }

        /**
         * Creates a placeholder value {@link Label}
         * initialised with an em dash.
         *
         * @return a styled {@link Label} used as a summary value field.
         */
        private Label valueLabel() {
                Label l = new Label("\u2014");

                return l;
        }

        /**
         * Refreshes all summary labels from the current state
         * of the user profile.
         * Should be called immediately before the summary step is displayed.
         */
        public void refresh() {
                name.setText(profile.getName()
                                .isEmpty() ? "\u2014" : profile.getName());
                yearOfStudy.setText(profile.getYearOfStudy());
                institution.setText(
                                profile.getInstitution().isEmpty()
                                                ? "Not specified"
                                                : profile.getInstitution());
                var subjects = profile.getSubjects();
                modules.setText(subjects
                                .isEmpty() ? "\u2014"
                                                : String.join(", ", subjects));
        }
}