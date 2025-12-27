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
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class StandardCard extends VBox {

    private final String title;
    private final String vendor;

    public enum GradientType {
        BLUE, PURPLE, PINK, ORANGE, TEAL, INDIGO, DEFAULT
    }

    // Original constructor for backward compatibility
    public StandardCard(
            String title,
            String vendor,
            String rating,
            String count,
            boolean isInstalled,
            Runnable onClick) {
        this(title, vendor, rating, count, isInstalled, onClick, GradientType.DEFAULT);
    }

    // New constructor with gradient support
    public StandardCard(
            String title,
            String vendor,
            String rating,
            String count,
            boolean isInstalled,
            Runnable onClick,
            GradientType gradientType) {
        this.title = title;
        this.vendor = vendor;
        getStyleClass().add("app-card");
        setMinWidth(180);
        setMaxWidth(Double.MAX_VALUE); // Allow horizontal growth
        setMinHeight(240);
        setCursor(javafx.scene.Cursor.HAND);

        setOnMouseClicked(e -> {
            if (onClick != null)
                onClick.run();
        });
        setPadding(new Insets(0)); // Padding handled internally

        // Top Image Area with Gradient
        StackPane imageArea = new StackPane();
        imageArea.setPrefHeight(140);

        String gradientStyle = getGradientStyle(gradientType);
        imageArea.setStyle(gradientStyle + " -fx-background-radius: 16px 16px 0 0;");

        FontIcon imgIcon = new FontIcon(Feather.IMAGE);
        imgIcon.setIconColor(Color.web("#ffffff33"));
        imageArea.getChildren().add(imgIcon);

        // Content Area
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        // Header (Icon + Title)
        HBox header = new HBox(12);
        StackPane iconBox = new StackPane();
        iconBox.setStyle(
                "-fx-background-color: #27272a; -fx-background-radius: 8px;");
        iconBox.setPrefSize(40, 40);
        FontIcon appIcon = new FontIcon(Feather.BOX);
        appIcon.setIconColor(Color.WHITE);
        iconBox.getChildren().add(appIcon);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        Label vendorLabel = new Label(vendor);
        vendorLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
        titleBox.getChildren().addAll(titleLabel, vendorLabel);

        header.getChildren().addAll(iconBox, titleBox);

        // Description/Tagline (Optional, skipping for density)

        // Footer (Rating + Button)
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox ratingBox = new VBox(4);
        FontIcon star = new FontIcon(Feather.STAR);
        star.setIconColor(Color.web("#fbbf24")); // Amber 400
        star.setIconSize(12);
        Label rateLabel = new Label(rating);
        rateLabel.setStyle(
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        HBox rateRow = new HBox(4);
        rateRow.setAlignment(Pos.CENTER_LEFT);
        rateRow.getChildren().addAll(star, rateLabel);

        // Editor's Choice badge
        HBox editorsChoiceRow = new HBox(4);
        editorsChoiceRow.setAlignment(Pos.CENTER_LEFT);
        FontIcon checkIcon = new FontIcon(Feather.CHECK_CIRCLE);
        checkIcon.setIconColor(Color.web("#22d3ee"));
        checkIcon.setIconSize(10);
        Label editorsChoiceLabel = new Label("Editor's Choice");
        editorsChoiceLabel.setStyle("-fx-text-fill: #22d3ee; -fx-font-size: 10px;");
        editorsChoiceRow.getChildren().addAll(checkIcon, editorsChoiceLabel);

        ratingBox.getChildren().addAll(rateRow, editorsChoiceRow);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action Button
        HBox actionBtnContainer = new HBox();
        if (isInstalled) {
            HBox badge = new HBox(4);
            badge.setAlignment(Pos.CENTER);
            badge.getStyleClass().add("installed-badge");
            FontIcon check = new FontIcon(Feather.CHECK);
            check.setIconSize(12);
            check.setIconColor(Color.WHITE);
            Label badgeText = new Label("Installed");
            badgeText.setTextFill(Color.WHITE);
            badgeText.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            badge.getChildren().addAll(check, badgeText);
            actionBtnContainer.getChildren().add(badge);
        } else {
            Button installBtn = new Button("Install");
            installBtn.getStyleClass().add("install-button");
            // Add download icon
            FontIcon dlIcon = new FontIcon(Feather.DOWNLOAD);
            dlIcon.setIconSize(12);
            installBtn.setGraphic(dlIcon);

            installBtn.setOnAction(e -> {
                installBtn.setText("Installing...");
                installBtn.setDisable(true);

                // Mock delay
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(1.5));
                pause.setOnFinished(ev -> {
                    actionBtnContainer.getChildren().clear();
                    Button openBtn = new Button("Open");
                    openBtn.setStyle(
                            "-fx-background-color: #27272a; -fx-text-fill: white; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-cursor: hand;");
                    openBtn.setOnAction(openEvent -> {
                        // TODO: Implement app launch functionality
                    });
                    actionBtnContainer.getChildren().add(openBtn);
                });
                pause.play();
            });

            actionBtnContainer.getChildren().add(installBtn);
        }

        footer.getChildren().addAll(ratingBox, spacer, actionBtnContainer);

        content.getChildren().addAll(header, footer);

        getChildren().addAll(imageArea, content);
    }

    public String getTitle() {
        return title;
    }

    public String getVendor() {
        return vendor;
    }

    private String getGradientStyle(GradientType type) {
        return switch (type) {
            case BLUE -> "-fx-background-color: linear-gradient(to bottom right, #1e40af, #3b82f6, #60a5fa);";
            case PURPLE -> "-fx-background-color: linear-gradient(to bottom right, #7c3aed, #a855f7, #c084fc);";
            case PINK -> "-fx-background-color: linear-gradient(to bottom right, #be185d, #ec4899, #f472b6);";
            case ORANGE -> "-fx-background-color: linear-gradient(to bottom right, #c2410c, #f97316, #fb923c);";
            case TEAL -> "-fx-background-color: linear-gradient(to bottom right, #0d9488, #14b8a6, #2dd4bf);";
            case INDIGO -> "-fx-background-color: linear-gradient(to bottom right, #4338ca, #6366f1, #818cf8);";
            case DEFAULT -> "-fx-background-color: linear-gradient(to bottom right, #374151, #4b5563, #6b7280);";
        };
    }
}
