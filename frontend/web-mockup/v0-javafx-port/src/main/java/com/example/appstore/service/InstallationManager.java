package com.example.appstore.service;

import com.example.appstore.model.App;
import com.example.appstore.service.InstallationService.InstallProgress;
import com.example.appstore.service.InstallationService.InstallResult;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages installation state globally, providing observable properties
 * for UI components (like the Sidebar's InstallationIndicator) to bind to.
 * 
 * This is a singleton that tracks the currently active installation and
 * provides callbacks for progress updates.
 */
public class InstallationManager {

    private static final Logger LOG = Logger.getLogger(InstallationManager.class.getName());
    private static InstallationManager instance;

    /**
     * Represents the current state of an installation.
     */
    public static class InstallationState {
        public enum Phase {
            IDLE,           // No installation in progress
            FETCHING,       // Fetching release info
            DOWNLOADING,    // Downloading asset
            EXTRACTING,     // Extracting archive
            INSTALLING,     // Installing to system
            VERIFYING,      // Verifying installation
            COMPLETED,      // Installation complete (awaiting dismiss)
            FAILED          // Installation failed (awaiting dismiss)
        }

        private final String appId;
        private final String appName;
        private final Phase phase;
        private final double progress;  // 0.0 to 1.0 for DOWNLOADING phase
        private final String message;
        private final String errorMessage;
        private final InstallResult result;

        private InstallationState(String appId, String appName, Phase phase, 
                                   double progress, String message, 
                                   String errorMessage, InstallResult result) {
            this.appId = appId;
            this.appName = appName;
            this.phase = phase;
            this.progress = progress;
            this.message = message;
            this.errorMessage = errorMessage;
            this.result = result;
        }

        public static InstallationState idle() {
            return new InstallationState(null, null, Phase.IDLE, 0, null, null, null);
        }

        public static InstallationState inProgress(String appId, String appName, 
                                                    Phase phase, double progress, String message) {
            return new InstallationState(appId, appName, phase, progress, message, null, null);
        }

        public static InstallationState completed(String appId, String appName, InstallResult result) {
            return new InstallationState(appId, appName, Phase.COMPLETED, 1.0, 
                                         "Completed", null, result);
        }

        public static InstallationState failed(String appId, String appName, String errorMessage) {
            return new InstallationState(appId, appName, Phase.FAILED, 0, 
                                         "Failed", errorMessage, null);
        }

        public String getAppId() { return appId; }
        public String getAppName() { return appName; }
        public Phase getPhase() { return phase; }
        public double getProgress() { return progress; }
        public String getMessage() { return message; }
        public String getErrorMessage() { return errorMessage; }
        public InstallResult getResult() { return result; }

        public boolean isIdle() { return phase == Phase.IDLE; }
        public boolean isActive() { 
            return phase != Phase.IDLE && phase != Phase.COMPLETED && phase != Phase.FAILED; 
        }
        public boolean isCompleted() { return phase == Phase.COMPLETED; }
        public boolean isFailed() { return phase == Phase.FAILED; }
        public boolean needsDismiss() { return phase == Phase.COMPLETED || phase == Phase.FAILED; }
    }

    // Observable property for UI binding
    private final ObjectProperty<InstallationState> currentState = 
        new SimpleObjectProperty<>(InstallationState.idle());

    // Listeners for detailed progress (used by AppDetailView)
    private final List<Consumer<InstallProgress>> progressListeners = new ArrayList<>();
    private String currentAppId = null;

    private InstallationManager() {
        LOG.info("[InstallationManager] Initialized");
    }

    public static synchronized InstallationManager getInstance() {
        if (instance == null) {
            instance = new InstallationManager();
        }
        return instance;
    }

    /**
     * Get the observable state property for binding.
     */
    public ObjectProperty<InstallationState> stateProperty() {
        return currentState;
    }

    /**
     * Get the current installation state.
     */
    public InstallationState getState() {
        return currentState.get();
    }

    /**
     * Check if an installation is currently active.
     */
    public boolean isInstalling() {
        return currentState.get().isActive();
    }

    /**
     * Check if a specific app is currently being installed.
     */
    public boolean isInstalling(String appId) {
        InstallationState state = currentState.get();
        return state.isActive() && appId.equals(state.getAppId());
    }

    /**
     * Add a progress listener for a specific app.
     * Used by AppDetailView to get detailed progress for the app being viewed.
     */
    public void addProgressListener(String appId, Consumer<InstallProgress> listener) {
        if (appId.equals(currentAppId)) {
            progressListeners.add(listener);
        }
    }

    /**
     * Remove a progress listener.
     */
    public void removeProgressListener(Consumer<InstallProgress> listener) {
        progressListeners.remove(listener);
    }

    /**
     * Clear all progress listeners.
     */
    public void clearProgressListeners() {
        progressListeners.clear();
    }

    /**
     * Dismiss the current completed/failed state, returning to idle.
     */
    public void dismiss() {
        InstallationState state = currentState.get();
        if (state.needsDismiss()) {
            LOG.info("[InstallationManager] Dismissing installation state for: " + state.getAppName());
            currentAppId = null;
            clearProgressListeners();
            updateState(InstallationState.idle());
        }
    }

    /**
     * Start installing an app. Returns a CompletableFuture with the result.
     * This method manages the global state and delegates to InstallationService.
     */
    public CompletableFuture<InstallResult> installApp(App app) {
        if (isInstalling()) {
            LOG.warning("[InstallationManager] Cannot start installation - another installation is in progress");
            return CompletableFuture.failedFuture(
                new IllegalStateException("Another installation is already in progress")
            );
        }

        LOG.info("[InstallationManager] Starting installation for: " + app.getName());
        currentAppId = app.getId();

        // Set initial state
        updateState(InstallationState.inProgress(
            app.getId(), app.getName(),
            InstallationState.Phase.FETCHING, 0, "Preparing..."
        ));

        return InstallationService.getInstance().installApp(app, this::handleProgress)
            .thenApply(result -> {
                LOG.info("[InstallationManager] Installation completed for: " + app.getName());
                updateState(InstallationState.completed(app.getId(), app.getName(), result));
                return result;
            })
            .exceptionally(ex -> {
                LOG.log(Level.SEVERE, "[InstallationManager] Installation failed for: " + app.getName(), ex);
                String errorMsg = ex.getCause() != null ? 
                    ex.getCause().getMessage() : ex.getMessage();
                updateState(InstallationState.failed(app.getId(), app.getName(), errorMsg));
                throw new RuntimeException(ex);
            });
    }

    private void handleProgress(InstallProgress progress) {
        // Notify detailed progress listeners (AppDetailView)
        for (Consumer<InstallProgress> listener : progressListeners) {
            try {
                listener.accept(progress);
            } catch (Exception e) {
                LOG.warning("[InstallationManager] Progress listener error: " + e.getMessage());
            }
        }

        // Update global state
        InstallationState state = currentState.get();
        if (state.isIdle()) return;

        InstallationState.Phase phase;
        double progressValue = progress.getProgress();
        String message = progress.getMessage();

        switch (progress.getStage()) {
            case FETCHING_RELEASE:
                phase = InstallationState.Phase.FETCHING;
                break;
            case DOWNLOADING:
                phase = InstallationState.Phase.DOWNLOADING;
                break;
            case EXTRACTING:
                phase = InstallationState.Phase.EXTRACTING;
                break;
            case INSTALLING:
                phase = InstallationState.Phase.INSTALLING;
                break;
            case VERIFYING:
                phase = InstallationState.Phase.VERIFYING;
                break;
            case COMPLETED:
                // Handled in thenApply
                return;
            case FAILED:
                // Handled in exceptionally
                return;
            default:
                return;
        }

        updateState(InstallationState.inProgress(
            state.getAppId(), state.getAppName(),
            phase, progressValue, message
        ));
    }

    private void updateState(InstallationState newState) {
        if (Platform.isFxApplicationThread()) {
            currentState.set(newState);
        } else {
            Platform.runLater(() -> currentState.set(newState));
        }
    }
}
