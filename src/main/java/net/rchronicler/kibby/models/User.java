package net.rchronicler.kibby.models;

public class User {
    private int user_id;
    private String username;
    private String created_at;

    public User(int user_id, String username, String created_at){
        this.user_id = user_id;
        this.username = username;
        this.created_at = created_at;
    }

    public int getId() {
        return this.user_id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getCreatedAt() {
        return this.created_at;
    }

    public void setId(int user_id) {
        this.user_id = user_id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }
}
