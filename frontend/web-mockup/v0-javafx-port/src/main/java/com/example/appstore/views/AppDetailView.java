package com.example.appstore.views;

import com.example.appstore.model.App;
import com.example.appstore.model.GithubRelease;
import com.example.appstore.service.ApiService;
import com.example.appstore.service.LibraryService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

import java.util.HashMap;
import java.util.Map;

public class AppDetailView extends ScrollPane {

    private final App app;
    private final Runnable onBack;
    private final Map<String, Button> tabButtons = new HashMap<>();
    private final Map<String, Node> tabContentNodes = new HashMap<>();
    private final VBox contentContainer = new VBox(32);
    
    // Labels to update with API data
    private Label versionLabel;
    private Label descriptionLabel;

    public AppDetailView(App app, Runnable onBack) {
        this.app = app;
        this.onBack = onBack;
        getStyleClass().add("edge-to-edge-scroll-pane");
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        
        VBox mainLayout = new VBox(32);
        mainLayout.setPadding(new Insets(32, 48, 48, 48));
        mainLayout.getStyleClass().add("content-area");
        mainLayout.setStyle("-fx-background-color: #010101;");

        // --- Header Section ---
        VBox headerSection = new VBox(24);
        
        // Back Button
        Button backBtn = new Button("Back");
        FontIcon backIcon = new FontIcon(Feather.ARROW_LEFT);
        backIcon.setIconColor(Color.web("#fafafa"));
        backIcon.setIconSize(16);
        backBtn.setGraphic(backIcon);
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #fafafa; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0;");
        backBtn.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });
        
        // App Info Header
        HBox appHeader = new HBox(24);
        appHeader.setAlignment(Pos.CENTER_LEFT);
        
        // Large Icon
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(128, 128);
        iconBox.setMinSize(128, 128);
        iconBox.setStyle("-fx-background-color: #27272a; -fx-background-radius: 24px;");
        FontIcon appIcon = new FontIcon(Feather.BOX);
        appIcon.setIconSize(64);
        appIcon.setIconColor(Color.WHITE);
        iconBox.getChildren().add(appIcon);
        
        // Title & Metadata
        VBox metaBox = new VBox(8);
        HBox.setHgrow(metaBox, Priority.ALWAYS);
        
        Label titleLabel = new Label(app.getName());
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");
        
        Label vendorLabel = new Label(app.getOwnerLogin());
        vendorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #a1a1aa;");
        
        HBox platforms = new HBox(8);
        platforms.getChildren().addAll(
            createPlatformBadge(Feather.MONITOR),
            createPlatformBadge(Feather.COMMAND),
            createPlatformBadge(Feather.TERMINAL)
        );
        
        metaBox.getChildren().addAll(titleLabel, vendorLabel, platforms);
        
        // Action Buttons
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        LibraryService libraryService = LibraryService.getInstance();
        boolean isInstalled = libraryService.isInstalled(app.getId());
        
        Button installBtn = new Button(isInstalled ? "Open" : "Install");
        FontIcon dlIcon = new FontIcon(isInstalled ? Feather.CHECK : Feather.DOWNLOAD);
        dlIcon.setIconColor(Color.BLACK);
        installBtn.setGraphic(dlIcon);
        
        if (isInstalled) {
            installBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 10 24; -fx-font-size: 14px; -fx-cursor: hand;");
            dlIcon.setIconColor(Color.WHITE);
        } else {
            installBtn.setStyle("-fx-background-color: #fafafa; -fx-text-fill: #010101; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 10 24; -fx-font-size: 14px; -fx-cursor: hand;");
        }
        
        // Progress indicator for install
        javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(0);
        progressBar.setPrefWidth(120);
        progressBar.setVisible(false);
        
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
        
        installBtn.setOnAction(e -> {
            if (libraryService.isInstalled(app.getId())) {
                // Already installed - just show "Running"
                installBtn.setText("Running...");
                installBtn.setDisable(true);
                Timeline reset = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                    installBtn.setText("Open");
                    installBtn.setDisable(false);
                }));
                reset.play();
            } else {
                // Simulate install
                installBtn.setDisable(true);
                installBtn.setText("Installing...");
                progressBar.setVisible(true);
                
                Timeline timeline = new Timeline();
                for (int i = 0; i <= 20; i++) {
                    final int step = i;
                    KeyFrame keyFrame = new KeyFrame(Duration.millis(i * 100), ev -> {
                        double progress = step / 20.0;
                        progressBar.setProgress(progress);
                        if (progress < 0.5) {
                            statusLabel.setText("Downloading...");
                        } else if (progress < 0.9) {
                            statusLabel.setText("Installing...");
                        } else {
                            statusLabel.setText("Finishing...");
                        }
                    });
                    timeline.getKeyFrames().add(keyFrame);
                }
                
                KeyFrame completeFrame = new KeyFrame(Duration.millis(2100), ev -> {
                    // Get version from API or use default
                    String version = versionLabel.getText().equals("Loading...") ? "1.0.0" : versionLabel.getText();
                    libraryService.installApp(app.getId(), app.getName(), app.getOwnerLogin(), 
                        app.getCategory() != null ? app.getCategory() : "Unknown", version, "100 MB");
                    
                    progressBar.setVisible(false);
                    installBtn.setText("Open");
                    installBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 10 24; -fx-font-size: 14px; -fx-cursor: hand;");
                    FontIcon checkIcon = new FontIcon(Feather.CHECK);
                    checkIcon.setIconColor(Color.WHITE);
                    installBtn.setGraphic(checkIcon);
                    installBtn.setDisable(false);
                    statusLabel.setText("Installed!");
                });
                timeline.getKeyFrames().add(completeFrame);
                timeline.play();
            }
        });
        
        VBox actionBox = new VBox(4);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.getChildren().addAll(installBtn, progressBar, statusLabel);
        
        actions.getChildren().add(actionBox);
        
        appHeader.getChildren().addAll(iconBox, metaBox, actions);
        headerSection.getChildren().addAll(backBtn, appHeader);
        
        // --- Gallery Section ---
        StackPane gallery = new StackPane();
        gallery.setPrefHeight(350);
        gallery.setMinHeight(350);
        gallery.setStyle("-fx-background-color: #0d0d0d; -fx-background-radius: 12px; -fx-border-color: #181818; -fx-border-radius: 12px;");
        
        // Loading state
        ProgressIndicator galleryLoader = new ProgressIndicator();
        galleryLoader.setMaxSize(40, 40);
        VBox loadingBox = new VBox(16, galleryLoader, new Label("Loading screenshots..."));
        loadingBox.setAlignment(Pos.CENTER);
        ((Label) loadingBox.getChildren().get(1)).setStyle("-fx-text-fill: #71717a;");
        gallery.getChildren().add(loadingBox);
        
        // Load screenshots from API
        loadScreenshots(gallery);

        // --- Tabs ---
        HBox tabs = new HBox(24);
        tabs.setPadding(new Insets(16, 0, 16, 0));
        tabs.getChildren().addAll(
            createTab("Overview"),
            createTab("Changelog")
        );

        // --- Content Sections ---
        
        // 1. Overview Content
        VBox overviewContent = new VBox(32);
        
        // About
        VBox aboutSection = new VBox(16);
        Label aboutHeader = new Label("About this app");
        aboutHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");
        descriptionLabel = new Label(app.getDescription() != null ? app.getDescription() : "No description available");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #a1a1aa; -fx-line-spacing: 4px;");
        aboutSection.getChildren().addAll(aboutHeader, descriptionLabel);

        // Info Grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(16);
        infoGrid.setVgap(16);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(25);
        ColumnConstraints col4 = new ColumnConstraints(); col4.setPercentWidth(25);
        infoGrid.getColumnConstraints().addAll(col1, col2, col3, col4);
        
        versionLabel = new Label("Loading...");
        versionLabel.setStyle("-fx-text-fill: #fafafa; -fx-font-weight: bold; -fx-font-size: 14px;");
        infoGrid.add(createInfoBox("Version", versionLabel), 0, 0);
        infoGrid.add(createInfoBox("Category", app.getCategory() != null ? app.getCategory() : "Unknown"), 1, 0);
        infoGrid.add(createInfoBox("Owner", app.getOwnerLogin()), 2, 0);

        // Developer Footer
        HBox footer = new HBox(16);
        footer.setPadding(new Insets(24));
        footer.setStyle("-fx-background-color: #040404; -fx-background-radius: 8px; -fx-border-color: #181818; -fx-border-radius: 8px;");
        footer.setAlignment(Pos.CENTER_LEFT);
        StackPane devIcon = new StackPane();
        devIcon.setPrefSize(40, 40);
        devIcon.setStyle("-fx-background-color: #181818; -fx-background-radius: 20px;");
        Label devInitials = new Label(app.getOwnerLogin().substring(0, 1).toUpperCase());
        devInitials.setStyle("-fx-text-fill: #fafafa; -fx-font-weight: bold;");
        devIcon.getChildren().add(devInitials);
        VBox devInfo = new VBox(2);
        Label devName = new Label(app.getOwnerLogin());
        devName.setStyle("-fx-text-fill: #fafafa; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label devRole = new Label("Developer");
        devRole.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12px;");
        devInfo.getChildren().addAll(devName, devRole);
        footer.getChildren().addAll(devIcon, devInfo);

        overviewContent.getChildren().addAll(aboutSection, infoGrid, footer);
        
        // 2. Changelog Content
        VBox changelogContent = new VBox(16);
        Label changelogHeader = new Label("Latest Release");
        changelogHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");
        ProgressIndicator changelogLoader = new ProgressIndicator();
        changelogLoader.setMaxSize(30, 30);
        changelogContent.getChildren().addAll(changelogHeader, changelogLoader);

        // Register Content
        tabContentNodes.put("Overview", overviewContent);
        tabContentNodes.put("Changelog", changelogContent);

        // Assemble Main Layout
        mainLayout.getChildren().addAll(
            headerSection,
            gallery,
            tabs,
            contentContainer
        );
        
        setContent(mainLayout);
        
        // Initialize
        switchTab("Overview");
        
        // Load release info from API
        loadReleaseInfo(changelogContent);
    }
    
    private void loadReleaseInfo(VBox changelogContent) {
        ApiService.getInstance().getLatestRelease(app.getId()).thenAccept(release -> {
            Platform.runLater(() -> {
                if (release != null) {
                    versionLabel.setText(release.getTagName());
                    
                    // Update changelog content
                    changelogContent.getChildren().clear();
                    Label header = new Label("Version " + release.getTagName());
                    header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");
                    
                    Label body = new Label(release.getBody() != null ? release.getBody() : "No release notes");
                    body.setWrapText(true);
                    body.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 14px;");
                    
                    Label published = new Label("Published: " + (release.getPublishedAt() != null ? release.getPublishedAt() : "Unknown"));
                    published.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12px;");
                    
                    changelogContent.getChildren().addAll(header, published, body);
                } else {
                    versionLabel.setText("N/A");
                    changelogContent.getChildren().clear();
                    Label noRelease = new Label("No release information available");
                    noRelease.setStyle("-fx-text-fill: #71717a;");
                    changelogContent.getChildren().add(noRelease);
                }
            });
        });
    }
    
    private void loadScreenshots(StackPane gallery) {
        ApiService.getInstance().getScreenshots(app.getId()).thenAccept(urls -> {
            Platform.runLater(() -> {
                gallery.getChildren().clear();
                
                if (urls.isEmpty()) {
                    FontIcon noImgIcon = new FontIcon(Feather.IMAGE);
                    noImgIcon.setIconSize(64);
                    noImgIcon.setIconColor(Color.web("#27272a"));
                    Label noImgLabel = new Label("No screenshots available");
                    noImgLabel.setStyle("-fx-text-fill: #52525b; -fx-font-size: 14px;");
                    VBox noImgBox = new VBox(16, noImgIcon, noImgLabel);
                    noImgBox.setAlignment(Pos.CENTER);
                    gallery.getChildren().add(noImgBox);
                    return;
                }
                
                // Create image views for each screenshot
                java.util.List<javafx.scene.image.ImageView> imageViews = new java.util.ArrayList<>();
                final int[] currentIndex = {0};
                
                for (String url : urls) {
                    try {
                        javafx.scene.image.Image img = new javafx.scene.image.Image(url, true);
                        javafx.scene.image.ImageView view = new javafx.scene.image.ImageView(img);
                        view.setPreserveRatio(true);
                        view.setFitHeight(320);
                        view.setVisible(imageViews.isEmpty()); // Only first is visible
                        imageViews.add(view);
                        gallery.getChildren().add(view);
                    } catch (Exception e) {
                        System.err.println("Failed to load image: " + url);
                    }
                }
                
                if (imageViews.isEmpty()) {
                    Label errorLabel = new Label("Failed to load screenshots");
                    errorLabel.setStyle("-fx-text-fill: #71717a;");
                    gallery.getChildren().add(errorLabel);
                    return;
                }
                
                // Navigation arrows
                Button leftBtn = new Button();
                leftBtn.setGraphic(new FontIcon(Feather.CHEVRON_LEFT));
                leftBtn.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-cursor: hand;");
                
                Button rightBtn = new Button();
                rightBtn.setGraphic(new FontIcon(Feather.CHEVRON_RIGHT));
                rightBtn.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-cursor: hand;");
                
                leftBtn.setOnAction(e -> {
                    imageViews.get(currentIndex[0]).setVisible(false);
                    currentIndex[0] = (currentIndex[0] - 1 + imageViews.size()) % imageViews.size();
                    imageViews.get(currentIndex[0]).setVisible(true);
                });
                
                rightBtn.setOnAction(e -> {
                    imageViews.get(currentIndex[0]).setVisible(false);
                    currentIndex[0] = (currentIndex[0] + 1) % imageViews.size();
                    imageViews.get(currentIndex[0]).setVisible(true);
                });
                
                HBox navBox = new HBox();
                navBox.setAlignment(Pos.CENTER);
                navBox.setPadding(new Insets(0, 20, 0, 20));
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                navBox.getChildren().addAll(leftBtn, spacer, rightBtn);
                
                // Counter label
                Label counterLabel = new Label("1 / " + imageViews.size());
                counterLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
                StackPane.setAlignment(counterLabel, Pos.BOTTOM_CENTER);
                counterLabel.setTranslateY(-10);
                
                leftBtn.setOnAction(e -> {
                    imageViews.get(currentIndex[0]).setVisible(false);
                    currentIndex[0] = (currentIndex[0] - 1 + imageViews.size()) % imageViews.size();
                    imageViews.get(currentIndex[0]).setVisible(true);
                    counterLabel.setText((currentIndex[0] + 1) + " / " + imageViews.size());
                });
                
                rightBtn.setOnAction(e -> {
                    imageViews.get(currentIndex[0]).setVisible(false);
                    currentIndex[0] = (currentIndex[0] + 1) % imageViews.size();
                    imageViews.get(currentIndex[0]).setVisible(true);
                    counterLabel.setText((currentIndex[0] + 1) + " / " + imageViews.size());
                });
                
                gallery.getChildren().addAll(navBox, counterLabel);
                StackPane.setAlignment(navBox, Pos.CENTER);
            });
        });
    }

    private void switchTab(String tabName) {
        tabButtons.forEach((name, btn) -> {
            if (name.equals(tabName)) {
                btn.setStyle("-fx-background-color: #27272a; -fx-text-fill: #fafafa; -fx-background-radius: 16px; -fx-padding: 6 16; -fx-font-size: 13px; -fx-font-weight: bold;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #a1a1aa; -fx-padding: 6 16; -fx-font-size: 13px; -fx-cursor: hand;");
            }
        });
        
        contentContainer.getChildren().clear();
        Node content = tabContentNodes.get(tabName);
        if (content != null) {
            contentContainer.getChildren().add(content);
        }
    }
    
    private StackPane createPlatformBadge(Feather icon) {
        StackPane badge = new StackPane();
        badge.setPrefSize(28, 28);
        badge.setStyle("-fx-background-color: #181818; -fx-background-radius: 6px;");
        FontIcon i = new FontIcon(icon);
        i.setIconColor(Color.web("#a1a1aa"));
        i.setIconSize(14);
        badge.getChildren().add(i);
        return badge;
    }
    
    private Button createTab(String text) {
        Button tab = new Button(text);
        tab.setOnAction(e -> switchTab(text));
        tabButtons.put(text, tab);
        return tab;
    }
    
    private VBox createInfoBox(String label, String value) {
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #fafafa; -fx-font-weight: bold; -fx-font-size: 14px;");
        return createInfoBox(label, v);
    }
    
    private VBox createInfoBox(String label, Label valueLabel) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #040404; -fx-background-radius: 8px; -fx-border-color: #181818; -fx-border-radius: 8px;");
        
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12px;");
        
        box.getChildren().addAll(l, valueLabel);
        return box;
    }
}
