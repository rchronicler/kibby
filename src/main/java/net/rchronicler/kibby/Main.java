package net.rchronicler.kibby;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.rchronicler.kibby.utils.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class Main extends Application {
    final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage stage) {
        // AtlantaFX theme
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // The window icon
        Image icon = new Image("key-press-48.png");

        Scene scene;
        FXMLLoader loader;
        logger.info("Checking user session...");
        if (UserSession.getCurrentUser() == null && UserSession.getSavedToken() == null) {
            logger.info("User hasn't logged in. Loading login.fxml...");
            loader = new FXMLLoader(getClass().getResource("login.fxml"));
        } else {
            logger.info("User has logged in. Loading dashboard.fxml...");
            loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        }

        try {
            Parent root = loader.load();
            logger.info("Creating scene...");
            scene = new Scene(root);
        } catch (IOException e) {
            logger.error("Error loading the FXML file:", e);
            return;
        }

        logger.info("Setting up stage...");
        // Window
        stage.setTitle("Kibby!");
        stage.getIcons().add(icon);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
