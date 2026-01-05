package com.example.appstore.components;

import com.example.appstore.model.App;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class HeroCard extends VBox {

    private final App app;

    public HeroCard(App app, boolean isInstalled) {
        this.app = app;
        getStyleClass().add("app-card");
        setPrefSize(350, 300);
        setMinSize(350, 300);
        setPadding(new Insets(0));

        StackPane imageArea = new StackPane();
        imageArea.setPrefHeight(180);
        imageArea.setStyle(
            "-fx-background-color: #27272a; -fx-background-radius: 12px 12px 0 0;"
        );

        FontIcon bgIcon = new FontIcon(Feather.IMAGE);
        bgIcon.setIconSize(64);
        bgIcon.setIconColor(Color.web("#3f3f46"));
        imageArea.getChildren().add(bgIcon);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        VBox.setVgrow(content, Priority.ALWAYS);

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setStyle(
            "-fx-background-color: #27272a; -fx-background-radius: 10px;"
        );
        iconBox.setPrefSize(48, 48);

        if (app.getOwnerAvatarUrl() != null && !app.getOwnerAvatarUrl().isEmpty()) {
            ImageView avatarView = new ImageView();
            try {
                Image avatarImage = new Image(app.getOwnerAvatarUrl(), 48, 48, true, true);
                avatarView.setImage(avatarImage);
                avatarView.setFitWidth(48);
                avatarView.setFitHeight(48);
                Circle clip = new Circle(24, 24, 24);
                avatarView.setClip(clip);
                iconBox.getChildren().add(avatarView);
            } catch (Exception e) {
                FontIcon appIcon = new FontIcon(Feather.BOX);
                appIcon.setIconColor(Color.WHITE);
                appIcon.setIconSize(24);
                iconBox.getChildren().add(appIcon);
            }
        } else {
            FontIcon appIcon = new FontIcon(Feather.BOX);
            appIcon.setIconColor(Color.WHITE);
            appIcon.setIconSize(24);
            iconBox.getChildren().add(appIcon);
        }

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label(app.getName());
        titleLabel.setStyle(
            "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px;"
        );
        Label descLabel = new Label(app.getDescription() != null ? app.getDescription() : "No description");
        descLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 13px;");
        descLabel.setWrapText(true);

        HBox starsRow = new HBox(6);
        starsRow.setAlignment(Pos.CENTER_LEFT);
        FontIcon star = new FontIcon(Feather.STAR);
        star.setIconColor(Color.web("#fbbf24"));
        star.setIconSize(14);
        Label starsLabel = new Label(app.getFormattedStars() + " stars");
        starsLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
        starsRow.getChildren().addAll(star, starsLabel);

        titleBox.getChildren().addAll(titleLabel, descLabel, starsRow);

        header.getChildren().addAll(iconBox, titleBox);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

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

    public App getApp() {
        return app;
    }
}
