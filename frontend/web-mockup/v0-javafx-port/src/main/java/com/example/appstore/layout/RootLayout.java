package com.example.appstore.layout;

import com.example.appstore.components.Sidebar;
import com.example.appstore.views.AppDetailView;
import com.example.appstore.views.HomeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Root layout with sidebar, top bar, and content area.
 * Uses StackPane for modal overlay support.
 */
public class RootLayout extends BorderPane {

    private final StackPane contentArea;
    private final StackPane modalOverlay;
    private final Sidebar sidebar;
    private final HomeView homeView;
    private com.example.appstore.views.Searchable currentSearchableView;
    private final java.util.Stack<String> history = new java.util.Stack<>();
    private boolean isBackNavigation = false;

    public RootLayout() {
        getStyleClass().add("root-layout");
        setStyle("-fx-background-color: #111111;");

        this.homeView = new HomeView(this);

        // Sidebar
        sidebar = new Sidebar(this::navigate);
        setLeft(sidebar);

        // Center layout with top bar and content
        VBox centerLayout = new VBox();
        centerLayout.setStyle("-fx-background-color: #111111;");

        // TopBar
        centerLayout.getChildren().add(
                new com.example.appstore.components.TopBar(
                        query -> {
                            if (currentSearchableView != null)
                                currentSearchableView.onSearch(query);
                        },
                        platform -> {
                            if (currentSearchableView != null)
                                currentSearchableView.onFilter(platform);
                        }));

        // Content area wrapped in StackPane for modal support
        StackPane contentWrapper = new StackPane();
        VBox.setVgrow(contentWrapper, javafx.scene.layout.Priority.ALWAYS);

        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        contentArea.setStyle("-fx-background-color: #111111;");

        // Modal overlay (initially hidden)
        modalOverlay = new StackPane();
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);

        contentWrapper.getChildren().addAll(contentArea, modalOverlay);
        centerLayout.getChildren().add(contentWrapper);
        setCenter(centerLayout);

        // Initial view
        navigate("Discover");
    }

    public void navigate(String viewName) {
        contentArea.getChildren().clear();
        currentSearchableView = null;

        // Hide modal if visible
        hideModal();

        switch (viewName) {
            case "Discover":
                contentArea.getChildren().add(homeView);
                if (homeView instanceof com.example.appstore.views.Searchable) {
                    currentSearchableView = (com.example.appstore.views.Searchable) homeView;
                }
                break;
            case "Library":
                contentArea.getChildren().add(new com.example.appstore.views.LibraryView());
                break;
            case "Updates":
                contentArea.getChildren().add(new com.example.appstore.views.UpdatesView());
                break;
            case "Settings":
                contentArea.getChildren().add(new com.example.appstore.views.SettingsView());
                break;
            case "Developer Tools":
            case "Productivity":
            case "Graphics & Design":
            case "Games":
            case "Music & Audio":
            case "Video":
            case "Utilities":
            case "Security":
            case "AI":
                var catView = new com.example.appstore.views.CategoryView(viewName, this);
                contentArea.getChildren().add(catView);
                currentSearchableView = catView;
                break;
            default:
                contentArea.getChildren().add(homeView);
                if (homeView instanceof com.example.appstore.views.Searchable) {
                    currentSearchableView = (com.example.appstore.views.Searchable) homeView;
                }
        }

        if (!isBackNavigation) {
            history.push(viewName);
        }
        isBackNavigation = false;
    }

    public void showAppDetails(com.example.appstore.model.App app) {
        // Show app details as modal overlay
        modalOverlay.getChildren().clear();
        modalOverlay.getChildren().add(new AppDetailView(app, this::hideModal));
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        history.push("AppDetails");
    }

    private void hideModal() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        modalOverlay.getChildren().clear();
    }

    public void goBack() {
        if (history.size() > 1) {
            history.pop(); // Remove current
            String previousView = history.peek();

            if ("AppDetails".equals(previousView)) {
                // If previous was also AppDetails, keep going back
                goBack();
                return;
            }

            isBackNavigation = true;
            history.pop();
            navigate(previousView);
        } else {
            navigate("Discover");
        }
    }

    public StackPane getContentArea() {
        return contentArea;
    }
}
