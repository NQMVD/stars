package com.example.appstore.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

public class AppCard extends VBox {

    public AppCard(String title, String description, String iconPath) {
        getStyleClass().add("app-card");
        setSpacing(12);
        setPrefWidth(200);
        setMinWidth(200);
        setMaxWidth(200);
        
        // Icon
        ImageView iconView = new ImageView();
        try {
            // Try to load from resources, fallback to placeholder if fails
            String imagePath = "/images/" + iconPath;
            if (getClass().getResource(imagePath) != null) {
                iconView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
            } else {
                // Fallback or use a colored rectangle
                // iconView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.svg")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        iconView.setFitWidth(64);
        iconView.setFitHeight(64);
        
        // Clip icon to rounded rect
        Rectangle clip = new Rectangle(64, 64);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        iconView.setClip(clip);

        // Text Content
        VBox textContainer = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
        
        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.getStyleClass().add("text-muted");
        descLabel.setStyle("-fx-font-size: 12px;");
        
        textContainer.getChildren().addAll(titleLabel, descLabel);
        
        getChildren().addAll(iconView, textContainer);
    }
}
