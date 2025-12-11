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
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

public class AppListRow extends HBox {

    public AppListRow(String title, String vendor, String version, String size, boolean isUpdate) {
        getStyleClass().add("app-card"); // Reuse card style for background/border
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(16));
        setSpacing(16);
        
        // Icon
        StackPane iconBox = new StackPane();
        iconBox.setStyle("-fx-background-color: #27272a; -fx-background-radius: 8px;");
        iconBox.setPrefSize(48, 48);
        FontIcon appIcon = new FontIcon(Feather.BOX);
        appIcon.setIconColor(Color.WHITE);
        appIcon.setIconSize(24);
        iconBox.getChildren().add(appIcon);
        
        // Info
        VBox info = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        
        HBox meta = new HBox(8);
        Label vendorLabel = new Label(vendor);
        vendorLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
        
        Label versionLabel = new Label(version + " • " + size);
        versionLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12px;");
        
        if (isUpdate) {
             // Show update info
             Label updateArrow = new Label("→");
             updateArrow.setTextFill(Color.web("#71717a"));
             Label newVersion = new Label("v2.0.0"); // Mock
             newVersion.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
             meta.getChildren().addAll(vendorLabel, versionLabel, updateArrow, newVersion);
        } else {
             meta.getChildren().addAll(vendorLabel, versionLabel);
        }
        
        info.getChildren().addAll(titleLabel, meta);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        if (isUpdate) {
            Button updateBtn = new Button("Update");
            updateBtn.getStyleClass().add("install-button");
            updateBtn.setGraphic(new FontIcon(Feather.DOWNLOAD_CLOUD));
            actions.getChildren().add(updateBtn);
        } else {
            Button updateBtn = new Button("Update");
            updateBtn.setStyle("-fx-background-color: #27272a; -fx-text-fill: white; -fx-background-radius: 6px; -fx-cursor: hand;");
            updateBtn.setGraphic(new FontIcon(Feather.REFRESH_CW));
            
            Button openBtn = new Button("Open");
            openBtn.setStyle("-fx-background-color: #27272a; -fx-text-fill: white; -fx-background-radius: 6px; -fx-cursor: hand;");
            
            Button deleteBtn = new Button();
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            FontIcon trash = new FontIcon(Feather.TRASH_2);
            trash.setIconColor(Color.web("#71717a"));
            deleteBtn.setGraphic(trash);
            
            actions.getChildren().addAll(updateBtn, openBtn, deleteBtn);
        }
        
        getChildren().addAll(iconBox, info, spacer, actions);
    }
}
