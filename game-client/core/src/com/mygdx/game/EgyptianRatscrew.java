package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EgyptianRatscrew extends Game {
	// public BitmapFont font;
	public SpriteBatch batch;
    public GameAssetManager assetManager;

	public void create () {
		batch = new SpriteBatch();
		// font = new BitmapFont();
		assetManager = new GameAssetManager();
		assetManager.loadAssets();
		assetManager.manager.finishLoading();
		this.setScreen(new MainMenuScreen(this));
	}

	public void render () {
		super.render();
	}

	public void dispose () {
		batch.dispose();
		// font.dispose();
		assetManager.dispose();
	}

}
