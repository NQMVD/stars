package com.example.appstore.views;

import com.example.appstore.model.InstalledApp;
import com.example.appstore.service.LibraryService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryView extends ScrollPane {

    private final Map<String, Button> tabButtons = new HashMap<>();
    private final Map<String, Node> tabContentNodes = new HashMap<>();
    private final VBox contentContainer = new VBox(8);
    private final LibraryService libraryService;
    private final Label subtitleLabel;

    public LibraryView() {
        libraryService = LibraryService.getInstance();
        
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        VBox mainLayout = new VBox(24);
        mainLayout.setPadding(new Insets(32));
        mainLayout.setStyle("-fx-background-color: #09090b;");
        
        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(4);
        Label title = new Label("My Library");
        title.getStyleClass().add("h1");
        subtitleLabel = new Label(libraryService.getInstalledCount() + " apps installed");
        subtitleLabel.getStyleClass().add("text-muted");
        titleBox.getChildren().addAll(title, subtitleLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #27272a; -fx-text-fill: white; -fx-background-radius: 6px; -fx-cursor: hand;");
        refreshBtn.setGraphic(new FontIcon(Feather.REFRESH_CW));
        refreshBtn.setOnAction(e -> refreshLibrary());
        
        header.getChildren().addAll(titleBox, spacer, refreshBtn);
        
        // Tabs
        HBox tabs = new HBox(16);
        tabs.getChildren().addAll(
            createTab("Installed"),
            createTab("Purchases"),
            createTab("Wishlist")
        );
        
        // Content Sections - will be populated dynamically
        
        // 2. Purchases (Placeholder)
        VBox purchasesList = new VBox(24);
        purchasesList.setAlignment(Pos.CENTER);
        Label purchasesLabel = new Label("No purchases found");
        purchasesLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 14px;");
        purchasesList.getChildren().add(purchasesLabel);
        
        // 3. Wishlist (Placeholder)
        VBox wishlistList = new VBox(24);
        wishlistList.setAlignment(Pos.CENTER);
        Label wishlistLabel = new Label("Your wishlist is empty");
        wishlistLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 14px;");
        wishlistList.getChildren().add(wishlistLabel);
        
        // Register Content
        tabContentNodes.put("Purchases", purchasesList);
        tabContentNodes.put("Wishlist", wishlistList);
        
        // Assemble
        mainLayout.getChildren().addAll(header, tabs, contentContainer);
        setContent(mainLayout);
        
        // Initialize
        switchTab("Installed");
    }
    
    private void refreshLibrary() {
        libraryService.loadLibrary();
        subtitleLabel.setText(libraryService.getInstalledCount() + " apps installed");
        switchTab("Installed");
    }
    
    private VBox buildInstalledList() {
        VBox installedList = new VBox(8);
        List<InstalledApp> apps = libraryService.getInstalledApps();
        
        if (apps.isEmpty()) {
            Label emptyLabel = new Label("No apps installed yet. Install apps from the Discover tab.");
            emptyLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 14px;");
            installedList.setAlignment(Pos.CENTER);
            installedList.setPadding(new Insets(48));
            installedList.getChildren().add(emptyLabel);
        } else {
            for (InstalledApp app : apps) {
                installedList.getChildren().add(createLibraryRow(app));
            }
        }
        
        return installedList;
    }
    
    private HBox createLibraryRow(InstalledApp app) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: #18181b; -fx-background-radius: 8px; -fx-border-color: #27272a; -fx-border-radius: 8px;");
        
        // App info
        VBox info = new VBox(2);
        Label nameLabel = new Label(app.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label developerLabel = new Label(app.getDeveloper());
        developerLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
        
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label versionLabel = new Label("v" + app.getInstalledVersion());
        versionLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");
        Label sizeLabel = new Label(app.getSize());
        sizeLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");
        metaRow.getChildren().addAll(versionLabel, new Label("•"), sizeLabel);
        ((Label)metaRow.getChildren().get(1)).setStyle("-fx-text-fill: #71717a;");
        
        info.getChildren().addAll(nameLabel, developerLabel, metaRow);
        HBox.setHgrow(info, Priority.ALWAYS);
        
        // Buttons
        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6px; -fx-cursor: hand;");
        updateBtn.setGraphic(new FontIcon(Feather.REFRESH_CW));
        updateBtn.setOnAction(e -> {
            updateBtn.setDisable(true);
            updateBtn.setText("Updating...");
            
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                libraryService.updateApp(app.getId());
                updateBtn.setText("Updated!");
                updateBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6px;");
                
                Timeline refresh = new Timeline(new KeyFrame(Duration.seconds(0.5), r -> refreshLibrary()));
                refresh.play();
            }));
            timeline.play();
        });
        
        Button openBtn = new Button("Open");
        openBtn.setStyle("-fx-background-color: #27272a; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6px; -fx-cursor: hand;");
        
        Button removeBtn = new Button("Remove");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 6px; -fx-border-color: #ef4444; -fx-border-radius: 6px; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            removeBtn.setDisable(true);
            removeBtn.setText("Removing...");
            
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.3), ev -> {
                libraryService.removeApp(app.getId());
                refreshLibrary();
            }));
            timeline.play();
        });
        
        row.getChildren().addAll(info, updateBtn, openBtn, removeBtn);
        return row;
    }
    
    private void switchTab(String tabName) {
        // Update Buttons
        tabButtons.forEach((name, btn) -> {
            if (name.equals(tabName)) {
                btn.setStyle("-fx-background-color: #27272a; -fx-text-fill: white; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a1a1aa; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16;");
            }
        });
        
        // Update Content
        contentContainer.getChildren().clear();
        
        if (tabName.equals("Installed")) {
            contentContainer.getChildren().add(buildInstalledList());
        } else {
            Node content = tabContentNodes.get(tabName);
            if (content != null) {
                contentContainer.getChildren().add(content);
            }
        }
    }
    
    private Button createTab(String text) {
        Button btn = new Button(text);
        btn.setOnAction(e -> switchTab(text));
        tabButtons.put(text, btn);
        return btn;
    }
}
