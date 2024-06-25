package net.rchronicler.kibby.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.rchronicler.kibby.utils.Notify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class RegisterController {
    final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPasswordHidden;
    @FXML
    private Button btnRegister;
    @FXML
    private CheckBox checkShowPassword;
    @FXML
    private TextField txtPassword;
    @FXML
    private TextField txtEmail;

    public void register() {
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
        } else if (!txtEmail.getText().matches("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")) {
            Notify.error("Invalid email address.");
            return;
        } else if (txtUsername.getText().contains(" ")) {
            Notify.error("Username cannot contain spaces.");
            return;
        }

        // JSON serialization
        Map<String, String> registData = new HashMap<>();
        registData.put("username", txtUsername.getText());
        registData.put("password", txtPassword.getText());
        registData.put("email", txtEmail.getText());

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(registData);
        } catch (Exception e) {
            logger.error("Error serializing JSON:", e);
            return;
        }

        // Send POST request
        try {
            btnRegister.setDisable(true);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://kibby.dimas.lol/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Send request and get response
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                logger.info("Registration successful.");
                switchToLogin();
            } else {
                logger.error("Registration failed: " + response.body());
                Notify.error("Registration failed.");
            }
        } catch (Exception e) {
            logger.error("Error sending POST request:", e);
        } finally {
            btnRegister.setDisable(false);
        }

    }

    public void switchToLogin() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/net/rchronicler/kibby/login.fxml"));
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
