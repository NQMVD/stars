package com.example.appstore.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class SettingsView extends ScrollPane {

    public SettingsView() {
        setFitToWidth(true);
        setStyle(
            "-fx-background-color: transparent; -fx-background: transparent;"
        );

        VBox content = new VBox(32);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: #09090b;");

        // Header
        Label title = new Label("Settings");
        title.getStyleClass().add("h1");
        content.getChildren().add(title);

        // General Section
        content.getChildren().add(createSectionHeader("General"));
        content
            .getChildren()
            .add(
                createSettingRow(
                    "Language",
                    createComboBox("English", "Spanish", "French", "German")
                )
            );
        content
            .getChildren()
            .add(
                createSettingRow(
                    "Startup",
                    createCheckBox("Launch AppVault on startup", true)
                )
            );
        content
            .getChildren()
            .add(
                createSettingRow(
                    "Notifications",
                    createCheckBox("Enable desktop notifications", true)
                )
            );

        content.getChildren().add(new Separator());

        // Appearance Section
        content.getChildren().add(createSectionHeader("Appearance"));
        content
            .getChildren()
            .add(
                createSettingRow(
                    "Theme",
                    createComboBox("Dark", "Light", "System")
                )
            );
        content
            .getChildren()
            .add(
                createSettingRow(
                    "Accent Color",
                    createColorPickerMock("#fafafa")
                )
            );
        content
            .getChildren()
            .add(
                createSettingRow(
                    "Compact Mode",
                    createCheckBox("Reduce spacing in lists", false)
                )
            );

        content.getChildren().add(new Separator());

        // Account Section
        content.getChildren().add(createSectionHeader("Account"));
        content
            .getChildren()
            .add(createSettingRow("Username", createTextField("John Doe")));
        content
            .getChildren()
            .add(
                createSettingRow(
                    "Email",
                    createTextField("john.doe@example.com")
                )
            );
        content
            .getChildren()
            .add(createSettingRow("Subscription", new Label("Free Plan")));

        HBox accountActions = new HBox(16);
        Button manageBtn = new Button("Manage Subscription");
        manageBtn.setStyle(
            "-fx-background-color: #27272a; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8 16; -fx-cursor: hand;"
        );
        Button logoutBtn = new Button("Log Out");
        logoutBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-border-color: #ef4444; -fx-border-radius: 6px; -fx-padding: 7 16; -fx-cursor: hand;"
        );
        accountActions.getChildren().addAll(manageBtn, logoutBtn);
        content.getChildren().add(accountActions);

        content.getChildren().add(new Separator());

        // About Section
        content.getChildren().add(createSectionHeader("About"));
        VBox aboutBox = new VBox(4);
        Label appName = new Label("AppVault Desktop");
        appName.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label version = new Label("Version 1.0.0-beta");
        version.setStyle("-fx-text-fill: #a1a1aa;");
        aboutBox.getChildren().addAll(appName, version);
        content.getChildren().add(aboutBox);

        Button checkUpdatesBtn = new Button("Check for Updates");
        checkUpdatesBtn.setStyle(
            "-fx-background-color: #27272a; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8 16; -fx-cursor: hand;"
        );
        content.getChildren().add(checkUpdatesBtn);

        setContent(content);
    }

    private Label createSectionHeader(String text) {
        Label label = new Label(text);
        label.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #fafafa;"
        );
        return label;
    }

    private VBox createSettingRow(String labelText, javafx.scene.Node control) {
        VBox row = new VBox(8);
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 14px;");
        row.getChildren().addAll(label, control);
        return row;
    }

    private ComboBox<String> createComboBox(String... items) {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll(items);
        box.setValue(items[0]);
        box.setStyle(
            "-fx-background-color: #27272a; -fx-text-fill: white; -fx-mark-color: white;"
        );
        box.setPrefWidth(200);
        return box;
    }

    private CheckBox createCheckBox(String text, boolean selected) {
        CheckBox box = new CheckBox(text);
        box.setSelected(selected);
        box.setStyle("-fx-text-fill: #fafafa;");
        return box;
    }

    private TextField createTextField(String text) {
        TextField field = new TextField(text);
        field.setStyle(
            "-fx-background-color: #27272a; -fx-text-fill: white; -fx-background-radius: 6px;"
        );
        field.setPrefWidth(300);
        return field;
    }

    private HBox createColorPickerMock(String colorHex) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        Region colorPreview = new Region();
        colorPreview.setPrefSize(24, 24);
        colorPreview.setStyle(
            "-fx-background-color: " +
                colorHex +
                "; -fx-background-radius: 12px; -fx-border-color: #27272a; -fx-border-radius: 12px;"
        );
        Label label = new Label(colorHex);
        label.setStyle("-fx-text-fill: #fafafa;");
        box.getChildren().addAll(colorPreview, label);
        return box;
    }
}
