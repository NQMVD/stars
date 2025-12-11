package com.example.appstore.views;

import com.example.appstore.components.AppListRow;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

public class UpdatesView extends ScrollPane {

    public UpdatesView() {
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        VBox content = new VBox(32);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: #09090b;");
        
        // Header
        HBox header = new HBox(16);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(4);
        Label title = new Label("Updates");
        title.getStyleClass().add("h1");
        Label subtitle = new Label("3 updates available");
        subtitle.getStyleClass().add("text-muted");
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #27272a; -fx-text-fill: white; -fx-background-radius: 6px; -fx-cursor: hand;");
        refreshBtn.setGraphic(new FontIcon(Feather.REFRESH_CW));
        
        Button updateAllBtn = new Button("Update All");
        updateAllBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-cursor: hand;");
        updateAllBtn.setGraphic(new FontIcon(Feather.DOWNLOAD));
        
        header.getChildren().addAll(titleBox, spacer, refreshBtn, updateAllBtn);
        
        // Available Updates
        VBox updatesList = new VBox(8);
        updatesList.getChildren().add(new AppListRow("Visual Studio Code", "Microsoft", "v1.85.0", "95 MB", true));
        updatesList.getChildren().add(new AppListRow("Discord", "Discord Inc.", "v0.0.292", "160 MB", true));
        updatesList.getChildren().add(new AppListRow("1Password", "AgileBits", "v8.10.18", "85 MB", true));
        
        // Recently Updated
        VBox recentSection = new VBox(16);
        Label recentTitle = new Label("Recently Updated");
        recentTitle.getStyleClass().add("h2");
        
        VBox recentList = new VBox(8);
        recentList.getChildren().add(new AppListRow("Slack", "Salesforce", "v4.35.126", "Updated 3 days ago", false));
        recentList.getChildren().add(new AppListRow("Figma", "Figma, Inc.", "v124.0", "Updated 3 days ago", false));
        
        recentSection.getChildren().addAll(recentTitle, recentList);
        
        content.getChildren().addAll(header, updatesList, recentSection);
        setContent(content);
    }
}
