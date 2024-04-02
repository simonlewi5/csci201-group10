package com.mygdx.game;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {
    final Egyptian_Ratscrew game;

    // Here, we can instantiate objects for each of the classes

    // It'll be easiest to make all the cards rectangles, so we'd instantiate a cards class like this:
    // Array<Rectangle> cards;

    // The Texture object is where we'll store images for the different things in the game
    // So we'll need a texture for each of the 52 cards

    // There is also the Music object and the Sound object for obvious reasons

    // This is needed to basically position the game on the screen
    OrthographicCamera camera;

    public GameScreen(final Egyptian_Ratscrew game) {
        this.game = game;

        // Here we'd load all the images from the asset folder like this:

        // objectName = new Texture(Gdx.files.internal("fileName"));

        // To make the music loop, do:

        // musicName.setLooping(true);

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        //  Create any necessary first objects, like maybe the shapes that represent the players

    }


    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
        // arguments to clear are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        ScreenUtils.clear(0, 0, 0.2f, 1);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

        // begin a new batch and draw all the textures
        game.batch.begin();

        // This writes text on the screen at a specific x,y coordinate
        game.font.draw(game.batch, "something something", 0, 480);

        // This commented out code is how you would draw a rectangle on the screen
        // This is important since we're probably going to be rendering the cards as rectangles

        // game.batch.draw(name of object, x co-ord, y co-ord, width, height);

        // process user input
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            //
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT)){
            // Add any potential stuff that left arrow key presses might do in the game
            // This is mostly just leftover code from a different project I made which may or may not be helpful
        }


        if (Gdx.input.isKeyPressed(Keys.RIGHT)){
            // Same here but right arrow key
        }

            // Basically, put anything here that needs to be done over and over again while the game is running
            // So, updating the cards that are facing up, updating any text displayed on the screen, etc

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown:
        // musicName.play();
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
        // Dispose of all the assets we loaded earlier
        // objectName.dispose();
    }

}
