package com.example.appstore;

import com.example.appstore.layout.RootLayout;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        RootLayout root = new RootLayout();
        Scene scene = new Scene(root, 1200, 800);

        // Load custom fonts
        javafx.scene.text.Font.loadFont(
            getClass().getResourceAsStream("/fonts/VioletSans-Regular.ttf"), 14
        );
        javafx.scene.text.Font.loadFont(
            getClass().getResourceAsStream("/fonts/Styrene.ttf"), 14
        );

        // Load CSS
        scene
            .getStylesheets()
            .add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Desktop App Store");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
