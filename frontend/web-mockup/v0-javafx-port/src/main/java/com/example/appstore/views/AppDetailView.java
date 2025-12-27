package com.example.appstore.views;

import com.example.appstore.model.App;
import com.example.appstore.model.GithubRelease;
import com.example.appstore.model.InstalledApp;
import com.example.appstore.service.ApiService;
import com.example.appstore.service.InstallationManager;
import com.example.appstore.service.InstallationManager.InstallationState;
import com.example.appstore.service.InstallationService;
import com.example.appstore.service.LibraryService;
import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class AppDetailView extends ScrollPane {

        private static final Logger LOG = LogManager.getLogger(AppDetailView.class);

        private final App app;
        private final Runnable onBack;
        private final Map<String, Button> tabButtons = new HashMap<>();
        private final Map<String, Node> tabContentNodes = new HashMap<>();
        private final VBox contentContainer = new VBox(32);

        // Labels to update with API data
        private Label versionLabel;
        private Label descriptionLabel;

        // Screenshot gallery
        private java.util.List<Node> galleryItems = new java.util.ArrayList<>();
        private int currentImageIndex = 0;
        private Label counterLabel;
        private HBox dotsContainer;

        public AppDetailView(App app, Runnable onBack) {
                this.app = app;
                this.onBack = onBack;
                getStyleClass().add("edge-to-edge-scroll-pane");
                setFitToWidth(true);
                setHbarPolicy(ScrollBarPolicy.NEVER);
                setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

                VBox mainLayout = new VBox(24);
                mainLayout.setPadding(new Insets(24, 32, 48, 32));
                mainLayout.getStyleClass().add("content-area");
                mainLayout.setStyle("-fx-background-color: #0a0a0a;");

                // --- Back Button ---
                HBox backRow = new HBox();
                Button backBtn = new Button("← Back");
                backBtn.setStyle(
                                "-fx-background-color: transparent; -fx-text-fill: #3b82f6; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 4 0;");
                backBtn.setOnAction(e -> {
                        if (onBack != null)
                                onBack.run();
                });
                backRow.getChildren().add(backBtn);

                // --- Header Section ---
                HBox headerSection = new HBox(16);
                headerSection.setAlignment(Pos.CENTER_LEFT);

                // App Icon (rounded square)
                StackPane iconBox = new StackPane();
                iconBox.setPrefSize(72, 72);
                iconBox.setMinSize(72, 72);
                iconBox.setMaxSize(72, 72);
                iconBox.setStyle(
                                "-fx-background-color: #1a1a1a; -fx-background-radius: 14px; -fx-border-color: #2a2a2a; -fx-border-radius: 14px;");
                FontIcon appIcon = new FontIcon(Feather.BOX);
                appIcon.setIconSize(32);
                appIcon.setIconColor(Color.WHITE);
                iconBox.getChildren().add(appIcon);

                // Title & Metadata
                VBox metaBox = new VBox(4);
                HBox.setHgrow(metaBox, Priority.ALWAYS);

                Label titleLabel = new Label(app.getName());
                titleLabel.setStyle(
                                "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");

                Label vendorLabel = new Label(app.getOwnerLogin());
                vendorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #a1a1aa;");

                // Platform badges row
                HBox platformBadges = new HBox(8);
                platformBadges.setAlignment(Pos.CENTER_LEFT);
                platformBadges.getChildren().addAll(
                                createPlatformBadge(Feather.MONITOR),
                                createPlatformBadge(Feather.COMMAND),
                                createPlatformBadge(Feather.TERMINAL));

                metaBox.getChildren().addAll(titleLabel, vendorLabel, platformBadges);

                // Button container - right aligned
                HBox buttonsBox = new HBox(8);
                buttonsBox.setAlignment(Pos.CENTER_RIGHT);

                // GitHub button - opens the app's GitHub page
                Button githubBtn = new Button();
                FontIcon githubIcon = new FontIcon(Feather.GITHUB);
                githubIcon.setIconSize(16);
                githubIcon.setIconColor(Color.WHITE);
                githubBtn.setGraphic(githubIcon);
                githubBtn.setStyle(
                                "-fx-background-color: #27272a; -fx-background-radius: 8px; -fx-padding: 8 12; -fx-cursor: hand;");
                githubBtn.setOnAction(e -> {
                        try {
                                String githubUrl = "https://github.com/" + app.getId();
                                Desktop.getDesktop().browse(new URI(githubUrl));
                                LOG.info("Opened GitHub page: {}", githubUrl);
                        } catch (Exception ex) {
                                LOG.warn("Failed to open GitHub page for {}: {}", app.getId(), ex.getMessage());
                        }
                });

                // Install/Open button
                LibraryService libraryService = LibraryService.getInstance();
                boolean isInstalled = libraryService.isInstalled(app.getId());

                Button installBtn = new Button(isInstalled ? "Open" : "Install");
                FontIcon dlIcon = new FontIcon(isInstalled ? Feather.EXTERNAL_LINK : Feather.DOWNLOAD);
                dlIcon.setIconSize(14);
                dlIcon.setIconColor(Color.WHITE);
                installBtn.setGraphic(dlIcon);
                installBtn.setStyle(
                                "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8 20; -fx-font-size: 13px; -fx-cursor: hand;");

                // Progress display (hidden initially)
                VBox progressBox = new VBox(4);
                progressBox.setAlignment(Pos.CENTER_RIGHT);
                progressBox.setVisible(false);
                progressBox.setManaged(false);

                ProgressBar progressBar = new ProgressBar(0);
                progressBar.setPrefWidth(120);
                progressBar.setStyle("-fx-accent: #3b82f6;");

                Label progressLabel = new Label("Preparing...");
                progressLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 10px;");

                progressBox.getChildren().addAll(progressBar, progressLabel);

                Label statusLabel = new Label("");
                statusLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");

                // Check if this app is currently being installed
                InstallationManager installManager = InstallationManager.getInstance();
                if (installManager.isInstalling(app.getId())) {
                        installBtn.setVisible(false);
                        installBtn.setManaged(false);
                        progressBox.setVisible(true);
                        progressBox.setManaged(true);

                        installManager.addProgressListener(app.getId(), progress -> {
                                Platform.runLater(() -> updateProgressDisplay(progressBar, progressLabel, progress));
                        });
                }

                installBtn.setOnAction(e -> handleInstallAction(
                                installBtn, progressBox, progressBar, progressLabel, statusLabel,
                                libraryService, installManager));

                buttonsBox.getChildren().addAll(progressBox, statusLabel, githubBtn, installBtn);

                headerSection.getChildren().addAll(iconBox, metaBox, buttonsBox);

                // --- Version Badges ---
                HBox badgesRow = new HBox(8);
                badgesRow.setAlignment(Pos.CENTER_LEFT);
                badgesRow.setPadding(new Insets(0, 0, 8, 0));

                // Crates.io badge (or similar source badge)
                Label sourceLabel = new Label("crates.io");
                sourceLabel.setStyle(
                                "-fx-background-color: #3b3b3b; -fx-text-fill: #a1a1aa; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px;");

                // Version badge
                versionLabel = new Label("v0.0.0");
                versionLabel.setStyle(
                                "-fx-background-color: #f97316; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");

                badgesRow.getChildren().addAll(sourceLabel, versionLabel);

                // --- Screenshot Gallery ---
                VBox gallerySection = new VBox(12);

                StackPane galleryContainer = new StackPane();
                galleryContainer.setMinHeight(280);
                galleryContainer.setPrefHeight(280);
                galleryContainer.setStyle(
                                "-fx-background-color: #111111; -fx-background-radius: 12px;");

                // Clip for rounded corners
                Rectangle galleryClip = new Rectangle();
                galleryClip.widthProperty().bind(galleryContainer.widthProperty());
                galleryClip.heightProperty().bind(galleryContainer.heightProperty());
                galleryClip.setArcWidth(24);
                galleryClip.setArcHeight(24);
                galleryContainer.setClip(galleryClip);

                // Navigation arrows
                Button leftArrow = new Button();
                FontIcon leftIcon = new FontIcon(Feather.CHEVRON_LEFT);
                leftIcon.setIconSize(24);
                leftIcon.setIconColor(Color.WHITE);
                leftArrow.setGraphic(leftIcon);
                leftArrow.setStyle(
                                "-fx-background-color: #00000080; -fx-background-radius: 50%; -fx-padding: 12; -fx-cursor: hand;");
                StackPane.setAlignment(leftArrow, Pos.CENTER_LEFT);
                StackPane.setMargin(leftArrow, new Insets(0, 0, 0, 16));
                leftArrow.setOnAction(e -> navigateGallery(-1));

                Button rightArrow = new Button();
                FontIcon rightIcon = new FontIcon(Feather.CHEVRON_RIGHT);
                rightIcon.setIconSize(24);
                rightIcon.setIconColor(Color.WHITE);
                rightArrow.setGraphic(rightIcon);
                rightArrow.setStyle(
                                "-fx-background-color: #00000080; -fx-background-radius: 50%; -fx-padding: 12; -fx-cursor: hand;");
                StackPane.setAlignment(rightArrow, Pos.CENTER_RIGHT);
                StackPane.setMargin(rightArrow, new Insets(0, 16, 0, 0));
                rightArrow.setOnAction(e -> navigateGallery(1));

                // Counter label
                counterLabel = new Label("1 / 2");
                counterLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 12px;");
                StackPane.setAlignment(counterLabel, Pos.BOTTOM_CENTER);
                StackPane.setMargin(counterLabel, new Insets(0, 0, 12, 0));

                // Loading indicator
                ProgressIndicator galleryLoader = new ProgressIndicator();
                galleryLoader.setMaxSize(40, 40);
                galleryContainer.getChildren().add(galleryLoader);

                gallerySection.getChildren().addAll(galleryContainer);

                // Load screenshots
                loadGalleryScreenshots(galleryContainer, leftArrow, rightArrow);

                // --- Tabs Section ---
                HBox tabsRow = new HBox(0);
                tabsRow.setAlignment(Pos.CENTER_LEFT);
                tabsRow.setPadding(new Insets(8, 0, 0, 0));

                Button overviewTab = createTab("Overview", true);
                Button changelogTab = createTab("Changelog", false);

                tabButtons.put("Overview", overviewTab);
                tabButtons.put("Changelog", changelogTab);

                overviewTab.setOnAction(e -> switchTab("Overview"));
                changelogTab.setOnAction(e -> switchTab("Changelog"));

                tabsRow.getChildren().addAll(overviewTab, changelogTab);

                // --- Overview Content ---
                VBox overviewContent = new VBox(24);
                overviewContent.setPadding(new Insets(16, 0, 0, 0));

                // About this app section
                VBox aboutSection = new VBox(8);
                Label aboutHeader = new Label("About this app");
                aboutHeader.setStyle(
                                "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");

                descriptionLabel = new Label(
                                app.getDescription() != null
                                                ? app.getDescription()
                                                : "No description available");
                descriptionLabel.setWrapText(true);
                descriptionLabel.setStyle("-fx-text-fill: #f97316; -fx-font-size: 13px;");

                aboutSection.getChildren().addAll(aboutHeader, descriptionLabel);

                // Info boxes row
                HBox infoBoxesRow = new HBox(12);
                infoBoxesRow.setPadding(new Insets(8, 0, 0, 0));

                // Version box
                VBox versionInfoBox = createInfoBox("Version", versionLabel.getText());

                // Category box
                VBox categoryInfoBox = createInfoBox("Category",
                                app.getCategory() != null ? app.getCategory() : "Unknown");

                // Owner box
                VBox ownerInfoBox = createInfoBox("Owner", app.getOwnerLogin());

                infoBoxesRow.getChildren().addAll(versionInfoBox, categoryInfoBox, ownerInfoBox);
                HBox.setHgrow(versionInfoBox, Priority.ALWAYS);
                HBox.setHgrow(categoryInfoBox, Priority.ALWAYS);
                HBox.setHgrow(ownerInfoBox, Priority.ALWAYS);

                overviewContent.getChildren().addAll(aboutSection, infoBoxesRow);
                tabContentNodes.put("Overview", overviewContent);

                // --- Changelog Content ---
                VBox changelogContent = new VBox(16);
                changelogContent.setPadding(new Insets(16, 0, 0, 0));
                changelogContent.setVisible(false);
                changelogContent.setManaged(false);

                Label changelogLoading = new Label("Loading changelog...");
                changelogLoading.setStyle("-fx-text-fill: #71717a;");
                changelogContent.getChildren().add(changelogLoading);

                tabContentNodes.put("Changelog", changelogContent);

                // Load release info
                loadReleaseInfo(changelogContent);
                loadVersionFromGitHub();

                // Assemble Main Layout
                mainLayout.getChildren().addAll(
                                backRow,
                                headerSection,
                                badgesRow,
                                gallerySection,
                                tabsRow,
                                overviewContent,
                                changelogContent);

                setContent(mainLayout);
        }

        private void handleInstallAction(
                        Button installBtn, VBox progressBox, ProgressBar progressBar,
                        Label progressLabel, Label statusLabel,
                        LibraryService libraryService, InstallationManager installManager) {

                LOG.info("Install button clicked for app: {} (id: {})", app.getName(), app.getId());

                if (libraryService.isInstalled(app.getId())) {
                        // Already installed - launch the app
                        LOG.info("App already installed, attempting to launch: {} (id: {})",
                                        app.getName(), app.getId());
                        Optional<InstalledApp> installedApp = libraryService.getInstalledApp(app.getId());
                        if (installedApp.isPresent() && installedApp.get().getExecutablePath() != null) {
                                String execPath = installedApp.get().getExecutablePath();
                                LOG.info("Launching app from: {} (app: {})", execPath, app.getName());
                                installBtn.setText("Launching...");
                                installBtn.setDisable(true);

                                try {
                                        InstallationService.getInstance().launchApp(execPath);
                                        LOG.info("App launched successfully: {} (executable: {})",
                                                        app.getName(), execPath);
                                        statusLabel.setText("Launched!");
                                } catch (Exception ex) {
                                        LOG.warn("Failed to launch app: {} (executable: {})",
                                                        app.getName(), execPath, ex);
                                        statusLabel.setText("Failed to launch: " + ex.getMessage());
                                }

                                Timeline reset = new Timeline(
                                                new KeyFrame(Duration.seconds(1), ev -> {
                                                        installBtn.setText("Open");
                                                        installBtn.setDisable(false);
                                                        statusLabel.setText("");
                                                }));
                                reset.play();
                        } else {
                                LOG.warn("No executable path found for installed app: {} (id: {})",
                                                app.getName(), app.getId());
                                statusLabel.setText("Path not found - reinstall recommended");
                        }
                } else {
                        // Check if another installation is in progress
                        if (installManager.isInstalling()) {
                                LOG.warn("Another installation is in progress, cannot start installation for: {}",
                                                app.getName());
                                statusLabel.setText("Another installation in progress");
                                return;
                        }

                        // Start installation
                        LOG.info("Starting installation for app: {} (id: {})", app.getName(), app.getId());
                        installBtn.setVisible(false);
                        installBtn.setManaged(false);
                        progressBox.setVisible(true);
                        progressBox.setManaged(true);
                        progressBar.setProgress(0);
                        progressLabel.setText("Preparing...");
                        statusLabel.setText("");

                        installManager.addProgressListener(app.getId(), progress -> {
                                Platform.runLater(() -> updateProgressDisplay(progressBar, progressLabel, progress));
                        });

                        installManager.installApp(app)
                                        .thenAccept(result -> {
                                                Platform.runLater(() -> {
                                                        LOG.info("Installation completed for app: {} (id: {}, version: {})",
                                                                        app.getName(), app.getId(),
                                                                        result.getVersion());

                                                        libraryService.installApp(
                                                                        app.getId(), app.getName(), app.getOwnerLogin(),
                                                                        app.getCategory() != null ? app.getCategory()
                                                                                        : "Unknown",
                                                                        result.getVersion(), result.getFormattedSize(),
                                                                        result.getInstallPath(),
                                                                        result.getExecutablePath());

                                                        progressBox.setVisible(false);
                                                        progressBox.setManaged(false);
                                                        installBtn.setVisible(true);
                                                        installBtn.setManaged(true);
                                                        installBtn.setText("Open");
                                                        installBtn.setDisable(false);
                                                        statusLabel.setText("Installed " + result.getVersion());

                                                        installManager.clearProgressListeners();
                                                });
                                        })
                                        .exceptionally(ex -> {
                                                Platform.runLater(() -> {
                                                        LOG.error("Installation error for app: {} (id: {})",
                                                                        app.getName(), app.getId(), ex);
                                                        String errorMsg = ex.getCause() != null
                                                                        ? ex.getCause().getMessage()
                                                                        : ex.getMessage();

                                                        progressBox.setVisible(false);
                                                        progressBox.setManaged(false);
                                                        installBtn.setVisible(true);
                                                        installBtn.setManaged(true);
                                                        installBtn.setDisable(false);
                                                        statusLabel.setText("Failed: " + errorMsg);

                                                        installManager.clearProgressListeners();
                                                });
                                                return null;
                                        });
                }
        }

        private void loadVersionFromGitHub() {
                ApiService.getInstance()
                                .getLatestRelease(app.getId())
                                .thenAccept(release -> {
                                        Platform.runLater(() -> {
                                                if (release != null) {
                                                        versionLabel.setText(release.getTagName());
                                                } else {
                                                        versionLabel.setText("N/A");
                                                }
                                        });
                                })
                                .exceptionally(ex -> {
                                        Platform.runLater(() -> versionLabel.setText("N/A"));
                                        return null;
                                });
        }

        private void loadReleaseInfo(VBox changelogContent) {
                ApiService.getInstance()
                                .getLatestRelease(app.getId())
                                .thenAccept(release -> {
                                        Platform.runLater(() -> {
                                                changelogContent.getChildren().clear();
                                                if (release != null) {
                                                        Label header = new Label("Version " + release.getTagName());
                                                        header.setStyle(
                                                                        "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");

                                                        Label body = new Label(
                                                                        release.getBody() != null
                                                                                        ? release.getBody()
                                                                                        : "No release notes");
                                                        body.setWrapText(true);
                                                        body.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 13px;");

                                                        Label published = new Label(
                                                                        "Published: " + (release
                                                                                        .getPublishedAt() != null
                                                                                                        ? release.getPublishedAt()
                                                                                                        : "Unknown"));
                                                        published.setStyle(
                                                                        "-fx-text-fill: #71717a; -fx-font-size: 11px;");

                                                        changelogContent.getChildren().addAll(header, published, body);
                                                } else {
                                                        Label noRelease = new Label("No release information available");
                                                        noRelease.setStyle("-fx-text-fill: #71717a;");
                                                        changelogContent.getChildren().add(noRelease);
                                                }
                                        });
                                });
        }

        private void loadGalleryScreenshots(StackPane gallery, Button leftArrow, Button rightArrow) {
                ApiService.getInstance()
                                .getScreenshots(app.getId())
                                .thenAccept(urls -> {
                                        Platform.runLater(() -> {
                                                gallery.getChildren().clear();
                                                galleryItems.clear();

                                                if (urls.isEmpty()) {
                                                        FontIcon noImgIcon = new FontIcon(Feather.IMAGE);
                                                        noImgIcon.setIconSize(48);
                                                        noImgIcon.setIconColor(Color.web("#27272a"));
                                                        Label noImgLabel = new Label("No screenshots available");
                                                        noImgLabel.setStyle(
                                                                        "-fx-text-fill: #52525b; -fx-font-size: 13px;");
                                                        VBox noImgBox = new VBox(12, noImgIcon, noImgLabel);
                                                        noImgBox.setAlignment(Pos.CENTER);
                                                        gallery.getChildren().add(noImgBox);
                                                        counterLabel.setText("0 / 0");
                                                        return;
                                                }

                                                HttpClient httpClient = HttpClient.newBuilder()
                                                                .followRedirects(HttpClient.Redirect.NORMAL)
                                                                .connectTimeout(java.time.Duration.ofSeconds(30))
                                                                .build();

                                                final int[] loadedCount = { 0 };
                                                final int totalUrls = urls.size();

                                                for (int i = 0; i < urls.size(); i++) {
                                                        String url = urls.get(i);
                                                        final int imageIndex = i;

                                                        ImageView view = new ImageView();
                                                        view.setPreserveRatio(true);
                                                        view.fitHeightProperty()
                                                                        .bind(gallery.heightProperty().subtract(20));
                                                        view.fitWidthProperty()
                                                                        .bind(gallery.widthProperty().subtract(80));
                                                        view.setVisible(imageIndex == 0);

                                                        galleryItems.add(view);
                                                        gallery.getChildren().add(view);

                                                        loadImageWithHttpClient(httpClient, url)
                                                                        .thenAccept(imageBytes -> {
                                                                                Platform.runLater(() -> {
                                                                                        loadedCount[0]++;
                                                                                        if (imageBytes != null
                                                                                                        && imageBytes.length > 0) {
                                                                                                try {
                                                                                                        Image img = new Image(
                                                                                                                        new ByteArrayInputStream(
                                                                                                                                        imageBytes));
                                                                                                        if (!img.isError()) {
                                                                                                                view.setImage(img);
                                                                                                                LOG.debug("Loaded screenshot {} for app {}",
                                                                                                                                imageIndex + 1,
                                                                                                                                app.getName());
                                                                                                        }
                                                                                                } catch (Exception e) {
                                                                                                        LOG.warn("Failed to load screenshot {}: {}",
                                                                                                                        imageIndex + 1,
                                                                                                                        e.getMessage());
                                                                                                }
                                                                                        }

                                                                                        if (loadedCount[0] >= totalUrls) {
                                                                                                // All loaded, add
                                                                                                // navigation
                                                                                                gallery.getChildren()
                                                                                                                .addAll(leftArrow,
                                                                                                                                rightArrow,
                                                                                                                                counterLabel);
                                                                                                updateCounter();
                                                                                        }
                                                                                });
                                                                        });
                                                }
                                        });
                                });
        }

        private CompletableFuture<byte[]> loadImageWithHttpClient(HttpClient client, String url) {
                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .header("User-Agent", "Mozilla/5.0 JavaFX AppStore")
                                .GET()
                                .build();

                return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                                .thenApply(response -> {
                                        if (response.statusCode() == 200) {
                                                return response.body();
                                        }
                                        return null;
                                })
                                .exceptionally(ex -> {
                                        LOG.warn("Failed to load image from {}: {}", url, ex.getMessage());
                                        return null;
                                });
        }

        private void navigateGallery(int direction) {
                if (galleryItems.isEmpty())
                        return;

                // Hide current
                if (currentImageIndex >= 0 && currentImageIndex < galleryItems.size()) {
                        galleryItems.get(currentImageIndex).setVisible(false);
                }

                // Navigate
                currentImageIndex += direction;
                if (currentImageIndex < 0) {
                        currentImageIndex = galleryItems.size() - 1;
                } else if (currentImageIndex >= galleryItems.size()) {
                        currentImageIndex = 0;
                }

                // Show new
                galleryItems.get(currentImageIndex).setVisible(true);
                updateCounter();
        }

        private void updateCounter() {
                if (!galleryItems.isEmpty()) {
                        counterLabel.setText((currentImageIndex + 1) + " / " + galleryItems.size());
                }
        }

        private void switchTab(String tabName) {
                for (Map.Entry<String, Button> entry : tabButtons.entrySet()) {
                        boolean isActive = entry.getKey().equals(tabName);
                        Button btn = entry.getValue();
                        if (isActive) {
                                btn.setStyle(
                                                "-fx-background-color: #27272a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 8 20; -fx-font-size: 12px; -fx-cursor: hand;");
                        } else {
                                btn.setStyle(
                                                "-fx-background-color: transparent; -fx-text-fill: #71717a; -fx-font-weight: normal; -fx-background-radius: 20px; -fx-padding: 8 20; -fx-font-size: 12px; -fx-cursor: hand;");
                        }
                }

                for (Map.Entry<String, Node> entry : tabContentNodes.entrySet()) {
                        boolean isActive = entry.getKey().equals(tabName);
                        entry.getValue().setVisible(isActive);
                        entry.getValue().setManaged(isActive);
                }
        }

        private StackPane createPlatformBadge(Feather icon) {
                StackPane badge = new StackPane();
                badge.setPrefSize(24, 24);
                badge.setStyle("-fx-background-color: #27272a; -fx-background-radius: 6px;");
                FontIcon badgeIcon = new FontIcon(icon);
                badgeIcon.setIconSize(12);
                badgeIcon.setIconColor(Color.web("#a1a1aa"));
                badge.getChildren().add(badgeIcon);
                return badge;
        }

        private Button createTab(String text, boolean isActive) {
                Button btn = new Button(text);
                if (isActive) {
                        btn.setStyle(
                                        "-fx-background-color: #27272a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 8 20; -fx-font-size: 12px; -fx-cursor: hand;");
                } else {
                        btn.setStyle(
                                        "-fx-background-color: transparent; -fx-text-fill: #71717a; -fx-font-weight: normal; -fx-background-radius: 20px; -fx-padding: 8 20; -fx-font-size: 12px; -fx-cursor: hand;");
                }
                return btn;
        }

        private VBox createInfoBox(String label, String value) {
                VBox box = new VBox(4);
                box.setStyle(
                                "-fx-background-color: #1a1a1a; -fx-background-radius: 8px; -fx-padding: 12;");

                Label labelNode = new Label(label);
                labelNode.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");

                Label valueNode = new Label(value);
                valueNode.setStyle("-fx-text-fill: #fafafa; -fx-font-weight: bold; -fx-font-size: 13px;");

                box.getChildren().addAll(labelNode, valueNode);
                return box;
        }

        private void updateProgressDisplay(
                        ProgressBar progressBar,
                        Label progressLabel,
                        InstallationService.InstallProgress progress) {
                progressBar.setProgress(progress.getProgress());
                progressLabel.setText(progress.getMessage());
        }
}
