package com.mygdx.game;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class Match {
    private String id;
    private List<Player> players;
    @SerializedName("player_scores")
    private Map<String, Integer> playerScores;
    @SerializedName("match_state")
    private MatchState matchState;
    @SerializedName("turn_index")
    private int turnIndex;
    private Player winner;
    @SerializedName("start_time")
    private long startTime;
    @SerializedName("end_time")
    private long endTime;
    private Deck deck;

    public Match() {
    }

    public Match(String id, List<Player> players, Map<String, Integer> playerScores, MatchState matchState, int turnIndex, Player winner, long startTime, long endTime, Deck deck) {
        this.id = id;
        this.players = players;
        this.playerScores = playerScores;
        this.matchState = matchState;
        this.turnIndex = turnIndex;
        this.winner = winner;
        this.startTime = startTime;
        this.endTime = endTime;
        this.deck = deck;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Map<String, Integer> getPlayerScores() {
        return playerScores;
    }

    public void setPlayerScores(Map<String, Integer> playerScores) {
        this.playerScores = playerScores;
    }

    public MatchState getMatchState() {
        return matchState;
    }

    public void setMatchState(MatchState matchState) {
        this.matchState = matchState;
    }

    public int getTurnIndex() {
        return turnIndex;
    }

    public void setTurnIndex(int turnIndex) {
        this.turnIndex = turnIndex;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    
}
