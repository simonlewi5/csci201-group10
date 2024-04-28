package com.mygdx.game;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Map;

public class Match {
    private String id;
    private ArrayList<Player> players;
    private Map<String, Hand> hands; // map of player username to hand
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
    @SerializedName("center_pile")
    private CenterPile centerPile;
    @SerializedName("last_successful_slapper")
    private String lastSuccessfulSlapper;

    public Match() {
    }

    public Match(String id, ArrayList<Player> players, Map<String, Hand> hands, MatchState matchState, int turnIndex, Player winner, long startTime, long endTime, Deck deck, CenterPile centerPile) {
        this.id = id;
        this.players = players;
        this.hands = hands;
        this.matchState = matchState;
        this.turnIndex = turnIndex;
        this.winner = winner;
        this.startTime = startTime;
        this.endTime = endTime;
        this.deck = deck;
        this.centerPile = centerPile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
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

    public CenterPile getCenterPile() {
        return centerPile;
    }

    public void setCenterPile(CenterPile centerPile) {
        this.centerPile = centerPile;
    }

    public Map<String, Hand> getHands() {
        return hands;
    }

    public void setHands(Map<String, Hand> hands) {
        this.hands = hands;
    }

    public String getlastSuccessfulSlapper() {
        return id;
    }

    public void setlastSuccessfulSlapper(String lastSuccessfulSlapper) {
        this.lastSuccessfulSlapper = lastSuccessfulSlapper;
    }
}
