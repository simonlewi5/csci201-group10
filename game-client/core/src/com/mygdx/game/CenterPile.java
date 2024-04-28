package com.mygdx.game;

import java.util.ArrayList;

public class CenterPile {
    private ArrayList<Card> cards;

    public CenterPile() {
        cards = new ArrayList<Card>();
    }

    public CenterPile(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void addCard(Card card) {
        cards.add(card);
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
