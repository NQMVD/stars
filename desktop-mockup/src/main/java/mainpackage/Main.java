package mainpackage;

import atlantafx.base.theme.PrimerDark;
import java.net.URL;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {

    private static final Logger logger = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(
            new PrimerDark().getUserAgentStylesheet()
        );
        
        // Load custom fonts
        javafx.scene.text.Font violetFont = javafx.scene.text.Font.loadFont(
            getClass().getResourceAsStream("/fonts/VioletSans-Regular.ttf"), 14
        );
        javafx.scene.text.Font styreneFont = javafx.scene.text.Font.loadFont(
            getClass().getResourceAsStream("/fonts/Styrene.ttf"), 14
        );
        logger.info("Loaded fonts - Violet: " + (violetFont != null ? violetFont.getFamily() : "FAILED") 
            + ", Styrene: " + (styreneFont != null ? styreneFont.getFamily() : "FAILED"));
        
        logger.info("Starting application");
        URL fxmlFileUrl = getClass()
            .getClassLoader()
            .getResource("sample.fxml");
        Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlFileUrl));
        primaryStage.setTitle("AppVault");
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
