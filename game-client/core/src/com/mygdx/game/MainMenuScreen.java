package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;


public class MainMenuScreen implements Screen {

    final EgyptianRatscrew game;

    OrthographicCamera camera;
    Viewport viewport;

    Texture backgroundImage;
    Music mainMenuMusic;

    private final float ASPECT_RATIO = 16 / 9f;
    private int lastWidth, lastHeight;

    public MainMenuScreen(final EgyptianRatscrew game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 800 / ASPECT_RATIO, camera); // Use desired aspect ratio
        camera.setToOrtho(false, 800, 800 / ASPECT_RATIO); // Set camera size to match viewport size

        backgroundImage = new Texture(Gdx.files.internal("main_menu_background.png"));
        mainMenuMusic = Gdx.audio.newMusic(Gdx.files.internal("Taj_Mahal.ogg"));

        mainMenuMusic.setLooping(true);

        // Add a resize listener to handle window resizing
        Gdx.graphics.setResizable(true);

        // Initialize lastWidth and lastHeight with initial screen size
        lastWidth = Gdx.graphics.getWidth();
        lastHeight = Gdx.graphics.getHeight();
    }

    @Override
    public void show() {

        mainMenuMusic.play();

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        game.batch.begin();

        float viewportWidth = viewport.getScreenWidth();
        float viewportHeight = viewport.getScreenHeight();
        float windowWidth = Gdx.graphics.getWidth();
        float windowHeight = Gdx.graphics.getHeight();

        // Calculate the start position to center the viewport in the window
        float startX = (windowWidth - viewportWidth) / 2;
        float startY = (windowHeight - viewportHeight) / 2;

        // Set the viewport position to center it in the window
        viewport.setScreenPosition((int) startX, (int) startY);

        // Draw background image based on viewport dimensions
        game.batch.draw(backgroundImage, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

//        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("styles.css"));
//        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
//        parameter.size = 24; // Set the font size
//        BitmapFont customFont = generator.generateFont(parameter);
//        generator.dispose(); // Dispose the generator when you're done
//
//        // Set the custom font
//        game.font = customFont;

        game.font.draw(game.batch, "Welcome to Egyptian Ratscrew!!! ", 100, 150);
        game.font.draw(game.batch, "Tap anywhere to begin!", 100, 100);

        game.batch.end();

        if (Gdx.input.isTouched()) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {

        // Update the viewport with the new dimensions
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

}