package net.rchronicler.kibby.utils;

import net.rchronicler.kibby.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.prefs.Preferences;

public class UserSession {
    final static Logger logger = LoggerFactory.getLogger(UserSession.class);
    private static final String SECRET_KEY = "7ebUK_JWr-V2iBP4wcxozZq-vfrC-anT";

    private static User currentUser;

    public static void setCurrentUser(User user){
        currentUser = user;

        // Save the user information to local storage
        logger.info("Saving the user session to local storage...");
        Preferences pref = Preferences.userNodeForPackage(UserSession.class);
        try {
            pref.putInt("user_id", user.getId());
            pref.put("username", user.getUsername());
            pref.put("created_at", user.getCreatedAt());
        } catch (Exception e){
            logger.error("Error saving user session:", e);
        }
    }

    public static User getCurrentUser() {
        Preferences prefs = Preferences.userNodeForPackage(UserSession.class);

        // Get the saved user information
        int userId = prefs.getInt("user_id", 0);
        String username = prefs.get("username", null);
        String createdAt = prefs.get("created_at", null);

        if (userId == 0 || username == null || createdAt == null) {
            return null;
        }

        currentUser = new User(prefs.getInt("user_id", 0), prefs.get("username", null), prefs.get("created_at", null));
        return currentUser;
    }

    public static void saveToken(String token) {
        String encryptedToken;

        logger.info("Encrypting the JWT token...");
        try {
            // Encryption
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
            encryptedToken = Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            logger.error("Error encrypting token:", e);
            return;
        }

        // Save the encrypted token
        logger.info("Saving the encrypted JWT token to local storage...");
        Preferences prefs = Preferences.userNodeForPackage(UserSession.class);
        prefs.put("token", encryptedToken);
    }

    public static String getSavedToken() {
        Preferences prefs = Preferences.userNodeForPackage(UserSession.class);
        String encryptedToken = prefs.get("token", null);

        logger.info("Decrypting the JWT token...");
        if (encryptedToken != null) {
            try {
                // Decryption
                Cipher cipher = Cipher.getInstance("AES");
                SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
                return new String(decryptedBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Error decrypting token:", e);
            }
        }
        return null;
    }

    public static void logout() {
        try {
            // Remove the saved credentials
            logger.info("Removing the saved credentials...");
            Preferences prefs = Preferences.userNodeForPackage(UserSession.class);
            prefs.remove("user_id");
            prefs.remove("username");
            prefs.remove("created_at");
            prefs.remove("token");
        } catch (Exception e) {
            throw new RuntimeException("Error removing credentials:", e);
        }
    }
}
