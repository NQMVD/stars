package com.example.appstore.views;

import com.example.appstore.components.HeroCard;
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
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public class HomeView extends ScrollPane implements Searchable {

    private final RootLayout rootLayout;
    private final GridPane trendingRow;
    private final GridPane allAppsGrid;
    private final HBox featuredRow;
    private final java.util.List<javafx.scene.Node> allCards = new java.util.ArrayList<>();
    private List<App> allApps = new java.util.ArrayList<>();

    public HomeView(RootLayout rootLayout) {
        this.rootLayout = rootLayout;
        getStyleClass().add("edge-to-edge-scroll-pane");
        setFitToWidth(true);
        setFitToHeight(true);
        // Disable scrolling
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        setStyle(
                "-fx-background-color: transparent; -fx-background: transparent;");

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

        // Configure 4 columns to fill available space equally
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            trendingRow.getColumnConstraints().add(col);
        }

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

        // Configure 4 columns to fill available space equally
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            allAppsGrid.getColumnConstraints().add(col);
        }

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
        // Create 3 sample featured cards matching the screenshot design
        featuredRow.getChildren().clear();

        // Card 1: Visual Studio Code - Blue gradient
        HeroCard vsCodeCard = new HeroCard(
                "The most popular code editor",
                "Visual Studio Code",
                "Developer Tools",
                HeroCard.GradientType.BLUE,
                Feather.CODE);
        HBox.setHgrow(vsCodeCard, Priority.ALWAYS);

        // Card 2: Figma - Pink/Purple gradient
        HeroCard figmaCard = new HeroCard(
                "Design, prototype, and gather feedback",
                "Figma",
                "Design",
                HeroCard.GradientType.PINK_PURPLE,
                Feather.PEN_TOOL);
        HBox.setHgrow(figmaCard, Priority.ALWAYS);

        // Card 3: Slack - Purple/Orange gradient
        HeroCard slackCard = new HeroCard(
                "Where work happens",
                "Slack",
                "Business",
                HeroCard.GradientType.PURPLE_ORANGE,
                Feather.MESSAGE_SQUARE);
        HBox.setHgrow(slackCard, Priority.ALWAYS);

        featuredRow.getChildren().addAll(vsCodeCard, figmaCard, slackCard);
    }

    private void loadTrendingApps() {
        // Define gradient types for variety
        StandardCard.GradientType[] gradients = {
                StandardCard.GradientType.BLUE,
                StandardCard.GradientType.PURPLE,
                StandardCard.GradientType.PINK,
                StandardCard.GradientType.ORANGE
        };

        ApiService.getInstance()
                .getApps()
                .thenAccept(apps -> {
                    Platform.runLater(() -> {
                        trendingRow.getChildren().clear();
                        allCards.clear();
                        allApps = apps;

                        if (apps.isEmpty()) {
                            Label noApps = new Label(
                                    "No apps available. Is the backend running?");
                            noApps.setStyle("-fx-text-fill: #71717a;");
                            trendingRow.add(noApps, 0, 0);
                        } else {
                            int col = 0;
                            LibraryService libraryService = LibraryService.getInstance();
                            // Show 4 apps in the trending section (single row)
                            for (int i = 0; i < Math.min(4, apps.size()); i++) {
                                App app = apps.get(i);
                                StandardCard.GradientType gradient = gradients[i % gradients.length];
                                boolean isInstalled = libraryService.isInstalled(app.getId());
                                StandardCard card = new StandardCard(
                                        app.getName(),
                                        app.getOwnerLogin(),
                                        "4.5", // Placeholder rating
                                        "1k", // Placeholder count
                                        isInstalled,
                                        () -> rootLayout.showAppDetails(app),
                                        gradient);
                                trendingRow.add(card, col, 0);
                                allCards.add(card);
                                col++;
                            }
                        }
                    });
                });
    }

    private void loadAllApps() {
        // Define gradient types for variety
        StandardCard.GradientType[] gradients = {
                StandardCard.GradientType.PURPLE,
                StandardCard.GradientType.ORANGE,
                StandardCard.GradientType.TEAL,
                StandardCard.GradientType.INDIGO
        };

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
                            LibraryService libraryService = LibraryService.getInstance();
                            // Show 4 apps in the All Apps section (single row)
                            for (int i = 0; i < Math.min(4, apps.size()); i++) {
                                App app = apps.get(i);
                                StandardCard.GradientType gradient = gradients[i % gradients.length];
                                boolean isInstalled = libraryService.isInstalled(app.getId());
                                StandardCard card = new StandardCard(
                                        app.getName(),
                                        app.getOwnerLogin(),
                                        "4.5",
                                        "1k",
                                        isInstalled,
                                        () -> rootLayout.showAppDetails(app),
                                        gradient);
                                allAppsGrid.add(card, col, 0);
                                col++;
                            }
                        }
                    });
                });
    }

    @Override
    public void onSearch(String query) {
        // If query is empty, restore original trending view
        if (query == null || query.trim().isEmpty()) {
            loadTrendingApps();
            return;
        }

        // Define gradient types for variety
        StandardCard.GradientType[] gradients = {
                StandardCard.GradientType.BLUE,
                StandardCard.GradientType.PURPLE,
                StandardCard.GradientType.PINK,
                StandardCard.GradientType.ORANGE,
                StandardCard.GradientType.TEAL,
                StandardCard.GradientType.INDIGO
        };

        trendingRow.getChildren().clear();
        int col = 0;
        int row = 0;
        int gradientIndex = 0;
        LibraryService libraryService = LibraryService.getInstance();

        String lowerQuery = query.toLowerCase();

        for (App app : allApps) {
            if (app.getName().toLowerCase().contains(lowerQuery) ||
                    app.getOwnerLogin().toLowerCase().contains(lowerQuery) ||
                    (app.getDescription() != null &&
                            app.getDescription().toLowerCase().contains(lowerQuery))) {
                StandardCard.GradientType gradient = gradients[gradientIndex % gradients.length];
                boolean isInstalled = libraryService.isInstalled(app.getId());
                StandardCard card = new StandardCard(
                        app.getName(),
                        app.getOwnerLogin(),
                        "4.5",
                        "1k",
                        isInstalled,
                        () -> rootLayout.showAppDetails(app),
                        gradient);
                trendingRow.add(card, col, row);
                col++;
                gradientIndex++;
                // Keep 4 columns like the trending layout
                if (col > 3) {
                    col = 0;
                    row++;
                }
            }
        }
    }
}
