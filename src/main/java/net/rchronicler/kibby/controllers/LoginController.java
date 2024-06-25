package net.rchronicler.kibby.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.rchronicler.kibby.models.User;
import net.rchronicler.kibby.utils.Notify;
import net.rchronicler.kibby.utils.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class LoginController {
    final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPasswordHidden;
    @FXML
    private Button btnLogin;
    @FXML
    private CheckBox checkShowPassword;
    @FXML
    private TextField txtPassword;


    public void login() {
        if (!checkShowPassword.isSelected()) {
            txtPassword.setText(txtPasswordHidden.getText());
        }

        // Validation
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            Notify.error("Please fill in all fields.");
            return;
        } else if (txtUsername.getText().length() < 4 || txtPassword.getText().length() < 4) {
            Notify.error("Username and password must be at least 4 characters long.");
            return;
        }

        // JSON serialization
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", txtUsername.getText());
        loginData.put("password", txtPassword.getText());

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(loginData);
        } catch (JsonProcessingException e) {
            logger.error("Error converting login data to JSON:", e);
            return;
        }

        // Send login and get response (JWT token)
        try {
            btnLogin.setDisable(true);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://kibby.dimas.lol/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Send request and get response
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check for successful login (status code 200)
            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                // Extract and save the JWT token from the response
                String responseBody = response.body();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(responseBody);
                String token = jsonNode.get("token").asText();
                String username = jsonNode.get("username").asText();
                int userId = jsonNode.get("user_id").asInt();
                String createdAt = jsonNode.get("created_at").asText();

                User user = new User(userId, username, createdAt);

                UserSession.setCurrentUser(user);
                UserSession.saveToken(token);

                Notify.info("Login successful.");
                logger.info("Login successful.");
            } else {
                Notify.error("Login failed. Please check your credentials.");
                logger.error("Login failed with code:" + response.statusCode());
                return;
            }
        } catch (Exception e) {
            Notify.error("Error during login request. Please try again.");
            logger.error("Error during login request:", e);
            return;
        } finally {
            Platform.runLater(() -> {
                btnLogin.setDisable(false);
            });
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/net/rchronicler/kibby/dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            logger.error("Error loading the FXML file:", e);
            return;
        }
    }

    public void switchToRegister() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/net/rchronicler/kibby/register.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (Exception e) {
            logger.error("Error loading the FXML file:", e);
            return;
        }

        stage.getScene().setRoot(root);
    }

    public void togglePasswordText() {
        if (checkShowPassword.isSelected()) {
            txtPasswordHidden.setVisible(false);
            txtPassword.setVisible(true);
            txtPassword.setText(txtPasswordHidden.getText());
        } else {
            txtPasswordHidden.setVisible(true);
            txtPassword.setVisible(false);
            txtPasswordHidden.setText(txtPassword.getText());
        }
    }
}
