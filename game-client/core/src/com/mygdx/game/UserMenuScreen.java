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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class UserMenuScreen implements Screen {

    final EgyptianRatscrew game;
    private BitmapFont fontMedium;

    OrthographicCamera camera;
    Viewport viewport;

    Texture backgroundImage;
    Music mainMenuMusic;

    private final float ASPECT_RATIO = 16 / 9f;

    private float buttonHeight = 50;
    private float buttonSpacing = 40;
    private float bstartY;

    private TextField codeInputField, volumeField;
    private TextButton quickPlayButton, codeSubmitButton, settingsButton, exitButton,
            setVolumeButton, settingsExitButton;
    private Slider volumeSlider;
    private Table playMenu, settingsMenu;

    String color = "#e7e5e4";

    private Stage stage;

    // BitmapFont font;

    public UserMenuScreen(final EgyptianRatscrew game) {
        this.game = game;
        fontMedium = game.assetManager.getFontMedium();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1600, 1600 / ASPECT_RATIO, camera);
        camera.setToOrtho(false, 800, 800 / ASPECT_RATIO);

        backgroundImage = game.assetManager.getBackgroundImage();
        mainMenuMusic = game.assetManager.getBackgroundMusic();
        mainMenuMusic.setVolume(game.getMusicVolume());
        mainMenuMusic.setLooping(true);

        // Add a resize listener to handle window resizing
        Gdx.graphics.setResizable(true);

        bstartY = (viewport.getWorldHeight() - (buttonHeight * 3 + buttonSpacing * 2)) / 2;

        stage = new Stage(viewport, game.batch);
    }

    @Override
    public void show() {
        mainMenuMusic.setVolume(game.getMusicVolume());
        mainMenuMusic.play();

        Gdx.input.setInputProcessor(stage);

        TextField.TextFieldStyle textFieldStyle = game.assetManager.getTextFieldStyle(1.0f);
        TextButton.TextButtonStyle textButtonStyle = game.assetManager.getTextButtonStyle(1.0f);

        loadPlayMenu(textFieldStyle, textButtonStyle);
        loadSettingsMenu(textFieldStyle, textButtonStyle);
        settingsMenu.setVisible(false);
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

    private void loadSettingsMenu(TextField.TextFieldStyle textFieldStyle, TextButton.TextButtonStyle textButtonStyle) {
        // Create volume slider, exit settings button
        volumeField = new TextField(String.valueOf((int) (game.getMusicVolume() * 100)), textFieldStyle);
        setVolumeButton = new TextButton("Set Volume", textButtonStyle);
        settingsExitButton = new TextButton("Back", textButtonStyle);

        // Add event listeners
        setVolumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    int volumeLevel = Integer.parseInt(volumeField.getText());
                    game.setMusicVolume(volumeLevel);
                    mainMenuMusic.setVolume(game.getMusicVolume());
                } catch (NumberFormatException ignore) {}
                volumeField.setText(String.valueOf((int) (game.getMusicVolume() * 100)));
            }
        });
        settingsExitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                displaySettings(false);
            }
        });

        // Organize as table
        settingsMenu = new Table();
        settingsMenu.setFillParent(true);
        settingsMenu.center();
        settingsMenu.add(volumeField).expandX().padTop(10).padLeft(-9).width(150).right();
        settingsMenu.add(setVolumeButton).expandX().padTop(10).padLeft(-5).width(150).left();
        settingsMenu.row();
        settingsMenu.add(settingsExitButton).pad(20).colspan(2).expandX().center();

        stage.addActor(settingsMenu);
    }

    private void displaySettings(boolean showSettings) {
        if (showSettings) {
            playMenu.setVisible(false);
            settingsMenu.setVisible(true);
        }
        else {
            playMenu.setVisible(true);
            settingsMenu.setVisible(false);
        }
    }

    private void loadPlayMenu(TextField.TextFieldStyle textFieldStyle, TextButton.TextButtonStyle textButtonStyle) {
        // Create text, buttons, input field
        quickPlayButton = new TextButton("Quick Play", textButtonStyle);
        codeInputField = new TextField("", textFieldStyle);
        codeInputField.setMessageText("Enter game code");
        codeSubmitButton = new TextButton("Submit", textButtonStyle);
        settingsButton = new TextButton("Stats & Settings", textButtonStyle);
        exitButton = new TextButton("Exit", textButtonStyle);

        // Button styling
        settingsButton.getLabel().setColor(Color.valueOf(color));
        quickPlayButton.getLabel().setColor(Color.valueOf(color));
        exitButton.getLabel().setColor(Color.valueOf(color));

        // Add button event listeners
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                displaySettings(true);
            }
        });
        quickPlayButton.addListener(new ClickListener() {
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

        // Organize the UI elements as a table so that code input/submit can be on the same row
        playMenu = new Table();
        playMenu.setFillParent(true);
        playMenu.center();
        playMenu.add(quickPlayButton).pad(10).colspan(2).expandX().center();
        playMenu.row();
        playMenu.add(codeInputField).expandX().padTop(10).padLeft(-9).width(300).right();
        playMenu.add(codeSubmitButton).expandX().padTop(10).padLeft(-14).width(125).left();
        playMenu.row();
        playMenu.add(settingsButton).padTop(60).colspan(2).expandX().center();
        playMenu.row();
        playMenu.add(exitButton).pad(20).colspan(2).expandX().center();

        stage.addActor(playMenu);
    }

    @Override
    public void resize(int width, int height) {
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
        stage.dispose();
    }
}