package com.example.appstore.views;

import com.example.appstore.model.InstalledApp;
import com.example.appstore.service.InstallationService;
import com.example.appstore.service.LibraryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Claude-style library view showing installed apps.
 */
public class LibraryView extends ScrollPane {

    private final Map<String, Button> tabButtons = new HashMap<>();
    private final Map<String, Node> tabContentNodes = new HashMap<>();
    private final VBox contentContainer = new VBox(16);
    private final LibraryService libraryService;
    private final Label subtitleLabel;

    public LibraryView() {
        libraryService = LibraryService.getInstance();

        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox mainLayout = new VBox(32);
        mainLayout.setPadding(new Insets(32));
        mainLayout.setStyle("-fx-background-color: #111111;");

        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(8);
        Label title = new Label("Meine Bibliothek");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 300; -fx-text-fill: #ffffff;");

        subtitleLabel = new Label(libraryService.getInstalledCount() + " Apps installiert");
        subtitleLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(title, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Aktualisieren");
        FontIcon refreshIcon = new FontIcon(Feather.REFRESH_CW);
        refreshIcon.setIconSize(14);
        refreshIcon.setIconColor(Color.web("#f0f0f0"));
        refreshBtn.setGraphic(refreshIcon);
        refreshBtn.setGraphicTextGap(8);
        refreshBtn.setStyle(
                "-fx-background-color: #222222; " +
                        "-fx-text-fill: #f0f0f0; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-cursor: hand;");
        refreshBtn.setOnMouseEntered(e -> refreshBtn.setStyle(
                "-fx-background-color: #2a2a2a; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-cursor: hand;"));
        refreshBtn.setOnMouseExited(e -> refreshBtn.setStyle(
                "-fx-background-color: #222222; " +
                        "-fx-text-fill: #f0f0f0; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-cursor: hand;"));
        refreshBtn.setOnAction(e -> refreshLibrary());

        header.getChildren().addAll(titleBox, spacer, refreshBtn);

        // Tabs
        HBox tabs = new HBox(8);
        tabs.getChildren().addAll(
                createTab("Installiert"),
                createTab("Käufe"),
                createTab("Wunschliste"));

        // Placeholder Content
        VBox purchasesList = new VBox(24);
        purchasesList.setAlignment(Pos.CENTER);
        purchasesList.setPadding(new Insets(48));
        Label purchasesLabel = new Label("Keine Käufe gefunden");
        purchasesLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
        purchasesList.getChildren().add(purchasesLabel);

        VBox wishlistList = new VBox(24);
        wishlistList.setAlignment(Pos.CENTER);
        wishlistList.setPadding(new Insets(48));
        Label wishlistLabel = new Label("Deine Wunschliste ist leer");
        wishlistLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
        wishlistList.getChildren().add(wishlistLabel);

        tabContentNodes.put("Käufe", purchasesList);
        tabContentNodes.put("Wunschliste", wishlistList);

        mainLayout.getChildren().addAll(header, tabs, contentContainer);
        setContent(mainLayout);

        switchTab("Installiert");
    }

    private void refreshLibrary() {
        libraryService.loadLibrary();
        subtitleLabel.setText(libraryService.getInstalledCount() + " Apps installiert");
        switchTab("Installiert");
    }

    private VBox buildInstalledList() {
        VBox installedList = new VBox(12);
        List<InstalledApp> apps = libraryService.getInstalledApps();

        if (apps.isEmpty()) {
            Label emptyLabel = new Label("Noch keine Apps installiert. Installiere Apps über Entdecken.");
            emptyLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
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
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setStyle(
                "-fx-background-color: #161616; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-border-color: #2a2a2a; " +
                        "-fx-border-radius: 8px;");

        // Icon
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(48, 48);
        iconBox.setPrefSize(48, 48);
        iconBox.setMaxSize(48, 48);
        iconBox.setStyle("-fx-background-color: #222222; -fx-background-radius: 8px;");
        FontIcon appIcon = new FontIcon(Feather.CODE);
        appIcon.setIconSize(24);
        appIcon.setIconColor(Color.web("#999999"));
        iconBox.getChildren().add(appIcon);

        // App info
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(app.getName());
        nameLabel.setStyle("-fx-text-fill: #f0f0f0; -fx-font-weight: 500; -fx-font-size: 16px;");

        Label developerLabel = new Label("von " + app.getDeveloper());
        developerLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

        HBox metaRow = new HBox(12);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label versionLabel = new Label(app.getInstalledVersion());
        versionLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px; -fx-font-family: monospace;");

        Label sizeLabel = new Label(app.getSize());
        sizeLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");

        metaRow.getChildren().addAll(versionLabel, sizeLabel);

        info.getChildren().addAll(nameLabel, developerLabel, metaRow);

        // Buttons
        Button openBtn = new Button("Öffnen");
        FontIcon openIcon = new FontIcon(Feather.EXTERNAL_LINK);
        openIcon.setIconSize(14);
        openIcon.setIconColor(Color.BLACK);
        openBtn.setGraphic(openIcon);
        openBtn.setGraphicTextGap(6);
        openBtn.setStyle(
                "-fx-background-color: #d97757; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-font-weight: 500; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-cursor: hand;");
        openBtn.setOnMouseEntered(e -> openBtn.setStyle(
                "-fx-background-color: #e08a6d; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-font-weight: 500; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-cursor: hand;"));
        openBtn.setOnMouseExited(e -> openBtn.setStyle(
                "-fx-background-color: #d97757; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-font-weight: 500; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-cursor: hand;"));
        openBtn.setOnAction(e -> {
            if (app.getExecutablePath() != null) {
                try {
                    InstallationService.getInstance().launchApp(app.getExecutablePath());
                } catch (Exception ex) {
                    // Ignore
                }
            }
        });

        Button removeBtn = new Button("Entfernen");
        removeBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-cursor: hand;");
        removeBtn.setOnMouseEntered(e -> removeBtn.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-text-fill: #ef4444; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-border-color: #ef4444; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-cursor: hand;"));
        removeBtn.setOnMouseExited(e -> removeBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-cursor: hand;"));
        removeBtn.setOnAction(e -> {
            removeBtn.setDisable(true);
            removeBtn.setText("Entferne...");
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(0.3), ev -> {
                        libraryService.removeApp(app.getId());
                        refreshLibrary();
                    }));
            timeline.play();
        });

        row.getChildren().addAll(iconBox, info, openBtn, removeBtn);
        return row;
    }

    private void switchTab(String tabName) {
        tabButtons.forEach((name, btn) -> {
            if (name.equals(tabName)) {
                btn.setStyle(
                        "-fx-background-color: #222222; " +
                                "-fx-text-fill: #d97757; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-font-weight: 500; " +
                                "-fx-padding: 8px 16px; " +
                                "-fx-cursor: hand;");
            } else {
                btn.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: #666666; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-font-weight: 500; " +
                                "-fx-padding: 8px 16px; " +
                                "-fx-cursor: hand;");
            }
        });

        contentContainer.getChildren().clear();
        if (tabName.equals("Installiert")) {
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
        btn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> {
            if (!tabButtons.entrySet().stream()
                    .filter(entry -> entry.getValue() == btn)
                    .findFirst()
                    .map(entry -> entry.getKey().equals("Installiert"))
                    .orElse(false)) {
                btn.setStyle(
                        "-fx-background-color: #1a1a1a; " +
                                "-fx-text-fill: #999999; " +
                                "-fx-background-radius: 6px; " +
                                "-fx-font-weight: 500; " +
                                "-fx-padding: 8px 16px; " +
                                "-fx-cursor: hand;");
            }
        });
        btn.setOnAction(e -> switchTab(text));
        tabButtons.put(text, btn);
        return btn;
    }
}
