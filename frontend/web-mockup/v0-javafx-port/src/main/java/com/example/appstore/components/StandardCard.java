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

    public StandardCard(
        String title,
        String vendor,
        String rating,
        String count,
        boolean isInstalled,
        Runnable onClick
    ) {
        this.title = title;
        this.vendor = vendor;
        getStyleClass().add("app-card");
        setPrefWidth(280);
        setMinSize(240, 280);
        setPadding(new Insets(16));
        setCursor(javafx.scene.Cursor.HAND);

        setOnMouseClicked(e -> {
            if (onClick != null) onClick.run();
        });
        setPadding(new Insets(0)); // Padding handled internally

        // Top Image Placeholder
        StackPane imageArea = new StackPane();
        imageArea.setPrefHeight(140);
        imageArea.setStyle(
            "-fx-background-color: #27272a; -fx-background-radius: 12px 12px 0 0;"
        );
        FontIcon imgIcon = new FontIcon(Feather.IMAGE);
        imgIcon.setIconColor(Color.web("#3f3f46"));
        imageArea.getChildren().add(imgIcon);

        // Content Area
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        // Header (Icon + Title)
        HBox header = new HBox(12);
        StackPane iconBox = new StackPane();
        iconBox.setStyle(
            "-fx-background-color: #27272a; -fx-background-radius: 8px;"
        );
        iconBox.setPrefSize(40, 40);
        FontIcon appIcon = new FontIcon(Feather.BOX);
        appIcon.setIconColor(Color.WHITE);
        iconBox.getChildren().add(appIcon);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;"
        );
        Label vendorLabel = new Label(vendor);
        vendorLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
        titleBox.getChildren().addAll(titleLabel, vendorLabel);

        header.getChildren().addAll(iconBox, titleBox);

        // Description/Tagline (Optional, skipping for density)

        // Footer (Rating + Button)
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox ratingBox = new VBox(4);
        HBox stars = new HBox(2);
        FontIcon star = new FontIcon(Feather.STAR);
        star.setIconColor(Color.web("#fbbf24")); // Amber 400
        star.setIconSize(12);
        Label rateLabel = new Label(rating);
        rateLabel.setStyle(
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;"
        );
        Label countLabel = new Label("(" + count + ")");
        countLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");

        HBox rateRow = new HBox(4);
        rateRow.setAlignment(Pos.CENTER_LEFT);
        rateRow.getChildren().addAll(star, rateLabel, countLabel);

        Label freeLabel = new Label("Free");
        freeLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");

        ratingBox.getChildren().addAll(rateRow, freeLabel);

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
                javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(1.5)
                    );
                pause.setOnFinished(ev -> {
                    actionBtnContainer.getChildren().clear();
                    Button openBtn = new Button("Open");
                    openBtn.setStyle(
                        "-fx-background-color: #27272a; -fx-text-fill: white; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-cursor: hand;"
                    );
                    openBtn.setOnAction(openEvent ->
                        System.out.println("Opening " + title)
                    );
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
}
