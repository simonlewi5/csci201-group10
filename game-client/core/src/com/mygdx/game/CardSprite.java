package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CardSprite extends Sprite {
    private Card card;

    public CardSprite(Card card, Texture texture) {
        super(texture);
        this.card = card;
    }

    public Card getCard() {
        return card;
    }
    
}
