package mainpackage;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    @FXML
    private HBox featuredContainer;

    @FXML
    private GridPane appsGrid;

    @FXML
    private TextField searchField;

    private List<AppData> allApps;
    private List<AppData> featuredApps;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeData();
        renderFeatured();
        renderTrending(allApps);

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterApps(newValue);
            });
        }

        // Make grid responsive to width changes
        appsGrid.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((o, oldWidth, newWidth) -> {
                    updateGridColumns(newWidth.doubleValue());
                });
                // Initial update
                updateGridColumns(newScene.getWidth());
            }
        });
    }

    private void updateGridColumns(double sceneWidth) {
        // Adjust columns based on available width (accounting for sidebar ~240px)
        double contentWidth = sceneWidth - 240;
        int columns;
        if (contentWidth < 500) {
            columns = 1;
        } else if (contentWidth < 750) {
            columns = 2;
        } else if (contentWidth < 1000) {
            columns = 3;
        } else {
            columns = 4;
        }

        // Update column constraints
        appsGrid.getColumnConstraints().clear();
        double percentWidth = 100.0 / columns;
        for (int i = 0; i < columns; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(percentWidth);
            appsGrid.getColumnConstraints().add(cc);
        }

        // Re-layout the cards with new column count
        relayoutGrid(columns);
    }

    private void relayoutGrid(int columns) {
        List<javafx.scene.Node> cards = new ArrayList<>(appsGrid.getChildren());
        appsGrid.getChildren().clear();

        int col = 0;
        int row = 0;
        for (javafx.scene.Node card : cards) {
            appsGrid.add(card, col, row);
            col++;
            if (col >= columns) {
                col = 0;
                row++;
            }
        }
    }

    private void initializeData() {
        // Featured Apps with vibrant gradients
        featuredApps = new ArrayList<>();
        featuredApps.add(new AppData("Visual Studio Code", "Microsoft", "Developer Tools",
                "The most popular code editor", "4.8", "Free", true,
                "linear-gradient(to bottom right, #0078d4, #00bcf2)",
                "M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z",
                null));
        featuredApps.add(new AppData("Figma", "Figma, Inc.", "Design",
                "Design, prototype, and gather feedback", "4.9", "Free", true,
                "linear-gradient(to bottom right, #f24e1e, #ff7262, #a259ff)",
                "M12 2a10 10 0 100 20 10 10 0 000-20z", null));
        featuredApps.add(new AppData("Slack", "Slack Technologies", "Business",
                "Where work happens", "4.7", "Free", false,
                "linear-gradient(to bottom right, #4a154b, #611f69, #ecb22e)",
                "M6 12h8M6 8h12M6 16h6", null));

        // Trending Apps
        allApps = new ArrayList<>();
        allApps.add(new AppData("Visual Studio Code", "Microsoft", "Developer Tools",
                "(125k)", "★ 4.8", "Free", true,
                "linear-gradient(135deg, #0078d4, #00bcf2)",
                "M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z",
                null));
        allApps.add(new AppData("Slack", "Slack Technologies", "Business",
                "(200k)", "★ 4.7", "Free", true,
                "linear-gradient(135deg, #4a154b, #611f69)",
                "M6 12h8M6 8h12M6 16h6", null));
        allApps.add(new AppData("Figma", "Figma, Inc.", "Design",
                "(89k)", "★ 4.9", "Free", true,
                "linear-gradient(135deg, #f24e1e, #a259ff)",
                "M12 2a10 10 0 100 20 10 10 0 000-20z", null));
        allApps.add(new AppData("Discord", "Discord Inc.", "Social",
                "(150k)", "★ 4.8", "Free", false,
                "linear-gradient(135deg, #5865F2, #7289da)",
                "M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z",
                null));
        allApps.add(new AppData("Spotify", "Spotify AB", "Music",
                "(300k)", "★ 4.6", "Free", false,
                "linear-gradient(135deg, #1DB954, #1ed760)",
                "M12 0C5.4 0 0 5.4 0 12s5.4 12 12 12 12-5.4 12-12S18.66 0 12 0zm5.521 17.34c-.24.359-.66.48-1.021.24-2.82-1.74-6.36-2.101-10.561-1.141-.418.122-.779-.179-.899-.539-.12-.421.18-.78.54-.9 4.56-1.021 8.52-.6 11.64 1.32.42.18.479.659.301 1.02zm1.44-3.3c-.301.42-.841.6-1.262.3-3.239-1.98-8.159-2.58-11.939-1.38-.479.12-1.02-.12-1.14-.6-.12-.48.12-1.021.6-1.141C9.6 9.9 15 10.561 18.72 12.84c.361.181.54.78.241 1.2zm.12-3.36C15.24 8.4 8.82 8.16 5.16 9.301c-.6.179-1.2-.181-1.38-.721-.18-.601.18-1.2.72-1.381 4.26-1.26 11.28-1.02 15.721 1.621.539.3.719 1.02.419 1.56-.299.421-1.02.599-1.559.3z",
                null));
        allApps.add(new AppData("Docker", "Docker Inc.", "DevOps",
                "(50k)", "★ 4.9", "Free", false,
                "linear-gradient(135deg, #2496ED, #066da5)",
                "M4 10h4v4H4v-4zm5 0h4v4H9v-4zm5 0h4v4h-4v-4zm-5-5h4v4H9V5zm5 0h4v4h-4V5z", null));
    }

    private void renderFeatured() {
        featuredContainer.getChildren().clear();
        featuredContainer.setSpacing(24);
        for (AppData app : featuredApps) {
            VBox card = createFeaturedCard(app);
            HBox.setHgrow(card, Priority.ALWAYS);
            featuredContainer.getChildren().add(card);
        }
    }

    private void renderTrending(List<AppData> appsToRender) {
        appsGrid.getChildren().clear();
        int col = 0;
        int row = 0;

        for (AppData app : appsToRender) {
            VBox card = createTrendingCard(app);
            appsGrid.add(card, col, row);

            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private void filterApps(String query) {
        String lowerCaseQuery = query.toLowerCase();
        List<AppData> filteredList = allApps.stream()
                .filter(app -> app.title().toLowerCase().contains(lowerCaseQuery)
                        || app.category().toLowerCase().contains(lowerCaseQuery)
                        || app.author().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
        renderTrending(filteredList);
    }

    private VBox createFeaturedCard(AppData app) {
        VBox card = new VBox();
        card.getStyleClass().add("featured-card");
        card.setMinWidth(280);
        card.setPrefWidth(350);
        card.setPrefHeight(300);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.TOP_LEFT);

        // Background with gradient
        card.setStyle("-fx-background-color: " + app.color() + "; -fx-background-radius: 20;");

        // Top Content Area - Badge + Headline
        VBox topContent = new VBox(8);
        topContent.setPadding(new Insets(28, 28, 20, 28));
        topContent.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(topContent, Priority.ALWAYS);

        // Badge (e.g., "WORLD PREMIERE")
        HBox badge = new HBox(6);
        badge.setAlignment(Pos.CENTER_LEFT);
        Label badgeText = new Label("FEATURED");
        badgeText.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label badgeDot = new Label("●");
        badgeDot.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 8px;");
        badge.getChildren().addAll(badgeText, badgeDot);

        // Large Headline
        Label headline = new Label(app.description());
        headline.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");
        headline.setWrapText(true);
        headline.setMaxWidth(280);

        topContent.getChildren().addAll(badge, headline);

        // Bottom Frosted Glass Panel
        HBox bottomPanel = new HBox(14);
        bottomPanel.setAlignment(Pos.CENTER_LEFT);
        bottomPanel.setPadding(new Insets(16, 20, 16, 20));
        String panelDefaultStyle = "-fx-background-color: rgba(255, 255, 255, 0.12); -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);";
        String panelHoverStyle = "-fx-background-color: rgba(255, 255, 255, 0.22); -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 4);";
        bottomPanel.setStyle(panelDefaultStyle);
        bottomPanel.setOnMouseEntered(e -> bottomPanel.setStyle(panelHoverStyle));
        bottomPanel.setOnMouseExited(e -> bottomPanel.setStyle(panelDefaultStyle));

        // App Icon
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(52, 52);
        iconBox.setMaxSize(52, 52);
        iconBox.setStyle("-fx-background-color: #6366f1; -fx-background-radius: 14;");
        iconBox.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent(app.svgPath());
        icon.setFill(Color.WHITE);
        icon.setScaleX(1.3);
        icon.setScaleY(1.3);
        iconBox.getChildren().add(icon);

        // App Info
        VBox appInfo = new VBox(2);
        Label appName = new Label(app.title());
        appName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label appCategory = new Label(app.category());
        appCategory.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 13px;");
        appInfo.getChildren().addAll(appName, appCategory);
        HBox.setHgrow(appInfo, Priority.ALWAYS);

        // Right side: Button + subtitle
        VBox buttonArea = new VBox(4);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        Button getBtn = new Button("GET");
        getBtn.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 24; -fx-background-radius: 16; -fx-cursor: hand;");

        Label priceNote = new Label(app.price().equals("Free") ? "" : "In-App Purchases");
        priceNote.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 10px;");

        buttonArea.getChildren().addAll(getBtn, priceNote);

        bottomPanel.getChildren().addAll(iconBox, appInfo, buttonArea);

        // Wrapper for bottom panel with margin
        VBox bottomWrapper = new VBox();
        bottomWrapper.setPadding(new Insets(0, 20, 20, 20));
        bottomWrapper.getChildren().add(bottomPanel);

        card.getChildren().addAll(topContent, bottomWrapper);
        return card;
    }

    private VBox createTrendingCard(AppData app) {
        VBox card = new VBox(0);
        card.getStyleClass().add("app-card");

        // Dark Thumbnail with small placeholder icon
        StackPane thumbnail = new StackPane();
        thumbnail.setPrefHeight(180);
        thumbnail.setMinHeight(180);
        thumbnail.setStyle("-fx-background-color: #1c1c1f; -fx-background-radius: 16 16 0 0;");
        thumbnail.setAlignment(Pos.CENTER);

        // Small placeholder image icon
        SVGPath placeholderIcon = new SVGPath();
        placeholderIcon.setContent(
                "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z");
        placeholderIcon.setFill(Color.rgb(63, 63, 70));
        placeholderIcon.setScaleX(1.0);
        placeholderIcon.setScaleY(1.0);
        thumbnail.getChildren().add(placeholderIcon);

        // Details Section - Dark panel
        VBox details = new VBox(6);
        details.setPadding(new Insets(20, 20, 24, 20));
        details.setStyle("-fx-background-color: #18181b; -fx-background-radius: 0 0 16 16;");

        // Header Row: Colored Icon + Title/Description
        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.TOP_LEFT);

        // Colored Icon Box
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(60, 60);
        iconBox.setMaxSize(60, 60);
        String iconColor = app.color().contains("#")
                ? app.color().substring(app.color().lastIndexOf("#"),
                Math.min(app.color().lastIndexOf("#") + 7, app.color().length()))
                : "#6366f1";
        iconBox.setStyle("-fx-background-color: " + iconColor + "; -fx-background-radius: 16;");
        iconBox.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent(app.svgPath());
        icon.setFill(Color.WHITE);
        icon.setScaleX(1.5);
        icon.setScaleY(1.5);
        iconBox.getChildren().add(icon);

        // Title + Description + Rating
        VBox infoBox = new VBox(3);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label title = new Label(app.title());
        title.setStyle("-fx-text-fill: #f4f4f5; -fx-font-weight: bold; -fx-font-size: 17px;");

        Label description = new Label(app.category());
        description.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 14px;");

        // Rating row
        HBox ratingRow = new HBox(6);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        ratingRow.setPadding(new Insets(4, 0, 0, 0));

        Label starIcon = new Label("★");
        starIcon.setStyle("-fx-text-fill: #71717a; -fx-font-size: 14px;");

        Label ratingLabel = new Label(app.rating().replace("★ ", ""));
        ratingLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 14px; -fx-font-weight: 500;");

        Label separator = new Label("•");
        separator.setStyle("-fx-text-fill: #52525b; -fx-font-size: 14px;");

        Label badge = new Label("Editor's Choice");
        badge.setStyle("-fx-text-fill: #6366f1; -fx-font-size: 13px; -fx-font-weight: 500;");

        ratingRow.getChildren().addAll(starIcon, ratingLabel, separator, badge);

        infoBox.getChildren().addAll(title, description, ratingRow);

        headerRow.getChildren().addAll(iconBox, infoBox);

        details.getChildren().add(headerRow);

        card.getChildren().addAll(thumbnail, details);
        return card;
    }
}