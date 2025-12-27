package com.example.appstore.components;

import com.example.appstore.model.App;
import com.example.appstore.service.ApiService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Claude-style compact app card with icon, stars, and version tag.
 */
public class StandardCard extends VBox {

    private final String title;
    private final String vendor;
    private final App app;
    private final FontIcon chevronIcon;
    private final Label titleLabel;
    private final Label versionTag;

    public StandardCard(
            String title,
            String vendor,
            String rating,
            String count,
            boolean isInstalled,
            Runnable onClick,
            App app) {
        this.title = title;
        this.vendor = vendor;
        this.app = app;

        setStyle(
                "-fx-background-color: #161616; " +
                        "-fx-border-color: #2a2a2a; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px;");
        setPadding(new Insets(20));
        setSpacing(16);
        setCursor(javafx.scene.Cursor.HAND);
        setMinWidth(180);
        setMaxWidth(Double.MAX_VALUE);

        // Header row: Icon + Stars (top-right like featured cards)
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.TOP_LEFT);

        // Icon box
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(40, 40);
        iconBox.setPrefSize(40, 40);
        iconBox.setMaxSize(40, 40);
        iconBox.setStyle("-fx-background-color: #222222; -fx-background-radius: 4px;");

        // Category-based icon
        FontIcon appIcon;
        if (app != null && "AI".equals(app.getCategory())) {
            appIcon = new FontIcon(Feather.CPU);
            appIcon.setIconColor(Color.web("#d97757"));
        } else {
            appIcon = new FontIcon(Feather.CODE);
            appIcon.setIconColor(Color.web("#999999"));
        }
        appIcon.setIconSize(20);
        iconBox.getChildren().add(appIcon);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        // Stars in top-right (like featured cards)
        HBox starsRow = new HBox(4);
        starsRow.setAlignment(Pos.CENTER_RIGHT);
        FontIcon starIcon = new FontIcon(Feather.STAR);
        starIcon.setIconSize(12);
        starIcon.setIconColor(Color.web("#666666"));
        Label starsLabel = new Label(rating);
        starsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        starsRow.getChildren().addAll(starIcon, starsLabel);

        headerRow.getChildren().addAll(iconBox, headerSpacer, starsRow);

        // Title
        titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 500; -fx-text-fill: #f0f0f0;");

        // Vendor
        Label vendorLabel = new Label("von " + vendor);
        vendorLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        VBox.setMargin(vendorLabel, new Insets(-12, 0, 0, 0));

        // Description
        String desc = app != null && app.getDescription() != null
                ? app.getDescription()
                : "Keine Beschreibung verfügbar";
        Label descLabel = new Label(desc);
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(36);
        descLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

        // Footer with tags and chevron
        VBox footer = new VBox(16);
        footer.setStyle("-fx-border-color: #222222; -fx-border-width: 1px 0 0 0; -fx-padding: 16 0 0 0;");

        HBox footerRow = new HBox();
        footerRow.setAlignment(Pos.CENTER_LEFT);

        // Tags
        HBox tagsBox = new HBox(4);
        tagsBox.setAlignment(Pos.CENTER_LEFT);

        if (app != null && app.getCategory() != null) {
            Label categoryTag = createTag(app.getCategory());
            tagsBox.getChildren().add(categoryTag);
        }

        // Version tag - will be updated from API
        versionTag = createTag("...");
        tagsBox.getChildren().add(versionTag);

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        // Chevron indicator
        chevronIcon = new FontIcon(Feather.CHEVRON_RIGHT);
        chevronIcon.setIconSize(14);
        chevronIcon.setIconColor(Color.web("#333333"));

        footerRow.getChildren().addAll(tagsBox, footerSpacer, chevronIcon);
        footer.getChildren().add(footerRow);

        getChildren().addAll(headerRow, titleLabel, vendorLabel, descLabel, footer);

        // Hover effects
        setOnMouseEntered(e -> {
            setStyle(
                    "-fx-background-color: #1a1a1a; " +
                            "-fx-border-color: #2a2a2a; " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 8px; " +
                            "-fx-background-radius: 8px;");
            iconBox.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 4px;");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 500; -fx-text-fill: #ffffff;");
            chevronIcon.setIconColor(Color.web("#d97757"));
        });

        setOnMouseExited(e -> {
            setStyle(
                    "-fx-background-color: #161616; " +
                            "-fx-border-color: #2a2a2a; " +
                            "-fx-border-width: 1px; " +
                            "-fx-border-radius: 8px; " +
                            "-fx-background-radius: 8px;");
            iconBox.setStyle("-fx-background-color: #222222; -fx-background-radius: 4px;");
            titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 500; -fx-text-fill: #f0f0f0;");
            chevronIcon.setIconColor(Color.web("#333333"));
        });

        setOnMouseClicked(e -> {
            if (onClick != null)
                onClick.run();
        });

        // Load version from API
        if (app != null) {
            loadVersion();
        }
    }

    private void loadVersion() {
        ApiService.getInstance()
                .getLatestRelease(app.getId())
                .thenAccept(release -> {
                    Platform.runLater(() -> {
                        if (release != null && release.getTagName() != null) {
                            versionTag.setText(release.getTagName());
                        } else {
                            versionTag.setText("N/A");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> versionTag.setText("N/A"));
                    return null;
                });
    }

    private Label createTag(String text) {
        Label tag = new Label(text);
        tag.setStyle(
                "-fx-background-color: #222222; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-font-size: 9px; " +
                        "-fx-padding: 2px 6px; " +
                        "-fx-background-radius: 2px;");
        return tag;
    }

    public String getTitle() {
        return title;
    }

    public String getVendor() {
        return vendor;
    }

    public App getApp() {
        return app;
    }

    // Legacy constructors for backward compatibility
    public enum GradientType {
        BLUE, PURPLE, PINK, ORANGE, TEAL, INDIGO, DEFAULT
    }

    public StandardCard(
            String title,
            String vendor,
            String rating,
            String count,
            boolean isInstalled,
            Runnable onClick) {
        this(title, vendor, rating, count, isInstalled, onClick, (App) null);
    }

    public StandardCard(
            String title,
            String vendor,
            String rating,
            String count,
            boolean isInstalled,
            Runnable onClick,
            GradientType gradientType) {
        this(title, vendor, rating, count, isInstalled, onClick, (App) null);
    }

    public StandardCard(
            String title,
            String vendor,
            String rating,
            String count,
            boolean isInstalled,
            Runnable onClick,
            GradientType gradientType,
            App app) {
        this(title, vendor, rating, count, isInstalled, onClick, app);
    }
}
