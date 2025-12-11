package com.example.appstore.layout;

import com.example.appstore.components.Sidebar;
import com.example.appstore.views.HomeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class RootLayout extends BorderPane {

    private final StackPane contentArea;
    private final Sidebar sidebar;
    private final HomeView homeView; // Added homeView field

    private com.example.appstore.views.Searchable currentSearchableView;

    public RootLayout() {
        getStyleClass().add("root-layout");

        this.homeView = new HomeView(this);

        // Sidebar
        sidebar = new Sidebar(this::navigate);
        setLeft(sidebar);

        // Content Area
        VBox centerLayout = new VBox();
        centerLayout.setStyle("-fx-background-color: #09090b;");

        // TopBar
        centerLayout
            .getChildren()
            .add(
                new com.example.appstore.components.TopBar(
                    query -> {
                        if (
                            currentSearchableView != null
                        ) currentSearchableView.onSearch(query);
                    },
                    platform -> {
                        if (
                            currentSearchableView != null
                        ) currentSearchableView.onFilter(platform);
                    }
                )
            );

        // View Area
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        VBox.setVgrow(contentArea, javafx.scene.layout.Priority.ALWAYS);

        centerLayout.getChildren().add(contentArea);
        setCenter(centerLayout);

        // Initial View
        navigate("Discover");
    }

    private final java.util.Stack<String> history = new java.util.Stack<>();
    private boolean isBackNavigation = false;

    public void navigate(String viewName) {
        if (!isBackNavigation) {
            // If navigating to a new view, push current view to history if it's not the same
            // Actually, we should track what was the *previous* view.
            // Simplified: Push the *current* view name before switching, if we can track it.
            // Better: Just push the viewName we are *leaving*? No, we need to know where to go back TO.
            // Let's just push the viewName we are navigating TO onto the stack.
            // When going back, pop current, then peek previous.
            // But wait, if we are at "Discover", and go to "AppDetail", history should have "Discover".
            // So we need to track "currentViewName".
        }

        // This is getting complicated with the String viewName.
        // Let's just use a simple approach:
        // When showAppDetails is called, we know we are coming from *somewhere*.
        // We can store that "somewhere" in a field `lastViewName`.

        contentArea.getChildren().clear();
        currentSearchableView = null; // Reset

        // Update sidebar selection visually if needed (not implemented in Sidebar yet, but we can assume)

        switch (viewName) {
            case "Discover":
                contentArea.getChildren().add(homeView);
                if (homeView instanceof com.example.appstore.views.Searchable) {
                    currentSearchableView =
                        (com.example.appstore.views.Searchable) homeView;
                }
                break;
            case "Library":
                contentArea
                    .getChildren()
                    .add(new com.example.appstore.views.LibraryView());
                break;
            case "Updates":
                contentArea
                    .getChildren()
                    .add(new com.example.appstore.views.UpdatesView());
                break;
            case "Settings":
                contentArea
                    .getChildren()
                    .add(new com.example.appstore.views.SettingsView());
                break;
            case "Developer Tools":
            case "Productivity":
            case "Graphics & Design":
            case "Games":
            case "Music & Audio":
            case "Video":
            case "Utilities":
            case "Security":
                var catView = new com.example.appstore.views.CategoryView(
                    viewName,
                    this
                );
                contentArea.getChildren().add(catView);
                currentSearchableView = catView;
                break;
            default:
                contentArea.getChildren().add(homeView);
                if (homeView instanceof com.example.appstore.views.Searchable) {
                    currentSearchableView =
                        (com.example.appstore.views.Searchable) homeView;
                }
        }

        if (!isBackNavigation) {
            history.push(viewName);
        }
        isBackNavigation = false;
    }

    public void showAppDetails(com.example.appstore.model.App app) {
        // Don't clear history, just add AppDetails on top (visually) or treat it as a view.
        // But our navigate takes a String.
        // Let's just store the current view before switching.
        // Actually, if we use the history stack:
        // 1. navigate("Discover") -> Stack: ["Discover"]
        // 2. showAppDetails() -> Stack: ["Discover", "AppDetails"]

        contentArea.getChildren().clear();
        currentSearchableView = null;
        contentArea
            .getChildren()
            .add(
                new com.example.appstore.views.AppDetailView(app, this::goBack)
            );
        history.push("AppDetails");
    }

    public void goBack() {
        if (history.size() > 1) {
            history.pop(); // Remove current "AppDetails"
            String previousView = history.peek(); // Get "Discover" (or whatever)

            // We need to pop it so navigate doesn't push it again?
            // Or use a flag.
            isBackNavigation = true;
            history.pop(); // Pop it because navigate will push it back (or we handle it)
            // Actually, if navigate pushes, we should pop "Discover" before calling navigate("Discover")
            // so it gets pushed again as new top?
            // Or just set flag.

            navigate(previousView);
        } else {
            navigate("Discover"); // Fallback
        }
    }

    public StackPane getContentArea() {
        return contentArea;
    }
}
