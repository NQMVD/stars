package com.example.appstore.components;

import com.example.appstore.model.App;
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
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Claude-style featured app card with terracotta accents and GitHub icon
 * overlay.
 */
public class ClaudeFeaturedCard extends StackPane {

    private final App app;
    private final Runnable onClick;

    public ClaudeFeaturedCard(App app, Runnable onClick) {
        this.app = app;
        this.onClick = onClick;

        setStyle(
                "-fx-background-color: #161616; " +
                        "-fx-border-color: #2a2a2a; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 12px; " +
                        "-fx-background-radius: 12px;");
        setPadding(new Insets(24));
        setCursor(javafx.scene.Cursor.HAND);
        setMinHeight(180);

        // Content container (z-index: 10)
        VBox content = new VBox(16);
        content.setAlignment(Pos.TOP_LEFT);

        // Header row: Featured badge + Stars
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label featuredBadge = new Label("FEATURED");
        featuredBadge.setStyle(
                "-fx-background-color: #d977571a; " +
                        "-fx-text-fill: #d97757; " +
                        "-fx-font-size: 10px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 2px 8px; " +
                        "-fx-background-radius: 4px;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        // Stars display
        HBox starsBox = new HBox(4);
        starsBox.setAlignment(Pos.CENTER);
        FontIcon starIcon = new FontIcon(Feather.STAR);
        starIcon.setIconSize(12);
        starIcon.setIconColor(Color.web("#666666"));
        Label starsLabel = new Label(getStarsDisplay());
        starsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        starsBox.getChildren().addAll(starIcon, starsLabel);

        headerRow.getChildren().addAll(featuredBadge, headerSpacer, starsBox);

        // Title
        Label titleLabel = new Label(app.getName());
        titleLabel.setStyle(
                "-fx-font-size: 20px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-text-fill: #f0f0f0;");

        // Description
        Label descriptionLabel = new Label(
                app.getDescription() != null ? app.getDescription() : "No description available");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxHeight(60);
        descriptionLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 14px;");

        // Spacer to push button to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Install button
        HBox buttonRow = new HBox();
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        Button installBtn = new Button("Installieren");
        FontIcon downloadIcon = new FontIcon(Feather.DOWNLOAD);
        downloadIcon.setIconSize(14);
        downloadIcon.setIconColor(Color.web("#111111"));
        installBtn.setGraphic(downloadIcon);
        installBtn.setGraphicTextGap(8);
        installBtn.setStyle(
                "-fx-background-color: #f0f0f0; " +
                        "-fx-text-fill: #111111; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-padding: 6px 16px; " +
                        "-fx-cursor: hand;");

        installBtn.setOnMouseEntered(e -> installBtn.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-text-fill: #111111; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-padding: 6px 16px; " +
                        "-fx-cursor: hand;"));

        installBtn.setOnMouseExited(e -> installBtn.setStyle(
                "-fx-background-color: #f0f0f0; " +
                        "-fx-text-fill: #111111; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-padding: 6px 16px; " +
                        "-fx-cursor: hand;"));

        installBtn.setOnAction(e -> {
            e.consume(); // Don't trigger card click
            if (onClick != null)
                onClick.run();
        });

        buttonRow.getChildren().add(installBtn);

        content.getChildren().addAll(headerRow, titleLabel, descriptionLabel, spacer, buttonRow);

        // Background GitHub icon (subtle accent)
        FontIcon bgIcon = new FontIcon(Feather.GITHUB);
        bgIcon.setIconSize(120);
        bgIcon.setIconColor(Color.web("#ffffff08"));
        StackPane.setAlignment(bgIcon, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(bgIcon, new Insets(0, -16, -16, 0));

        getChildren().addAll(bgIcon, content);

        // Hover effects on card
        setOnMouseEntered(e -> {
            setStyle(
                    "-fx-background-color: #161616; " +
                            "-fx-border-color: #444444; " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 12px; " +
                            "-fx-background-radius: 12px;");
            bgIcon.setIconColor(Color.web("#ffffff14"));
            titleLabel.setStyle(
                    "-fx-font-size: 20px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-text-fill: #d97757;");
        });

        setOnMouseExited(e -> {
            setStyle(
                    "-fx-background-color: #161616; " +
                            "-fx-border-color: #2a2a2a; " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 12px; " +
                            "-fx-background-radius: 12px;");
            bgIcon.setIconColor(Color.web("#ffffff08"));
            titleLabel.setStyle(
                    "-fx-font-size: 20px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-text-fill: #f0f0f0;");
        });

        setOnMouseClicked(e -> {
            if (onClick != null)
                onClick.run();
        });
    }

    private String getStarsDisplay() {
        // Placeholder - in real implementation, fetch from API
        return "12.4k";
    }

    public App getApp() {
        return app;
    }
}
