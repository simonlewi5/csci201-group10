package com.mygdx.game;

import java.util.Random;

public class Card {
    private int value;
    private Suit suit;
    private float rotation;

    // offsets
    private float x;
    private float y;

    public Card(int value, Suit suit) {
        this.value = value;
        this.suit = suit;
    }

    public void rotateRandomly() {
        // rotate sprites between -15 to 15 degrees
        rotation = (new Random()).nextFloat() * 30 - 15;
    }

    public void offsetRandomly() {
        this.x = (new Random()).nextFloat() * 10 - 5;
        this.y = (new Random()).nextFloat() * 10 - 5;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    @Override
    public String toString() {
        return value + " of " + suit;
    }

    public float getRotation() {
        return rotation;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
