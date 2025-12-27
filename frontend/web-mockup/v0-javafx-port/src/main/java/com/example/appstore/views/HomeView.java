package com.example.appstore.views;

import com.example.appstore.components.ClaudeFeaturedCard;
import com.example.appstore.components.StandardCard;
import com.example.appstore.layout.RootLayout;
import com.example.appstore.model.App;
import com.example.appstore.service.ApiService;
import com.example.appstore.service.LibraryService;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Claude-style home view with Featured Apps and All Discoveries sections.
 */
public class HomeView extends ScrollPane implements Searchable {

    private final RootLayout rootLayout;
    private final GridPane featuredGrid;
    private final GridPane allAppsGrid;
    private List<App> allApps = new java.util.ArrayList<>();
    private String currentFilter = "All";

    public HomeView(RootLayout rootLayout) {
        this.rootLayout = rootLayout;
        getStyleClass().add("edge-to-edge-scroll-pane");
        setFitToWidth(true);
        setFitToHeight(false);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox content = new VBox(48);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: #111111;");

        // Featured Apps Section
        VBox featuredSection = new VBox(24);

        HBox featuredHeader = new HBox();
        featuredHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label featuredTitle = new Label("Empfohlene Apps");
        featuredTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 300; -fx-text-fill: #ffffff;");

        featuredHeader.getChildren().add(featuredTitle);

        // 3-column grid for featured apps
        featuredGrid = new GridPane();
        featuredGrid.setHgap(24);
        featuredGrid.setVgap(24);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.ALWAYS);
            featuredGrid.getColumnConstraints().add(col);
        }

        // Loading indicator
        ProgressIndicator featuredLoader = new ProgressIndicator();
        featuredLoader.setMaxSize(40, 40);
        featuredLoader.setStyle("-fx-accent: #d97757;");
        featuredGrid.add(featuredLoader, 0, 0);

        featuredSection.getChildren().addAll(featuredHeader, featuredGrid);

        // All Discoveries Section
        VBox allAppsSection = new VBox(24);

        HBox allAppsHeader = new HBox();
        allAppsHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label allAppsTitle = new Label("Alle Entdeckungen");
        allAppsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 300; -fx-text-fill: #ffffff;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Label countLabel = new Label("0 Ergebnisse");
        countLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px; -fx-font-family: monospace;");
        countLabel.setId("results-count");

        allAppsHeader.getChildren().addAll(allAppsTitle, headerSpacer, countLabel);

        // 4-column grid for all apps
        allAppsGrid = new GridPane();
        allAppsGrid.setHgap(16);
        allAppsGrid.setVgap(16);

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            allAppsGrid.getColumnConstraints().add(col);
        }

        ProgressIndicator allAppsLoader = new ProgressIndicator();
        allAppsLoader.setMaxSize(40, 40);
        allAppsLoader.setStyle("-fx-accent: #d97757;");
        allAppsGrid.add(allAppsLoader, 0, 0);

        allAppsSection.getChildren().addAll(allAppsHeader, allAppsGrid);

        content.getChildren().addAll(featuredSection, allAppsSection);
        setContent(content);

        // Load data
        loadApps();
    }

    private void loadApps() {
        ApiService.getInstance()
                .getApps()
                .thenAccept(apps -> {
                    Platform.runLater(() -> {
                        allApps = apps;
                        displayFeaturedApps(apps);
                        displayAllApps(apps);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        featuredGrid.getChildren().clear();
                        Label errorLabel = new Label("Apps konnten nicht geladen werden. Läuft das Backend?");
                        errorLabel.setStyle("-fx-text-fill: #666666;");
                        featuredGrid.add(errorLabel, 0, 0);
                    });
                    return null;
                });
    }

    private void displayFeaturedApps(List<App> apps) {
        featuredGrid.getChildren().clear();

        if (apps.isEmpty()) {
            Label noApps = new Label("Keine empfohlenen Apps verfügbar");
            noApps.setStyle("-fx-text-fill: #666666;");
            featuredGrid.add(noApps, 0, 0);
            return;
        }

        // Display first 3 apps as featured
        int col = 0;
        int count = 0;
        for (App app : apps) {
            if (count >= 3)
                break;

            ClaudeFeaturedCard card = new ClaudeFeaturedCard(
                    app,
                    () -> rootLayout.showAppDetails(app));
            featuredGrid.add(card, col, 0);
            col++;
            count++;
        }
    }

    private void displayAllApps(List<App> apps) {
        allAppsGrid.getChildren().clear();

        // Update count label
        Label countLabel = (Label) getContent().lookup("#results-count");
        if (countLabel != null) {
            countLabel.setText(apps.size() + " Ergebnisse");
        }

        if (apps.isEmpty()) {
            Label noApps = new Label("Keine Apps verfügbar");
            noApps.setStyle("-fx-text-fill: #666666;");
            allAppsGrid.add(noApps, 0, 0);
            return;
        }

        LibraryService libraryService = LibraryService.getInstance();
        int col = 0;
        int row = 0;

        for (App app : apps) {
            boolean isInstalled = libraryService.isInstalled(app.getId());

            StandardCard card = new StandardCard(
                    app.getName(),
                    app.getOwnerLogin(),
                    getStarsForApp(app),
                    "1k",
                    isInstalled,
                    () -> rootLayout.showAppDetails(app),
                    app);

            allAppsGrid.add(card, col, row);
            col++;
            if (col > 3) {
                col = 0;
                row++;
            }
        }
    }

    private String getStarsForApp(App app) {
        // Placeholder - in real implementation, fetch from API
        String[] stars = { "12.4k", "8.9k", "15.2k", "5.1k", "7.3k", "10.8k" };
        int hash = Math.abs(app.getName().hashCode());
        return stars[hash % stars.length];
    }

    @Override
    public void onSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            displayAllApps(allApps);
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<App> filtered = allApps.stream()
                .filter(app -> app.getName().toLowerCase().contains(lowerQuery) ||
                        app.getOwnerLogin().toLowerCase().contains(lowerQuery) ||
                        (app.getDescription() != null && app.getDescription().toLowerCase().contains(lowerQuery)))
                .toList();

        displayAllApps(filtered);
    }

    @Override
    public void onFilter(String platform) {
        this.currentFilter = platform;
        // In the Claude design, there are no platform filters
        // This is kept for interface compatibility
    }
}
