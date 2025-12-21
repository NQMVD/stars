package com.example.appstore;

import com.example.appstore.layout.RootLayout;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        LOG.info("Starting Desktop App Store application");
        RootLayout root = new RootLayout();
        Scene scene = new Scene(root, 1200, 800);

        // Load custom fonts
        javafx.scene.text.Font.loadFont(
            getClass().getResourceAsStream("/fonts/VioletSans-Regular.ttf"),
            14
        );
        javafx.scene.text.Font.loadFont(
            getClass().getResourceAsStream("/fonts/Styrene.ttf"),
            14
        );

        // Load CSS
        scene
            .getStylesheets()
            .add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Desktop App Store");
        primaryStage.setScene(scene);
        primaryStage.show();
        LOG.info("Application window displayed successfully");
    }

    public static void main(String[] args) {
        LOG.info("Application starting...");
        launch(args);
    }
}
