package mainpackage;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    @FXML
    private GridPane appsGrid; // Verknüpfung zum FXML Grid

    @FXML
    private TextField searchField; // Verknüpfung zum Suchfeld

    // Die Liste als Klassenvariable speichern, damit wir später filtern können
    private List<AppData> allApps;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Die Liste einmalig befüllen
        allApps = List.of(
                new AppData("Prism Core", "DevTool", "High-performance rendering engine for modern UI.", "★ 4.9", "M12 2L2 22h20L12 2z", null),
                new AppData("Box Container", "System", "Isolated environments for secure execution.", "★ 4.7", "M4 4h16v16H4z", null),
                new AppData("Orbit Sync", "Network", "Real-time database synchronization.", "★ 4.8", "M12 2a10 10 0 100 20 10 10 0 000-20z", null),
                new AppData("Frame Wire", "Design", "Prototyping tools for interface designers.", "★ 4.5", "M1 1h22v22H1z", null),
                new AppData("Type Safe", "DevTool", "Static analysis for dynamic languages.", "★ 4.9", null, "TS"),
                new AppData("Micro Kit", "Lib", "Tiny utility belt for small apps.", "★ 4.6", null, "µ"),
                new AppData("Func Logic", "Math", "Lambda calculus visualizations.", "★ 4.8", null, "Fn"),
                new AppData("Deep Base", "Storage", "Persistent storage for ephemeral nodes.", "★ 4.9", null, "dB"),
                new AppData("Input/Out", "System", "Stream management system.", "★ 4.4", null, "Io"),
                new AppData("Pixel Perf", "Design", "Compression algorithm.", "★ 4.7", null, "Px"),
                new AppData("Void Ray", "Security", "Null-pointer protection.", "★ 4.9", null, "Vd"),
                new AppData("Zero Day", "Security", "Vulnerability scanner.", "★ 4.8", null, "Ze")
        );

        // 2. Das Grid initial anzeigen (alle Apps)
        renderGrid(allApps);

        // 3. Listener für das Suchfeld aktivieren
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterApps(newValue); // Ruft die Filter-Methode auf, wenn getippt wird
            });
        }
    }

    // --- Filter Methode: Sucht nach Text ---
    private void filterApps(String query) {
        String lowerCaseQuery = query.toLowerCase();

        // Filtern: Behalte nur Apps, wo Titel oder Kategorie passen
        List<AppData> filteredList = allApps.stream()
                .filter(app -> app.title().toLowerCase().contains(lowerCaseQuery)
                        || app.category().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());

        // Grid neu zeichnen mit den gefilterten Ergebnissen
        renderGrid(filteredList);
    }

    // --- Render Methode: Baut das Grid auf ---
    private void renderGrid(List<AppData> appsToRender) {
        appsGrid.getChildren().clear(); // WICHTIG: Erst das alte Grid leeren!

        int col = 0;
        int row = 0;

        for (AppData app : appsToRender) {
            VBox card = createAppCard(app);
            appsGrid.add(card, col, row);

            col++;
            if (col == 4) { // Nach 4 Spalten neue Zeile
                col = 0;
                row++;
            }
        }
    }

    // --- Helper Methode: Baut EINE Karte ---
    private VBox createAppCard(AppData app) {
        VBox card = new VBox(12); // 12px Abstand vertikal
        card.getStyleClass().add("app-card"); // CSS Klasse aus styles.css

        // --- Header Bereich (Icon + Titel Text) ---
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon Box (StackPane)
        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("icon-box");

        if (app.badgeText() != null) {
            // Text Badge
            Label badge = new Label(app.badgeText());
            badge.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Monospaced'");
            iconBox.getChildren().add(badge);
        } else {
            // SVG Icon
            SVGPath icon = new SVGPath();
            icon.setContent(app.svgPath());
            icon.setFill(Color.valueOf("#fafafa"));
            icon.setScaleX(0.6);
            icon.setScaleY(0.6);
            // Sonderfall für das "Frame Wire" Icon
            if(app.title().equals("Frame Wire")) {
                icon.setFill(Color.TRANSPARENT);
                icon.setStroke(Color.valueOf("#fafafa"));
                icon.setStrokeWidth(2);
            }
            iconBox.getChildren().add(icon);
        }

        // Titel & Kategorie Box
        VBox titles = new VBox();
        Label lblTitle = new Label(app.title());
        lblTitle.getStyleClass().add("card-title");

        Label lblCat = new Label(app.category());
        lblCat.getStyleClass().add("card-subtitle");

        titles.getChildren().addAll(lblTitle, lblCat);
        header.getChildren().addAll(iconBox, titles);

        // Beschreibung
        Label lblDesc = new Label(app.description());
        lblDesc.getStyleClass().add("card-desc");
        VBox.setVgrow(lblDesc, Priority.ALWAYS); // Drückt das Rating nach unten

        // Rating
        Label lblRating = new Label(app.rating());
        lblRating.getStyleClass().add("card-subtitle");

        card.getChildren().addAll(header, lblDesc, lblRating);
        return card;
    }
}