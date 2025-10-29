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
    logger.info("Starting application");
    URL fxmlFileUrl = getClass().getClassLoader().getResource("sample.fxml");
    Parent root = FXMLLoader.load(Objects.requireNonNull(fxmlFileUrl));
    primaryStage.setTitle("Hello Berk");
    primaryStage.setScene(new Scene(root, 300, 275));
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
