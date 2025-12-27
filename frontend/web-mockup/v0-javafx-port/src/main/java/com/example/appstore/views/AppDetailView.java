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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

                // Back Button - more subtle "← Back to Discover" style
                Button backBtn = new Button("← Back to Discover");
                backBtn.setStyle(
                                "-fx-background-color: transparent; -fx-text-fill: #71717a; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 0;");
                backBtn.setOnAction(e -> {
                        if (onBack != null)
                                onBack.run();
                });

                // App Info Header
                HBox appHeader = new HBox(20);
                appHeader.setAlignment(Pos.TOP_LEFT);

                // Large Icon with Gradient
                StackPane iconBox = new StackPane();
                iconBox.setPrefSize(80, 80);
                iconBox.setMinSize(80, 80);
                iconBox.setMaxSize(80, 80);
                // Blue gradient background like in the screenshot
                iconBox.setStyle(
                                "-fx-background-color: linear-gradient(to bottom right, #0ea5e9, #3b82f6); -fx-background-radius: 16px;");
                FontIcon appIcon = new FontIcon(Feather.CODE);
                appIcon.setIconSize(36);
                appIcon.setIconColor(Color.WHITE);
                iconBox.getChildren().add(appIcon);

                // Title & Metadata
                VBox metaBox = new VBox(6);
                HBox.setHgrow(metaBox, Priority.ALWAYS);

                Label titleLabel = new Label(app.getName());
                titleLabel.setStyle(
                                "-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");

                Label vendorLabel = new Label(app.getOwnerLogin());
                vendorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #a1a1aa;");

                // Category in cyan color
                Label categoryLabel = new Label(app.getCategory() != null ? app.getCategory() : "Developer Tools");
                categoryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #22d3ee;");

                // Rating Row with version
                HBox ratingRow = new HBox(12);
                ratingRow.setAlignment(Pos.CENTER_LEFT);

                // Star rating
                HBox ratingBox = new HBox(4);
                ratingBox.setAlignment(Pos.CENTER_LEFT);
                FontIcon starIcon = new FontIcon(Feather.STAR);
                starIcon.setIconSize(14);
                starIcon.setIconColor(Color.web("#fbbf24"));
                Label ratingLabel = new Label("4.8");
                ratingLabel.setStyle("-fx-text-fill: #fafafa; -fx-font-weight: bold; -fx-font-size: 13px;");
                Label ratingCount = new Label("(125k)");
                ratingCount.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12px;");
                ratingBox.getChildren().addAll(starIcon, ratingLabel, ratingCount);

                // Version from GitHub
                HBox versionBox = new HBox(4);
                versionBox.setAlignment(Pos.CENTER_LEFT);
                FontIcon tagIcon = new FontIcon(Feather.TAG);
                tagIcon.setIconSize(12);
                tagIcon.setIconColor(Color.web("#71717a"));
                versionLabel = new Label("Loading...");
                versionLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
                versionBox.getChildren().addAll(tagIcon, versionLabel);

                ratingRow.getChildren().addAll(ratingBox, versionBox);

                metaBox.getChildren().addAll(titleLabel, vendorLabel, categoryLabel, ratingRow);

                appHeader.getChildren().addAll(iconBox, metaBox);

                // GET Button (green, rounded, below the metadata)
                LibraryService libraryService = LibraryService.getInstance();
                boolean isInstalled = libraryService.isInstalled(app.getId());

                Button installBtn = new Button(isInstalled ? "OPEN" : "GET");
                if (isInstalled) {
                        installBtn.setStyle(
                                        "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 10 32; -fx-font-size: 13px; -fx-cursor: hand;");
                } else {
                        installBtn.setStyle(
                                        "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 10 32; -fx-font-size: 13px; -fx-cursor: hand;");
                }

                // Simplified inline progress display
                VBox progressBox = new VBox(6);
                progressBox.setAlignment(Pos.CENTER_LEFT);
                progressBox.setVisible(false);
                progressBox.setManaged(false);

                ProgressBar progressBar = new ProgressBar(0);
                progressBar.setPrefWidth(150);
                progressBar.setStyle("-fx-accent: #3b82f6;");

                Label progressLabel = new Label("Preparing...");
                progressLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");

                progressBox.getChildren().addAll(progressBar, progressLabel);

                Label statusLabel = new Label("");
                statusLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");

                // Check if this app is currently being installed
                InstallationManager installManager = InstallationManager.getInstance();
                if (installManager.isInstalling(app.getId())) {
                        // Show progress for ongoing installation
                        installBtn.setVisible(false);
                        installBtn.setManaged(false);
                        progressBox.setVisible(true);
                        progressBox.setManaged(true);

                        // Listen for progress updates
                        installManager.addProgressListener(app.getId(), progress -> {
                                Platform.runLater(() -> updateProgressDisplay(progressBar, progressLabel, progress));
                        });
                }

                installBtn.setOnAction(e -> {
                        LOG.info(
                                        "Install button clicked for app: {} (id: {})",
                                        app.getName(),
                                        app.getId());

                        if (libraryService.isInstalled(app.getId())) {
                                // Already installed - launch the app
                                LOG.info(
                                                "App already installed, attempting to launch: {} (id: {})",
                                                app.getName(),
                                                app.getId());
                                Optional<InstalledApp> installedApp = libraryService.getInstalledApp(app.getId());
                                if (installedApp.isPresent() &&
                                                installedApp.get().getExecutablePath() != null) {
                                        String execPath = installedApp.get().getExecutablePath();
                                        LOG.info(
                                                        "Launching app from: {} (app: {})",
                                                        execPath,
                                                        app.getName());
                                        installBtn.setText("Launching...");
                                        installBtn.setDisable(true);

                                        try {
                                                InstallationService.getInstance().launchApp(execPath);
                                                LOG.info(
                                                                "App launched successfully: {} (executable: {})",
                                                                app.getName(),
                                                                execPath);
                                                statusLabel.setText("Launched!");
                                        } catch (Exception ex) {
                                                LOG.warn(
                                                                "Failed to launch app: {} (executable: {})",
                                                                app.getName(),
                                                                execPath,
                                                                ex);
                                                statusLabel.setText(
                                                                "Failed to launch: " + ex.getMessage());
                                        }

                                        Timeline reset = new Timeline(
                                                        new KeyFrame(Duration.seconds(1), ev -> {
                                                                installBtn.setText("OPEN");
                                                                installBtn.setDisable(false);
                                                                statusLabel.setText("");
                                                        }));
                                        reset.play();
                                } else {
                                        LOG.warn(
                                                        "No executable path found for installed app: {} (id: {})",
                                                        app.getName(),
                                                        app.getId());
                                        statusLabel.setText(
                                                        "Path not found - reinstall recommended");
                                }
                        } else {
                                // Check if another installation is in progress
                                if (installManager.isInstalling()) {
                                        LOG.warn(
                                                        "Another installation is in progress, cannot start installation for: {}",
                                                        app.getName());
                                        statusLabel.setText("Another installation in progress");
                                        return;
                                }

                                // Start installation via InstallationManager
                                LOG.info(
                                                "Starting installation for app: {} (id: {})",
                                                app.getName(),
                                                app.getId());
                                installBtn.setVisible(false);
                                installBtn.setManaged(false);
                                progressBox.setVisible(true);
                                progressBox.setManaged(true);
                                progressBar.setProgress(0);
                                progressLabel.setText("Preparing...");
                                statusLabel.setText("");

                                // Listen for detailed progress updates
                                installManager.addProgressListener(app.getId(), progress -> {
                                        Platform.runLater(() -> updateProgressDisplay(
                                                        progressBar,
                                                        progressLabel,
                                                        progress));
                                });

                                installManager
                                                .installApp(app)
                                                .thenAccept(result -> {
                                                        Platform.runLater(() -> {
                                                                LOG.info(
                                                                                "Installation completed for app: {} (id: {}, version: {})",
                                                                                app.getName(),
                                                                                app.getId(),
                                                                                result.getVersion());

                                                                // Save to library with real paths
                                                                libraryService.installApp(
                                                                                app.getId(),
                                                                                app.getName(),
                                                                                app.getOwnerLogin(),
                                                                                app.getCategory() != null
                                                                                                ? app.getCategory()
                                                                                                : "Unknown",
                                                                                result.getVersion(),
                                                                                result.getFormattedSize(),
                                                                                result.getInstallPath(),
                                                                                result.getExecutablePath());

                                                                // Update UI to show "Open" button
                                                                progressBox.setVisible(false);
                                                                progressBox.setManaged(false);
                                                                installBtn.setVisible(true);
                                                                installBtn.setManaged(true);
                                                                installBtn.setText("OPEN");
                                                                installBtn.setStyle(
                                                                                "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 10 32; -fx-font-size: 13px; -fx-cursor: hand;");
                                                                installBtn.setDisable(false);
                                                                statusLabel.setText(
                                                                                "Installed " + result.getVersion());

                                                                installManager.clearProgressListeners();
                                                        });
                                                })
                                                .exceptionally(ex -> {
                                                        Platform.runLater(() -> {
                                                                LOG.error(
                                                                                "Installation error for app: {} (id: {})",
                                                                                app.getName(),
                                                                                app.getId(),
                                                                                ex);
                                                                String errorMsg = ex.getCause() != null
                                                                                ? ex.getCause().getMessage()
                                                                                : ex.getMessage();

                                                                // Show error and restore install button
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
                });

                // Button container - right aligned, vertically centered in header
                HBox buttonsBox = new HBox(12);
                buttonsBox.setAlignment(Pos.CENTER_RIGHT);

                // Remove button (only visible when installed) - red
                Button removeBtn = new Button("Remove");
                FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
                trashIcon.setIconSize(14);
                trashIcon.setIconColor(Color.WHITE);
                removeBtn.setGraphic(trashIcon);
                removeBtn.setStyle(
                                "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 10 24; -fx-font-size: 13px; -fx-cursor: hand;");
                removeBtn.setVisible(isInstalled);
                removeBtn.setManaged(isInstalled);

                removeBtn.setOnAction(e -> {
                        LOG.info("Remove button clicked for app: {} (id: {})", app.getName(), app.getId());
                        libraryService.removeApp(app.getId());

                        // Update UI
                        removeBtn.setVisible(false);
                        removeBtn.setManaged(false);
                        installBtn.setText("GET");
                        installBtn.setStyle(
                                        "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 10 32; -fx-font-size: 13px; -fx-cursor: hand;");
                        statusLabel.setText("Removed");
                });

                // Order: OPEN/GET (green) then Remove (red)
                buttonsBox.getChildren().addAll(progressBox, statusLabel, installBtn, removeBtn);

                // Add buttons to the appHeader (same row as icon and metadata)
                appHeader.getChildren().add(buttonsBox);
                appHeader.setAlignment(Pos.CENTER_LEFT);

                headerSection.getChildren().addAll(backBtn, appHeader);

                // --- Description Section ---
                VBox descriptionSection = new VBox(12);
                Label descHeader = new Label("Description");
                descHeader.setStyle(
                                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");
                descriptionLabel = new Label(
                                app.getDescription() != null
                                                ? app.getDescription()
                                                : "This is a powerful application that helps you be more productive. With an intuitive interface and advanced features, "
                                                                +
                                                                app.getName()
                                                                + " is designed to streamline your workflow and enhance your experience. "
                                                                +
                                                                "Download now and discover why millions of users trust this app for their daily tasks.");
                descriptionLabel.setWrapText(true);
                descriptionLabel.setStyle(
                                "-fx-font-size: 13px; -fx-text-fill: #a1a1aa; -fx-line-spacing: 4px;");
                descriptionSection.getChildren().addAll(descHeader, descriptionLabel);

                // --- Screenshots Section ---
                VBox screenshotsSection = new VBox(16);
                Label screenshotsHeader = new Label("Screenshots");
                screenshotsHeader.setStyle(
                                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");

                // Horizontal row of screenshot placeholders
                HBox screenshotsRow = new HBox(16);

                // Create 3 placeholder boxes that will be populated with images
                java.util.List<StackPane> placeholders = new java.util.ArrayList<>();
                java.util.List<Label> placeholderLabels = new java.util.ArrayList<>();

                for (int i = 1; i <= 3; i++) {
                        VBox screenshotBox = new VBox(8);
                        screenshotBox.setAlignment(Pos.CENTER);

                        // Screenshot placeholder - larger size
                        StackPane placeholder = new StackPane();
                        placeholder.setPrefSize(300, 200);
                        placeholder.setMinHeight(200);
                        placeholder.setMaxWidth(Double.MAX_VALUE);
                        placeholder.setStyle(
                                        "-fx-background-color: #1a1a1a; -fx-background-radius: 12px; -fx-border-color: #2a2a2a; -fx-border-radius: 12px;");

                        // Loading indicator
                        ProgressIndicator loader = new ProgressIndicator();
                        loader.setMaxSize(24, 24);
                        placeholder.getChildren().add(loader);

                        placeholders.add(placeholder);
                        HBox.setHgrow(placeholder, Priority.ALWAYS);

                        // Label below
                        Label screenshotLabel = new Label("Screenshot " + i);
                        screenshotLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");
                        placeholderLabels.add(screenshotLabel);

                        screenshotBox.getChildren().addAll(placeholder, screenshotLabel);
                        HBox.setHgrow(screenshotBox, Priority.ALWAYS);
                        screenshotsRow.getChildren().add(screenshotBox);
                }

                screenshotsSection.getChildren().addAll(screenshotsHeader, screenshotsRow);

                // Load screenshots from API
                loadScreenshotsIntoRow(placeholders, placeholderLabels);

                // Load version from GitHub API
                loadVersionFromGitHub();

                // Assemble Main Layout
                mainLayout
                                .getChildren()
                                .addAll(headerSection, descriptionSection, screenshotsSection);

                setContent(mainLayout);
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
                                                if (release != null) {
                                                        versionLabel.setText(release.getTagName());

                                                        // Update changelog content
                                                        changelogContent.getChildren().clear();
                                                        Label header = new Label(
                                                                        "Version " + release.getTagName());
                                                        header.setStyle(
                                                                        "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #fafafa;");

                                                        Label body = new Label(
                                                                        release.getBody() != null
                                                                                        ? release.getBody()
                                                                                        : "No release notes");
                                                        body.setWrapText(true);
                                                        body.setStyle(
                                                                        "-fx-text-fill: #a1a1aa; -fx-font-size: 14px;");

                                                        Label published = new Label(
                                                                        "Published: " +
                                                                                        (release.getPublishedAt() != null
                                                                                                        ? release.getPublishedAt()
                                                                                                        : "Unknown"));
                                                        published.setStyle(
                                                                        "-fx-text-fill: #71717a; -fx-font-size: 12px;");

                                                        changelogContent
                                                                        .getChildren()
                                                                        .addAll(header, published, body);
                                                } else {
                                                        versionLabel.setText("N/A");
                                                        changelogContent.getChildren().clear();
                                                        Label noRelease = new Label(
                                                                        "No release information available");
                                                        noRelease.setStyle("-fx-text-fill: #71717a;");
                                                        changelogContent.getChildren().add(noRelease);
                                                }
                                        });
                                });
        }

        private void loadScreenshotsIntoRow(java.util.List<StackPane> placeholders, java.util.List<Label> labels) {
                ApiService.getInstance()
                                .getScreenshots(app.getId())
                                .thenAccept(urls -> {
                                        Platform.runLater(() -> {
                                                if (urls.isEmpty()) {
                                                        // Show "no screenshots" icon in each placeholder
                                                        for (int i = 0; i < placeholders.size(); i++) {
                                                                StackPane placeholder = placeholders.get(i);
                                                                placeholder.getChildren().clear();
                                                                FontIcon noImgIcon = new FontIcon(Feather.IMAGE);
                                                                noImgIcon.setIconSize(24);
                                                                noImgIcon.setIconColor(Color.web("#404040"));
                                                                placeholder.getChildren().add(noImgIcon);
                                                        }
                                                        return;
                                                }

                                                // Create HttpClient for loading images
                                                HttpClient httpClient = HttpClient.newBuilder()
                                                                .followRedirects(HttpClient.Redirect.NORMAL)
                                                                .connectTimeout(java.time.Duration.ofSeconds(30))
                                                                .build();

                                                // Load up to 3 screenshots
                                                int numToLoad = Math.min(urls.size(), placeholders.size());
                                                for (int i = 0; i < numToLoad; i++) {
                                                        String url = urls.get(i);
                                                        final int index = i;
                                                        final StackPane placeholder = placeholders.get(i);

                                                        LOG.debug("Loading screenshot {} for app {}: {}", index + 1,
                                                                        app.getName(), url);

                                                        // Load image asynchronously
                                                        loadImageWithHttpClient(httpClient, url)
                                                                        .thenAccept(imageBytes -> {
                                                                                Platform.runLater(() -> {
                                                                                        if (imageBytes != null
                                                                                                        && imageBytes.length > 0) {
                                                                                                try {
                                                                                                        javafx.scene.image.Image img = new javafx.scene.image.Image(
                                                                                                                        new ByteArrayInputStream(
                                                                                                                                        imageBytes));

                                                                                                        if (!img.isError()) {
                                                                                                                javafx.scene.image.ImageView view = new javafx.scene.image.ImageView(
                                                                                                                                img);
                                                                                                                view.setPreserveRatio(
                                                                                                                                true);
                                                                                                                view.setFitHeight(
                                                                                                                                190);
                                                                                                                view.fitWidthProperty()
                                                                                                                                .bind(placeholder
                                                                                                                                                .widthProperty()
                                                                                                                                                .subtract(10));

                                                                                                                placeholder.getChildren()
                                                                                                                                .clear();
                                                                                                                placeholder.getChildren()
                                                                                                                                .add(view);
                                                                                                                LOG.debug("Successfully loaded screenshot {} for app {}",
                                                                                                                                index + 1,
                                                                                                                                app.getName());
                                                                                                        } else {
                                                                                                                showPlaceholderIcon(
                                                                                                                                placeholder);
                                                                                                        }
                                                                                                } catch (Exception e) {
                                                                                                        LOG.warn("Failed to create image for screenshot {}: {}",
                                                                                                                        index + 1,
                                                                                                                        e.getMessage());
                                                                                                        showPlaceholderIcon(
                                                                                                                        placeholder);
                                                                                                }
                                                                                        } else {
                                                                                                showPlaceholderIcon(
                                                                                                                placeholder);
                                                                                        }
                                                                                });
                                                                        })
                                                                        .exceptionally(ex -> {
                                                                                Platform.runLater(
                                                                                                () -> showPlaceholderIcon(
                                                                                                                placeholder));
                                                                                return null;
                                                                        });
                                                }

                                                // Show placeholder icon for remaining slots
                                                for (int i = numToLoad; i < placeholders.size(); i++) {
                                                        showPlaceholderIcon(placeholders.get(i));
                                                }
                                        });
                                });
        }

        private void showPlaceholderIcon(StackPane placeholder) {
                placeholder.getChildren().clear();
                FontIcon noImgIcon = new FontIcon(Feather.IMAGE);
                noImgIcon.setIconSize(24);
                noImgIcon.setIconColor(Color.web("#404040"));
                placeholder.getChildren().add(noImgIcon);
        }

        private void loadScreenshots(StackPane gallery) {
                ApiService.getInstance()
                                .getScreenshots(app.getId())
                                .thenAccept(urls -> {
                                        Platform.runLater(() -> {
                                                gallery.getChildren().clear();

                                                if (urls.isEmpty()) {
                                                        FontIcon noImgIcon = new FontIcon(Feather.IMAGE);
                                                        noImgIcon.setIconSize(64);
                                                        noImgIcon.setIconColor(Color.web("#27272a"));
                                                        Label noImgLabel = new Label(
                                                                        "No screenshots available");
                                                        noImgLabel.setStyle(
                                                                        "-fx-text-fill: #52525b; -fx-font-size: 14px;");
                                                        VBox noImgBox = new VBox(16, noImgIcon, noImgLabel);
                                                        noImgBox.setAlignment(Pos.CENTER);
                                                        gallery.getChildren().add(noImgBox);
                                                        return;
                                                }

                                                // Create nodes for each screenshot - use HttpClient for GitHub URLs
                                                java.util.List<Node> galleryItems = new java.util.ArrayList<>();
                                                final int[] currentIndex = { 0 };
                                                final int[] loadedCount = { 0 };
                                                final int totalUrls = urls.size();

                                                // Counter label - declared early so it can be updated from progress
                                                // listeners
                                                final Label counterLabel = new Label("Loading...");
                                                counterLabel.setStyle(
                                                                "-fx-text-fill: #a1a1aa; -fx-font-size: 12px;");
                                                StackPane.setAlignment(counterLabel, Pos.BOTTOM_CENTER);
                                                counterLabel.setTranslateY(-10);

                                                // Create shared HttpClient for loading images with proper headers
                                                HttpClient httpClient = HttpClient.newBuilder()
                                                                .followRedirects(HttpClient.Redirect.NORMAL)
                                                                .connectTimeout(java.time.Duration.ofSeconds(30))
                                                                .build();

                                                for (int urlIndex = 0; urlIndex < urls.size(); urlIndex++) {
                                                        String url = urls.get(urlIndex);
                                                        final int imageIndex = urlIndex;

                                                        LOG.debug(
                                                                        "Loading screenshot {} for app {}: {}",
                                                                        imageIndex + 1,
                                                                        app.getName(),
                                                                        url);

                                                        if (url.toLowerCase().contains(".svg")) {
                                                                WebView webView = new WebView();
                                                                webView.setPageFill(Color.TRANSPARENT);
                                                                webView.getEngine().load(url);

                                                                webView.setPrefHeight(320);
                                                                webView
                                                                                .maxWidthProperty()
                                                                                .bind(gallery.widthProperty()
                                                                                                .subtract(40));
                                                                webView.setVisible(false);

                                                                galleryItems.add(webView);
                                                                gallery.getChildren().add(webView);

                                                                // Treat SVG as immediately "loaded" for the counter
                                                                loadedCount[0]++;

                                                                // Show first successfully loaded item
                                                                if (galleryItems
                                                                                .stream()
                                                                                .filter(v -> v != null && v.isVisible())
                                                                                .count() == 0) {
                                                                        webView.setVisible(true);
                                                                        currentIndex[0] = imageIndex;
                                                                }

                                                                if (loadedCount[0] >= totalUrls) {
                                                                        updateImageCounter(
                                                                                        galleryItems,
                                                                                        counterLabel,
                                                                                        currentIndex[0]);
                                                                }
                                                        } else {
                                                                // Create placeholder ImageView
                                                                javafx.scene.image.ImageView view = new javafx.scene.image.ImageView();
                                                                view.setPreserveRatio(true);
                                                                view.setFitHeight(320);
                                                                view
                                                                                .fitWidthProperty()
                                                                                .bind(gallery.widthProperty()
                                                                                                .subtract(40));
                                                                view.setVisible(false);

                                                                galleryItems.add(view);
                                                                gallery.getChildren().add(view);

                                                                // Load image asynchronously using HttpClient with
                                                                // proper headers
                                                                loadImageWithHttpClient(httpClient, url)
                                                                                .thenAccept(imageBytes -> {
                                                                                        Platform.runLater(() -> {
                                                                                                if (imageBytes != null
                                                                                                                &&
                                                                                                                imageBytes.length > 0) {
                                                                                                        try {
                                                                                                                javafx.scene.image.Image img = new javafx.scene.image.Image(
                                                                                                                                new ByteArrayInputStream(
                                                                                                                                                imageBytes));

                                                                                                                if (!img.isError()) {
                                                                                                                        view.setImage(img);
                                                                                                                        LOG.debug(
                                                                                                                                        "Successfully loaded screenshot {} for app {}: {}",
                                                                                                                                        imageIndex + 1,
                                                                                                                                        app.getName(),
                                                                                                                                        url);

                                                                                                                        // Show
                                                                                                                        // first
                                                                                                                        // successfully
                                                                                                                        // loaded
                                                                                                                        // image
                                                                                                                        if (galleryItems
                                                                                                                                        .stream()
                                                                                                                                        .filter(
                                                                                                                                                        v -> v != null &&
                                                                                                                                                                        v.isVisible())
                                                                                                                                        .count() == 0) {
                                                                                                                                view.setVisible(true);
                                                                                                                                currentIndex[0] = imageIndex;
                                                                                                                        }
                                                                                                                } else {
                                                                                                                        LOG.warn(
                                                                                                                                        "Image decode error for screenshot {} of app {}: {}",
                                                                                                                                        imageIndex + 1,
                                                                                                                                        app.getName(),
                                                                                                                                        url);
                                                                                                                        gallery
                                                                                                                                        .getChildren()
                                                                                                                                        .remove(view);
                                                                                                                        galleryItems.set(
                                                                                                                                        imageIndex,
                                                                                                                                        null);
                                                                                                                }
                                                                                                        } catch (Exception e) {
                                                                                                                LOG.warn(
                                                                                                                                "Failed to create image for screenshot {} of app {}: {} - {}",
                                                                                                                                imageIndex + 1,
                                                                                                                                app.getName(),
                                                                                                                                url,
                                                                                                                                e.getMessage(),
                                                                                                                                e);
                                                                                                                gallery
                                                                                                                                .getChildren()
                                                                                                                                .remove(view);
                                                                                                                galleryItems.set(
                                                                                                                                imageIndex,
                                                                                                                                null);
                                                                                                        }
                                                                                                } else {
                                                                                                        LOG.warn(
                                                                                                                        "Empty or null image data for screenshot {} of app {}: {}",
                                                                                                                        imageIndex + 1,
                                                                                                                        app.getName(),
                                                                                                                        url);
                                                                                                        gallery.getChildren()
                                                                                                                        .remove(view);
                                                                                                        galleryItems.set(
                                                                                                                        imageIndex,
                                                                                                                        null);
                                                                                                }

                                                                                                loadedCount[0]++;
                                                                                                // Update counter and
                                                                                                // check for all
                                                                                                // failures when all
                                                                                                // images attempted
                                                                                                if (loadedCount[0] >= totalUrls) {
                                                                                                        if (galleryItems
                                                                                                                        .stream()
                                                                                                                        .allMatch(v -> v == null)) {
                                                                                                                gallery.getChildren()
                                                                                                                                .clear();
                                                                                                                Label errorLabel = new Label(
                                                                                                                                "Failed to load screenshots");
                                                                                                                errorLabel.setStyle(
                                                                                                                                "-fx-text-fill: #71717a;");
                                                                                                                gallery
                                                                                                                                .getChildren()
                                                                                                                                .add(errorLabel);
                                                                                                        } else {
                                                                                                                updateImageCounter(
                                                                                                                                galleryItems,
                                                                                                                                counterLabel,
                                                                                                                                currentIndex[0]);
                                                                                                        }
                                                                                                }
                                                                                        });
                                                                                })
                                                                                .exceptionally(ex -> {
                                                                                        Platform.runLater(() -> {
                                                                                                LOG.warn(
                                                                                                                "HTTP error loading screenshot {} for app {}: {} - {}",
                                                                                                                imageIndex + 1,
                                                                                                                app.getName(),
                                                                                                                url,
                                                                                                                ex.getMessage(),
                                                                                                                ex);
                                                                                                gallery.getChildren()
                                                                                                                .remove(view);
                                                                                                galleryItems.set(
                                                                                                                imageIndex,
                                                                                                                null);

                                                                                                loadedCount[0]++;
                                                                                                if (loadedCount[0] >= totalUrls) {
                                                                                                        if (galleryItems
                                                                                                                        .stream()
                                                                                                                        .allMatch(v -> v == null)) {
                                                                                                                gallery.getChildren()
                                                                                                                                .clear();
                                                                                                                Label errorLabel = new Label(
                                                                                                                                "Failed to load screenshots");
                                                                                                                errorLabel.setStyle(
                                                                                                                                "-fx-text-fill: #71717a;");
                                                                                                                gallery
                                                                                                                                .getChildren()
                                                                                                                                .add(errorLabel);
                                                                                                        } else {
                                                                                                                updateImageCounter(
                                                                                                                                galleryItems,
                                                                                                                                counterLabel,
                                                                                                                                currentIndex[0]);
                                                                                                        }
                                                                                                }
                                                                                        });
                                                                                        return null;
                                                                                });
                                                        }
                                                }

                                                // Navigation arrows
                                                Button leftBtn = new Button();
                                                leftBtn.setGraphic(new FontIcon(Feather.CHEVRON_LEFT));
                                                leftBtn.setStyle(
                                                                "-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-cursor: hand;");

                                                Button rightBtn = new Button();
                                                rightBtn.setGraphic(new FontIcon(Feather.CHEVRON_RIGHT));
                                                rightBtn.setStyle(
                                                                "-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-cursor: hand;");

                                                leftBtn.setOnAction(e -> {
                                                        navigateImages(galleryItems, currentIndex, -1);
                                                        updateImageCounter(
                                                                        galleryItems,
                                                                        counterLabel,
                                                                        currentIndex[0]);
                                                });

                                                rightBtn.setOnAction(e -> {
                                                        navigateImages(galleryItems, currentIndex, 1);
                                                        updateImageCounter(
                                                                        galleryItems,
                                                                        counterLabel,
                                                                        currentIndex[0]);
                                                });

                                                HBox navBox = new HBox();
                                                navBox.setAlignment(Pos.CENTER);
                                                navBox.setPadding(new Insets(0, 20, 0, 20));
                                                Region spacer = new Region();
                                                HBox.setHgrow(spacer, Priority.ALWAYS);
                                                navBox.getChildren().addAll(leftBtn, spacer, rightBtn);

                                                gallery.getChildren().addAll(navBox, counterLabel);
                                                StackPane.setAlignment(navBox, Pos.CENTER);
                                        });
                                });
        }

        /**
         * Load an image from a URL using HttpClient with proper headers.
         * This is necessary because JavaFX's Image class doesn't send User-Agent
         * headers,
         * which causes GitHub's raw.githubusercontent.com to reject requests.
         */
        private CompletableFuture<byte[]> loadImageWithHttpClient(
                        HttpClient client,
                        String url) {
                try {
                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(url))
                                        .header("User-Agent", "Stars-AppStore/1.0 (JavaFX)")
                                        .header("Accept", "image/*")
                                        .timeout(java.time.Duration.ofSeconds(30))
                                        .GET()
                                        .build();

                        return client
                                        .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                                        .thenApply(response -> {
                                                if (response.statusCode() >= 200 &&
                                                                response.statusCode() < 300) {
                                                        return response.body();
                                                } else {
                                                        LOG.warn(
                                                                        "HTTP {} for screenshot URL: {} (app: {})",
                                                                        response.statusCode(),
                                                                        url,
                                                                        app.getName());
                                                        return null;
                                                }
                                        });
                } catch (Exception e) {
                        LOG.warn(
                                        "Failed to create HTTP request for screenshot URL: {} (app: {}) - {}",
                                        url,
                                        app.getName(),
                                        e.getMessage(),
                                        e);
                        return CompletableFuture.completedFuture(null);
                }
        }

        private void switchTab(String tabName) {
                tabButtons.forEach((name, btn) -> {
                        if (name.equals(tabName)) {
                                btn.setStyle(
                                                "-fx-background-color: #27272a; -fx-text-fill: #fafafa; -fx-background-radius: 16px; -fx-padding: 6 16; -fx-font-size: 13px; -fx-font-weight: bold;");
                        } else {
                                btn.setStyle(
                                                "-fx-background-color: transparent; -fx-text-fill: #a1a1aa; -fx-padding: 6 16; -fx-font-size: 13px; -fx-cursor: hand;");
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
                badge.setStyle(
                                "-fx-background-color: #181818; -fx-background-radius: 6px;");
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
                v.setStyle(
                                "-fx-text-fill: #fafafa; -fx-font-weight: bold; -fx-font-size: 14px;");
                return createInfoBox(label, v);
        }

        private VBox createInfoBox(String label, Label valueLabel) {
                VBox box = new VBox(8);
                box.setPadding(new Insets(16));
                box.setStyle(
                                "-fx-background-color: #040404; -fx-background-radius: 8px; -fx-border-color: #181818; -fx-border-radius: 8px;");

                Label l = new Label(label);
                l.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12px;");

                box.getChildren().addAll(l, valueLabel);
                return box;
        }

        /**
         * Update the inline progress display based on installation progress.
         */
        private void updateProgressDisplay(
                        ProgressBar progressBar,
                        Label progressLabel,
                        InstallationService.InstallProgress progress) {
                if (progress.isFailed()) {
                        progressLabel.setText("Failed: " + progress.getMessage());
                        progressLabel.setStyle(
                                        "-fx-text-fill: #ef4444; -fx-font-size: 11px;");
                        return;
                }

                // Calculate overall progress
                double overallProgress = 0;
                String message = progress.getMessage();

                switch (progress.getStage()) {
                        case FETCHING_RELEASE:
                                overallProgress = 0.05;
                                message = "Fetching release...";
                                break;
                        case DOWNLOADING:
                                overallProgress = 0.05 + (progress.getProgress() * 0.6);
                                message = String.format(
                                                "Downloading... %.0f%%",
                                                progress.getProgress() * 100);
                                break;
                        case EXTRACTING:
                                overallProgress = 0.70;
                                message = "Extracting...";
                                break;
                        case INSTALLING:
                                overallProgress = 0.80;
                                message = "Installing...";
                                break;
                        case VERIFYING:
                                overallProgress = 0.95;
                                message = "Verifying...";
                                break;
                        case COMPLETED:
                                overallProgress = 1.0;
                                message = "Complete!";
                                break;
                        default:
                                break;
                }

                progressBar.setProgress(overallProgress);
                progressLabel.setText(message);
                progressLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");
        }

        /**
         * Navigate through images, skipping null (failed) entries.
         */
        private void navigateImages(
                        java.util.List<Node> galleryItems,
                        int[] currentIndex,
                        int direction) {
                // Count valid images
                long validCount = galleryItems
                                .stream()
                                .filter(v -> v != null)
                                .count();
                if (validCount <= 1)
                        return;

                // Hide current
                Node current = galleryItems.get(currentIndex[0]);
                if (current != null) {
                        current.setVisible(false);
                }

                // Find next valid image
                int size = galleryItems.size();
                int next = currentIndex[0];
                for (int i = 0; i < size; i++) {
                        next = (next + direction + size) % size;
                        if (galleryItems.get(next) != null) {
                                break;
                        }
                }

                currentIndex[0] = next;
                Node nextView = galleryItems.get(next);
                if (nextView != null) {
                        nextView.setVisible(true);
                }
        }

        /**
         * Update the image counter label, accounting for null (failed) images.
         */
        private void updateImageCounter(
                        java.util.List<Node> galleryItems,
                        Label counterLabel,
                        int currentIndex) {
                // Count valid images
                long validCount = galleryItems
                                .stream()
                                .filter(v -> v != null)
                                .count();

                if (validCount == 0) {
                        counterLabel.setText("No images");
                        return;
                }

                // Find the position of current image among valid ones
                int position = 0;
                for (int i = 0; i <= currentIndex && i < galleryItems.size(); i++) {
                        if (galleryItems.get(i) != null) {
                                position++;
                        }
                }

                counterLabel.setText(position + " / " + validCount);
        }
}
