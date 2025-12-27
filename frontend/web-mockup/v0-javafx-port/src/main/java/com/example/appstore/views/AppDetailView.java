package com.example.appstore.views;

import com.example.appstore.model.App;
import com.example.appstore.model.InstalledApp;
import com.example.appstore.service.ApiService;
import com.example.appstore.service.InstallationManager;
import com.example.appstore.service.InstallationService;
import com.example.appstore.service.LibraryService;
import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Claude-style modal app detail view with clean layout.
 */
public class AppDetailView extends StackPane {

        private static final Logger LOG = LogManager.getLogger(AppDetailView.class);

        private final App app;
        private final Runnable onBack;
        private Label versionLabel;

        public AppDetailView(App app, Runnable onBack) {
                this.app = app;
                this.onBack = onBack;

                // Modal backdrop
                setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
                setAlignment(Pos.CENTER);

                // Modal card
                VBox modalCard = new VBox();
                modalCard.setMaxWidth(800);
                modalCard.setMaxHeight(700);
                modalCard.setStyle(
                                "-fx-background-color: #161616; " +
                                                "-fx-border-color: #333333; " +
                                                "-fx-border-width: 1px; " +
                                                "-fx-border-radius: 16px; " +
                                                "-fx-background-radius: 16px; " +
                                                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 40, 0, 0, 10);");

                // Clip for rounded corners
                Rectangle clip = new Rectangle();
                clip.widthProperty().bind(modalCard.widthProperty());
                clip.heightProperty().bind(modalCard.heightProperty());
                clip.setArcWidth(32);
                clip.setArcHeight(32);
                modalCard.setClip(clip);

                // Header
                HBox header = createHeader();

                // Scrollable content
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
                VBox.setVgrow(scrollPane, Priority.ALWAYS);

                VBox mainContent = createMainContent();
                mainContent.setPadding(new Insets(32));
                scrollPane.setContent(mainContent);

                // Footer
                HBox footer = createFooter();

                modalCard.getChildren().addAll(header, scrollPane, footer);
                getChildren().add(modalCard);

                // Close on backdrop click
                setOnMouseClicked(e -> {
                        if (e.getTarget() == this && onBack != null) {
                                onBack.run();
                        }
                });

                // Prevent modal card clicks from closing
                modalCard.setOnMouseClicked(e -> e.consume());

                // Load version from API
                loadVersionFromGitHub();
        }

        private HBox createHeader() {
                HBox header = new HBox(24);
                header.setAlignment(Pos.CENTER_LEFT);
                header.setPadding(new Insets(24));
                header.setStyle("-fx-border-color: #2a2a2a; -fx-border-width: 0 0 1px 0;");

                // App icon
                StackPane iconBox = new StackPane();
                iconBox.setMinSize(80, 80);
                iconBox.setPrefSize(80, 80);
                iconBox.setMaxSize(80, 80);
                iconBox.setStyle(
                                "-fx-background-color: #222222; " +
                                                "-fx-background-radius: 16px;");

                FontIcon appIcon = new FontIcon(Feather.GITHUB);
                appIcon.setIconSize(40);
                appIcon.setIconColor(Color.web("#d97757"));
                iconBox.getChildren().add(appIcon);

                // Title and metadata
                VBox titleBox = new VBox(8);
                HBox.setHgrow(titleBox, Priority.ALWAYS);

                Label titleLabel = new Label(app.getName());
                titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 300; -fx-text-fill: #f0f0f0;");

                // Metadata row
                HBox metaRow = new HBox(16);
                metaRow.setAlignment(Pos.CENTER_LEFT);

                // Stars
                HBox starsBox = new HBox(4);
                starsBox.setAlignment(Pos.CENTER);
                FontIcon starIcon = new FontIcon(Feather.STAR);
                starIcon.setIconSize(14);
                starIcon.setIconColor(Color.web("#666666"));
                Label starsLabel = new Label("12.4k");
                starsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
                starsBox.getChildren().addAll(starIcon, starsLabel);

                // Version
                versionLabel = new Label("v0.0.0");
                versionLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");

                // Category badge
                Label categoryBadge = new Label(app.getCategory() != null ? app.getCategory() : "Unknown");
                categoryBadge.setStyle(
                                "-fx-background-color: #222222; " +
                                                "-fx-text-fill: #999999; " +
                                                "-fx-font-size: 12px; " +
                                                "-fx-padding: 2px 8px; " +
                                                "-fx-background-radius: 4px;");

                metaRow.getChildren().addAll(starsBox, versionLabel, categoryBadge);
                titleBox.getChildren().addAll(titleLabel, metaRow);

                // Close button
                Button closeBtn = new Button();
                FontIcon closeIcon = new FontIcon(Feather.X);
                closeIcon.setIconSize(24);
                closeIcon.setIconColor(Color.web("#666666"));
                closeBtn.setGraphic(closeIcon);
                closeBtn.setStyle(
                                "-fx-background-color: transparent; " +
                                                "-fx-background-radius: 50%; " +
                                                "-fx-padding: 8px; " +
                                                "-fx-cursor: hand;");

                closeBtn.setOnMouseEntered(e -> {
                        closeBtn.setStyle(
                                        "-fx-background-color: #222222; " +
                                                        "-fx-background-radius: 50%; " +
                                                        "-fx-padding: 8px; " +
                                                        "-fx-cursor: hand;");
                        closeIcon.setIconColor(Color.WHITE);
                });

                closeBtn.setOnMouseExited(e -> {
                        closeBtn.setStyle(
                                        "-fx-background-color: transparent; " +
                                                        "-fx-background-radius: 50%; " +
                                                        "-fx-padding: 8px; " +
                                                        "-fx-cursor: hand;");
                        closeIcon.setIconColor(Color.web("#666666"));
                });

                closeBtn.setOnAction(e -> {
                        if (onBack != null)
                                onBack.run();
                });

                header.getChildren().addAll(iconBox, titleBox, closeBtn);
                return header;
        }

        private VBox createMainContent() {
                VBox content = new VBox(32);

                // Two-column layout
                GridPane grid = new GridPane();
                grid.setHgap(32);
                grid.setVgap(24);

                ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(66);
                col1.setHgrow(Priority.ALWAYS);

                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPercentWidth(34);
                col2.setHgrow(Priority.ALWAYS);

                grid.getColumnConstraints().addAll(col1, col2);

                // Left column - Description
                VBox leftColumn = new VBox(24);

                // About section
                VBox aboutSection = new VBox(12);

                Label aboutHeader = new Label("ÜBER DIESE APP");
                aboutHeader.setStyle(
                                "-fx-text-fill: #666666; " +
                                                "-fx-font-size: 11px; " +
                                                "-fx-font-weight: bold;");

                String description = app.getDescription() != null
                                ? "\"" + app.getDescription() + "\""
                                : "\"No description available\"";
                Label descriptionLabel = new Label(description);
                descriptionLabel.setWrapText(true);
                descriptionLabel.setStyle(
                                "-fx-text-fill: #aaaaaa; " +
                                                "-fx-font-size: 18px; " +
                                                "-fx-font-style: italic;");

                Label detailsLabel = new Label(
                                "Optimiert für Entwickler, die Wert auf Geschwindigkeit und Sicherheit legen. " +
                                                "Integriert sich nahtlos in deinen bestehenden Workflow und bietet umfangreiche "
                                                +
                                                "Anpassungsmöglichkeiten über die `config.yaml`.");
                detailsLabel.setWrapText(true);
                detailsLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");

                aboutSection.getChildren().addAll(aboutHeader, descriptionLabel, detailsLabel);

                // Info boxes
                HBox infoBoxes = new HBox(16);

                VBox techBox = createInfoBox("TECHNOLOGIE", "Rust / WebAssembly");
                VBox licenseBox = createInfoBox("LIZENZ", "MIT Open Source");

                HBox.setHgrow(techBox, Priority.ALWAYS);
                HBox.setHgrow(licenseBox, Priority.ALWAYS);

                infoBoxes.getChildren().addAll(techBox, licenseBox);

                leftColumn.getChildren().addAll(aboutSection, infoBoxes);

                // Right column - Buttons and developer
                VBox rightColumn = new VBox(16);

                // Install button
                Button installBtn = createInstallButton();

                // GitHub button
                Button githubBtn = new Button("GitHub Repo");
                FontIcon externalIcon = new FontIcon(Feather.EXTERNAL_LINK);
                externalIcon.setIconSize(18);
                externalIcon.setIconColor(Color.web("#cccccc"));
                githubBtn.setGraphic(externalIcon);
                githubBtn.setGraphicTextGap(8);
                githubBtn.setMaxWidth(Double.MAX_VALUE);
                githubBtn.setStyle(
                                "-fx-background-color: transparent; " +
                                                "-fx-border-color: #333333; " +
                                                "-fx-border-width: 1px; " +
                                                "-fx-border-radius: 8px; " +
                                                "-fx-background-radius: 8px; " +
                                                "-fx-text-fill: #cccccc; " +
                                                "-fx-padding: 12px 24px; " +
                                                "-fx-cursor: hand;");

                githubBtn.setOnMouseEntered(e -> githubBtn.setStyle(
                                "-fx-background-color: #222222; " +
                                                "-fx-border-color: #333333; " +
                                                "-fx-border-width: 1px; " +
                                                "-fx-border-radius: 8px; " +
                                                "-fx-background-radius: 8px; " +
                                                "-fx-text-fill: #cccccc; " +
                                                "-fx-padding: 12px 24px; " +
                                                "-fx-cursor: hand;"));

                githubBtn.setOnMouseExited(e -> githubBtn.setStyle(
                                "-fx-background-color: transparent; " +
                                                "-fx-border-color: #333333; " +
                                                "-fx-border-width: 1px; " +
                                                "-fx-border-radius: 8px; " +
                                                "-fx-background-radius: 8px; " +
                                                "-fx-text-fill: #cccccc; " +
                                                "-fx-padding: 12px 24px; " +
                                                "-fx-cursor: hand;"));

                githubBtn.setOnAction(e -> {
                        try {
                                String githubUrl = "https://github.com/" + app.getId();
                                Desktop.getDesktop().browse(new URI(githubUrl));
                                LOG.info("Opened GitHub page: {}", githubUrl);
                        } catch (Exception ex) {
                                LOG.warn("Failed to open GitHub page: {}", ex.getMessage());
                        }
                });

                // Developer section
                VBox developerSection = new VBox(16);
                developerSection.setPadding(new Insets(32, 0, 0, 0));

                Label devHeader = new Label("ENTWICKLER");
                devHeader.setStyle(
                                "-fx-text-fill: #444444; " +
                                                "-fx-font-size: 12px; " +
                                                "-fx-font-weight: bold;");

                HBox developerRow = new HBox(12);
                developerRow.setAlignment(Pos.CENTER_LEFT);

                StackPane devAvatar = new StackPane();
                devAvatar.setMinSize(32, 32);
                devAvatar.setPrefSize(32, 32);
                devAvatar.setMaxSize(32, 32);
                devAvatar.setStyle("-fx-background-color: #333333; -fx-background-radius: 4px;");

                Label devName = new Label(app.getOwnerLogin());
                devName.setStyle("-fx-text-fill: #999999; -fx-font-size: 14px;");

                developerRow.getChildren().addAll(devAvatar, devName);
                developerSection.getChildren().addAll(devHeader, developerRow);

                rightColumn.getChildren().addAll(installBtn, githubBtn, developerSection);

                grid.add(leftColumn, 0, 0);
                grid.add(rightColumn, 1, 0);

                content.getChildren().add(grid);
                return content;
        }

        private Button createInstallButton() {
                LibraryService libraryService = LibraryService.getInstance();
                boolean isInstalled = libraryService.isInstalled(app.getId());

                Button installBtn = new Button(isInstalled ? "Öffnen" : "Installieren");
                FontIcon icon = new FontIcon(isInstalled ? Feather.EXTERNAL_LINK : Feather.DOWNLOAD);
                icon.setIconSize(18);
                icon.setIconColor(Color.BLACK);
                installBtn.setGraphic(icon);
                installBtn.setGraphicTextGap(8);
                installBtn.setMaxWidth(Double.MAX_VALUE);
                installBtn.setStyle(
                                "-fx-background-color: #d97757; " +
                                                "-fx-text-fill: #000000; " +
                                                "-fx-font-weight: 600; " +
                                                "-fx-font-size: 14px; " +
                                                "-fx-background-radius: 8px; " +
                                                "-fx-padding: 12px 24px; " +
                                                "-fx-cursor: hand;");

                installBtn.setOnMouseEntered(e -> installBtn.setStyle(
                                "-fx-background-color: #e08a6d; " +
                                                "-fx-text-fill: #000000; " +
                                                "-fx-font-weight: 600; " +
                                                "-fx-font-size: 14px; " +
                                                "-fx-background-radius: 8px; " +
                                                "-fx-padding: 12px 24px; " +
                                                "-fx-cursor: hand;"));

                installBtn.setOnMouseExited(e -> installBtn.setStyle(
                                "-fx-background-color: #d97757; " +
                                                "-fx-text-fill: #000000; " +
                                                "-fx-font-weight: 600; " +
                                                "-fx-font-size: 14px; " +
                                                "-fx-background-radius: 8px; " +
                                                "-fx-padding: 12px 24px; " +
                                                "-fx-cursor: hand;"));

                installBtn.setOnAction(e -> handleInstallAction(installBtn, libraryService));

                return installBtn;
        }

        private void handleInstallAction(Button installBtn, LibraryService libraryService) {
                if (libraryService.isInstalled(app.getId())) {
                        // Launch app
                        Optional<InstalledApp> installedApp = libraryService.getInstalledApp(app.getId());
                        if (installedApp.isPresent() && installedApp.get().getExecutablePath() != null) {
                                String execPath = installedApp.get().getExecutablePath();
                                installBtn.setText("Starte...");
                                installBtn.setDisable(true);

                                try {
                                        InstallationService.getInstance().launchApp(execPath);
                                        LOG.info("App launched: {}", app.getName());
                                } catch (Exception ex) {
                                        LOG.warn("Failed to launch: {}", ex.getMessage());
                                }

                                Timeline reset = new Timeline(
                                                new KeyFrame(Duration.seconds(1), ev -> {
                                                        installBtn.setText("Öffnen");
                                                        installBtn.setDisable(false);
                                                }));
                                reset.play();
                        }
                } else {
                        // Start installation
                        InstallationManager installManager = InstallationManager.getInstance();

                        if (installManager.isInstalling()) {
                                LOG.warn("Another installation in progress");
                                return;
                        }

                        installBtn.setText("Installiere...");
                        installBtn.setDisable(true);

                        installManager.installApp(app)
                                        .thenAccept(result -> {
                                                Platform.runLater(() -> {
                                                        libraryService.installApp(
                                                                        app.getId(), app.getName(), app.getOwnerLogin(),
                                                                        app.getCategory() != null ? app.getCategory()
                                                                                        : "Unknown",
                                                                        result.getVersion(), result.getFormattedSize(),
                                                                        result.getInstallPath(),
                                                                        result.getExecutablePath());

                                                        installBtn.setText("Öffnen");
                                                        installBtn.setDisable(false);

                                                        FontIcon icon = new FontIcon(Feather.EXTERNAL_LINK);
                                                        icon.setIconSize(18);
                                                        icon.setIconColor(Color.BLACK);
                                                        installBtn.setGraphic(icon);
                                                });
                                        })
                                        .exceptionally(ex -> {
                                                Platform.runLater(() -> {
                                                        installBtn.setText("Fehler");
                                                        installBtn.setDisable(false);

                                                        Timeline reset = new Timeline(
                                                                        new KeyFrame(Duration.seconds(2), ev -> {
                                                                                installBtn.setText("Installieren");
                                                                        }));
                                                        reset.play();
                                                });
                                                return null;
                                        });
                }
        }

        private VBox createInfoBox(String label, String value) {
                VBox box = new VBox(8);
                box.setStyle(
                                "-fx-background-color: #1a1a1a; " +
                                                "-fx-border-color: #222222; " +
                                                "-fx-border-width: 1px; " +
                                                "-fx-border-radius: 8px; " +
                                                "-fx-background-radius: 8px; " +
                                                "-fx-padding: 16px;");

                Label labelNode = new Label(label);
                labelNode.setStyle(
                                "-fx-text-fill: #555555; " +
                                                "-fx-font-size: 10px; " +
                                                "-fx-font-weight: bold;");

                Label valueNode = new Label(value);
                valueNode.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 14px;");

                box.getChildren().addAll(labelNode, valueNode);
                return box;
        }

        private HBox createFooter() {
                HBox footer = new HBox();
                footer.setAlignment(Pos.CENTER_LEFT);
                footer.setPadding(new Insets(24));
                footer.setStyle(
                                "-fx-background-color: #0d0d0d; " +
                                                "-fx-border-color: #2a2a2a; " +
                                                "-fx-border-width: 1px 0 0 0;");

                // Verified badge
                HBox verifiedBox = new HBox(8);
                verifiedBox.setAlignment(Pos.CENTER_LEFT);

                FontIcon shieldIcon = new FontIcon(Feather.SHIELD);
                shieldIcon.setIconSize(14);
                shieldIcon.setIconColor(Color.web("#555555"));

                Label verifiedLabel = new Label("Verifiziertes Repository");
                verifiedLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");

                verifiedBox.getChildren().addAll(shieldIcon, verifiedLabel);

                Region footerSpacer = new Region();
                HBox.setHgrow(footerSpacer, Priority.ALWAYS);

                Label publishedLabel = new Label("Erstmals veröffentlicht: 2023");
                publishedLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");

                footer.getChildren().addAll(verifiedBox, footerSpacer, publishedLabel);
                return footer;
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
}
