package com.mygdx.game;

import java.util.Random;

public class Card {
    private int value;
    private Suit suit;
    private float rotation;
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

    public void offsetRandomly(float x, float y) {
        // randomly offset position by -5 to 5 pixels in both x and y
        x += (new Random()).nextFloat() * 10 - 5;
        y += (new Random()).nextFloat() * 10 - 5;
        this.x = x;
        this.y = y;
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
