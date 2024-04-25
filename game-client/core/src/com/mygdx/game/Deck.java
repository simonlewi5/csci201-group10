package com.mygdx.game;

import java.util.List;

public class Deck {
    private List<Card> cards;
    public Deck(List<Card> cards) {
        this.cards = cards;
    }
    public List<Card> getCards() {
        return cards;
    }
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
}
