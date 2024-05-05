package com.mygdx.game;

import java.net.URISyntaxException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class UserMenuScreen implements Screen, MessageListener {
    final EgyptianRatscrew game;
    private final float ASPECT_RATIO = 16 / 9f;
    private Texture backgroundImage;
    private Music mainMenuMusic;
    private OrthographicCamera camera;
    private String serverMessage;
    private GameWebSocketClient webSocketClient;
    private Viewport viewport;
    private Stage stage;
    
    private TextField codeInputField, volumeField;
    private TextButton quickPlayButton, codeSubmitButton, settingsButton, exitButton,
            setVolumeButton, settingsExitButton, statsButton, statsExitButton;
    private Table playMenu, settingsMenu, statsMenu;
    String color = "#e7e5e4";
    String statsTextColor = "#000000";

    public UserMenuScreen(final EgyptianRatscrew game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800 / ASPECT_RATIO);
        viewport = new FitViewport(1600, 1600 / ASPECT_RATIO, camera);
        backgroundImage = game.assetManager.getBackgroundImage();


        mainMenuMusic = game.assetManager.getBackgroundMusic();
        mainMenuMusic.setVolume(game.getMusicVolume());
        mainMenuMusic.setLooping(true);

        Gdx.graphics.setResizable(true);


        stage = new Stage(viewport, game.batch);
    }

    @Override
    public void messageReceived(String message) {
        serverMessage = message;
        System.out.println("Message received: " + serverMessage);
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
        loadStatsMenu(textFieldStyle, textButtonStyle);
        settingsMenu.setVisible(false);
        statsMenu.setVisible(false);

        try {
            webSocketClient = new GameWebSocketClient("wss://egyptianratscrew.dev/ws", this);
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void displaySettings(boolean showSettings) {
        // toggle which menu is displayed
        if (showSettings) {
            playMenu.setVisible(false);
            settingsMenu.setVisible(true);
        }
        else {
            playMenu.setVisible(true);
            settingsMenu.setVisible(false);
        }
    }

    private void loadSettingsMenu(TextField.TextFieldStyle textFieldStyle, TextButton.TextButtonStyle textButtonStyle) {
        // volume and back buttons
        volumeField = new TextField(String.valueOf((int) (game.getMusicVolume() * 100)), textFieldStyle);
        setVolumeButton = new TextButton("Set Volume", textButtonStyle);
        settingsExitButton = new TextButton("Back", textButtonStyle);

        // event listeners
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

        // structure elements as table
        settingsMenu = new Table();
        settingsMenu.setFillParent(true);
        settingsMenu.center();
        settingsMenu.add(volumeField).expandX().padTop(10).padLeft(-9).width(150).right();
        settingsMenu.add(setVolumeButton).expandX().padTop(10).padLeft(-5).width(150).left();
        settingsMenu.row();
        settingsMenu.add(settingsExitButton).pad(20).colspan(2).expandX().center();
        stage.addActor(settingsMenu);
    }
    
    private void loadStatsMenu(TextField.TextFieldStyle textFieldStyle, TextButton.TextButtonStyle textButtonStyle) {

        Label.LabelStyle labelStyle = game.assetManager.getSmallLabelStyle(1.5f);

    	// games played, won, lost
        Label gamesPlayedLabel = new Label("Games played: ", labelStyle);
        gamesPlayedLabel.setColor(Color.valueOf(statsTextColor));
     
        Label gamesWonLabel = new Label("Games won: ", labelStyle);
        gamesWonLabel.setColor(Color.valueOf(statsTextColor));
 
        Label gamesLostLabel = new Label("Games lost: ", labelStyle);
        gamesLostLabel.setColor(Color.valueOf(statsTextColor));
    	statsExitButton = new TextButton("Back", textButtonStyle);
    	
    	statsExitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                displayStats(false);
            }
    	});
    	
    	statsMenu = new Table();
        statsMenu.setFillParent(true);
        statsMenu.center();
        statsMenu.row();
        statsMenu.add(gamesPlayedLabel).expandY().padTop(10);
        statsMenu.row();
        statsMenu.add(gamesWonLabel).expandY().padTop(10);
        statsMenu.row();
        statsMenu.add(gamesLostLabel).expandY().padTop(10);
        statsMenu.row();
        statsMenu.add(statsExitButton).pad(20).colspan(2).expandY().center();
        stage.addActor(statsMenu);
    	
    }
    
    private void displayStats(boolean showStats) {
        // toggle which menu is displayed
        if (showStats) {
            playMenu.setVisible(false);
            statsMenu.setVisible(true);
        }
        else {
            playMenu.setVisible(true);
            statsMenu.setVisible(false);
        }
    }

    private void loadPlayMenu(TextField.TextFieldStyle textFieldStyle, TextButton.TextButtonStyle textButtonStyle) {
        // buttons
        quickPlayButton = new TextButton("Quick Play", textButtonStyle);
        quickPlayButton.getLabel().setColor(Color.valueOf(color));
        statsButton = new TextButton("Player Stats", textButtonStyle);
        statsButton.getLabel().setColor(Color.valueOf(color));
        settingsButton = new TextButton("Settings", textButtonStyle);
        settingsButton.getLabel().setColor(Color.valueOf(color));
        exitButton = new TextButton("Exit", textButtonStyle);
        exitButton.getLabel().setColor(Color.valueOf(color));

        // event listeners
        quickPlayButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (webSocketClient != null) {
                    System.out.println("Closing connection");
                }
                game.setScreen(new MatchMakingScreen(game));
            }
        });
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                displaySettings(true);
            }
        });
        statsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                displayStats(true);
            }
        });
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // structure elements as table
        playMenu = new Table();
        playMenu.setFillParent(true);
        playMenu.center();
        playMenu.add(quickPlayButton).pad(10).colspan(2).expandX().center();
        playMenu.row();
        playMenu.add(codeInputField).expandX().padTop(10).padLeft(-9).width(300).right();
        playMenu.add(codeSubmitButton).expandX().padTop(10).padLeft(-14).width(125).left();
        playMenu.row();
        playMenu.add(statsButton).padTop(60).colspan(2).expandX().center();
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