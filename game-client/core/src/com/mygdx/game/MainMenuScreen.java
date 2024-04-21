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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;


public class MainMenuScreen implements Screen {

    final EgyptianRatscrew game;

    OrthographicCamera camera;
    Viewport viewport;

    Texture backgroundImage;
    Music mainMenuMusic;

    private final float ASPECT_RATIO = 16 / 9f;
    private int lastWidth, lastHeight;

    // Define a ShapeRenderer instance as a class member
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    BitmapFont font;

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

        // Calculate button dimensions and positions
        float buttonWidth = 200; // Adjust as needed
        float buttonHeight = 50; // Adjust as needed
        float buttonSpacing = 20; // Adjust as needed
        float bstartX = (viewport.getWorldWidth() - buttonWidth) / 2;
        float bstartY = (viewport.getWorldHeight() - (buttonHeight * 3 + buttonSpacing * 2)) / 2;

        // Set the viewport position to center it in the window
        viewport.setScreenPosition((int) startX, (int) startY);

        // Draw background image based on viewport dimensions
        game.batch.draw(backgroundImage, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        game.batch.end();

        // Draw buttons
        drawButton(bstartX, bstartY + (buttonHeight + buttonSpacing) * 2, buttonWidth, buttonHeight, "Start Game", delta);
        drawButton(bstartX, bstartY + (buttonHeight + buttonSpacing), buttonWidth, buttonHeight, "Join Game", delta);
        drawButton(bstartX, bstartY, buttonWidth, buttonHeight, "Exit", delta);

//        if (Gdx.input.isTouched()) {
//            int touchX = Gdx.input.getX();
//            int touchY = Gdx.input.getY();
//
//            // Check if any button is touched
//            if (touchX >= startX && touchX <= startX + buttonWidth &&
//                    touchY >= startY + (buttonHeight + buttonSpacing) * 2 && touchY <= startY + (buttonHeight + buttonSpacing) * 2 + buttonHeight) {
//                // Start Game button clicked
//                game.setScreen(new GameScreen(game));
//                dispose();
//            } else if (touchX >= startX && touchX <= startX + buttonWidth &&
//                    touchY >= startY + buttonHeight + buttonSpacing && touchY <= startY + (buttonHeight + buttonSpacing) * 2) {
//                // Join Game button clicked
//                // Implement Join Game functionality
//            } else if (touchX >= startX && touchX <= startX + buttonWidth &&
//                    touchY >= startY && touchY <= startY + buttonHeight) {
//                // Exit button clicked
//                Gdx.app.exit();
//            }
//
//
//        }

        if (Gdx.input.isTouched()) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    private void drawButton(float x, float y, float width, float height, String text, float delta) {
        // Ensure the ShapeRenderer is active
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setAutoShapeType(true); // Enable automatic shape type selection

        // Draw yellow outline
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        // Draw white button
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        game.batch.begin();

        // Draw text centered on the button
        GlyphLayout layout = new GlyphLayout();
        layout.setText(game.font, text);
        float textX = x + (width - layout.width) / 2;
        float textY = y + (height + layout.height) / 2;

        // Adjust text color to black
        game.font.setColor(Color.BLACK);
        game.font.draw(game.batch, layout, textX, textY);

        game.batch.end();
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