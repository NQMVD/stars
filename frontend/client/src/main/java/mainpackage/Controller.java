package mainpackage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class Controller implements Initializable {

    @FXML
    private HBox featuredContainer;

    @FXML
    private GridPane appsGrid;

    @FXML
    private GridPane allAppsGrid;

    @FXML
    private TextField searchField;

    // Navigation buttons
    @FXML
    private Button navDiscover;

    @FXML
    private Button navLibrary;

    @FXML
    private Button navUpdates;

    @FXML
    private Button navSettings;

    // Discover Icon for theme switching
    @FXML
    private SVGPath discoverIcon;

    // Views
    @FXML
    private StackPane contentArea;

    @FXML
    private VBox discoverView;

    @FXML
    private VBox settingsView;

    @FXML
    private ScrollPane appDetailView;

    @FXML
    private VBox appDetailContent;

    @FXML
    private Button themeToggleBtn;

    @FXML
    private ScrollPane libraryView;

    @FXML
    private ScrollPane updatesView;

    @FXML
    private GridPane libraryGrid;

    @FXML
    private GridPane updatesGrid;

    private List<AppData> allApps;
    private List<AppData> featuredApps;
    private List<AppData> allAppsList;
    private List<AppData> libraryApps;
    private List<AppData> updatesApps;

    private boolean isDarkMode = true;
    private LibraryService libraryService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[AppVault] Initializing application...");

        libraryService = new LibraryService();
        initializeData();
        renderFeatured();
        renderTrending(allApps);
        renderAllApps(allAppsList);
        renderLibrary();
        renderUpdates();

        // Setup navigation handlers
        setupNavigation();

        // Setup theme toggle
        if (themeToggleBtn != null) {
            themeToggleBtn.setOnAction(e -> {
                System.out.println("[AppVault] Theme toggle button clicked");
                toggleTheme();
            });
        }

        if (searchField != null) {
            searchField
                .textProperty()
                .addListener((observable, oldValue, newValue) -> {
                    System.out.println(
                        "[AppVault] Search query changed: \"" + newValue + "\""
                    );
                    filterApps(newValue);
                });
        }

        // Make grid responsive to width changes
        appsGrid
            .sceneProperty()
            .addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    // Apply system theme when scene is ready
                    applySystemTheme(newScene);

                    newScene
                        .widthProperty()
                        .addListener((o, oldWidth, newWidth) -> {
                            updateGridColumns(newWidth.doubleValue());
                        });
                    // Initial update
                    updateGridColumns(newScene.getWidth());
                }
            });

        // Make All Apps grid responsive to width changes
        allAppsGrid
            .sceneProperty()
            .addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene
                        .widthProperty()
                        .addListener((o, oldWidth, newWidth) -> {
                            updateAllAppsGridColumns(newWidth.doubleValue());
                        });
                    // Initial update
                    updateAllAppsGridColumns(newScene.getWidth());
                }
            });

        System.out.println("[AppVault] Initialization complete");
    }

    /**
     * Detects the system theme (dark/light mode) and applies it.
     * On Windows, checks the registry for the AppsUseLightTheme setting.
     */
    private void applySystemTheme(Scene scene) {
        boolean systemDarkMode = detectSystemDarkMode();
        System.out.println(
            "[AppVault] Detected system theme: " +
                (systemDarkMode ? "Dark Mode" : "Light Mode")
        );

        if (!systemDarkMode && isDarkMode) {
            // System is in light mode but app is in dark mode - switch to light
            isDarkMode = false;
            themeToggleBtn.setText("Dark Mode");
            scene.getRoot().setStyle("-fx-base: #ffffff;");
            applyLightMode(scene);

            // Re-render all cards to apply theme
            renderFeatured();
            renderTrending(allApps);
            renderAllApps(allAppsList);
            renderLibrary();
            renderUpdates();

            System.out.println(
                "[AppVault] Applied Light Mode based on system preference"
            );
        } else if (systemDarkMode && !isDarkMode) {
            // System is in dark mode but app is in light mode - switch to dark
            isDarkMode = true;
            themeToggleBtn.setText("Light Mode");
            scene.getRoot().setStyle("-fx-base: #0f0f12;");
            applyDarkMode(scene);

            // Re-render all cards to apply theme
            renderFeatured();
            renderTrending(allApps);
            renderAllApps(allAppsList);
            renderLibrary();
            renderUpdates();

            System.out.println(
                "[AppVault] Applied Dark Mode based on system preference"
            );
        }
    }

    /**
     * Detects if the system is using dark mode.
     * On Windows, reads the AppsUseLightTheme registry value.
     * Returns true for dark mode, false for light mode.
     */
    private boolean detectSystemDarkMode() {
        try {
            // Try to detect Windows dark mode via PowerShell
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-Command",
                "(Get-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize' -Name 'AppsUseLightTheme').AppsUseLightTheme"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            String result = reader.readLine();
            process.waitFor();

            if (result != null) {
                result = result.trim();
                // AppsUseLightTheme: 0 = Dark Mode, 1 = Light Mode
                return "0".equals(result);
            }
        } catch (Exception e) {
            System.out.println(
                "[AppVault] Could not detect system theme, defaulting to dark mode: " +
                    e.getMessage()
            );
        }
        // Default to dark mode if detection fails
        return true;
    }

    private void setupNavigation() {
        if (navDiscover != null) {
            navDiscover.setOnAction(e -> {
                System.out.println("[AppVault] Navigation: Discover clicked");
                showView("discover");
            });
        }
        if (navLibrary != null) {
            navLibrary.setOnAction(e -> {
                System.out.println("[AppVault] Navigation: Library clicked");
                showView("library");
            });
        }
        if (navUpdates != null) {
            navUpdates.setOnAction(e -> {
                System.out.println("[AppVault] Navigation: Updates clicked");
                showView("updates");
            });
        }
        if (navSettings != null) {
            navSettings.setOnAction(e -> {
                System.out.println("[AppVault] Navigation: Settings clicked");
                showView("settings");
            });
        }
    }

    private void showView(String viewName) {
        System.out.println("[AppVault] Switching to view: " + viewName);

        // Hide all views
        if (discoverView != null) discoverView.setVisible(false);
        if (libraryView != null) libraryView.setVisible(false);
        if (updatesView != null) updatesView.setVisible(false);
        if (settingsView != null) settingsView.setVisible(false);
        if (appDetailView != null) appDetailView.setVisible(false);

        // Update active button styling
        updateNavButtonStyle(navDiscover, false);
        updateNavButtonStyle(navLibrary, false);
        updateNavButtonStyle(navUpdates, false);
        updateNavButtonStyle(navSettings, false);

        // Show selected view
        switch (viewName) {
            case "discover":
                if (discoverView != null) discoverView.setVisible(true);
                updateNavButtonStyle(navDiscover, true);
                break;
            case "library":
                if (libraryView != null) libraryView.setVisible(true);
                updateNavButtonStyle(navLibrary, true);
                break;
            case "updates":
                if (updatesView != null) updatesView.setVisible(true);
                updateNavButtonStyle(navUpdates, true);
                break;
            case "settings":
                if (settingsView != null) settingsView.setVisible(true);
                updateNavButtonStyle(navSettings, true);
                break;
            case "appDetail":
                if (appDetailView != null) appDetailView.setVisible(true);
                break;
        }
    }

    private void updateNavButtonStyle(Button button, boolean isActive) {
        if (button == null) return;
        button.getStyleClass().remove("active");
        if (isActive) {
            button.getStyleClass().add("active");
        }
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        Scene scene = themeToggleBtn.getScene();
        System.out.println(
            "[AppVault] Toggling theme to: " +
                (isDarkMode ? "Dark Mode" : "Light Mode")
        );

        if (isDarkMode) {
            themeToggleBtn.setText("Light Mode");
            scene.getRoot().setStyle("-fx-base: #0f0f12;");
            // Apply dark mode styles
            applyDarkMode(scene);
        } else {
            themeToggleBtn.setText("Dark Mode");
            scene.getRoot().setStyle("-fx-base: #ffffff;");
            // Apply light mode styles
            applyLightMode(scene);
        }

        // Re-render all cards to apply theme
        renderFeatured();
        renderTrending(allApps);
        renderAllApps(allAppsList);
        renderLibrary();
        renderUpdates();

        System.out.println("[AppVault] Theme applied successfully");
    }

    private void applyDarkMode(Scene scene) {
        scene.getRoot().getStyleClass().remove("light-mode");
    }

    private void applyLightMode(Scene scene) {
        scene.getRoot().getStyleClass().add("light-mode");
    }

    // Helper method to get text color based on theme
    private String getTextColor() {
        return isDarkMode ? "#f4f4f5" : "#1e293b";
    }

    private String getSecondaryTextColor() {
        return isDarkMode ? "#a1a1aa" : "#475569";
    }

    private String getMutedTextColor() {
        return isDarkMode ? "#71717a" : "#64748b";
    }

    private String getCardBgColor() {
        return isDarkMode ? "#1c1c1f" : "#ffffff";
    }

    private String getDetailsBgColor() {
        return isDarkMode ? "#18181b" : "#f8fafc";
    }

    public void showAppDetail(AppData app) {
        System.out.println("[AppVault] Opening app details: " + app.title());

        if (appDetailContent == null) return;

        appDetailContent.getChildren().clear();

        // Back button
        Button backBtn = new Button("← Back to Discover");
        backBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: " +
                getSecondaryTextColor() +
                "; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0;"
        );
        backBtn.setOnAction(e -> {
            System.out.println(
                "[AppVault] Back button clicked - returning to Discover"
            );
            showView("discover");
        });

        // App header
        HBox header = new HBox(24);
        header.setAlignment(Pos.TOP_LEFT);

        // App icon
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(120, 120);
        iconBox.setMaxSize(120, 120);
        String iconColor = app.color().contains("#")
            ? app
                  .color()
                  .substring(
                      app.color().lastIndexOf("#"),
                      Math.min(
                          app.color().lastIndexOf("#") + 7,
                          app.color().length()
                      )
                  )
            : "#6366f1";
        iconBox.setStyle(
            "-fx-background-color: " +
                iconColor +
                "; -fx-background-radius: 24;"
        );
        iconBox.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent(app.svgPath());
        icon.setFill(Color.WHITE);
        icon.setScaleX(3.0);
        icon.setScaleY(3.0);
        iconBox.getChildren().add(icon);

        // App info
        VBox info = new VBox(8);
        info.setAlignment(Pos.TOP_LEFT);

        Label title = new Label(app.title());
        title.setStyle(
            "-fx-text-fill: " +
                getTextColor() +
                "; -fx-font-size: 32px; -fx-font-weight: bold;"
        );

        Label author = new Label(app.author());
        author.setStyle(
            "-fx-text-fill: " +
                getSecondaryTextColor() +
                "; -fx-font-size: 16px;"
        );

        Label category = new Label(app.category());
        category.setStyle(
            "-fx-text-fill: #6366f1; -fx-font-size: 14px; -fx-font-weight: 500;"
        );

        HBox ratingBox = new HBox(8);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label star = new Label("★");
        star.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 18px;");
        Label rating = new Label(app.rating().replace("★ ", ""));
        rating.setStyle(
            "-fx-text-fill: " +
                getTextColor() +
                "; -fx-font-size: 18px; -fx-font-weight: 600;"
        );
        Label reviews = new Label(app.description());
        reviews.setStyle(
            "-fx-text-fill: " + getMutedTextColor() + "; -fx-font-size: 14px;"
        );
        ratingBox.getChildren().addAll(star, rating, reviews);

        // Install/Open button area
        VBox buttonArea = new VBox(8);
        buttonArea.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(buttonArea, new Insets(16, 0, 0, 0));

        boolean isInstalled = libraryService.isInstalled(app.title());

        if (isInstalled) {
            Button openBtn = new Button("OPEN");
            openBtn.setStyle(
                "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 14 48; -fx-background-radius: 24; -fx-cursor: hand;"
            );
            openBtn.setOnAction(e -> {
                // Simulate opening the app
                openBtn.setText("Running...");
                openBtn.setDisable(true);
                Timeline reset = new Timeline(
                    new KeyFrame(Duration.seconds(1), ev -> {
                        openBtn.setText("OPEN");
                        openBtn.setDisable(false);
                    })
                );
                reset.play();
            });
            buttonArea.getChildren().add(openBtn);

            // Show installed version
            libraryService
                .getInstalledApp(app.title())
                .ifPresent(installed -> {
                    Label versionLabel = new Label(
                        "Installed: v" + installed.installedVersion()
                    );
                    versionLabel.setStyle(
                        "-fx-text-fill: " +
                            getMutedTextColor() +
                            "; -fx-font-size: 12px;"
                    );
                    buttonArea.getChildren().add(versionLabel);
                });
        } else {
            Button installBtn = new Button("GET");
            installBtn.setStyle(
                "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 14 48; -fx-background-radius: 24; -fx-cursor: hand;"
            );

            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(140);
            progressBar.setVisible(false);
            progressBar.setStyle("-fx-accent: #6366f1;");

            Label statusLabel = new Label("");
            statusLabel.setStyle(
                "-fx-text-fill: " +
                    getSecondaryTextColor() +
                    "; -fx-font-size: 12px;"
            );

            installBtn.setOnAction(e -> {
                // Start install animation
                installBtn.setDisable(true);
                installBtn.setText("Installing...");
                progressBar.setVisible(true);
                statusLabel.setText("Downloading...");

                // Simulate download progress over 3 seconds
                Timeline timeline = new Timeline();
                for (int i = 0; i <= 30; i++) {
                    final int step = i;
                    KeyFrame keyFrame = new KeyFrame(
                        Duration.millis(i * 100),
                        ev -> {
                            double progress = step / 30.0;
                            progressBar.setProgress(progress);
                            if (progress < 0.5) {
                                statusLabel.setText(
                                    "Downloading... " +
                                        (int) (progress * 200) +
                                        "%"
                                );
                            } else if (progress < 0.9) {
                                statusLabel.setText("Installing...");
                            } else {
                                statusLabel.setText("Finishing up...");
                            }
                        }
                    );
                    timeline.getKeyFrames().add(keyFrame);
                }

                // Complete installation
                KeyFrame completeFrame = new KeyFrame(
                    Duration.millis(3100),
                    ev -> {
                        libraryService.installApp(app);
                        progressBar.setVisible(false);
                        installBtn.setText("OPEN");
                        installBtn.setStyle(
                            "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 14 48; -fx-background-radius: 24; -fx-cursor: hand;"
                        );
                        installBtn.setDisable(false);
                        statusLabel.setText("Installed! v1.0.0");

                        // Refresh library view
                        renderLibrary();
                    }
                );
                timeline.getKeyFrames().add(completeFrame);
                timeline.play();
            });

            buttonArea
                .getChildren()
                .addAll(installBtn, progressBar, statusLabel);
        }

        info
            .getChildren()
            .addAll(title, author, category, ratingBox, buttonArea);

        header.getChildren().addAll(iconBox, info);

        // Description section
        VBox descSection = new VBox(12);
        descSection.setPadding(new Insets(32, 0, 0, 0));

        Label descTitle = new Label("Description");
        descTitle.setStyle(
            "-fx-text-fill: " +
                getTextColor() +
                "; -fx-font-size: 20px; -fx-font-weight: bold;"
        );

        Label descText = new Label(
            "This is a powerful application that helps you be more productive. " +
                "With an intuitive interface and advanced features, " +
                app.title() +
                " is designed to streamline your workflow and enhance your experience. " +
                "Download now and discover why millions of users trust this app for their daily tasks."
        );
        descText.setStyle(
            "-fx-text-fill: " +
                getSecondaryTextColor() +
                "; -fx-font-size: 15px; -fx-line-spacing: 4;"
        );
        descText.setWrapText(true);

        descSection.getChildren().addAll(descTitle, descText);

        // Screenshots section
        VBox screenshotsSection = new VBox(16);
        screenshotsSection.setPadding(new Insets(32, 0, 0, 0));

        Label screenshotsTitle = new Label("Screenshots");
        screenshotsTitle.setStyle(
            "-fx-text-fill: " +
                getTextColor() +
                "; -fx-font-size: 20px; -fx-font-weight: bold;"
        );

        HBox screenshots = new HBox(16);
        for (int i = 0; i < 3; i++) {
            StackPane screenshot = new StackPane();
            screenshot.setMinSize(280, 180);
            screenshot.setMaxSize(280, 180);
            screenshot.setStyle(
                "-fx-background-color: " +
                    getCardBgColor() +
                    "; -fx-background-radius: 16;"
            );
            screenshot.setAlignment(Pos.CENTER);

            SVGPath placeholder = new SVGPath();
            placeholder.setContent(
                "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
            );
            placeholder.setFill(
                isDarkMode ? Color.rgb(63, 63, 70) : Color.rgb(148, 163, 184)
            );
            placeholder.setScaleX(1.5);
            placeholder.setScaleY(1.5);

            Label label = new Label("Screenshot " + (i + 1));
            label.setStyle(
                "-fx-text-fill: " +
                    getMutedTextColor() +
                    "; -fx-font-size: 12px;"
            );
            VBox.setMargin(label, new Insets(8, 0, 0, 0));

            VBox screenshotBox = new VBox(4);
            screenshotBox.setAlignment(Pos.CENTER);
            screenshot.getChildren().add(placeholder);
            screenshotBox.getChildren().addAll(screenshot, label);
            screenshots.getChildren().add(screenshotBox);
        }

        screenshotsSection.getChildren().addAll(screenshotsTitle, screenshots);

        appDetailContent
            .getChildren()
            .addAll(backBtn, header, descSection, screenshotsSection);

        showView("appDetail");
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

    private void updateAllAppsGridColumns(double sceneWidth) {
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
        allAppsGrid.getColumnConstraints().clear();
        double percentWidth = 100.0 / columns;
        for (int i = 0; i < columns; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(percentWidth);
            allAppsGrid.getColumnConstraints().add(cc);
        }

        // Re-layout the cards with new column count
        relayoutAllAppsGrid(columns);
    }

    private void relayoutAllAppsGrid(int columns) {
        List<javafx.scene.Node> cards = new ArrayList<>(
            allAppsGrid.getChildren()
        );
        allAppsGrid.getChildren().clear();

        int col = 0;
        int row = 0;
        for (javafx.scene.Node card : cards) {
            allAppsGrid.add(card, col, row);
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
        featuredApps.add(
            new AppData(
                "Visual Studio Code",
                "Microsoft",
                "Developer Tools",
                "The most popular code editor",
                "4.8",
                "Free",
                true,
                "linear-gradient(to bottom right, #0078d4, #00bcf2)",
                "M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z",
                null
            )
        );
        featuredApps.add(
            new AppData(
                "Figma",
                "Figma, Inc.",
                "Design",
                "Design, prototype, and gather feedback",
                "4.9",
                "Free",
                true,
                "linear-gradient(to bottom right, #f24e1e, #ff7262, #a259ff)",
                "M12 2a10 10 0 100 20 10 10 0 000-20z",
                null
            )
        );
        featuredApps.add(
            new AppData(
                "Slack",
                "Slack Technologies",
                "Business",
                "Where work happens",
                "4.7",
                "Free",
                false,
                "linear-gradient(to bottom right, #4a154b, #611f69, #ecb22e)",
                "M6 12h8M6 8h12M6 16h6",
                null
            )
        );

        // Trending Apps
        allApps = new ArrayList<>();
        allApps.add(
            new AppData(
                "Visual Studio Code",
                "Microsoft",
                "Developer Tools",
                "(125k)",
                "★ 4.8",
                "Free",
                true,
                "linear-gradient(135deg, #0078d4, #00bcf2)",
                "M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z",
                null
            )
        );
        allApps.add(
            new AppData(
                "Slack",
                "Slack Technologies",
                "Business",
                "(200k)",
                "★ 4.7",
                "Free",
                true,
                "linear-gradient(135deg, #4a154b, #611f69)",
                "M6 12h8M6 8h12M6 16h6",
                null
            )
        );
        allApps.add(
            new AppData(
                "Figma",
                "Figma, Inc.",
                "Design",
                "(89k)",
                "★ 4.9",
                "Free",
                true,
                "linear-gradient(135deg, #f24e1e, #a259ff)",
                "M12 2a10 10 0 100 20 10 10 0 000-20z",
                null
            )
        );
        allApps.add(
            new AppData(
                "Discord",
                "Discord Inc.",
                "Social",
                "(150k)",
                "★ 4.8",
                "Free",
                false,
                "linear-gradient(135deg, #5865F2, #7289da)",
                "M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z",
                null
            )
        );
        allApps.add(
            new AppData(
                "Spotify",
                "Spotify AB",
                "Music",
                "(300k)",
                "★ 4.6",
                "Free",
                false,
                "linear-gradient(135deg, #1DB954, #1ed760)",
                "M12 0C5.4 0 0 5.4 0 12s5.4 12 12 12 12-5.4 12-12S18.66 0 12 0zm5.521 17.34c-.24.359-.66.48-1.021.24-2.82-1.74-6.36-2.101-10.561-1.141-.418.122-.779-.179-.899-.539-.12-.421.18-.78.54-.9 4.56-1.021 8.52-.6 11.64 1.32.42.18.479.659.301 1.02zm1.44-3.3c-.301.42-.841.6-1.262.3-3.239-1.98-8.159-2.58-11.939-1.38-.479.12-1.02-.12-1.14-.6-.12-.48.12-1.021.6-1.141C9.6 9.9 15 10.561 18.72 12.84c.361.181.54.78.241 1.2zm.12-3.36C15.24 8.4 8.82 8.16 5.16 9.301c-.6.179-1.2-.181-1.38-.721-.18-.601.18-1.2.72-1.381 4.26-1.26 11.28-1.02 15.721 1.621.539.3.719 1.02.419 1.56-.299.421-1.02.599-1.559.3z",
                null
            )
        );
        allApps.add(
            new AppData(
                "Docker",
                "Docker Inc.",
                "DevOps",
                "(50k)",
                "★ 4.9",
                "Free",
                false,
                "linear-gradient(135deg, #2496ED, #066da5)",
                "M4 10h4v4H4v-4zm5 0h4v4H9v-4zm5 0h4v4h-4v-4zm-5-5h4v4H9V5zm5 0h4v4h-4V5z",
                null
            )
        );

        // All Apps list
        allAppsList = new ArrayList<>();
        allAppsList.add(
            new AppData(
                "Notion",
                "Notion Labs",
                "Productivity",
                "(180k)",
                "★ 4.9",
                "Free",
                true,
                "linear-gradient(135deg, #000000, #333333)",
                "M4 4v16h16V4H4zm2 2h12v12H6V6z",
                null
            )
        );
        allAppsList.add(
            new AppData(
                "Postman",
                "Postman Inc.",
                "Developer Tools",
                "(95k)",
                "★ 4.7",
                "Free",
                true,
                "linear-gradient(135deg, #FF6C37, #ff914d)",
                "M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5",
                null
            )
        );
        allAppsList.add(
            new AppData(
                "Zoom",
                "Zoom Video",
                "Communication",
                "(500k)",
                "★ 4.5",
                "Free",
                false,
                "linear-gradient(135deg, #2D8CFF, #0B5CFF)",
                "M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z",
                null
            )
        );
        allAppsList.add(
            new AppData(
                "1Password",
                "AgileBits",
                "Security",
                "(75k)",
                "★ 4.8",
                "Free",
                true,
                "linear-gradient(135deg, #1A8CFF, #0066CC)",
                "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z",
                null
            )
        );
        allAppsList.add(
            new AppData(
                "Obsidian",
                "Obsidian",
                "Notes",
                "(65k)",
                "★ 4.9",
                "Free",
                false,
                "linear-gradient(135deg, #7C3AED, #A78BFA)",
                "M12 2L2 12l10 10 10-10L12 2z",
                null
            )
        );
        allAppsList.add(
            new AppData(
                "Blender",
                "Blender Foundation",
                "3D Graphics",
                "(120k)",
                "★ 4.8",
                "Free",
                false,
                "linear-gradient(135deg, #F5792A, #E87D0D)",
                "M12 2a10 10 0 00-6 18l6-9 6 9a10 10 0 00-6-18z",
                null
            )
        );
        allAppsList.add(
            new AppData(
                "OBS Studio",
                "OBS Project",
                "Video",
                "(200k)",
                "★ 4.7",
                "Free",
                false,
                "linear-gradient(135deg, #1D1D1D, #4A4A4A)",
                "M12 12m-10 0a10 10 0 1020 0 10 10 0 10-20 0",
                null
            )
        );
        allAppsList.add(
            new AppData(
                "VLC",
                "VideoLAN",
                "Media",
                "(400k)",
                "★ 4.6",
                "Free",
                false,
                "linear-gradient(135deg, #FF8800, #FF6600)",
                "M12 2L4 20h16L12 2z",
                null
            )
        );

        // Library apps (installed apps)
        libraryApps = new ArrayList<>();
        libraryApps.add(
            new AppData(
                "Visual Studio Code",
                "Microsoft",
                "Developer Tools",
                "(2.5M)",
                "★ 4.8",
                "Free",
                true,
                "linear-gradient(135deg, #007ACC, #1f6feb)",
                "M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4",
                null
            )
        );
        libraryApps.add(
            new AppData(
                "Figma",
                "Figma Inc.",
                "Design",
                "(890k)",
                "★ 4.9",
                "Free",
                true,
                "linear-gradient(135deg, #F24E1E, #FF7262)",
                "M12 2a4 4 0 00-4 4v4a4 4 0 004 4 4 4 0 004-4V6a4 4 0 00-4-4z",
                null
            )
        );
        libraryApps.add(
            new AppData(
                "Slack",
                "Slack Technologies",
                "Business",
                "(1.2M)",
                "★ 4.7",
                "Free",
                true,
                "linear-gradient(135deg, #ECB22E, #36C5F0)",
                "M6 12a2 2 0 002 2v4h4v-4a2 2 0 10-4-2H6zm12 0a2 2 0 01-2 2v4h-4v-4a2 2 0 114 2h2z",
                null
            )
        );
        libraryApps.add(
            new AppData(
                "Discord",
                "Discord Inc.",
                "Social",
                "(3.1M)",
                "★ 4.6",
                "Free",
                false,
                "linear-gradient(135deg, #5865F2, #7289DA)",
                "M20.317 4.492c-1.53-.69-3.17-1.2-4.885-1.49a.075.075 0 00-.079.036c-.21.369-.444.85-.608 1.23a18.566 18.566 0 00-5.487 0 12.36 12.36 0 00-.617-1.23A.077.077 0 008.562 3c-1.714.29-3.354.8-4.885 1.491a.07.07 0 00-.032.027C.533 9.093-.32 13.555.099 17.961a.08.08 0 00.031.055 20.03 20.03 0 005.993 2.98.078.078 0 00.084-.026 13.83 13.83 0 001.226-1.963.074.074 0 00-.041-.104 13.175 13.175 0 01-1.872-.878.075.075 0 01-.008-.125c.126-.093.252-.19.372-.287a.075.075 0 01.078-.01c3.927 1.764 8.18 1.764 12.061 0a.075.075 0 01.079.009c.12.098.245.195.372.288a.075.075 0 01-.006.125c-.598.344-1.22.635-1.873.877a.075.075 0 00-.041.105c.36.687.772 1.341 1.225 1.962a.077.077 0 00.084.028 19.963 19.963 0 006.002-2.981.076.076 0 00.032-.054c.5-5.094-.838-9.52-3.549-13.442a.06.06 0 00-.031-.028zM8.02 15.278c-1.182 0-2.157-1.069-2.157-2.38 0-1.312.956-2.38 2.157-2.38 1.21 0 2.176 1.077 2.157 2.38 0 1.312-.956 2.38-2.157 2.38zm7.975 0c-1.183 0-2.157-1.069-2.157-2.38 0-1.312.955-2.38 2.157-2.38 1.21 0 2.176 1.077 2.157 2.38 0 1.312-.946 2.38-2.157 2.38z",
                null
            )
        );

        // Updates apps (apps with updates available)
        updatesApps = new ArrayList<>();
        updatesApps.add(
            new AppData(
                "Visual Studio Code",
                "Microsoft",
                "Update: v1.85.0 → v1.86.0",
                "New features and bug fixes",
                "★ 4.8",
                "Free",
                true,
                "linear-gradient(135deg, #007ACC, #1f6feb)",
                "M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4",
                null
            )
        );
        updatesApps.add(
            new AppData(
                "Figma",
                "Figma Inc.",
                "Update: v116.5.2 → v116.6.0",
                "Performance improvements",
                "★ 4.9",
                "Free",
                true,
                "linear-gradient(135deg, #F24E1E, #FF7262)",
                "M12 2a4 4 0 00-4 4v4a4 4 0 004 4 4 4 0 004-4V6a4 4 0 00-4-4z",
                null
            )
        );
        updatesApps.add(
            new AppData(
                "Slack",
                "Slack Technologies",
                "Update: v4.35.126 → v4.36.140",
                "Security patches",
                "★ 4.7",
                "Free",
                true,
                "linear-gradient(135deg, #ECB22E, #36C5F0)",
                "M6 12a2 2 0 002 2v4h4v-4a2 2 0 10-4-2H6zm12 0a2 2 0 01-2 2v4h-4v-4a2 2 0 114 2h2z",
                null
            )
        );
    }

    private void renderLibrary() {
        if (libraryGrid == null) return;
        libraryGrid.getChildren().clear();

        List<InstalledApp> installedApps = libraryService.getInstalledApps();

        if (installedApps.isEmpty()) {
            // Show empty state
            Label emptyLabel = new Label("No apps installed yet");
            emptyLabel.setStyle(
                "-fx-text-fill: " +
                    getMutedTextColor() +
                    "; -fx-font-size: 16px;"
            );
            libraryGrid.add(emptyLabel, 0, 0);
            return;
        }

        int col = 0;
        int row = 0;

        for (InstalledApp app : installedApps) {
            VBox card = createLibraryCard(app);
            libraryGrid.add(card, col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createLibraryCard(InstalledApp app) {
        VBox card = new VBox(0);
        card.getStyleClass().add("app-card");

        // Thumbnail with placeholder
        StackPane thumbnail = new StackPane();
        thumbnail.setPrefHeight(100);
        thumbnail.setMinHeight(100);
        String thumbnailBg = isDarkMode ? "#1c1c1f" : "#e2e8f0";
        thumbnail.setStyle(
            "-fx-background-color: " +
                thumbnailBg +
                "; -fx-background-radius: 14 14 0 0;"
        );
        thumbnail.setAlignment(Pos.CENTER);

        // App icon in thumbnail
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(48, 48);
        iconBox.setMaxSize(48, 48);
        String iconColor = app.color().contains("#")
            ? app
                  .color()
                  .substring(
                      app.color().lastIndexOf("#"),
                      Math.min(
                          app.color().lastIndexOf("#") + 7,
                          app.color().length()
                      )
                  )
            : "#6366f1";
        iconBox.setStyle(
            "-fx-background-color: " +
                iconColor +
                "; -fx-background-radius: 12;"
        );
        iconBox.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent(app.svgPath());
        icon.setFill(Color.WHITE);
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);
        iconBox.getChildren().add(icon);
        thumbnail.getChildren().add(iconBox);

        // Details Section
        VBox details = new VBox(8);
        details.setPadding(new Insets(14, 14, 14, 14));
        details.setStyle(
            "-fx-background-color: " +
                getDetailsBgColor() +
                "; -fx-background-radius: 0 0 14 14;"
        );

        // Title and version
        Label title = new Label(app.title());
        title.setStyle(
            "-fx-text-fill: " +
                getTextColor() +
                "; -fx-font-weight: bold; -fx-font-size: 14px;"
        );

        Label version = new Label("v" + app.installedVersion());
        version.setStyle(
            "-fx-text-fill: " + getMutedTextColor() + "; -fx-font-size: 11px;"
        );

        Label category = new Label(app.category());
        category.setStyle(
            "-fx-text-fill: " +
                getSecondaryTextColor() +
                "; -fx-font-size: 12px;"
        );

        // Action buttons
        HBox buttonRow = new HBox(8);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        Button updateBtn = new Button("Update");
        updateBtn.setStyle(
            "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        updateBtn.setOnAction(e -> {
            updateBtn.setDisable(true);
            updateBtn.setText("Updating...");

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1.5), ev -> {
                    libraryService.updateApp(app.title());
                    updateBtn.setText("Updated!");
                    updateBtn.setStyle(
                        "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 8;"
                    );

                    // Refresh after a moment
                    Timeline refresh = new Timeline(
                        new KeyFrame(Duration.seconds(0.5), r ->
                            renderLibrary()
                        )
                    );
                    refresh.play();
                })
            );
            timeline.play();
        });

        Button removeBtn = new Button("Remove");
        removeBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-radius: 8; -fx-cursor: hand;"
        );
        removeBtn.setOnAction(e -> {
            removeBtn.setDisable(true);
            removeBtn.setText("Removing...");

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), ev -> {
                    libraryService.removeApp(app.title());
                    renderLibrary();
                })
            );
            timeline.play();
        });

        buttonRow.getChildren().addAll(updateBtn, removeBtn);

        details.getChildren().addAll(title, version, category, buttonRow);

        card.getChildren().addAll(thumbnail, details);
        return card;
    }

    private void renderUpdates() {
        if (updatesGrid == null || updatesApps == null) return;
        updatesGrid.getChildren().clear();
        int col = 0;
        int row = 0;

        for (AppData app : updatesApps) {
            VBox card = createTrendingCard(app);
            updatesGrid.add(card, col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private void renderFeatured() {
        featuredContainer.getChildren().clear();
        featuredContainer.setSpacing(20);
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
        int maxApps = 4; // Show only 4 apps (1 row) initially
        int count = 0;

        for (AppData app : appsToRender) {
            if (count >= maxApps) break;
            VBox card = createTrendingCard(app);
            appsGrid.add(card, col, row);

            col++;
            count++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private void renderAllApps(List<AppData> appsToRender) {
        allAppsGrid.getChildren().clear();
        int col = 0;
        int row = 0;
        int maxApps = 4; // Show only 4 apps (1 row) initially
        int count = 0;

        for (AppData app : appsToRender) {
            if (count >= maxApps) break;
            VBox card = createTrendingCard(app); // Uses same card style as Trending
            allAppsGrid.add(card, col, row);

            col++;
            count++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private void filterApps(String query) {
        String lowerCaseQuery = query.toLowerCase();
        List<AppData> filteredList = allApps
            .stream()
            .filter(
                app ->
                    app.title().toLowerCase().contains(lowerCaseQuery) ||
                    app.category().toLowerCase().contains(lowerCaseQuery) ||
                    app.author().toLowerCase().contains(lowerCaseQuery)
            )
            .collect(Collectors.toList());
        renderTrending(filteredList);
    }

    private VBox createFeaturedCard(AppData app) {
        VBox card = new VBox();
        card.getStyleClass().add("featured-card");
        card.setMinWidth(240);
        card.setPrefWidth(320);
        card.setPrefHeight(240);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.TOP_LEFT);

        // Background with gradient
        card.setStyle(
            "-fx-background-color: " +
                app.color() +
                "; -fx-background-radius: 20;"
        );

        // Top Content Area - Badge + Headline
        VBox topContent = new VBox(6);
        topContent.setPadding(new Insets(20, 20, 12, 20));
        topContent.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(topContent, Priority.ALWAYS);

        // Badge (e.g., "WORLD PREMIERE")
        HBox badge = new HBox(5);
        badge.setAlignment(Pos.CENTER_LEFT);
        Label badgeText = new Label("FEATURED");
        badgeText.setStyle(
            "-fx-text-fill: #a78bfa; -fx-font-size: 10px; -fx-font-weight: bold;"
        );
        Label badgeDot = new Label("●");
        badgeDot.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 7px;");
        badge.getChildren().addAll(badgeText, badgeDot);

        // Large Headline
        Label headline = new Label(app.description());
        headline.setStyle(
            "-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;"
        );
        headline.setWrapText(true);
        headline.setMaxWidth(260);

        topContent.getChildren().addAll(badge, headline);

        // Bottom Frosted Glass Panel
        HBox bottomPanel = new HBox(12);
        bottomPanel.setAlignment(Pos.CENTER_LEFT);
        bottomPanel.setPadding(new Insets(12, 14, 12, 14));
        String panelDefaultStyle =
            "-fx-background-color: rgba(255, 255, 255, 0.12); -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);";
        String panelHoverStyle =
            "-fx-background-color: rgba(255, 255, 255, 0.22); -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 4);";
        bottomPanel.setStyle(panelDefaultStyle);
        bottomPanel.setOnMouseEntered(e ->
            bottomPanel.setStyle(panelHoverStyle)
        );
        bottomPanel.setOnMouseExited(e ->
            bottomPanel.setStyle(panelDefaultStyle)
        );

        // App Icon
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(42, 42);
        iconBox.setMaxSize(42, 42);
        iconBox.setStyle(
            "-fx-background-color: #6366f1; -fx-background-radius: 12;"
        );
        iconBox.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent(app.svgPath());
        icon.setFill(Color.WHITE);
        icon.setScaleX(1.0);
        icon.setScaleY(1.0);
        iconBox.getChildren().add(icon);

        // App Info
        VBox appInfo = new VBox(2);
        Label appName = new Label(app.title());
        appName.setStyle(
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;"
        );
        Label appCategory = new Label(app.category());
        appCategory.setStyle(
            "-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;"
        );
        appInfo.getChildren().addAll(appName, appCategory);
        HBox.setHgrow(appInfo, Priority.ALWAYS);

        // Right side: Button + subtitle
        VBox buttonArea = new VBox(3);
        buttonArea.setAlignment(Pos.CENTER_RIGHT);

        Button getBtn = new Button("GET");
        getBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 18; -fx-background-radius: 14; -fx-cursor: hand;"
        );

        Label priceNote = new Label(
            app.price().equals("Free") ? "" : "In-App Purchases"
        );
        priceNote.setStyle(
            "-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 9px;"
        );

        buttonArea.getChildren().addAll(getBtn, priceNote);

        bottomPanel.getChildren().addAll(iconBox, appInfo, buttonArea);

        // Wrapper for bottom panel with margin
        VBox bottomWrapper = new VBox();
        bottomWrapper.setPadding(new Insets(0, 14, 14, 14));
        bottomWrapper.getChildren().add(bottomPanel);

        card.getChildren().addAll(topContent, bottomWrapper);

        // Add click handler to open app detail
        card.setOnMouseClicked(e -> showAppDetail(app));

        return card;
    }

    private VBox createTrendingCard(AppData app) {
        VBox card = new VBox(0);
        card.getStyleClass().add("app-card");

        // Thumbnail with small placeholder icon
        StackPane thumbnail = new StackPane();
        thumbnail.setPrefHeight(140);
        thumbnail.setMinHeight(140);
        String thumbnailBg = isDarkMode ? "#1c1c1f" : "#e2e8f0";
        thumbnail.setStyle(
            "-fx-background-color: " +
                thumbnailBg +
                "; -fx-background-radius: 14 14 0 0;"
        );
        thumbnail.setAlignment(Pos.CENTER);

        // Small placeholder image icon
        SVGPath placeholderIcon = new SVGPath();
        placeholderIcon.setContent(
            "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
        );
        placeholderIcon.setFill(
            isDarkMode ? Color.rgb(63, 63, 70) : Color.rgb(148, 163, 184)
        );
        placeholderIcon.setScaleX(0.9);
        placeholderIcon.setScaleY(0.9);
        thumbnail.getChildren().add(placeholderIcon);

        // Details Section
        VBox details = new VBox(6);
        details.setPadding(new Insets(14, 14, 16, 14));
        details.setStyle(
            "-fx-background-color: " +
                getDetailsBgColor() +
                "; -fx-background-radius: 0 0 14 14;"
        );

        // Header Row: Colored Icon + Title/Description
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.TOP_LEFT);

        // Colored Icon Box
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(48, 48);
        iconBox.setMaxSize(48, 48);
        String iconColor = app.color().contains("#")
            ? app
                  .color()
                  .substring(
                      app.color().lastIndexOf("#"),
                      Math.min(
                          app.color().lastIndexOf("#") + 7,
                          app.color().length()
                      )
                  )
            : "#6366f1";
        iconBox.setStyle(
            "-fx-background-color: " +
                iconColor +
                "; -fx-background-radius: 12;"
        );
        iconBox.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent(app.svgPath());
        icon.setFill(Color.WHITE);
        icon.setScaleX(1.2);
        icon.setScaleY(1.2);
        iconBox.getChildren().add(icon);

        // Title + Description + Rating
        VBox infoBox = new VBox(2);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label title = new Label(app.title());
        title.setStyle(
            "-fx-text-fill: " +
                getTextColor() +
                "; -fx-font-weight: bold; -fx-font-size: 14px;"
        );

        Label description = new Label(app.category());
        description.setStyle(
            "-fx-text-fill: " +
                getSecondaryTextColor() +
                "; -fx-font-size: 12px;"
        );

        // Rating row
        HBox ratingRow = new HBox(5);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        ratingRow.setPadding(new Insets(3, 0, 0, 0));

        Label starIcon = new Label("★");
        starIcon.setStyle(
            "-fx-text-fill: " + getMutedTextColor() + "; -fx-font-size: 12px;"
        );

        Label ratingLabel = new Label(app.rating().replace("★ ", ""));
        ratingLabel.setStyle(
            "-fx-text-fill: " +
                getMutedTextColor() +
                "; -fx-font-size: 12px; -fx-font-weight: 500;"
        );

        Label separator = new Label("•");
        String separatorColor = isDarkMode ? "#52525b" : "#94a3b8";
        separator.setStyle(
            "-fx-text-fill: " + separatorColor + "; -fx-font-size: 12px;"
        );

        Label badge = new Label("Editor's Choice");
        badge.setStyle(
            "-fx-text-fill: #6366f1; -fx-font-size: 11px; -fx-font-weight: 500;"
        );

        ratingRow.getChildren().addAll(starIcon, ratingLabel, separator, badge);

        infoBox.getChildren().addAll(title, description, ratingRow);

        headerRow.getChildren().addAll(iconBox, infoBox);

        details.getChildren().add(headerRow);

        card.getChildren().addAll(thumbnail, details);

        // Add click handler to open app detail
        card.setOnMouseClicked(e -> showAppDetail(app));

        return card;
    }
}
