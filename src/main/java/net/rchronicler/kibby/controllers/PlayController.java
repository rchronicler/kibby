package net.rchronicler.kibby.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import net.rchronicler.kibby.utils.Notify;
import net.rchronicler.kibby.utils.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlayController {
    final Logger logger = LoggerFactory.getLogger(PlayController.class);

    private ArrayList<String> importedWords = new ArrayList<>();
    private ArrayList<String> activeWords = new ArrayList<>();

    private int activeWordIndex = 0;
    private int correctWords = 0;
    private int incorrectWords = 0;
    private int totalChars = 0;

    private boolean gameRunning = false;

    private int GAME_TIME = 15; // Seconds
    private Timeline timeline;

    @FXML
    private Text txtTimer;
    @FXML
    private TextFlow wordsContainer;
    @FXML
    private TextField inputField;
    @FXML
    private RadioButton radio15;
    @FXML
    private RadioButton radio60;

    public void initialize() {
        importWords();
        renderWords();
    }

    private void importWords() {
        BufferedReader reader;
        try {
            InputStream inputStream = PlayController.class.getResourceAsStream("/eng1k.txt");
            assert inputStream != null;
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            while (line != null) {
                importedWords.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            logger.error("Error importing words:", e);
        }
    }

    private void renderWords() {
        wordsContainer.getChildren().clear();
        activeWords.clear();

        Collections.shuffle(importedWords);
        for (int i = 0; i < 40; i++) {
            activeWords.add(importedWords.get(i));
        }

        double fontSize = 20;
        Font font = Font.font("System", FontWeight.NORMAL, fontSize);
        for (String word : activeWords) {
            Text wordText = new Text(word);
            wordText.setFont(font);

            Text spaceText = new Text(" ");
            spaceText.setFont(font);

            wordsContainer.getChildren().addAll(wordText, spaceText);
        }

        // Highlight the first word
        Text firstWord = (Text) wordsContainer.getChildren().get(0);
        firstWord.setStyle("-fx-underline: true;");
    }

    public void startGame(KeyEvent keyEvent) {
        if (!gameRunning) {
            gameRunning = true;
            startTimer();
        }
        String input = inputField.getText().trim();

        if (keyEvent.getCode().toString().equals("SPACE") && !input.isEmpty()) {
            inputField.clear();

            // Calculate the index of the word node, skipping spaces
            int wordNodeIndex = activeWordIndex * 2;

            if (input.equals(activeWords.get(activeWordIndex))) {
                // Highlight the correct word
                Text correctWord = (Text) wordsContainer.getChildren().get(wordNodeIndex);
                correctWord.setStyle("-fx-fill: #0969da; -fx-font-style: italic;");

                if (wordNodeIndex + 2 < wordsContainer.getChildren().size()) {
                    // Highlight the next word (if exists)
                    Text nextWord = (Text) wordsContainer.getChildren().get(wordNodeIndex + 2);
                    nextWord.setStyle("-fx-underline: true;");
                }

                // Total characters
                totalChars += input.length();

                // Go to next word
                activeWordIndex++;
                // Record the correct word
                correctWords++;

                if (activeWordIndex == 40) {
                    logger.info("Game over!");
                    logger.info("Correct words: " + correctWords);
                    logger.info("Incorrect words: " + incorrectWords);
                    activeWordIndex = 0;
                    initialize();
                }
            } else {
                // Highlight the incorrect word
                Text incorrectWord = (Text) wordsContainer.getChildren().get(wordNodeIndex);
                incorrectWord.setStyle("-fx-fill: #f26755; -fx-font-style: italic;");

                // Highlight the next word
                if (wordNodeIndex + 2 < wordsContainer.getChildren().size()) {
                    // Highlight the next word (if exists)
                    Text nextWord = (Text) wordsContainer.getChildren().get(wordNodeIndex + 2);
                    nextWord.setStyle("-fx-underline: true;");
                }
                // Go to next word
                activeWordIndex++;
                // Record the incorrect word
                incorrectWords++;
            }

            logger.info("Input word: " + input);
            logger.info(activeWords.get(activeWordIndex));
            logger.info("Correct words: " + correctWords);
        }
    }

    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(GAME_TIME), e -> endGame()));
        timeline.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            txtTimer.setText(String.valueOf(GAME_TIME - (int) newValue.toSeconds()));
        });

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void endGame() {
        inputField.setDisable(true);
        timeline.stop();
        gameRunning = false;

        // Calculate WPM
        double minutes = GAME_TIME / 60.0;
        double netWPM = (correctWords - incorrectWords) / minutes; // Corrected for errors
        double accuracy = ((double) correctWords / (correctWords + incorrectWords)) * 100;
        int rawWPM = (int) ((correctWords + incorrectWords) / minutes); // Not corrected for errors
        int correctWords2 = this.correctWords;
        int incorrectWords2 = this.incorrectWords;
        int cpm = (int) (totalChars / minutes);

        // Logging
        logger.info("Game's ended.");
        logger.info("Correct words: " + correctWords);
        logger.info("Incorrect words: " + incorrectWords);
        logger.info("Gross WPM: " + rawWPM);
        logger.info("Net WPM (adjusted for errors): " + netWPM);

        javafx.application.Platform.runLater(() -> {

            if (netWPM < 10 || netWPM > 1000) {
                Notify.error("Invalid WPM. Please try again.");
                inputField.setDisable(false);
                inputField.clear();
                return;
            }

            Notify.info("Time's up! Game over! Your score is " + (int) netWPM + " WPM."
                    + "\nCorrect words: " + correctWords2
                    + "\nIncorrect words: " + incorrectWords2
                    + "\nCPM: " + cpm
                    + "\nRaw WPM: " + rawWPM + " WPM"
                    + "\nAccuracy: " + String.format("%.2f", accuracy) + "%");

            Map<String, Object> scoreData = new HashMap<>();
            scoreData.put("user_id", UserSession.getCurrentUser().getId());
            scoreData.put("raw", rawWPM);
            scoreData.put("cpm", cpm);
            scoreData.put("wpm", (int) netWPM);
            scoreData.put("accuracy", accuracy);
            scoreData.put("duration", GAME_TIME);
            scoreData.put("difficulty", "easy");

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody;
            try {
                jsonBody = objectMapper.writeValueAsString(scoreData);
            } catch (JsonProcessingException e) {
                logger.error("Error converting score data to JSON:", e);
                inputField.setDisable(false);
                inputField.clear();
                return;
            }

            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(new URI("https://kibby.dimas.lol/scores"))
                        .header("Authorization", "Bearer " + UserSession.getSavedToken())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 201) {
                    logger.info("Score saved successfully.");
                } else {
                    logger.error("Error saving score: " + response.body());
                    Notify.error("Error saving score: " + response.body());
                }
            } catch (URISyntaxException | IOException | InterruptedException e) {
                logger.error("Error saving score: ", e);
            }

            inputField.setDisable(false);
            inputField.clear();
        });

        // Reset game state
        activeWordIndex = 0;
        correctWords = 0;
        incorrectWords = 0;
        totalChars = 0;
        txtTimer.setText("Start typing!");
        renderWords();
    }

    public void selectTime(ActionEvent e) {
        timeline.stop();
        gameRunning = false;

        RadioButton selectedBtn = (RadioButton) e.getSource();

        // Reset game state
        activeWordIndex = 0;
        correctWords = 0;
        incorrectWords = 0;
        totalChars = 0;

        if (selectedBtn == radio15) {
            GAME_TIME = 15;
            inputField.clear();
            txtTimer.setText("Start typing!");
        } else if (selectedBtn == radio60) {
            GAME_TIME = 60;
            inputField.clear();
            txtTimer.setText("Start typing!");
        }

        initialize();
    }
}
