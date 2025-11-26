package mainpackage;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.collections.transformation.FilteredList;
import javafx.collections.FXCollections;
import javafx.scene.shape.SVGPath;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private GridPane appsGrid; // Verknüpfung zum FXML Grid
    private TextField searchField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<AppData> apps = List.of(

                new AppData("Prism Core", "DevTool", "High-performance rendering engine for modern UI.", "★ 4.9", "M12 2L2 22h20L12 2z", null),
                new AppData("Box Container", "System", "Isolated environments for secure execution.", "★ 4.7", "M4 4h16v16H4z", null),
                new AppData("Orbit Sync", "Network", "Real-time database synchronization.", "★ 4.8", "M12 2a10 10 0 100 20 10 10 0 000-20z", null),
                new AppData("Frame Wire", "Design", "Prototyping tools for interface designers.", "★ 4.5", "M1 1h22v22H1z", null), // Achtung: Hier war im Original stroke, wir nutzen fill vereinfacht oder passen es unten an

                new AppData("Type Safe", "DevTool", "Static analysis for dynamic languages.", "★ 4.9", null, "TS"),
                new AppData("Micro Kit", "Lib", "Tiny utility belt for small apps.", "★ 4.6", null, "µ"),
                new AppData("Func Logic", "Math", "Lambda calculus visualizations.", "★ 4.8", null, "Fn"),
                new AppData("Deep Base", "Storage", "Persistent storage for ephemeral nodes.", "★ 4.9", null, "dB"),

                new AppData("Input/Out", "System", "Stream management system.", "★ 4.4", null, "Io"),
                new AppData("Pixel Perf", "Design", "Compression algorithm.", "★ 4.7", null, "Px"),
                new AppData("Void Ray", "Security", "Null-pointer protection.", "★ 4.9", null, "Vd"),
                new AppData("Zero Day", "Security", "Vulnerability scanner.", "★ 4.8", null, "Ze")
        );

        // 2. Grid befüllen
        int col = 0;
        int row = 0;

        for (AppData app : apps) {
            VBox card = createAppCard(app);
            appsGrid.add(card, col, row);

            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    //  baut ein panel zusammen
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
            // Sonderfall für das "Frame Wire" Icon (nur Rahmen), falls nötig:
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

        //  Beschreibung
        Label lblDesc = new Label(app.description());
        lblDesc.getStyleClass().add("card-desc");
        VBox.setVgrow(lblDesc, Priority.ALWAYS); // Drückt das Rating nach unten

        //  Rating
        Label lblRating = new Label(app.rating());
        lblRating.getStyleClass().add("card-subtitle");

        card.getChildren().addAll(header, lblDesc, lblRating);
        return card;
    }
}