package net.rchronicler.kibby.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.rchronicler.kibby.utils.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ProfileController {
    final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @FXML
    private Text txtUsername;
    @FXML
    private Text txtCompleted;
    @FXML
    private Text txtAvgWPM;
    @FXML
    private Text txtTopWPM;
    @FXML
    private Text txtAvgAccuracy;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private LineChart<String, Number> chartLine;

    private XYChart.Series<String, Number> wpmSeries = new XYChart.Series<>();

    public void initialize() {
        txtUsername.setText(UserSession.getCurrentUser().getUsername());

        fetchProfileData();

        yAxis.setLabel("WPM");

        chartLine.setTitle("WPM Statistic");

        wpmSeries.setName("WPM");

        // Prevent overlap on the x-axis
        chartLine.getXAxis().setTickLabelsVisible(false);

        // Show data points
        chartLine.setCreateSymbols(true);

        chartLine.getData().clear();
        try {
            chartLine.getData().add(wpmSeries);
        } catch (Exception e) {
            logger.error("Error adding series to the chart:", e);
        }
    }


    private void fetchProfileData() {
        Task<Void> fetchTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(new URI("https://kibby.dimas.lol/profile"))
                            .header("Authorization", "Bearer " + UserSession.getSavedToken())
                            .GET()
                            .build();
                    HttpClient client = HttpClient.newHttpClient();
                    HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

                    if (res.statusCode() == 401) {
                        logger.error("Unauthorized access. Logging out...");
                        Platform.runLater(() -> logout());
                        return null;
                    } else if (res.statusCode() != 200) {
                        logger.error("Error fetching profile data. Status code: " + res.body());
                        return null;
                    }

                    String responseBody = res.body();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(responseBody);

                    Platform.runLater(() -> {
                        txtTopWPM.setText(jsonNode.get("top_wpm").asText());
                        txtAvgWPM.setText(jsonNode.get("avg_wpm").asText());
                        txtAvgAccuracy.setText(jsonNode.get("avg_accuracy").asText());
                        txtCompleted.setText(jsonNode.get("total_tests").asText());

                        // Clear existing data
                        wpmSeries.getData().clear();

                        // Populate chart data
                        JsonNode last10ScoresNode = jsonNode.get("last_10_scores");
                        if (last10ScoresNode != null && last10ScoresNode.isArray()) {
                            for (int i = last10ScoresNode.size() - 1; i >= 0; i--) {
                                JsonNode scoreNode = last10ScoresNode.get(i);
                                if (scoreNode.has("wpm")) {
                                    double wpm = scoreNode.get("wpm").asDouble();
                                    wpmSeries.getData().add(new XYChart.Data<>(String.valueOf(last10ScoresNode.size() - i), wpm));
                                } else {
                                    System.err.println("Missing wpm data at index: " + i);
                                }
                            }
                        }
                    });

                } catch (Exception e) {
                    logger.error("Error fetching profile data:", e);
                }
                return null;
            }
        };

        new Thread(fetchTask).start();
    }

    public void logout() {

        UserSession.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/net/rchronicler/kibby/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            logger.error("Error loading the FXML file:", e);
            return;
        }
    }
}
