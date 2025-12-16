package com.example.appstore.components;

import com.example.appstore.service.InstallationManager;
import com.example.appstore.service.InstallationManager.InstallationState;

import java.util.function.Consumer;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class Sidebar extends VBox {

    private static final Logger LOG = Logger.getLogger(Sidebar.class.getName());

    private final Consumer<String> onNavigate;
    private final VBox indicatorContainer;
    private InstallationIndicator indicator;
    private String currentIndicatorAppId;  // Track which app the indicator is for

    public Sidebar(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        getStyleClass().add("sidebar");

        // Header (Logo)
        HBox header = new HBox(12);
        header.getStyleClass().add("sidebar-header");
        header.setAlignment(Pos.CENTER_LEFT);

        // Logo Icon (White rounded square with icon)
        HBox logoIcon = new HBox();
        logoIcon.setStyle(
            "-fx-background-color: white; -fx-background-radius: 8px; -fx-min-width: 32px; -fx-min-height: 32px;"
        );
        logoIcon.setAlignment(Pos.CENTER);
        FontIcon appIcon = new FontIcon(Feather.BOX);
        appIcon.setIconColor(Color.BLACK);
        appIcon.setIconSize(20);
        logoIcon.getChildren().add(appIcon);

        Label title = new Label("AppVault");
        title.getStyleClass().add("sidebar-logo-text");

        header.getChildren().addAll(logoIcon, title);
        getChildren().add(header);

        // Navigation Items
        addNavButton("Discover", Feather.HOME, true);
        addNavButton("Library", Feather.DOWNLOAD, false);
        addNavButton("Updates", Feather.REFRESH_CW, false, 3);
        addNavButton("Settings", Feather.SETTINGS, false);

        // Categories
        addSectionLabel("CATEGORIES");
        addNavButton("Developer Tools", Feather.CODE, false);
        addNavButton("Productivity", Feather.BRIEFCASE, false);
        addNavButton("Graphics & Design", Feather.PEN_TOOL, false);
        addNavButton("Games", Feather.PLAY, false);
        addNavButton("Music & Audio", Feather.MUSIC, false);
        addNavButton("Video", Feather.VIDEO, false);
        addNavButton("Utilities", Feather.TOOL, false);
        addNavButton("Security", Feather.SHIELD, false);

        // Spacer to push Installation Indicator to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        // Installation Indicator container (initially hidden)
        indicatorContainer = new VBox();
        indicatorContainer.setStyle("-fx-padding: 12px; -fx-border-color: #181818; -fx-border-width: 1px 0 0 0;");
        indicatorContainer.setVisible(false);
        indicatorContainer.setManaged(false);
        getChildren().add(indicatorContainer);

        // Listen to InstallationManager state changes
        setupInstallationListener();
    }

    private void setupInstallationListener() {
        InstallationManager manager = InstallationManager.getInstance();
        
        // Listen to state changes
        manager.stateProperty().addListener((obs, oldState, newState) -> {
            LOG.fine("[Sidebar] Installation state changed: " + 
                     (oldState != null ? oldState.getPhase() : "null") + " -> " + newState.getPhase());
            updateIndicator(newState);
        });

        // Check initial state
        updateIndicator(manager.getState());
    }

    private void updateIndicator(InstallationState state) {
        if (state.isIdle()) {
            // Hide indicator
            indicatorContainer.setVisible(false);
            indicatorContainer.setManaged(false);
            indicatorContainer.getChildren().clear();
            indicator = null;
            currentIndicatorAppId = null;
            LOG.fine("[Sidebar] Hiding installation indicator");
        } else {
            // Only create new indicator if app changed
            if (indicator == null || !state.getAppId().equals(currentIndicatorAppId)) {
                LOG.info("[Sidebar] Creating indicator for: " + state.getAppName());
                indicatorContainer.getChildren().clear();
                indicator = new InstallationIndicator(state.getAppName());
                currentIndicatorAppId = state.getAppId();
                indicator.setOnDismiss(v -> {
                    LOG.info("[Sidebar] User dismissed installation indicator");
                    InstallationManager.getInstance().dismiss();
                });
                indicatorContainer.getChildren().add(indicator);
            }

            // Update indicator state
            updateIndicatorFromState(state);

            indicatorContainer.setVisible(true);
            indicatorContainer.setManaged(true);
        }
    }

    private void updateIndicatorFromState(InstallationState state) {
        if (indicator == null) return;

        switch (state.getPhase()) {
            case FETCHING:
                indicator.update(InstallationIndicator.Step.DOWNLOADING, "Fetching release...");
                indicator.setStatus(InstallationIndicator.Status.PROCESSING);
                break;
            case DOWNLOADING:
                String msg = String.format("Downloading... %.0f%%", state.getProgress() * 100);
                indicator.update(InstallationIndicator.Step.DOWNLOADING, msg);
                indicator.setStatus(InstallationIndicator.Status.PROCESSING);
                break;
            case EXTRACTING:
                indicator.update(InstallationIndicator.Step.EXTRACTING, "Extracting...");
                indicator.setStatus(InstallationIndicator.Status.PROCESSING);
                break;
            case INSTALLING:
                indicator.update(InstallationIndicator.Step.INSTALLING, state.getMessage());
                indicator.setStatus(InstallationIndicator.Status.PROCESSING);
                break;
            case VERIFYING:
                indicator.update(InstallationIndicator.Step.VERIFYING, "Verifying...");
                indicator.setStatus(InstallationIndicator.Status.PROCESSING);
                break;
            case COMPLETED:
                indicator.complete();
                break;
            case FAILED:
                indicator.fail(state.getErrorMessage() != null ? 
                               state.getErrorMessage() : "Installation failed");
                break;
            default:
                break;
        }
    }

    private void addSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("sidebar-section-label");
        getChildren().add(label);
    }

    private void addNavButton(String text, Feather icon, boolean isSelected) {
        addNavButton(text, icon, isSelected, 0);
    }

    private void addNavButton(
        String text,
        Feather icon,
        boolean isSelected,
        int badgeCount
    ) {
        HBox button = new HBox(12);
        button.getStyleClass().add("nav-button");
        if (isSelected) {
            button.setStyle("-fx-text-fill: white;");
        }

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(18);

        Label label = new Label(text);
        HBox.setHgrow(label, Priority.ALWAYS);

        button.getChildren().addAll(fontIcon, label);

        if (badgeCount > 0) {
            Label badge = new Label(String.valueOf(badgeCount));
            badge.setStyle(
                "-fx-background-color: white; -fx-text-fill: black; -fx-background-radius: 10px; -fx-padding: 0 6px; -fx-font-size: 11px; -fx-font-weight: bold;"
            );
            button.getChildren().add(badge);
        }

        getChildren().add(button);

        button.setOnMouseClicked(e -> {
            LOG.fine("[Sidebar] Navigation clicked: " + text);
            if (onNavigate != null) onNavigate.accept(text);
        });
    }
}
