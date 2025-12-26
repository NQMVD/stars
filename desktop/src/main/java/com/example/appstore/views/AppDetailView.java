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

        // Back Button
        Button backBtn = new Button("Back");
        FontIcon backIcon = new FontIcon(Feather.ARROW_LEFT);
        backIcon.setIconColor(Color.web("#fafafa"));
        backIcon.setIconSize(16);
        backBtn.setGraphic(backIcon);
        backBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #fafafa; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0;"
        );
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
        iconBox.setStyle(
            "-fx-background-color: #27272a; -fx-background-radius: 24px;"
        );
        FontIcon appIcon = new FontIcon(Feather.BOX);
        appIcon.setIconSize(64);
        appIcon.setIconColor(Color.WHITE);
        iconBox.getChildren().add(appIcon);

        // Title & Metadata
        VBox metaBox = new VBox(8);
        HBox.setHgrow(metaBox, Priority.ALWAYS);

        Label titleLabel = new Label(app.getName());
        titleLabel.setStyle(
            "-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #fafafa;"
        );

        Label vendorLabel = new Label(app.getOwnerLogin());
        vendorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #a1a1aa;");

        HBox platforms = new HBox(8);
        platforms
            .getChildren()
            .addAll(
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
        FontIcon dlIcon = new FontIcon(
            isInstalled ? Feather.CHECK : Feather.DOWNLOAD
        );
        dlIcon.setIconColor(Color.BLACK);
        installBtn.setGraphic(dlIcon);

        if (isInstalled) {
            installBtn.setStyle(
                "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 10 24; -fx-font-size: 14px; -fx-cursor: hand;"
            );
            dlIcon.setIconColor(Color.WHITE);
        } else {
            installBtn.setStyle(
                "-fx-background-color: #fafafa; -fx-text-fill: #010101; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 10 24; -fx-font-size: 14px; -fx-cursor: hand;"
            );
        }

        // Simplified inline progress display
        VBox progressBox = new VBox(6);
        progressBox.setAlignment(Pos.CENTER_RIGHT);
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
                Platform.runLater(() ->
                    updateProgressDisplay(progressBar, progressLabel, progress)
                );
            });
        }

        installBtn.setOnAction(e -> {
            LOG.info(
                "Install button clicked for app: {} (id: {})",
                app.getName(),
                app.getId()
            );

            if (libraryService.isInstalled(app.getId())) {
                // Already installed - launch the app
                LOG.info(
                    "App already installed, attempting to launch: {} (id: {})",
                    app.getName(),
                    app.getId()
                );
                Optional<InstalledApp> installedApp =
                    libraryService.getInstalledApp(app.getId());
                if (
                    installedApp.isPresent() &&
                    installedApp.get().getExecutablePath() != null
                ) {
                    String execPath = installedApp.get().getExecutablePath();
                    LOG.info(
                        "Launching app from: {} (app: {})",
                        execPath,
                        app.getName()
                    );
                    installBtn.setText("Launching...");
                    installBtn.setDisable(true);

                    try {
                        InstallationService.getInstance().launchApp(execPath);
                        LOG.info(
                            "App launched successfully: {} (executable: {})",
                            app.getName(),
                            execPath
                        );
                        statusLabel.setText("Launched!");
                    } catch (Exception ex) {
                        LOG.warn(
                            "Failed to launch app: {} (executable: {})",
                            app.getName(),
                            execPath,
                            ex
                        );
                        statusLabel.setText(
                            "Failed to launch: " + ex.getMessage()
                        );
                    }

                    Timeline reset = new Timeline(
                        new KeyFrame(Duration.seconds(1), ev -> {
                            installBtn.setText("Open");
                            installBtn.setDisable(false);
                            statusLabel.setText("");
                        })
                    );
                    reset.play();
                } else {
                    LOG.warn(
                        "No executable path found for installed app: {} (id: {})",
                        app.getName(),
                        app.getId()
                    );
                    statusLabel.setText(
                        "Path not found - reinstall recommended"
                    );
                }
            } else {
                // Check if another installation is in progress
                if (installManager.isInstalling()) {
                    LOG.warn(
                        "Another installation is in progress, cannot start installation for: {}",
                        app.getName()
                    );
                    statusLabel.setText("Another installation in progress");
                    return;
                }

                // Start installation via InstallationManager
                LOG.info(
                    "Starting installation for app: {} (id: {})",
                    app.getName(),
                    app.getId()
                );
                installBtn.setVisible(false);
                installBtn.setManaged(false);
                progressBox.setVisible(true);
                progressBox.setManaged(true);
                progressBar.setProgress(0);
                progressLabel.setText("Preparing...");
                statusLabel.setText("");

                // Listen for detailed progress updates
                installManager.addProgressListener(app.getId(), progress -> {
                    Platform.runLater(() ->
                        updateProgressDisplay(
                            progressBar,
                            progressLabel,
                            progress
                        )
                    );
                });

                installManager
                    .installApp(app)
                    .thenAccept(result -> {
                        Platform.runLater(() -> {
                            LOG.info(
                                "Installation completed for app: {} (id: {}, version: {})",
                                app.getName(),
                                app.getId(),
                                result.getVersion()
                            );

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
                                result.getExecutablePath()
                            );

                            // Update UI to show "Open" button
                            progressBox.setVisible(false);
                            progressBox.setManaged(false);
                            installBtn.setVisible(true);
                            installBtn.setManaged(true);
                            installBtn.setText("Open");
                            installBtn.setStyle(
                                "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 10 24; -fx-font-size: 14px; -fx-cursor: hand;"
                            );
                            FontIcon checkIcon = new FontIcon(Feather.CHECK);
                            checkIcon.setIconColor(Color.WHITE);
                            installBtn.setGraphic(checkIcon);
                            installBtn.setDisable(false);
                            statusLabel.setText(
                                "Installed " + result.getVersion()
                            );

                            installManager.clearProgressListeners();
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            LOG.error(
                                "Installation error for app: {} (id: {})",
                                app.getName(),
                                app.getId(),
                                ex
                            );
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

        // GitHub Button
        Button githubBtn = new Button();
        FontIcon githubIcon = new FontIcon(Feather.GITHUB);
        githubIcon.setIconColor(Color.WHITE);
        githubIcon.setIconSize(16);
        githubBtn.setGraphic(githubIcon);
        githubBtn.setStyle(
            "-fx-background-color: #1f2937; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 10 12; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: #374151; -fx-border-radius: 6px; -fx-border-width: 1;"
        );
        githubBtn.setOnAction(e -> {
            try {
                String githubUrl =
                    "https://github.com/" +
                    app.getOwnerLogin() +
                    "/" +
                    app.getId();
                Desktop.getDesktop().browse(new URI(githubUrl));
            } catch (Exception ex) {
                LOG.warn(
                    "Failed to open GitHub repo for app: {} (id: {})",
                    app.getName(),
                    app.getId(),
                    ex
                );
            }
        });

        // Button container - install and github buttons side by side
        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.getChildren().addAll(githubBtn, installBtn);

        VBox actionBox = new VBox(4);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.getChildren().addAll(buttonsBox, progressBox, statusLabel);

        actions.getChildren().add(actionBox);

        appHeader.getChildren().addAll(iconBox, metaBox, actions);
        headerSection.getChildren().addAll(backBtn, appHeader);

        // --- Gallery Section ---
        StackPane gallery = new StackPane();
        gallery.setPrefHeight(350);
        gallery.setMinHeight(350);
        gallery.setMaxHeight(350);
        gallery.setStyle(
            "-fx-background-color: #0d0d0d; -fx-background-radius: 12px; -fx-border-color: #181818; -fx-border-radius: 12px;"
        );
        // Clip content to prevent images from overflowing
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(gallery.widthProperty());
        clip.heightProperty().bind(gallery.heightProperty());
        clip.setArcWidth(24); // Match border-radius
        clip.setArcHeight(24);
        gallery.setClip(clip);

        // Loading state
        ProgressIndicator galleryLoader = new ProgressIndicator();
        galleryLoader.setMaxSize(40, 40);
        VBox loadingBox = new VBox(
            16,
            galleryLoader,
            new Label("Loading screenshots...")
        );
        loadingBox.setAlignment(Pos.CENTER);
        ((Label) loadingBox.getChildren().get(1)).setStyle(
            "-fx-text-fill: #71717a;"
        );
        gallery.getChildren().add(loadingBox);

        // Load screenshots from API
        loadScreenshots(gallery);

        // --- Tabs ---
        HBox tabs = new HBox(24);
        tabs.setPadding(new Insets(16, 0, 16, 0));
        tabs
            .getChildren()
            .addAll(createTab("Overview"), createTab("Changelog"));

        // --- Content Sections ---

        // 1. Overview Content
        VBox overviewContent = new VBox(32);

        // About
        VBox aboutSection = new VBox(16);
        Label aboutHeader = new Label("About this app");
        aboutHeader.setStyle(
            "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #fafafa;"
        );
        descriptionLabel = new Label(
            app.getDescription() != null
                ? app.getDescription()
                : "No description available"
        );
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle(
            "-fx-font-size: 14px; -fx-text-fill: #a1a1aa; -fx-line-spacing: 4px;"
        );
        aboutSection.getChildren().addAll(aboutHeader, descriptionLabel);

        // Info Grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(16);
        infoGrid.setVgap(16);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(25);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(25);
        infoGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

        versionLabel = new Label("Loading...");
        versionLabel.setStyle(
            "-fx-text-fill: #fafafa; -fx-font-weight: bold; -fx-font-size: 14px;"
        );
        infoGrid.add(createInfoBox("Version", versionLabel), 0, 0);
        infoGrid.add(
            createInfoBox(
                "Category",
                app.getCategory() != null ? app.getCategory() : "Unknown"
            ),
            1,
            0
        );
        infoGrid.add(createInfoBox("Owner", app.getOwnerLogin()), 2, 0);

        // Developer Footer
        HBox footer = new HBox(16);
        footer.setPadding(new Insets(24));
        footer.setStyle(
            "-fx-background-color: #040404; -fx-background-radius: 8px; -fx-border-color: #181818; -fx-border-radius: 8px;"
        );
        footer.setAlignment(Pos.CENTER_LEFT);
        StackPane devIcon = new StackPane();
        devIcon.setPrefSize(40, 40);
        devIcon.setStyle(
            "-fx-background-color: #181818; -fx-background-radius: 20px;"
        );
        Label devInitials = new Label(
            app.getOwnerLogin().substring(0, 1).toUpperCase()
        );
        devInitials.setStyle("-fx-text-fill: #fafafa; -fx-font-weight: bold;");
        devIcon.getChildren().add(devInitials);
        VBox devInfo = new VBox(2);
        Label devName = new Label(app.getOwnerLogin());
        devName.setStyle(
            "-fx-text-fill: #fafafa; -fx-font-weight: bold; -fx-font-size: 14px;"
        );
        Label devRole = new Label("Developer");
        devRole.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12px;");
        devInfo.getChildren().addAll(devName, devRole);
        footer.getChildren().addAll(devIcon, devInfo);

        overviewContent.getChildren().addAll(aboutSection, infoGrid, footer);

        // 2. Changelog Content
        VBox changelogContent = new VBox(16);
        Label changelogHeader = new Label("Latest Release");
        changelogHeader.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #fafafa;"
        );
        ProgressIndicator changelogLoader = new ProgressIndicator();
        changelogLoader.setMaxSize(30, 30);
        changelogContent.getChildren().addAll(changelogHeader, changelogLoader);

        // Register Content
        tabContentNodes.put("Overview", overviewContent);
        tabContentNodes.put("Changelog", changelogContent);

        // Assemble Main Layout
        mainLayout
            .getChildren()
            .addAll(headerSection, gallery, tabs, contentContainer);

        setContent(mainLayout);

        // Initialize
        switchTab("Overview");

        // Load release info from API
        loadReleaseInfo(changelogContent);
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
                            "Version " + release.getTagName()
                        );
                        header.setStyle(
                            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #fafafa;"
                        );

                        Label body = new Label(
                            release.getBody() != null
                                ? release.getBody()
                                : "No release notes"
                        );
                        body.setWrapText(true);
                        body.setStyle(
                            "-fx-text-fill: #a1a1aa; -fx-font-size: 14px;"
                        );

                        Label published = new Label(
                            "Published: " +
                                (release.getPublishedAt() != null
                                    ? release.getPublishedAt()
                                    : "Unknown")
                        );
                        published.setStyle(
                            "-fx-text-fill: #71717a; -fx-font-size: 12px;"
                        );

                        changelogContent
                            .getChildren()
                            .addAll(header, published, body);
                    } else {
                        versionLabel.setText("N/A");
                        changelogContent.getChildren().clear();
                        Label noRelease = new Label(
                            "No release information available"
                        );
                        noRelease.setStyle("-fx-text-fill: #71717a;");
                        changelogContent.getChildren().add(noRelease);
                    }
                });
            });
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
                            "No screenshots available"
                        );
                        noImgLabel.setStyle(
                            "-fx-text-fill: #52525b; -fx-font-size: 14px;"
                        );
                        VBox noImgBox = new VBox(16, noImgIcon, noImgLabel);
                        noImgBox.setAlignment(Pos.CENTER);
                        gallery.getChildren().add(noImgBox);
                        return;
                    }

                    // Create nodes for each screenshot - use HttpClient for GitHub URLs
                    java.util.List<Node> galleryItems =
                        new java.util.ArrayList<>();
                    final int[] currentIndex = { 0 };
                    final int[] loadedCount = { 0 };
                    final int totalUrls = urls.size();

                    // Counter label - declared early so it can be updated from progress listeners
                    final Label counterLabel = new Label("Loading...");
                    counterLabel.setStyle(
                        "-fx-text-fill: #a1a1aa; -fx-font-size: 12px;"
                    );
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
                            url
                        );

                        if (url.toLowerCase().contains(".svg")) {
                            WebView webView = new WebView();
                            webView.setPageFill(Color.TRANSPARENT);
                            webView.getEngine().load(url);

                            webView.setPrefHeight(320);
                            webView
                                .maxWidthProperty()
                                .bind(gallery.widthProperty().subtract(40));
                            webView.setVisible(false);

                            galleryItems.add(webView);
                            gallery.getChildren().add(webView);

                            // Treat SVG as immediately "loaded" for the counter
                            loadedCount[0]++;

                            // Show first successfully loaded item
                            if (
                                galleryItems
                                    .stream()
                                    .filter(v -> v != null && v.isVisible())
                                    .count() ==
                                0
                            ) {
                                webView.setVisible(true);
                                currentIndex[0] = imageIndex;
                            }

                            if (loadedCount[0] >= totalUrls) {
                                updateImageCounter(
                                    galleryItems,
                                    counterLabel,
                                    currentIndex[0]
                                );
                            }
                        } else {
                            // Create placeholder ImageView
                            javafx.scene.image.ImageView view =
                                new javafx.scene.image.ImageView();
                            view.setPreserveRatio(true);
                            view.setFitHeight(320);
                            view
                                .fitWidthProperty()
                                .bind(gallery.widthProperty().subtract(40));
                            view.setVisible(false);

                            galleryItems.add(view);
                            gallery.getChildren().add(view);

                            // Load image asynchronously using HttpClient with proper headers
                            loadImageWithHttpClient(httpClient, url)
                                .thenAccept(imageBytes -> {
                                    Platform.runLater(() -> {
                                        if (
                                            imageBytes != null &&
                                            imageBytes.length > 0
                                        ) {
                                            try {
                                                javafx.scene.image.Image img =
                                                    new javafx.scene.image.Image(
                                                        new ByteArrayInputStream(
                                                            imageBytes
                                                        )
                                                    );

                                                if (!img.isError()) {
                                                    view.setImage(img);
                                                    LOG.debug(
                                                        "Successfully loaded screenshot {} for app {}: {}",
                                                        imageIndex + 1,
                                                        app.getName(),
                                                        url
                                                    );

                                                    // Show first successfully loaded image
                                                    if (
                                                        galleryItems
                                                            .stream()
                                                            .filter(
                                                                v ->
                                                                    v != null &&
                                                                    v.isVisible()
                                                            )
                                                            .count() ==
                                                        0
                                                    ) {
                                                        view.setVisible(true);
                                                        currentIndex[0] =
                                                            imageIndex;
                                                    }
                                                } else {
                                                    LOG.warn(
                                                        "Image decode error for screenshot {} of app {}: {}",
                                                        imageIndex + 1,
                                                        app.getName(),
                                                        url
                                                    );
                                                    gallery
                                                        .getChildren()
                                                        .remove(view);
                                                    galleryItems.set(
                                                        imageIndex,
                                                        null
                                                    );
                                                }
                                            } catch (Exception e) {
                                                LOG.warn(
                                                    "Failed to create image for screenshot {} of app {}: {} - {}",
                                                    imageIndex + 1,
                                                    app.getName(),
                                                    url,
                                                    e.getMessage(),
                                                    e
                                                );
                                                gallery
                                                    .getChildren()
                                                    .remove(view);
                                                galleryItems.set(
                                                    imageIndex,
                                                    null
                                                );
                                            }
                                        } else {
                                            LOG.warn(
                                                "Empty or null image data for screenshot {} of app {}: {}",
                                                imageIndex + 1,
                                                app.getName(),
                                                url
                                            );
                                            gallery.getChildren().remove(view);
                                            galleryItems.set(imageIndex, null);
                                        }

                                        loadedCount[0]++;
                                        // Update counter and check for all failures when all images attempted
                                        if (loadedCount[0] >= totalUrls) {
                                            if (
                                                galleryItems
                                                    .stream()
                                                    .allMatch(v -> v == null)
                                            ) {
                                                gallery.getChildren().clear();
                                                Label errorLabel = new Label(
                                                    "Failed to load screenshots"
                                                );
                                                errorLabel.setStyle(
                                                    "-fx-text-fill: #71717a;"
                                                );
                                                gallery
                                                    .getChildren()
                                                    .add(errorLabel);
                                            } else {
                                                updateImageCounter(
                                                    galleryItems,
                                                    counterLabel,
                                                    currentIndex[0]
                                                );
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
                                            ex
                                        );
                                        gallery.getChildren().remove(view);
                                        galleryItems.set(imageIndex, null);

                                        loadedCount[0]++;
                                        if (loadedCount[0] >= totalUrls) {
                                            if (
                                                galleryItems
                                                    .stream()
                                                    .allMatch(v -> v == null)
                                            ) {
                                                gallery.getChildren().clear();
                                                Label errorLabel = new Label(
                                                    "Failed to load screenshots"
                                                );
                                                errorLabel.setStyle(
                                                    "-fx-text-fill: #71717a;"
                                                );
                                                gallery
                                                    .getChildren()
                                                    .add(errorLabel);
                                            } else {
                                                updateImageCounter(
                                                    galleryItems,
                                                    counterLabel,
                                                    currentIndex[0]
                                                );
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
                        "-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-cursor: hand;"
                    );

                    Button rightBtn = new Button();
                    rightBtn.setGraphic(new FontIcon(Feather.CHEVRON_RIGHT));
                    rightBtn.setStyle(
                        "-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-cursor: hand;"
                    );

                    leftBtn.setOnAction(e -> {
                        navigateImages(galleryItems, currentIndex, -1);
                        updateImageCounter(
                            galleryItems,
                            counterLabel,
                            currentIndex[0]
                        );
                    });

                    rightBtn.setOnAction(e -> {
                        navigateImages(galleryItems, currentIndex, 1);
                        updateImageCounter(
                            galleryItems,
                            counterLabel,
                            currentIndex[0]
                        );
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
     * This is necessary because JavaFX's Image class doesn't send User-Agent headers,
     * which causes GitHub's raw.githubusercontent.com to reject requests.
     */
    private CompletableFuture<byte[]> loadImageWithHttpClient(
        HttpClient client,
        String url
    ) {
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
                    if (
                        response.statusCode() >= 200 &&
                        response.statusCode() < 300
                    ) {
                        return response.body();
                    } else {
                        LOG.warn(
                            "HTTP {} for screenshot URL: {} (app: {})",
                            response.statusCode(),
                            url,
                            app.getName()
                        );
                        return null;
                    }
                });
        } catch (Exception e) {
            LOG.warn(
                "Failed to create HTTP request for screenshot URL: {} (app: {}) - {}",
                url,
                app.getName(),
                e.getMessage(),
                e
            );
            return CompletableFuture.completedFuture(null);
        }
    }

    private void switchTab(String tabName) {
        tabButtons.forEach((name, btn) -> {
            if (name.equals(tabName)) {
                btn.setStyle(
                    "-fx-background-color: #27272a; -fx-text-fill: #fafafa; -fx-background-radius: 16px; -fx-padding: 6 16; -fx-font-size: 13px; -fx-font-weight: bold;"
                );
            } else {
                btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #a1a1aa; -fx-padding: 6 16; -fx-font-size: 13px; -fx-cursor: hand;"
                );
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
            "-fx-background-color: #181818; -fx-background-radius: 6px;"
        );
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
            "-fx-text-fill: #fafafa; -fx-font-weight: bold; -fx-font-size: 14px;"
        );
        return createInfoBox(label, v);
    }

    private VBox createInfoBox(String label, Label valueLabel) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: #040404; -fx-background-radius: 8px; -fx-border-color: #181818; -fx-border-radius: 8px;"
        );

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
        InstallationService.InstallProgress progress
    ) {
        if (progress.isFailed()) {
            progressLabel.setText("Failed: " + progress.getMessage());
            progressLabel.setStyle(
                "-fx-text-fill: #ef4444; -fx-font-size: 11px;"
            );
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
                    progress.getProgress() * 100
                );
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
        int direction
    ) {
        // Count valid images
        long validCount = galleryItems
            .stream()
            .filter(v -> v != null)
            .count();
        if (validCount <= 1) return;

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
        int currentIndex
    ) {
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
