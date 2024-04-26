package com.mygdx.game;

import java.util.ArrayList;

public class Hand {
    private ArrayList<Card> cards;

    public Hand() {
        cards = new ArrayList<Card>();
    }

    public Hand(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void addCards(ArrayList<Card> cards) {
        this.cards.addAll(cards);
    }

    public Card removeCard(int index) {
        return cards.remove(index);
    }

    public ArrayList<Card> removeCards(int index) {
        ArrayList<Card> removedCards = new ArrayList<Card>();
        for (int i = index; i < cards.size(); i++) {
            removedCards.add(cards.remove(i));
        }
        return removedCards;
    }
}
