package mainpackage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/**
 * Installation progress indicator component matching the web mockup.
 * Shows 4-step progress: Downloading → Transferring → Installing → Verifying
 */
public class InstallationIndicator extends VBox {

    public enum Status {
        PROCESSING, PAUSED, COMPLETED, ERROR
    }

    private static final String[] STEP_LABELS = {"Downloading", "Transferring", "Installing", "Verifying"};
    private static final String[] STEP_ICONS = {
        "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3", // Download
        "M17 1l4 4-4 4M3 11V9a4 4 0 0 1 4-4h14M7 23l-4-4 4-4M21 13v2a4 4 0 0 1-4 4H3", // Repeat/Transfer
        "M16.5 9.4l-9-5.19M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z", // Package
        "M22 11.08V12a10 10 0 1 1-5.93-9.14M22 4L12 14.01l-3-3" // Check Circle
    };

    // Colors matching the design
    private static final String BLUE_BG = "rgba(59, 130, 246, 0.2)";
    private static final String BLUE_FG = "#60a5fa";
    private static final String EMERALD_BG = "rgba(16, 185, 129, 0.2)";
    private static final String EMERALD_FG = "#34d399";
    private static final String RED_BG = "rgba(239, 68, 68, 0.2)";
    private static final String RED_FG = "#f87171";
    private static final String AMBER_BG = "rgba(245, 158, 11, 0.2)";
    private static final String AMBER_FG = "#fbbf24";
    private static final String BG_CARD = "#16161a";
    private static final String BORDER_COLOR = "#2a2a32";
    private static final String SECONDARY_BG = "#27272a";

    private int currentStep = 1;
    private Status status = Status.PROCESSING;
    private boolean isPaused = false;

    private final String appName;
    private final StackPane iconContainer;
    private final SVGPath stepIcon;
    private final Label statusLabel;
    private final HBox progressBar;
    private final HBox[] progressSegments;
    private final SVGPath controlIcon;
    private final StackPane controlButton;
    private Timeline autoAdvance;

    public InstallationIndicator(String appName, int initialStep, Status initialStatus) {
        this.appName = appName;
        this.currentStep = Math.max(1, Math.min(4, initialStep));
        this.status = initialStatus;

        setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 8; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 8; -fx-border-width: 1; -fx-padding: 12;");
        setSpacing(0);

        // Top row: Icon, text info, control button
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Icon container
        iconContainer = new StackPane();
        iconContainer.setMinSize(32, 32);
        iconContainer.setMaxSize(32, 32);
        iconContainer.setStyle("-fx-background-radius: 16;");

        stepIcon = new SVGPath();
        stepIcon.setContent(STEP_ICONS[currentStep - 1]);
        stepIcon.setFill(Color.TRANSPARENT);
        stepIcon.setStrokeWidth(1.5);
        stepIcon.setScaleX(0.6);
        stepIcon.setScaleY(0.6);
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
        controlButton.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-background-radius: 16; -fx-cursor: hand;");

        controlIcon = new SVGPath();
        controlIcon.setContent("M6 4h4v16H6zM14 4h4v16h-4z"); // Pause icon
        controlIcon.setFill(Color.web("#fafafa"));
        controlIcon.setScaleX(0.5);
        controlIcon.setScaleY(0.5);
        controlButton.getChildren().add(controlIcon);

        controlButton.setOnMouseClicked(e -> handleControlClick());
        controlButton.setOnMouseEntered(e -> controlButton.setStyle("-fx-background-color: #3f3f46; -fx-background-radius: 16; -fx-cursor: hand;"));
        controlButton.setOnMouseExited(e -> controlButton.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-background-radius: 16; -fx-cursor: hand;"));

        topRow.getChildren().addAll(iconContainer, textInfo, controlButton);

        // Progress bar (4 segments)
        progressBar = new HBox(6);
        progressBar.setPadding(new Insets(8, 0, 0, 0));

        progressSegments = new HBox[4];
        for (int i = 0; i < 4; i++) {
            HBox segment = new HBox();
            segment.setMinHeight(6);
            segment.setMaxHeight(6);
            segment.setStyle("-fx-background-radius: 3;");
            HBox.setHgrow(segment, Priority.ALWAYS);
            progressSegments[i] = segment;
            progressBar.getChildren().add(segment);
        }

        getChildren().addAll(topRow, progressBar);

        // Initial render
        updateUI();

        // Start auto-advance demo
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
        stepIcon.setContent(STEP_ICONS[currentStep - 1]);

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

        iconContainer.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 16;");
        stepIcon.setStroke(Color.web(fgColor));

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
            controlIcon.setContent("M18 6L6 18M6 6l12 12"); // X icon
        } else if (isPaused) {
            controlIcon.setContent("M5 3l14 9-14 9V3z"); // Play icon
        } else {
            controlIcon.setContent("M6 4h4v16H6zM14 4h4v16h-4z"); // Pause icon
        }

        // Update progress segments
        if (status == Status.COMPLETED) {
            // Merge into one continuous green bar
            progressBar.setSpacing(0);
            for (int i = 0; i < 4; i++) {
                String radius;
                if (i == 0) {
                    radius = "3 0 0 3";
                } else if (i == 3) {
                    radius = "0 3 3 0";
                } else {
                    radius = "0";
                }
                progressSegments[i].setStyle("-fx-background-color: " + EMERALD_FG + "; -fx-background-radius: " + radius + ";");
            }
        } else {
            // Segmented progress bar
            progressBar.setSpacing(6);
            for (int i = 0; i < 4; i++) {
                String segmentColor;
                if (i < currentStep - 1) {
                    segmentColor = BLUE_FG;
                } else if (i == currentStep - 1) {
                    if (status == Status.ERROR) {
                        segmentColor = RED_FG;
                    } else if (isPaused) {
                        segmentColor = AMBER_FG;
                    } else {
                        segmentColor = BLUE_FG;
                    }
                } else {
                    segmentColor = SECONDARY_BG;
                }
                progressSegments[i].setStyle("-fx-background-color: " + segmentColor + "; -fx-background-radius: 3;");
            }
        }
    }
}
