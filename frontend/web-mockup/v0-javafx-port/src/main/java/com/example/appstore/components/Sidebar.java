package com.example.appstore.components;

import com.example.appstore.service.InstallationManager;
import com.example.appstore.service.InstallationManager.InstallationState;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Claude-style minimalist sidebar with categories and user profile.
 */
public class Sidebar extends VBox {

    private static final Logger LOG = LogManager.getLogger(Sidebar.class);

    private final Consumer<String> onNavigate;
    private final VBox indicatorContainer;
    private InstallationIndicator indicator;
    private String currentIndicatorAppId;
    private String selectedNavItem = "Alle";
    private final java.util.Map<String, HBox> navButtons = new java.util.LinkedHashMap<>();

    // Categories matching the Claude design
    private static final String[] CATEGORIES = { "Alle", "AI", "Developer Tools", "Productivity", "Utilities",
            "Security" };
    private static final Feather[] CATEGORY_ICONS = {
            Feather.LAYOUT, Feather.CPU, Feather.CODE, Feather.ZAP, Feather.INFO, Feather.SHIELD
    };

    public Sidebar(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        getStyleClass().add("sidebar");
        setStyle("-fx-background-color: #0d0d0d;");

        // Header with Logo
        HBox header = new HBox(12);
        header.getStyleClass().add("sidebar-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(24, 24, 12, 24));

        // Logo box (terracotta with GitHub icon)
        StackPane logoBox = new StackPane();
        logoBox.setStyle("-fx-background-color: #d97757; -fx-background-radius: 4px;");
        logoBox.setMinSize(32, 32);
        logoBox.setPrefSize(32, 32);
        logoBox.setMaxSize(32, 32);
        FontIcon githubIcon = new FontIcon(Feather.GITHUB);
        githubIcon.setIconColor(Color.BLACK);
        githubIcon.setIconSize(20);
        logoBox.getChildren().add(githubIcon);

        Label title = new Label("GitStore");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 500; -fx-text-fill: #f0f0f0;");

        header.getChildren().addAll(logoBox, title);
        getChildren().add(header);

        // Navigation container with padding
        VBox navContainer = new VBox(2);
        navContainer.setPadding(new Insets(0, 16, 0, 16));

        // Categories section
        Label categoriesLabel = createSectionLabel("KATEGORIEN");
        navContainer.getChildren().add(categoriesLabel);

        for (int i = 0; i < CATEGORIES.length; i++) {
            HBox btn = createNavButton(CATEGORIES[i], CATEGORY_ICONS[i], CATEGORIES[i].equals("Alle"));
            navContainer.getChildren().add(btn);
            navButtons.put(CATEGORIES[i], btn);
        }

        // Account section
        Label accountLabel = createSectionLabel("MEIN ACCOUNT");
        VBox.setMargin(accountLabel, new Insets(16, 0, 0, 0));
        navContainer.getChildren().add(accountLabel);

        navContainer.getChildren().add(createNavButton("Installiert", Feather.DOWNLOAD, false));
        navContainer.getChildren().add(createNavButton("Einstellungen", Feather.SETTINGS, false));

        getChildren().add(navContainer);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        // Installation Indicator container
        indicatorContainer = new VBox();
        indicatorContainer.setStyle("-fx-padding: 12px; -fx-border-color: #2a2a2a; -fx-border-width: 1px 0 0 0;");
        indicatorContainer.setVisible(false);
        indicatorContainer.setManaged(false);
        getChildren().add(indicatorContainer);

        setupInstallationListener();
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px; -fx-font-weight: bold;");
        label.setPadding(new Insets(16, 8, 8, 8));
        return label;
    }

    private HBox createNavButton(String text, Feather icon, boolean isSelected) {
        HBox button = new HBox(12);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(8, 12, 8, 12));
        button.setStyle(isSelected ? "-fx-background-color: #222222; -fx-background-radius: 6px;"
                : "-fx-background-color: transparent; -fx-background-radius: 6px;");
        button.setCursor(javafx.scene.Cursor.HAND);

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        fontIcon.setIconColor(isSelected ? Color.web("#d97757") : Color.web("#999999"));

        Label label = new Label(text);
        label.setStyle(isSelected ? "-fx-text-fill: #d97757; -fx-font-size: 14px;"
                : "-fx-text-fill: #999999; -fx-font-size: 14px;");

        button.getChildren().addAll(fontIcon, label);

        // Hover effects
        button.setOnMouseEntered(e -> {
            if (!text.equals(selectedNavItem)) {
                button.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 6px;");
                fontIcon.setIconColor(Color.web("#cccccc"));
                label.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px;");
            }
        });

        button.setOnMouseExited(e -> {
            if (!text.equals(selectedNavItem)) {
                button.setStyle("-fx-background-color: transparent; -fx-background-radius: 6px;");
                fontIcon.setIconColor(Color.web("#999999"));
                label.setStyle("-fx-text-fill: #999999; -fx-font-size: 14px;");
            }
        });

        button.setOnMouseClicked(e -> {
            LOG.debug("Navigation clicked: {}", text);

            // Update selection state
            if (navButtons.containsKey(text)) {
                // Deselect previous
                if (navButtons.containsKey(selectedNavItem)) {
                    HBox prevBtn = navButtons.get(selectedNavItem);
                    prevBtn.setStyle("-fx-background-color: transparent; -fx-background-radius: 6px;");
                    if (prevBtn.getChildren().size() >= 2) {
                        ((FontIcon) prevBtn.getChildren().get(0)).setIconColor(Color.web("#999999"));
                        ((Label) prevBtn.getChildren().get(1)).setStyle("-fx-text-fill: #999999; -fx-font-size: 14px;");
                    }
                }

                // Select new
                selectedNavItem = text;
                button.setStyle("-fx-background-color: #222222; -fx-background-radius: 6px;");
                fontIcon.setIconColor(Color.web("#d97757"));
                label.setStyle("-fx-text-fill: #d97757; -fx-font-size: 14px;");
            }

            // Map category names to navigation targets
            String navTarget = mapCategoryToNavTarget(text);
            if (onNavigate != null)
                onNavigate.accept(navTarget);
        });

        return button;
    }

    private String mapCategoryToNavTarget(String category) {
        return switch (category) {
            case "Alle" -> "Discover";
            case "Installiert" -> "Library";
            case "Einstellungen" -> "Settings";
            default -> category;
        };
    }

    private void setupInstallationListener() {
        InstallationManager manager = InstallationManager.getInstance();

        manager.stateProperty().addListener((obs, oldState, newState) -> {
            if (oldState == null || oldState.getPhase() != newState.getPhase()) {
                LOG.debug("Installation phase changed: {} -> {} (app: {})",
                        oldState != null ? oldState.getPhase() : "null",
                        newState.getPhase(),
                        newState.getAppName());
            }
            updateIndicator(newState);
        });

        updateIndicator(manager.getState());
    }

    private void updateIndicator(InstallationState state) {
        if (state.isIdle()) {
            indicatorContainer.setVisible(false);
            indicatorContainer.setManaged(false);
            indicatorContainer.getChildren().clear();
            indicator = null;
            currentIndicatorAppId = null;
            LOG.debug("Hiding installation indicator");
        } else {
            if (indicator == null || !state.getAppId().equals(currentIndicatorAppId)) {
                LOG.info("Creating indicator for app: {}", state.getAppName());
                indicatorContainer.getChildren().clear();
                indicator = new InstallationIndicator(state.getAppName());
                currentIndicatorAppId = state.getAppId();
                indicator.setOnDismiss(v -> {
                    LOG.info("[Sidebar] User dismissed installation indicator");
                    InstallationManager.getInstance().dismiss();
                });
                indicatorContainer.getChildren().add(indicator);
            }

            updateIndicatorFromState(state);

            indicatorContainer.setVisible(true);
            indicatorContainer.setManaged(true);
        }
    }

    private void updateIndicatorFromState(InstallationState state) {
        if (indicator == null)
            return;

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
                indicator.fail(state.getErrorMessage() != null ? state.getErrorMessage() : "Installation failed");
                break;
            default:
                break;
        }
    }
}
