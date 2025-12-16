package com.example.appstore.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Installation progress indicator component matching the web mockup.
 * Shows 4-step progress: Downloading → Extracting → Installing → Verifying
 * 
 * This component is externally controlled - call setStep() and setStatus() 
 * to update the display based on actual installation progress.
 */
public class InstallationIndicator extends VBox {

    private static final Logger LOG = Logger.getLogger(InstallationIndicator.class.getName());

    public enum Status {
        PROCESSING, PAUSED, COMPLETED, ERROR
    }

    public enum Step {
        DOWNLOADING(1, "Downloading", Feather.DOWNLOAD),
        EXTRACTING(2, "Extracting", Feather.REPEAT),
        INSTALLING(3, "Installing", Feather.PACKAGE),
        VERIFYING(4, "Verifying", Feather.CHECK_CIRCLE);

        private final int index;
        private final String label;
        private final Feather icon;

        Step(int index, String label, Feather icon) {
            this.index = index;
            this.label = label;
            this.icon = icon;
        }

        public int getIndex() { return index; }
        public String getLabel() { return label; }
        public Feather getIcon() { return icon; }
    }

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

    private Step currentStep = Step.DOWNLOADING;
    private Status status = Status.PROCESSING;
    private String customMessage = null;
    private Consumer<Void> onCancelClick;
    private Consumer<Void> onDismissClick;

    private final String appName;
    private final StackPane iconContainer;
    private final FontIcon stepIcon;
    private final Label statusLabel;
    private final HBox progressBar;
    private final HBox[] progressSegments;
    private final FontIcon controlIcon;
    private final StackPane controlButton;

    public InstallationIndicator(String appName) {
        this(appName, Step.DOWNLOADING, Status.PROCESSING);
    }

    public InstallationIndicator(String appName, Step initialStep, Status initialStatus) {
        this.appName = appName;
        this.currentStep = initialStep;
        this.status = initialStatus;

        // Only log at FINE level - constructor is called frequently
        LOG.fine("[InstallationIndicator] Created for app: " + appName);

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

        stepIcon = new FontIcon(currentStep.getIcon());
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

        // Control button (cancel/dismiss)
        controlButton = new StackPane();
        controlButton.setMinSize(32, 32);
        controlButton.setMaxSize(32, 32);
        controlButton.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-background-radius: 16px; -fx-cursor: hand;");

        controlIcon = new FontIcon(Feather.X);
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
    }

    /**
     * Set the current installation step.
     */
    public void setStep(Step step) {
        if (this.currentStep != step) {
            LOG.fine("[InstallationIndicator] Step: " + this.currentStep + " -> " + step);
        }
        this.currentStep = step;
        this.customMessage = null;
        runOnFxThread(this::updateUI);
    }

    /**
     * Set the current status.
     */
    public void setStatus(Status status) {
        if (this.status != status) {
            LOG.fine("[InstallationIndicator] Status: " + this.status + " -> " + status);
        }
        this.status = status;
        runOnFxThread(this::updateUI);
    }

    /**
     * Set a custom message to display instead of the step label.
     */
    public void setMessage(String message) {
        this.customMessage = message;
        runOnFxThread(this::updateUI);
    }

    /**
     * Set both step and status at once.
     */
    public void update(Step step, Status status) {
        boolean stepChanged = this.currentStep != step;
        boolean statusChanged = this.status != status;
        if (stepChanged || statusChanged) {
            LOG.fine("[InstallationIndicator] Update: step=" + step + ", status=" + status);
        }
        this.currentStep = step;
        this.status = status;
        this.customMessage = null;
        runOnFxThread(this::updateUI);
    }

    /**
     * Set both step and custom message.
     */
    public void update(Step step, String message) {
        // Only log when step changes, not on every progress update
        if (this.currentStep != step) {
            LOG.fine("[InstallationIndicator] Step: " + step);
        }
        this.currentStep = step;
        this.customMessage = message;
        runOnFxThread(this::updateUI);
    }

    /**
     * Mark installation as completed.
     */
    public void complete() {
        if (this.status != Status.COMPLETED) {
            LOG.info("[InstallationIndicator] Completed: " + appName);
        }
        this.status = Status.COMPLETED;
        this.currentStep = Step.VERIFYING;
        this.customMessage = "Completed";
        runOnFxThread(this::updateUI);
    }

    /**
     * Mark installation as failed with error message.
     */
    public void fail(String errorMessage) {
        if (this.status != Status.ERROR) {
            LOG.warning("[InstallationIndicator] Failed: " + appName + " - " + errorMessage);
        }
        this.status = Status.ERROR;
        this.customMessage = errorMessage;
        runOnFxThread(this::updateUI);
    }

    /**
     * Set callback for cancel button click (during installation).
     */
    public void setOnCancel(Consumer<Void> handler) {
        this.onCancelClick = handler;
    }

    /**
     * Set callback for dismiss button click (after completion/error).
     */
    public void setOnDismiss(Consumer<Void> handler) {
        this.onDismissClick = handler;
    }

    public Step getCurrentStep() {
        return currentStep;
    }

    public Status getStatus() {
        return status;
    }

    private void handleControlClick() {
        LOG.fine("[InstallationIndicator] Control button clicked, status: " + status);
        if (status == Status.COMPLETED || status == Status.ERROR) {
            if (onDismissClick != null) {
                onDismissClick.accept(null);
            }
        } else {
            if (onCancelClick != null) {
                onCancelClick.accept(null);
            }
        }
    }

    private void updateUI() {
        // Update icon
        stepIcon.setIconCode(currentStep.getIcon());

        // Determine colors based on status
        String bgColor, fgColor;
        if (status == Status.COMPLETED) {
            bgColor = EMERALD_BG;
            fgColor = EMERALD_FG;
        } else if (status == Status.ERROR) {
            bgColor = RED_BG;
            fgColor = RED_FG;
        } else if (status == Status.PAUSED) {
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
        if (customMessage != null) {
            statusText = customMessage;
        } else if (status == Status.COMPLETED) {
            statusText = "COMPLETED";
        } else if (status == Status.ERROR) {
            statusText = "ERROR";
        } else if (status == Status.PAUSED) {
            statusText = "PAUSED";
        } else {
            statusText = currentStep.getLabel();
        }
        statusLabel.setText(statusText);
        statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 500; -fx-text-fill: " + fgColor + ";");

        // Update control button icon
        controlIcon.setIconCode(Feather.X);

        // Update progress segments
        if (status == Status.COMPLETED) {
            // Merge into one continuous green bar
            progressBar.setSpacing(0);
            for (int i = 0; i < 4; i++) {
                String radius;
                if (i == 0) {
                    radius = "3px 0 0 3px";
                } else if (i == 3) {
                    radius = "0 3px 3px 0";
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
                if (i < currentStep.getIndex() - 1) {
                    segmentColor = BLUE_FG; // Completed segments
                } else if (i == currentStep.getIndex() - 1) {
                    if (status == Status.ERROR) {
                        segmentColor = RED_FG;
                    } else if (status == Status.PAUSED) {
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

    private void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
