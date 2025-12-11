package com.example.appstore.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class TopBar extends HBox {

    private final java.util.function.Consumer<String> onSearch;
    private final java.util.function.Consumer<String> onFilter;
    private final java.util.List<Button> filterButtons =
        new java.util.ArrayList<>();

    public TopBar(
        java.util.function.Consumer<String> onSearch,
        java.util.function.Consumer<String> onFilter
    ) {
        this.onSearch = onSearch;
        this.onFilter = onFilter;

        getStyleClass().add("top-bar");
        setAlignment(Pos.CENTER_LEFT);

        // Search Bar
        HBox searchContainer = new HBox(8);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.getStyleClass().add("search-field");

        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconColor(javafx.scene.paint.Color.web("#71717a"));

        TextField searchInput = new TextField();
        searchInput.setPromptText("Search apps...");
        searchInput.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: white; -fx-prompt-text-fill: #71717a;"
        );
        searchInput.setPrefWidth(250);
        searchInput
            .textProperty()
            .addListener((obs, oldVal, newVal) -> {
                if (onSearch != null) onSearch.accept(newVal);
            });

        searchContainer.getChildren().addAll(searchIcon, searchInput);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filters
        HBox filters = new HBox(4);
        filters.setAlignment(Pos.CENTER);
        filters.setStyle(
            "-fx-background-color: #18181b; -fx-background-radius: 6px; -fx-padding: 2;"
        );

        filters.getChildren().add(createFilterBtn("All", true));
        filters.getChildren().add(createFilterBtn("Windows", false));
        filters.getChildren().add(createFilterBtn("macOS", false));
        filters.getChildren().add(createFilterBtn("Linux", false));

        // Feedback Button
        Button feedbackBtn = new Button("Feedback");
        feedbackBtn.getStyleClass().add("action-button");

        getChildren().addAll(searchContainer, spacer, filters, feedbackBtn);
    }

    private Button createFilterBtn(String text, boolean selected) {
        Button btn = new Button(text);
        btn.getStyleClass().add("filter-button");
        updateFilterBtnStyle(btn, selected);

        if (text.equals("Windows")) btn.setGraphic(
            new FontIcon(Feather.MONITOR)
        );

        btn.setOnAction(e -> {
            filterButtons.forEach(b -> updateFilterBtnStyle(b, false));
            updateFilterBtnStyle(btn, true);
            if (onFilter != null) onFilter.accept(text);
        });

        filterButtons.add(btn);
        return btn;
    }

    private void updateFilterBtnStyle(Button btn, boolean selected) {
        if (selected) {
            btn.setStyle(
                "-fx-background-color: #27272a; -fx-text-fill: white;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #a1a1aa;"
            );
        }
    }
}
