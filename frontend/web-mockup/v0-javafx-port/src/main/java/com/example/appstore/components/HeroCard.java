package com.example.appstore.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class HeroCard extends StackPane {

    public enum GradientType {
        BLUE, PINK_PURPLE, PURPLE_ORANGE
    }

    // Constructor for sample/featured cards with custom content
    public HeroCard(String headline, String appName, String category, GradientType gradientType, Feather appIconType) {
        getStyleClass().add("app-card");
        setMinWidth(280);
        setMaxWidth(Double.MAX_VALUE);
        setMinHeight(200);
        setPrefHeight(220);

        // Apply gradient to the entire card
        String gradientStyle = getGradientStyle(gradientType);
        setStyle(gradientStyle + " -fx-background-radius: 16px;");

        // Main content layout
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_LEFT);

        // "FEATURED" label at top
        Label featuredLabel = new Label("FEATURED");
        featuredLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 10px; -fx-font-weight: bold;");

        // Headline text - large and bold
        Label headlineLabel = new Label(headline);
        headlineLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        headlineLabel.setWrapText(true);
        headlineLabel.setMaxWidth(250);

        // Spacer to push app info to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // App info bar at bottom
        HBox appInfoBar = new HBox(10);
        appInfoBar.setAlignment(Pos.CENTER_LEFT);
        appInfoBar.setPadding(new Insets(10, 12, 10, 12));
        appInfoBar.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 10px;");

        // App icon (circular)
        StackPane iconCircle = new StackPane();
        iconCircle.setPrefSize(32, 32);
        iconCircle.setMinSize(32, 32);
        iconCircle.setMaxSize(32, 32);

        // Set icon background color based on app
        String iconBgColor = switch (gradientType) {
            case BLUE -> "#0078d4";
            case PINK_PURPLE -> "#1e1e1e";
            case PURPLE_ORANGE -> "#4a154b";
        };

        Circle bgCircle = new Circle(16);
        bgCircle.setFill(Color.web(iconBgColor));

        FontIcon appIcon = new FontIcon(appIconType);
        appIcon.setIconSize(16);
        appIcon.setIconColor(Color.WHITE);
        iconCircle.getChildren().addAll(bgCircle, appIcon);

        // App name and category
        VBox appTextBox = new VBox(2);
        HBox.setHgrow(appTextBox, Priority.ALWAYS);

        Label appNameLabel = new Label(appName);
        appNameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label categoryLabel = new Label(category);
        categoryLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 10px;");

        appTextBox.getChildren().addAll(appNameLabel, categoryLabel);

        // GET button
        Button getButton = new Button("GET");
        getButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 14px; -fx-padding: 6 16; -fx-font-size: 11px; -fx-cursor: hand;");

        appInfoBar.getChildren().addAll(iconCircle, appTextBox, getButton);

        content.getChildren().addAll(featuredLabel, headlineLabel, spacer, appInfoBar);

        getChildren().add(content);
    }

    // Legacy constructor for API data
    public HeroCard(String title, String description, boolean isInstalled) {
        this("Featured App", title, description, GradientType.BLUE, Feather.BOX);
    }

    public HeroCard(String title, String description, boolean isInstalled, GradientType gradientType) {
        this("Featured App", title, description, gradientType, Feather.BOX);
    }

    private String getGradientStyle(GradientType type) {
        return switch (type) {
            case BLUE -> "-fx-background-color: linear-gradient(to right, #0891b2, #06b6d4, #22d3ee);";
            case PINK_PURPLE -> "-fx-background-color: linear-gradient(to right, #db2777, #a855f7, #6366f1);";
            case PURPLE_ORANGE ->
                "-fx-background-color: linear-gradient(to bottom right, #7c3aed, #a855f7, #f59e0b, #fbbf24);";
        };
    }
}
