package com.example.appstore.views;

import com.example.appstore.components.StandardCard;
import com.example.appstore.layout.RootLayout;
import com.example.appstore.model.App;
import com.example.appstore.service.ApiService;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class CategoryView extends ScrollPane implements Searchable {

    private final FlowPane grid;
    private final String categoryName;
    private final RootLayout rootLayout;
    private List<App> allApps = new ArrayList<>();

    public CategoryView(String categoryName, RootLayout rootLayout) {
        this.categoryName = categoryName;
        this.rootLayout = rootLayout;
        setFitToWidth(true);
        setStyle(
            "-fx-background-color: transparent; -fx-background: transparent;"
        );

        VBox content = new VBox(24);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: #09090b;");

        Label title = new Label(categoryName);
        title.getStyleClass().add("h1");
        content.getChildren().add(title);

        // Grid of Apps
        grid = new FlowPane();
        grid.setHgap(16);
        grid.setVgap(16);

        // Show loading indicator
        ProgressIndicator loader = new ProgressIndicator();
        loader.setMaxSize(40, 40);
        grid.getChildren().add(loader);

        content.getChildren().add(grid);
        setContent(content);

        // Load apps from API
        loadApps();
    }

    private void loadApps() {
        ApiService.getInstance()
            .getApps()
            .thenAccept(apps -> {
                Platform.runLater(() -> {
                    grid.getChildren().clear();

                    // Filter by category if apps have categories, otherwise show all
                    List<App> filtered = new ArrayList<>();
                    for (App app : apps) {
                        if (
                            app.getCategory() != null &&
                            app.getCategory().equalsIgnoreCase(categoryName)
                        ) {
                            filtered.add(app);
                        }
                    }

                    // If no apps match the category, show all apps (category not set in data yet)
                    if (filtered.isEmpty()) {
                        filtered = apps;
                    }

                    allApps = filtered;

                    if (filtered.isEmpty()) {
                        Label noApps = new Label(
                            "No apps found in this category"
                        );
                        noApps.setStyle("-fx-text-fill: #71717a;");
                        grid.getChildren().add(noApps);
                    } else {
                        for (App app : filtered) {
                            StandardCard card = new StandardCard(
                                app.getName(),
                                app.getOwnerLogin(),
                                "4.5", // Placeholder
                                "1k", // Placeholder
                                false,
                                () -> rootLayout.showAppDetails(app)
                            );
                            grid.getChildren().add(card);
                        }
                    }
                });
            });
    }

    @Override
    public void onSearch(String query) {
        grid.getChildren().clear();
        String lowerQuery = query.toLowerCase();

        for (App app : allApps) {
            if (
                app.getName().toLowerCase().contains(lowerQuery) ||
                app.getOwnerLogin().toLowerCase().contains(lowerQuery) ||
                (app.getDescription() != null &&
                    app.getDescription().toLowerCase().contains(lowerQuery))
            ) {
                StandardCard card = new StandardCard(
                    app.getName(),
                    app.getOwnerLogin(),
                    "4.5",
                    "1k",
                    false,
                    () -> rootLayout.showAppDetails(app)
                );
                grid.getChildren().add(card);
            }
        }
    }
}
