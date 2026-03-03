package com.axono.onboarding;

import com.axono.model.UserProfile;
import com.axono.ui.UITheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class SubjectView extends StackPane {

    private final UserProfile profile;
    private final List<CheckBox> checkboxes = new ArrayList<>();

    private static final String[][] SUBJECTS = {
            { "Distributed Systems", "RMI, concurrency, message passing" },
            { "Computer Networks", "TCP/IP, DNS, HTTP, sockets" },
            { "Digital Communications", "16-QAM, OFDM, modulation, noise" },
            { "Signal Processing", "Fourier, filtering, SNR analysis" },
            { "Algorithms & Data Structs", "Sorting, graphs, complexity" },
            { "Operating Systems", "Processes, scheduling, memory" },
            { "Database Systems", "SQL, NoSQL, transactions, ACID" },
            { "Software Engineering", "Design patterns, testing, CI/CD" },
    };

    public SubjectView(UserProfile profile) {
        this.profile = profile;
        buildUI();
    }

    private void buildUI() {
        setStyle("-fx-background-color: " + UITheme.BG + ";");
        setPadding(new Insets(20));

        // Top row: title + Select All button
        Label heading = new Label("Select Your Modules");
        heading.setStyle("-fx-text-fill: " + UITheme.PRIMARY + ";" +
                "-fx-font-size: 20px; -fx-font-weight: bold;");
        HBox.setHgrow(heading, Priority.ALWAYS);

        Button selectAll = new Button("Select All");
        selectAll.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
        selectAll.setOnAction(e -> {
            boolean anyUnchecked = checkboxes.stream().anyMatch(c -> !c.isSelected());
            checkboxes.forEach(c -> c.setSelected(anyUnchecked));
        });

        HBox topRow = new HBox(10, heading, selectAll);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label sub = new Label("Choose at least one subject to study:");
        sub.setStyle("-fx-text-fill: " + UITheme.TEXT_MUTED + "; -fx-font-size: 14px;");

        // 2-column grid of subject cells
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col, col2);

        String normalStyle = "-fx-background-color: " + UITheme.BG + ";" +
                "-fx-border-color: " + UITheme.BORDER + ";" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;" +
                "-fx-padding: 8px 10px;";
        String selectedStyle = "-fx-background-color: #E8F7FB;" +
                "-fx-border-color: " + UITheme.ACCENT + "; -fx-border-width: 2px;" +
                "-fx-border-radius: 4px; -fx-background-radius: 4px;" +
                "-fx-padding: 8px 10px;";

        for (int i = 0; i < SUBJECTS.length; i++) {
            CheckBox cb = new CheckBox(SUBJECTS[i][0]);
            cb.setStyle("-fx-font-size: 14px; -fx-text-fill: " + UITheme.TEXT_DARK + ";");

            Label desc = new Label(SUBJECTS[i][1]);
            desc.setStyle("-fx-font-size: 11px; -fx-text-fill: " + UITheme.TEXT_MUTED + ";");

            VBox cell = new VBox(2, cb, desc);
            cell.setStyle(normalStyle);
            cb.selectedProperty().addListener((obs, old, sel) -> cell.setStyle(sel ? selectedStyle : normalStyle));

            checkboxes.add(cb);
            grid.add(cell, i % 2, i / 2);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setBorder(Border.EMPTY);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox card = new VBox(10, topRow, sub, scroll);
        card.setMaxWidth(580);
        card.setStyle(UITheme.CARD_STYLE + "-fx-padding: 24px 36px;");

        setAlignment(Pos.CENTER);
        getChildren().add(card);
    }

    public boolean validateInput() {
        boolean any = checkboxes.stream().anyMatch(CheckBox::isSelected);
        if (!any) {
            Alert a = new Alert(Alert.AlertType.WARNING,
                    "Please select at least one module.", ButtonType.OK);
            a.setHeaderText("Required");
            a.showAndWait();
        }
        return any;
    }

    public void saveData() {
        List<String> sel = new ArrayList<>();
        checkboxes.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .forEach(sel::add);
        profile.setSubjects(sel);
    }
}
