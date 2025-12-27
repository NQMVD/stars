package com.example.appstore.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Claude-style top bar with search and minimal actions.
 */
public class TopBar extends HBox {

    private final java.util.function.Consumer<String> onSearch;
    private final java.util.function.Consumer<String> onFilter;

    public TopBar(
            java.util.function.Consumer<String> onSearch,
            java.util.function.Consumer<String> onFilter) {
        this.onSearch = onSearch;
        this.onFilter = onFilter;

        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(0, 32, 0, 32));
        setMinHeight(64);
        setPrefHeight(64);
        setStyle(
                "-fx-background-color: rgba(17, 17, 17, 0.8); -fx-border-color: #2a2a2a; -fx-border-width: 0 0 1px 0;");

        // Search Container with rounded pill style
        HBox searchContainer = new HBox(8);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setPadding(new Insets(8, 16, 8, 16));
        searchContainer.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-border-color: #2a2a2a; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 20px; " +
                        "-fx-background-radius: 20px;");
        searchContainer.setMaxWidth(512);

        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(18);
        searchIcon.setIconColor(Color.web("#666666"));

        TextField searchInput = new TextField();
        searchInput.setPromptText("Apps, Entwickler oder Tools suchen...");
        searchInput.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #f0f0f0; " +
                        "-fx-prompt-text-fill: #444444; " +
                        "-fx-padding: 0;");
        searchInput.setPrefWidth(280);
        HBox.setHgrow(searchInput, Priority.ALWAYS);

        searchInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (onSearch != null)
                onSearch.accept(newVal);
        });

        // Focus styling
        searchInput.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                searchContainer.setStyle(
                        "-fx-background-color: #1a1a1a; " +
                                "-fx-border-color: #d97757; " +
                                "-fx-border-width: 1px; " +
                                "-fx-border-radius: 20px; " +
                                "-fx-background-radius: 20px;");
            } else {
                searchContainer.setStyle(
                        "-fx-background-color: #1a1a1a; " +
                                "-fx-border-color: #2a2a2a; " +
                                "-fx-border-width: 1px; " +
                                "-fx-border-radius: 20px; " +
                                "-fx-background-radius: 20px;");
            }
        });

        searchContainer.getChildren().addAll(searchIcon, searchInput);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right side actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button globeBtn = new Button();
        FontIcon globeIcon = new FontIcon(Feather.GLOBE);
        globeIcon.setIconSize(20);
        globeIcon.setIconColor(Color.web("#999999"));
        globeBtn.setGraphic(globeIcon);
        globeBtn.setStyle("-fx-background-color: transparent; -fx-padding: 8px; -fx-cursor: hand;");

        // Hover effect
        globeBtn.setOnMouseEntered(e -> globeIcon.setIconColor(Color.WHITE));
        globeBtn.setOnMouseExited(e -> globeIcon.setIconColor(Color.web("#999999")));

        actions.getChildren().add(globeBtn);

        getChildren().addAll(searchContainer, spacer, actions);
    }
}
