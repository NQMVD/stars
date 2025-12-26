package com.example.appstore.views;

import com.example.appstore.components.HeroCard;
import com.example.appstore.components.StandardCard;
import com.example.appstore.layout.RootLayout;
import com.example.appstore.model.App;
import com.example.appstore.service.ApiService;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class HomeView extends ScrollPane implements Searchable {

    private final RootLayout rootLayout;
    private final GridPane trendingRow;
    private final GridPane allAppsGrid;
    private final HBox featuredRow;
    private final java.util.List<javafx.scene.Node> allCards =
        new java.util.ArrayList<>();
    private List<App> allApps = new java.util.ArrayList<>();

    public HomeView(RootLayout rootLayout) {
        this.rootLayout = rootLayout;
        getStyleClass().add("edge-to-edge-scroll-pane");
        setFitToWidth(true);
        setStyle(
            "-fx-background-color: transparent; -fx-background: transparent;"
        );

        VBox content = new VBox(32);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: #09090b;");

        // Featured Section
        VBox featuredSection = new VBox(16);
        Label featuredTitle = new Label("Featured");
        featuredTitle.getStyleClass().add("h2");
        HBox featuredHeader = new HBox(8);
        featuredHeader
            .getChildren()
            .addAll(new FontIcon(Feather.STAR), featuredTitle);

        featuredRow = new HBox(16);
        // Show loading indicator initially
        ProgressIndicator featuredLoader = new ProgressIndicator();
        featuredLoader.setMaxSize(40, 40);
        featuredRow.getChildren().add(featuredLoader);

        featuredSection.getChildren().addAll(featuredHeader, featuredRow);

        // Trending Section
        VBox trendingSection = new VBox(16);
        Label trendingTitle = new Label("Trending");
        trendingTitle.getStyleClass().add("h2");
        HBox trendingHeader = new HBox(8);
        trendingHeader
            .getChildren()
            .addAll(new FontIcon(Feather.TRENDING_UP), trendingTitle);

        trendingRow = new GridPane();
        trendingRow.setHgap(16);
        trendingRow.setVgap(16);

        // Show loading indicator
        ProgressIndicator trendingLoader = new ProgressIndicator();
        trendingLoader.setMaxSize(40, 40);
        trendingRow.add(trendingLoader, 0, 0);

        trendingSection.getChildren().addAll(trendingHeader, trendingRow);

        // All Apps Section
        VBox allAppsSection = new VBox(16);
        Label allAppsTitle = new Label("All Apps");
        allAppsTitle.getStyleClass().add("h2");
        HBox allAppsHeader = new HBox(8);
        allAppsHeader
            .getChildren()
            .addAll(new FontIcon(Feather.GRID), allAppsTitle);

        allAppsGrid = new GridPane();
        allAppsGrid.setHgap(16);
        allAppsGrid.setVgap(16);

        ProgressIndicator allAppsLoader = new ProgressIndicator();
        allAppsLoader.setMaxSize(40, 40);
        allAppsGrid.add(allAppsLoader, 0, 0);

        allAppsSection.getChildren().addAll(allAppsHeader, allAppsGrid);

        content
            .getChildren()
            .addAll(featuredSection, trendingSection, allAppsSection);
        setContent(content);

        // Load data from API
        loadFeaturedApps();
        loadTrendingApps();
        loadAllApps();
    }

    private void loadFeaturedApps() {
        ApiService.getInstance()
            .getFeaturedApps()
            .thenAccept(apps -> {
                Platform.runLater(() -> {
                    featuredRow.getChildren().clear();
                    if (apps.isEmpty()) {
                        Label noApps = new Label("No featured apps available");
                        noApps.setStyle("-fx-text-fill: #71717a;");
                        featuredRow.getChildren().add(noApps);
                    } else {
                        for (int i = 0; i < Math.min(3, apps.size()); i++) {
                            App app = apps.get(i);
                            boolean highlight = i < 2; // First two are highlighted
                            HeroCard card = new HeroCard(
                                app.getName(),
                                app.getDescription() != null
                                    ? app.getDescription()
                                    : "No description",
                                highlight
                            );
                            featuredRow.getChildren().add(card);
                        }
                    }
                });
            });
    }

    private void loadTrendingApps() {
        ApiService.getInstance()
            .getApps()
            .thenAccept(apps -> {
                Platform.runLater(() -> {
                    trendingRow.getChildren().clear();
                    allCards.clear();
                    allApps = apps;

                    if (apps.isEmpty()) {
                        Label noApps = new Label(
                            "No apps available. Is the backend running?"
                        );
                        noApps.setStyle("-fx-text-fill: #71717a;");
                        trendingRow.add(noApps, 0, 0);
                    } else {
                        int col = 0;
                        int row = 0;
                        // Show up to 6 apps in the trending section
                        for (int i = 0; i < Math.min(6, apps.size()); i++) {
                            App app = apps.get(i);
                            StandardCard card = new StandardCard(
                                app.getName(),
                                app.getOwnerLogin(),
                                "4.5", // Placeholder rating
                                "1k", // Placeholder count
                                false, // Not installed
                                () -> rootLayout.showAppDetails(app)
                            );
                            trendingRow.add(card, col, row);
                            allCards.add(card);
                            col++;
                            if (col > 2) {
                                col = 0;
                                row++;
                            }
                        }
                    }
                });
            });
    }

    private void loadAllApps() {
        ApiService.getInstance()
            .getApps()
            .thenAccept(apps -> {
                Platform.runLater(() -> {
                    allAppsGrid.getChildren().clear();
                    allApps = apps;

                    if (apps.isEmpty()) {
                        Label noApps = new Label("No apps available");
                        noApps.setStyle("-fx-text-fill: #71717a;");
                        allAppsGrid.add(noApps, 0, 0);
                    } else {
                        int col = 0;
                        int row = 0;
                        for (App app : apps) {
                            StandardCard card = new StandardCard(
                                app.getName(),
                                app.getOwnerLogin(),
                                "4.5",
                                "1k",
                                false,
                                () -> rootLayout.showAppDetails(app)
                            );
                            allAppsGrid.add(card, col, row);
                            col++;
                            if (col > 2) {
                                col = 0;
                                row++;
                            }
                        }
                    }
                });
            });
    }

    @Override
    public void onSearch(String query) {
        trendingRow.getChildren().clear();
        int col = 0;
        int row = 0;

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
                trendingRow.add(card, col, row);
                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            }
        }
    }
}
