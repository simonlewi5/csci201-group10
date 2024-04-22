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

    public void create() {
        batch = new SpriteBatch();
        assetManager = new GameAssetManager();
        assetManager.loadAssets();
        assetManager.manager.finishLoading();
        
        setCustomCursor();

        this.setScreen(new MainMenuScreen(this));
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

}