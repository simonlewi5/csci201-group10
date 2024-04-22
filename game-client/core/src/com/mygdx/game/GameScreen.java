package com.mygdx.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;


public class GameScreen implements Screen, MessageListener {
    final EgyptianRatscrew game;
    private BitmapFont fontLarge;
    private Texture backgroundImage;
    private Music backgroundMusic;
    private OrthographicCamera camera;
    private String serverMessage;
    private GameWebSocketClient webSocketClient;
    
    public void messageReceived(String message) {
    	this.serverMessage = message;
        System.out.println("Message received: " + serverMessage);
    }

    public GameScreen(final EgyptianRatscrew game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
    }
    
    @Override
    public void show() {
        fontLarge = game.assetManager.getFontLarge();
        backgroundImage = game.assetManager.getBackgroundImage();
        backgroundMusic = game.assetManager.getBackgroundMusic();

        backgroundMusic.setVolume(game.getMusicVolume());
        backgroundMusic.setLooping(true);
        backgroundMusic.play();
    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);
        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(backgroundImage, 0, 0, 800, 480);

        GlyphLayout layout = new GlyphLayout(fontLarge, "GAME SCREEN");
        float x = (800 - layout.width) / 2;
        float y = (480 + layout.height) / 2;
        fontLarge.setColor(Color.WHITE);
        fontLarge.draw(game.batch, layout, x, y);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
    }



    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        backgroundMusic.stop();
    	webSocketClient.close();
    }

}
