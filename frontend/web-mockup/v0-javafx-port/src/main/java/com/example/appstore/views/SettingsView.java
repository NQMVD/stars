package com.example.appstore.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Claude-style settings view with German labels.
 */
public class SettingsView extends ScrollPane {

    public SettingsView() {
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox content = new VBox(32);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: #111111;");

        // Header
        Label title = new Label("Einstellungen");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 300; -fx-text-fill: #ffffff;");
        content.getChildren().add(title);

        // General Section
        content.getChildren().add(createSectionHeader("Allgemein"));
        content.getChildren().add(
                createSettingRow("Sprache", createComboBox("Deutsch", "English", "Español", "Français")));
        content.getChildren().add(
                createSettingRow("Autostart", createCheckBox("GitStore beim Start öffnen", false)));
        content.getChildren().add(
                createSettingRow("Benachrichtigungen", createCheckBox("Desktop-Benachrichtigungen aktivieren", true)));

        content.getChildren().add(createSeparator());

        // Appearance Section
        content.getChildren().add(createSectionHeader("Darstellung"));
        content.getChildren().add(
                createSettingRow("Theme", createComboBox("Dunkel", "Hell", "System")));
        content.getChildren().add(
                createSettingRow("Akzentfarbe", createColorPickerMock("#d97757")));
        content.getChildren().add(
                createSettingRow("Kompaktmodus", createCheckBox("Abstände in Listen reduzieren", false)));

        content.getChildren().add(createSeparator());

        // Downloads Section
        content.getChildren().add(createSectionHeader("Downloads"));
        content.getChildren().add(
                createSettingRow("Download-Ordner", createTextField("C:\\Users\\zkxkx\\.stars")));
        content.getChildren().add(
                createSettingRow("Automatische Updates", createCheckBox("Apps automatisch aktualisieren", true)));

        content.getChildren().add(createSeparator());

        // About Section
        content.getChildren().add(createSectionHeader("Über"));

        VBox aboutBox = new VBox(8);
        aboutBox.setStyle(
                "-fx-background-color: #161616; " +
                        "-fx-border-color: #2a2a2a; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-padding: 16px;");

        Label appName = new Label("GitStore Desktop");
        appName.setStyle("-fx-text-fill: #f0f0f0; -fx-font-weight: 500; -fx-font-size: 16px;");

        Label version = new Label("Version 4.2.0");
        version.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

        Label copyright = new Label("© 2024 Stardive. Alle Rechte vorbehalten.");
        copyright.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");

        aboutBox.getChildren().addAll(appName, version, copyright);
        content.getChildren().add(aboutBox);

        HBox aboutActions = new HBox(12);
        aboutActions.setPadding(new Insets(8, 0, 0, 0));

        Button checkUpdatesBtn = new Button("Nach Updates suchen");
        FontIcon updateIcon = new FontIcon(Feather.DOWNLOAD);
        updateIcon.setIconSize(14);
        updateIcon.setIconColor(Color.web("#f0f0f0"));
        checkUpdatesBtn.setGraphic(updateIcon);
        checkUpdatesBtn.setGraphicTextGap(8);
        checkUpdatesBtn.setStyle(
                "-fx-background-color: #222222; " +
                        "-fx-text-fill: #f0f0f0; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-cursor: hand;");
        checkUpdatesBtn.setOnMouseEntered(e -> checkUpdatesBtn.setStyle(
                "-fx-background-color: #2a2a2a; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-cursor: hand;"));
        checkUpdatesBtn.setOnMouseExited(e -> checkUpdatesBtn.setStyle(
                "-fx-background-color: #222222; " +
                        "-fx-text-fill: #f0f0f0; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-padding: 8px 16px; " +
                        "-fx-cursor: hand;"));

        Button githubBtn = new Button("GitHub");
        FontIcon githubIcon = new FontIcon(Feather.GITHUB);
        githubIcon.setIconSize(14);
        githubIcon.setIconColor(Color.web("#999999"));
        githubBtn.setGraphic(githubIcon);
        githubBtn.setGraphicTextGap(8);
        githubBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #999999; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-padding: 7px 16px; " +
                        "-fx-cursor: hand;");
        githubBtn.setOnMouseEntered(e -> {
            githubBtn.setStyle(
                    "-fx-background-color: #1a1a1a; " +
                            "-fx-text-fill: #cccccc; " +
                            "-fx-border-color: #444444; " +
                            "-fx-border-radius: 6px; " +
                            "-fx-background-radius: 6px; " +
                            "-fx-padding: 7px 16px; " +
                            "-fx-cursor: hand;");
            githubIcon.setIconColor(Color.web("#cccccc"));
        });
        githubBtn.setOnMouseExited(e -> {
            githubBtn.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #999999; " +
                            "-fx-border-color: #333333; " +
                            "-fx-border-radius: 6px; " +
                            "-fx-background-radius: 6px; " +
                            "-fx-padding: 7px 16px; " +
                            "-fx-cursor: hand;");
            githubIcon.setIconColor(Color.web("#999999"));
        });

        aboutActions.getChildren().addAll(checkUpdatesBtn, githubBtn);
        content.getChildren().add(aboutActions);

        setContent(content);
    }

    private Label createSectionHeader(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: 500; -fx-text-fill: #f0f0f0;");
        label.setPadding(new Insets(8, 0, 0, 0));
        return label;
    }

    private Region createSeparator() {
        Region separator = new Region();
        separator.setMinHeight(1);
        separator.setPrefHeight(1);
        separator.setMaxHeight(1);
        separator.setStyle("-fx-background-color: #2a2a2a;");
        return separator;
    }

    private VBox createSettingRow(String labelText, javafx.scene.Node control) {
        VBox row = new VBox(8);
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #999999; -fx-font-size: 12px;");
        row.getChildren().addAll(label, control);
        return row;
    }

    private ComboBox<String> createComboBox(String... items) {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(items);
        box.setValue(items[0]);
        box.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-text-fill: #f0f0f0; " +
                        "-fx-border-color: #2a2a2a; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-background-radius: 6px;");
        box.setPrefWidth(200);
        return box;
    }

    private CheckBox createCheckBox(String text, boolean selected) {
        CheckBox box = new CheckBox(text);
        box.setSelected(selected);
        box.setStyle("-fx-text-fill: #f0f0f0;");
        return box;
    }

    private TextField createTextField(String text) {
        TextField field = new TextField(text);
        field.setStyle(
                "-fx-background-color: #1a1a1a; " +
                        "-fx-text-fill: #f0f0f0; " +
                        "-fx-border-color: #2a2a2a; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-padding: 8px;");
        field.setPrefWidth(400);
        return field;
    }

    private HBox createColorPickerMock(String colorHex) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);

        Region colorPreview = new Region();
        colorPreview.setPrefSize(32, 32);
        colorPreview.setStyle(
                "-fx-background-color: " + colorHex + "; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-border-color: #2a2a2a; " +
                        "-fx-border-radius: 6px;");

        Label label = new Label(colorHex);
        label.setStyle("-fx-text-fill: #666666; -fx-font-family: monospace;");

        box.getChildren().addAll(colorPreview, label);
        return box;
    }
}
