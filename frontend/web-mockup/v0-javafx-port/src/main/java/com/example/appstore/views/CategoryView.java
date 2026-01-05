package com.example.appstore.views;

import com.example.appstore.components.StandardCard;
import com.example.appstore.layout.RootLayout;
import com.example.appstore.model.App;
import com.example.appstore.model.PaginatedAppsResponse;
import com.example.appstore.service.ApiService;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CategoryView extends ScrollPane implements Searchable {

    private final FlowPane grid;
    private final String categoryName;
    private final RootLayout rootLayout;
    private final HBox paginationBar;
    private final Label pageLabel;
    private final Button prevButton;
    private final Button nextButton;
    private List<App> allApps = new ArrayList<>();
    private int currentPage = 1;
    private static final int PAGE_SIZE = 20;
    private int totalPages = 1;

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

        paginationBar = new HBox(16);
        paginationBar.setAlignment(Pos.CENTER);
        paginationBar.setVisible(false);

        pageLabel = new Label("Page 1 of 1");
        pageLabel.setStyle("-fx-text-fill: #a1a1aa;");

        prevButton = new Button("Previous");
        prevButton.setStyle("-fx-background-color: #27272a; -fx-text-fill: white; -fx-cursor: hand;");
        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                loadPage(currentPage - 1);
            }
        });

        nextButton = new Button("Next");
        nextButton.setStyle("-fx-background-color: #27272a; -fx-text-fill: white; -fx-cursor: hand;");
        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                loadPage(currentPage + 1);
            }
        });

        paginationBar.getChildren().addAll(prevButton, pageLabel, nextButton);

        grid = new FlowPane();
        grid.setHgap(16);
        grid.setVgap(16);

        ProgressIndicator loader = new ProgressIndicator();
        loader.setMaxSize(40, 40);
        grid.getChildren().add(loader);

        content.getChildren().add(paginationBar);
        content.getChildren().add(grid);
        setContent(content);

        loadApps();
    }

    private void loadApps() {
        ApiService.getInstance()
            .getApps()
            .thenAccept(apps -> {
                Platform.runLater(() -> {
                    grid.getChildren().clear();

                    List<App> filtered = new ArrayList<>();
                    for (App app : apps) {
                        if (
                            app.getCategory() != null &&
                            app.getCategory().equalsIgnoreCase(categoryName)
                        ) {
                            filtered.add(app);
                        }
                    }

                    if (filtered.isEmpty()) {
                        filtered = apps;
                    }

                    allApps = filtered;
                    totalPages = (int) Math.ceil((double) filtered.size() / PAGE_SIZE);
                    loadPage(1);
                });
            });
    }

    private void loadPage(int page) {
        currentPage = page;
        paginationBar.setVisible(false);

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allApps.size());

        if (start >= allApps.size()) {
            start = Math.max(0, allApps.size() - PAGE_SIZE);
            end = allApps.size();
        }

        List<App> pageApps = allApps.subList(start, end);

        grid.getChildren().clear();

        if (pageApps.isEmpty()) {
            Label noApps = new Label("No apps found in this category");
            noApps.setStyle("-fx-text-fill: #71717a;");
            grid.getChildren().add(noApps);
        } else {
            for (App app : pageApps) {
                StandardCard card = new StandardCard(
                    app,
                    false,
                    () -> rootLayout.showAppDetails(app)
                );
                grid.getChildren().add(card);
            }

            updatePagination();
        }
    }

    private void updatePagination() {
        pageLabel.setText("Page " + currentPage + " of " + totalPages + " (" + allApps.size() + " apps)");
        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= totalPages);
        paginationBar.setVisible(totalPages > 1);
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
                    app,
                    false,
                    () -> rootLayout.showAppDetails(app)
                );
                grid.getChildren().add(card);
            }
        }
    }
}
