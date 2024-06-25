package net.rchronicler.kibby.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import net.rchronicler.kibby.models.LeaderboardEntry;
import net.rchronicler.kibby.utils.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardController {
    final Logger logger = LoggerFactory.getLogger(LeaderboardController.class);

    @FXML
    private TableView tableView;

    @FXML
    private TableColumn<LeaderboardEntry, Number> colRank;

    @FXML
    private TableColumn<LeaderboardEntry, String> colUsername;

    @FXML
    private TableColumn<LeaderboardEntry, Number> colTopWPM;

    @FXML
    private TableColumn<LeaderboardEntry, String> colDate;

    public void initialize() {
        fetchLeaderboard();
    }

    private void fetchLeaderboard() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI("https://kibby.dimas.lol/leaderboard")).header("Authorization", "Bearer " + UserSession.getSavedToken()).GET().build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("Error fetching leaderboard data. Status code: " + response.body());
                return;
            }

            String responseBody = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();

            javafx.application.Platform.runLater(() -> {
                JsonNode leaderList = jsonNode.get("leaderboard");
                int i = 1;
                for (JsonNode entry : leaderList) {
                    leaderboardEntries.add(new LeaderboardEntry(i, entry.get("username").asText(), entry.get("wpm").asInt(), entry.get("created_at").asText()));
                    i++;
                }

                colRank.setCellValueFactory(cellData -> cellData.getValue().rankProperty());
                colUsername.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
                colTopWPM.setCellValueFactory(cellData -> cellData.getValue().topWPMProperty());
                colDate.setCellValueFactory(cellData -> cellData.getValue().formattedDateProperty());

                tableView.getItems().addAll(leaderboardEntries);
            });
        } catch (Exception e) {
            logger.error("Error fetching leaderboard data:", e);
        }
    }
}
