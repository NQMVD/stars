package com.example.appstore.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
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
 * Installation progress indicator component matching the web mockup.
 * Shows 4-step progress: Downloading → Transferring → Installing → Verifying
 */
public class InstallationIndicator extends VBox {

    public enum Status {
        PROCESSING, PAUSED, COMPLETED, ERROR
    }

    private static final String[] STEP_LABELS = {"Downloading", "Transferring", "Installing", "Verifying"};
    private static final Feather[] STEP_ICONS = {Feather.DOWNLOAD, Feather.REPEAT, Feather.PACKAGE, Feather.CHECK_CIRCLE};

    // Colors matching web mockup
    private static final String BLUE_BG = "#3b82f633";
    private static final String BLUE_FG = "#60a5fa";
    private static final String EMERALD_BG = "#10b98133";
    private static final String EMERALD_FG = "#34d399";
    private static final String RED_BG = "#ef444433";
    private static final String RED_FG = "#f87171";
    private static final String AMBER_BG = "#f59e0b33";
    private static final String AMBER_FG = "#fbbf24";
    private static final String SECONDARY_BG = "#27272a";

    private int currentStep = 1;
    private Status status = Status.PROCESSING;
    private boolean isPaused = false;

    private final String appName;
    private final StackPane iconContainer;
    private final FontIcon stepIcon;
    private final Label statusLabel;
    private final HBox progressBar;
    private final HBox[] progressSegments;
    private final FontIcon controlIcon;
    private final StackPane controlButton;
    private Timeline autoAdvance;

    public InstallationIndicator(String appName, int initialStep, Status initialStatus) {
        this.appName = appName;
        this.currentStep = Math.max(1, Math.min(4, initialStep));
        this.status = initialStatus;

        getStyleClass().add("installation-indicator");
        setStyle("-fx-background-color: #040404; -fx-background-radius: 8px; -fx-border-color: #181818; -fx-border-radius: 8px; -fx-border-width: 1px; -fx-padding: 12px;");

        // Top row: Icon, text info, control button
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Icon container
        iconContainer = new StackPane();
        iconContainer.setMinSize(32, 32);
        iconContainer.setMaxSize(32, 32);
        iconContainer.setStyle("-fx-background-radius: 16px;");

        stepIcon = new FontIcon(STEP_ICONS[currentStep - 1]);
        stepIcon.setIconSize(16);
        iconContainer.getChildren().add(stepIcon);

        // Text info
        VBox textInfo = new VBox(2);
        HBox.setHgrow(textInfo, Priority.ALWAYS);

        Label nameLabel = new Label(appName);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #fafafa;");

        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 500;");

        textInfo.getChildren().addAll(nameLabel, statusLabel);

        // Control button
        controlButton = new StackPane();
        controlButton.setMinSize(32, 32);
        controlButton.setMaxSize(32, 32);
        controlButton.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-background-radius: 16px; -fx-cursor: hand;");

        controlIcon = new FontIcon(Feather.PAUSE);
        controlIcon.setIconSize(14);
        controlIcon.setIconColor(Color.web("#fafafa"));
        controlButton.getChildren().add(controlIcon);

        controlButton.setOnMouseClicked(e -> handleControlClick());
        controlButton.setOnMouseEntered(e -> controlButton.setStyle("-fx-background-color: #3f3f46; -fx-background-radius: 16px; -fx-cursor: hand;"));
        controlButton.setOnMouseExited(e -> controlButton.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-background-radius: 16px; -fx-cursor: hand;"));

        topRow.getChildren().addAll(iconContainer, textInfo, controlButton);

        // Progress bar (4 segments)
        progressBar = new HBox(6);
        progressBar.setPadding(new Insets(8, 0, 0, 0));

        progressSegments = new HBox[4];
        for (int i = 0; i < 4; i++) {
            HBox segment = new HBox();
            segment.setMinHeight(6);
            segment.setMaxHeight(6);
            segment.setStyle("-fx-background-radius: 3px;");
            HBox.setHgrow(segment, Priority.ALWAYS);
            progressSegments[i] = segment;
            progressBar.getChildren().add(segment);
        }

        getChildren().addAll(topRow, progressBar);

        // Initial render
        updateUI();

        // Start auto-advance demo (like web version)
        if (status == Status.PROCESSING && !isPaused) {
            startAutoAdvance();
        }
    }

    private void startAutoAdvance() {
        if (autoAdvance != null) {
            autoAdvance.stop();
        }
        autoAdvance = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (status == Status.PROCESSING && !isPaused) {
                if (currentStep >= 4) {
                    status = Status.COMPLETED;
                    autoAdvance.stop();
                } else {
                    currentStep++;
                }
                updateUI();
            }
        }));
        autoAdvance.setCycleCount(Timeline.INDEFINITE);
        autoAdvance.play();
    }

    private void handleControlClick() {
        if (status == Status.COMPLETED) {
            // Reset/dismiss
            currentStep = 1;
            status = Status.PROCESSING;
            isPaused = false;
            startAutoAdvance();
        } else {
            // Toggle pause
            isPaused = !isPaused;
            if (!isPaused && status == Status.PROCESSING) {
                startAutoAdvance();
            } else if (autoAdvance != null) {
                autoAdvance.pause();
            }
        }
        updateUI();
    }

    private void updateUI() {
        // Update icon and colors
        stepIcon.setIconCode(STEP_ICONS[currentStep - 1]);

        String bgColor, fgColor;
        if (status == Status.COMPLETED) {
            bgColor = EMERALD_BG;
            fgColor = EMERALD_FG;
        } else if (status == Status.ERROR) {
            bgColor = RED_BG;
            fgColor = RED_FG;
        } else if (isPaused) {
            bgColor = AMBER_BG;
            fgColor = AMBER_FG;
        } else {
            bgColor = BLUE_BG;
            fgColor = BLUE_FG;
        }

        iconContainer.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 16px;");
        stepIcon.setIconColor(Color.web(fgColor));

        // Update status text
        String statusText;
        if (status == Status.COMPLETED) {
            statusText = "COMPLETED";
        } else if (status == Status.ERROR) {
            statusText = "ERROR";
        } else if (isPaused) {
            statusText = "PAUSED";
        } else {
            statusText = STEP_LABELS[currentStep - 1];
        }
        statusLabel.setText(statusText);
        statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 500; -fx-text-fill: " + fgColor + ";");

        // Update control button icon
        if (status == Status.COMPLETED) {
            controlIcon.setIconCode(Feather.X);
        } else if (isPaused) {
            controlIcon.setIconCode(Feather.PLAY);
        } else {
            controlIcon.setIconCode(Feather.PAUSE);
        }

        // Update progress segments
        if (status == Status.COMPLETED) {
            // Merge into one continuous green bar
            progressBar.setSpacing(0);
            for (int i = 0; i < 4; i++) {
                String radius;
                if (i == 0) {
                    radius = "3px 0 0 3px"; // Left rounded
                } else if (i == 3) {
                    radius = "0 3px 3px 0"; // Right rounded
                } else {
                    radius = "0"; // No rounding for middle segments
                }
                progressSegments[i].setStyle("-fx-background-color: " + EMERALD_FG + "; -fx-background-radius: " + radius + ";");
            }
        } else {
            // Segmented progress bar
            progressBar.setSpacing(6);
            for (int i = 0; i < 4; i++) {
                String segmentColor;
                if (i < currentStep - 1) {
                    segmentColor = BLUE_FG; // Completed segments
                } else if (i == currentStep - 1) {
                    if (status == Status.ERROR) {
                        segmentColor = RED_FG;
                    } else if (isPaused) {
                        segmentColor = AMBER_FG;
                    } else {
                        segmentColor = BLUE_FG;
                    }
                } else {
                    segmentColor = SECONDARY_BG; // Future segments
                }
                progressSegments[i].setStyle("-fx-background-color: " + segmentColor + "; -fx-background-radius: 3px;");
            }
        }
    }
}
