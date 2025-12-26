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

public class HeroCard extends VBox {

    public HeroCard(String title, String description, boolean isInstalled) {
        getStyleClass().add("app-card");
        setPrefSize(350, 300); // Taller to accommodate image + text
        setMinSize(350, 300);
        setPadding(new Insets(0));

        // Large Image Area
        StackPane imageArea = new StackPane();
        imageArea.setPrefHeight(180);
        imageArea.setStyle(
            "-fx-background-color: #27272a; -fx-background-radius: 12px 12px 0 0;"
        );

        FontIcon bgIcon = new FontIcon(Feather.IMAGE);
        bgIcon.setIconSize(64);
        bgIcon.setIconColor(Color.web("#3f3f46"));
        imageArea.getChildren().add(bgIcon);

        // Content Area
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        VBox.setVgrow(content, Priority.ALWAYS);

        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setStyle(
            "-fx-background-color: #27272a; -fx-background-radius: 10px;"
        );
        iconBox.setPrefSize(48, 48);
        FontIcon appIcon = new FontIcon(Feather.BOX);
        appIcon.setIconColor(Color.WHITE);
        appIcon.setIconSize(24);
        iconBox.getChildren().add(appIcon);

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px;"
        );
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 13px;");
        descLabel.setWrapText(true);
        titleBox.getChildren().addAll(titleLabel, descLabel);

        header.getChildren().addAll(iconBox, titleBox);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer Actions
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);

        if (isInstalled) {
            HBox badge = new HBox(6);
            badge.setAlignment(Pos.CENTER);
            badge.getStyleClass().add("installed-badge");
            FontIcon check = new FontIcon(Feather.CHECK);
            check.setIconSize(14);
            check.setIconColor(Color.WHITE);
            Label badgeText = new Label("Installed");
            badgeText.setTextFill(Color.WHITE);
            badgeText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            badge.getChildren().addAll(check, badgeText);
            footer.getChildren().add(badge);
        } else {
            Button installBtn = new Button("Install");
            installBtn.getStyleClass().add("install-button");
            installBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 13px;");

            installBtn.setOnAction(e -> {
                installBtn.setText("Installing...");
                installBtn.setDisable(true);

                // Mock delay
                javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(1.5)
                    );
                pause.setOnFinished(ev -> {
                    footer.getChildren().remove(installBtn);
                    Button openBtn = new Button("Open");
                    openBtn.setStyle(
                        "-fx-background-color: #27272a; -fx-text-fill: white; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20; -fx-font-size: 13px;"
                    );
                    openBtn.setOnAction(openEvent -> {
                        // TODO: Implement app launch functionality
                    });
                    footer.getChildren().add(openBtn);
                });
                pause.play();
            });

            footer.getChildren().add(installBtn);
        }

        content.getChildren().addAll(header, spacer, footer);

        getChildren().addAll(imageArea, content);
    }
}
