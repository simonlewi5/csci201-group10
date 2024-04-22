package com.mygdx.game;

import com.google.gson.annotations.SerializedName;

public class Player {
    private String id;
    private String username;
    private String email;
    @SerializedName("games_played")
    private int gamesPlayed;
    @SerializedName("games_won")
    private int gamesWon;
    @SerializedName("games_lost")
    private int gamesLost;
    @SerializedName("total_score")
    private int totalScore;
    private boolean currentPlayer;

    public Player() {
    }

    public Player(String id, String username, String email, int gamesPlayed, int gamesWon, int gamesLost, int totalScore, boolean currentPlayer) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.gamesLost = gamesLost;
        this.totalScore = totalScore;
        this.currentPlayer = currentPlayer;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public boolean currentPlayer() {
        return currentPlayer;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public void setCurrentPlayer(boolean currPlayer) {
        currentPlayer = currPlayer;
    }

}
