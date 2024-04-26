package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EgyptianRatscrew extends Game {
    public SpriteBatch batch;
    public GameAssetManager assetManager;
    public Player player1;
    private float musicVolume = 0.5f;
    private Match currentMatch;

    public void create() {
        batch = new SpriteBatch();
        assetManager = new GameAssetManager();
        assetManager.loadAssets();
        assetManager.manager.finishLoading();
        
        setCustomCursor();

        // this.setScreen(new MainMenuScreen(this));
        this.setScreen(new GameScreen(this));
    }

    private void setCustomCursor() {
        Texture cursorImg = assetManager.getCursorImage();
        if (!cursorImg.getTextureData().isPrepared()) {
            cursorImg.getTextureData().prepare();
        }
        Pixmap pixmap = cursorImg.getTextureData().consumePixmap();
        int xHotspot = pixmap.getWidth() / 2;
        int yHotspot = pixmap.getHeight() / 2;
        Cursor cursor = Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot);
        Gdx.graphics.setCursor(cursor);

        pixmap.dispose();
    }

    public void render() {
        super.render();
    }

    public void dispose() {
        batch.dispose();
        assetManager.dispose();
    }

    public float getMusicVolume() {
        return musicVolume;
    }
    public void setMusicVolume(float volume) {
        volume /= 100.0f;
        if (volume > 1.0f) musicVolume = 1.0f;
        else if (volume < 0.0f) musicVolume = 0.0f;
        else musicVolume = volume;
    }
    
    public Match getCurrentMatch() {
        return currentMatch;
    }

    public void setCurrentMatch(Match currentMatch) {
        this.currentMatch = currentMatch;
    }
}