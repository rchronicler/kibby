package net.rchronicler.kibby.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LeaderboardEntry {
    private final IntegerProperty rank;
    private final StringProperty username;
    private final IntegerProperty topWPM;
    private final StringProperty formattedDate;

    public LeaderboardEntry(int rank, String username, int topWPM, String created_at) {
        this.rank = new SimpleIntegerProperty(rank);
        this.username = new SimpleStringProperty(username);
        this.topWPM = new SimpleIntegerProperty(topWPM);
        this.formattedDate = new SimpleStringProperty(formatDate(created_at));
    }

    public IntegerProperty rankProperty() {
        return rank;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public IntegerProperty topWPMProperty() {
        return topWPM;
    }

    public int getRank() {
        return rank.get();
    }

    public String getUsername() {
        return username.get();
    }

    public int getTopWPM() {
        return topWPM.get();
    }

    public StringProperty formattedDateProperty() {
        return formattedDate;
    }

    public String getFormattedDate() {
        return formattedDate.get();
    }

    private String formatDate(String isoDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_INSTANT;
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

        Instant instant = Instant.from(inputFormatter.parse(isoDate));
        return outputFormatter.format(instant.atZone(ZoneId.systemDefault()));
    }
}
