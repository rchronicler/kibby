package net.rchronicler.kibby.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.rchronicler.kibby.utils.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class DashboardController {
    final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML
    private Pane paneMain;
    @FXML
    private Pane paneDashboard;
    @FXML
    private Hyperlink linkPlay;
    @FXML
    private Hyperlink linkProfile;
    @FXML
    private Hyperlink linkLeaderboard;
    @FXML
    private Hyperlink linkRooms;

    public void initialize() {
        changePane("/net/rchronicler/kibby/play.fxml");
        linkPlay.setStyle("-fx-font-weight: bold; -fx-text-fill: #0969da;");
    }

    private void changePane(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent newContent = loader.load();

            paneDashboard.getChildren().clear();

            paneDashboard.getChildren().add(newContent);

        } catch (IOException e) {
            logger.error("Error loading the FXML file:", e);
        }
    }

    public void select(ActionEvent e) {
        Hyperlink selected = (Hyperlink) e.getSource();
        switch (selected.getId()) {
            case "linkPlay":
                changePane("/net/rchronicler/kibby/play.fxml");
                linkPlay.setStyle("-fx-font-weight: bold; -fx-text-fill: #0969da;");
                linkLeaderboard.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
                linkProfile.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
                break;
            case "linkProfile":
                changePane("/net/rchronicler/kibby/profile.fxml");
                linkProfile.setStyle("-fx-font-weight: bold; -fx-text-fill: #0969da;");
                linkPlay.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
                linkLeaderboard.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
                break;
            case "linkLeaderboard":
                changePane("/net/rchronicler/kibby/leaderboard.fxml");
                linkLeaderboard.setStyle("-fx-font-weight: bold; -fx-text-fill: #0969da;");
                linkPlay.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
                linkProfile.setStyle("-fx-font-weight: normal; -fx-text-fill: black;");
                break;
            case "linkRooms":
                logger.info("Loading rooms.fxml...");
                break;
        }
    }

    public void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be logged out of your account.");
        if (alert.showAndWait().get().getText().equals("Cancel")) {
            return;
        }

        logger.info("Logging out...");
        try {
            UserSession.logout();
        } catch (Exception e) {
            logger.error("Error logging out:", e);
            return;
        }

        logger.info("Loading login.fxml...");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/net/rchronicler/kibby/login.fxml"));
        Scene scene = null;
        try {
            Parent root = loader.load();
            logger.info("Creating scene...");
            scene = new Scene(root);
        } catch (IOException e) {
            logger.error("Error loading the FXML file:", e);
            return;
        }

        logger.info("Setting up stage...");
        Stage stage = (Stage) paneMain.getScene().getWindow();
        stage.setScene(scene);
        logger.info("Logged out successfully.");
    }
}
