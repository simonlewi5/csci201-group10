package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


public class MainMenuScreen implements Screen {

    final EgyptianRatscrew game;
    private BitmapFont fontMedium;

    OrthographicCamera camera;
    Viewport viewport;

    Texture backgroundImage;
    Music mainMenuMusic;

    private final float ASPECT_RATIO = 16 / 9f;
    private int lastWidth, lastHeight;

    private float buttonHeight = 50;
    private float buttonSpacing = 40; 
    private float bstartY;

    private TextButton loginButton;
    private TextButton registrationButton;
    private TextButton exitButton;

    String color = "#e7e5e4";

    private Stage stage;

    // BitmapFont font;

    public MainMenuScreen(final EgyptianRatscrew game) {
        this.game = game;
        fontMedium = game.assetManager.getFontMedium();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1600, 1600 / ASPECT_RATIO, camera);
        camera.setToOrtho(false, 800, 800 / ASPECT_RATIO); 

        backgroundImage = game.assetManager.getBackgroundImage();
        mainMenuMusic = game.assetManager.getBackgroundMusic();

        mainMenuMusic.setLooping(true);

        // Add a resize listener to handle window resizing
        Gdx.graphics.setResizable(true);

        // Initialize lastWidth and lastHeight with initial screen size
        lastWidth = Gdx.graphics.getWidth();
        lastHeight = Gdx.graphics.getHeight();
        System.out.println("Last width: " + lastWidth + ", Last height: " + lastHeight);

        bstartY = (viewport.getWorldHeight() - (buttonHeight * 3 + buttonSpacing * 2)) / 2;

        stage = new Stage(viewport, game.batch);
    }

    @Override
    public void show() {
        mainMenuMusic.setVolume(0.5f);
        mainMenuMusic.play();
        
        Gdx.input.setInputProcessor(stage);

        TextButton.TextButtonStyle textButtonStyle = game.assetManager.getTextButtonStyle(1.0f);
        
        loginButton = new TextButton("Login", textButtonStyle);
        registrationButton = new TextButton("Create a Profile", textButtonStyle);
        exitButton = new TextButton("Exit", textButtonStyle);

        loginButton.getLabel().setAlignment(Align.center);
        registrationButton.getLabel().setAlignment(Align.center);
        exitButton.getLabel().setAlignment(Align.center);
        
        float loginButtonWidth = loginButton.getPrefWidth();
        float registrationButtonWidth = registrationButton.getPrefWidth();
        float exitButtonWidth = exitButton.getPrefWidth();

        loginButton.getLabel().setColor(Color.valueOf(color));
        registrationButton.getLabel().setColor(Color.valueOf(color));
        exitButton.getLabel().setColor(Color.valueOf(color));

        loginButton.setPosition((viewport.getWorldWidth() - loginButtonWidth) / 2, bstartY + (buttonHeight + buttonSpacing) * 2);
        registrationButton.setPosition((viewport.getWorldWidth() - registrationButtonWidth) / 2, bstartY + (buttonHeight + buttonSpacing));
        exitButton.setPosition((viewport.getWorldWidth() - exitButtonWidth) / 2, bstartY);
        
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LoginScreen(game));
            }
        });

        registrationButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new RegistrationScreen(game));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        
        // needs listeners for registrationButton and exitButton
        
        stage.addActor(loginButton);
        stage.addActor(registrationButton);
        stage.addActor(exitButton);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        
        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        
        game.batch.begin();
        game.batch.draw(backgroundImage, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.batch.end();
        
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
    }
}