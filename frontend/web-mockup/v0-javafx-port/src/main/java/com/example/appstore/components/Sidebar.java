package com.example.appstore.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

import java.util.function.Consumer;

public class Sidebar extends VBox {

    private final Consumer<String> onNavigate;

    public Sidebar(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        getStyleClass().add("sidebar");
        
        // Header (Logo)
        HBox header = new HBox(12);
        header.getStyleClass().add("sidebar-header");
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Logo Icon (White rounded square with icon)
        HBox logoIcon = new HBox();
        logoIcon.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-min-width: 32px; -fx-min-height: 32px;");
        logoIcon.setAlignment(Pos.CENTER);
        FontIcon appIcon = new FontIcon(Feather.BOX); // Placeholder for AppVault logo
        appIcon.setIconColor(Color.BLACK);
        appIcon.setIconSize(20);
        logoIcon.getChildren().add(appIcon);
        
        Label title = new Label("AppVault");
        title.getStyleClass().add("sidebar-logo-text");
        
        header.getChildren().addAll(logoIcon, title);
        getChildren().add(header);

        // Navigation Items
        addNavButton("Discover", Feather.HOME, true);
        addNavButton("Library", Feather.DOWNLOAD, false);
        addNavButton("Updates", Feather.REFRESH_CW, false, 3); // Badge: 3
        addNavButton("Settings", Feather.SETTINGS, false);
        
        // Categories
        addSectionLabel("CATEGORIES");
        addNavButton("Developer Tools", Feather.CODE, false);
        addNavButton("Productivity", Feather.BRIEFCASE, false);
        addNavButton("Graphics & Design", Feather.PEN_TOOL, false);
        addNavButton("Games", Feather.PLAY, false); // Feather doesn't have gamepad, using PLAY
        addNavButton("Music & Audio", Feather.MUSIC, false);
        addNavButton("Video", Feather.VIDEO, false);
        addNavButton("Utilities", Feather.TOOL, false); // Feather doesn't have wrench/tool? It has TOOL
        addNavButton("Security", Feather.SHIELD, false);
        
        // Spacer to push User Profile to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);
        
        // User Profile
        HBox userProfile = new HBox(12);
        userProfile.getStyleClass().add("user-profile");
        
        Circle avatar = new Circle(16, Color.web("#ef4444")); // Red avatar
        Label avatarText = new Label("N");
        avatarText.setTextFill(Color.WHITE);
        avatarText.setStyle("-fx-font-weight: bold;");
        javafx.scene.layout.StackPane avatarPane = new javafx.scene.layout.StackPane(avatar, avatarText);
        
        VBox userInfo = new VBox(2);
        Label userName = new Label("John Doe");
        userName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        Label userPlan = new Label("Free Plan");
        userPlan.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");
        userInfo.getChildren().addAll(userName, userPlan);
        
        userProfile.getChildren().addAll(avatarPane, userInfo);
        getChildren().add(userProfile);
    }

    private void addSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("sidebar-section-label");
        getChildren().add(label);
    }

    private void addNavButton(String text, Feather icon, boolean isSelected) {
        addNavButton(text, icon, isSelected, 0);
    }

    private void addNavButton(String text, Feather icon, boolean isSelected, int badgeCount) {
        HBox button = new HBox(12);
        button.getStyleClass().add("nav-button");
        if (isSelected) {
            button.setStyle("-fx-text-fill: white;"); // Selected style handled by CSS hover mostly, but can force here
        }
        
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(18);
        
        Label label = new Label(text);
        HBox.setHgrow(label, Priority.ALWAYS);
        
        button.getChildren().addAll(fontIcon, label);
        
        if (badgeCount > 0) {
            Label badge = new Label(String.valueOf(badgeCount));
            badge.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-background-radius: 10px; -fx-padding: 0 6px; -fx-font-size: 11px; -fx-font-weight: bold;");
            button.getChildren().add(badge);
        }
        
        getChildren().add(button);
        
        button.setOnMouseClicked(e -> {
            System.out.println("Clicked: " + text);
            if (onNavigate != null) onNavigate.accept(text);
        });
    }
}
