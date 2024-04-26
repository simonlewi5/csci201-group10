package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class GameScreen implements Screen, MessageListener {
    final EgyptianRatscrew game;
    private final float ASPECT_RATIO = 16 / 9f;
    private Texture backgroundImage;
    private Music backgroundMusic;
    private OrthographicCamera camera;
    private String serverMessage;
    private GameWebSocketClient webSocketClient;
    private Viewport viewport;
    private Stage stage;

    private TextButton playButton;
    private TextButton slapButton;
    private Label gameLabel;
    private Deck deck;
    private Match match;
    private ArrayList<Card> player1Hand;
    private ArrayList<Card> player2Hand;
    private ArrayList<Card> player3Hand;
    private ArrayList<Card> player4Hand;
    private ArrayList<Card> centerPile;
    private int playerTurn;
    // private Boolean mustFace = false;
    // private int turns = 0;
    // private int numPlayers;
    // private List<String> players;

    public GameScreen(final EgyptianRatscrew game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(1600, 1600 / ASPECT_RATIO, camera);
        camera.setToOrtho(false, 800, 800 / ASPECT_RATIO);

        backgroundImage = game.assetManager.getBackgroundImage();
        backgroundMusic = game.assetManager.getBackgroundMusic();

        backgroundMusic.setLooping(true);

        // Add a resize listener to handle window resizing
        Gdx.graphics.setResizable(true);

        stage = new Stage(viewport, game.batch);

        // initialize hand using server info

        // numPlayers should be gotten from server

        // initialize players list using server info

        // numCards for each player = 52 / numPlayers

        // if numplayers = 3, add extra card to caenterPile (handled from server side I guess)
    }

    @Override
    public void messageReceived(String message) {
        this.serverMessage = message;
        System.out.println("Message received: " + serverMessage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        // fontLarge = game.assetManager.getFontLarge();

        backgroundImage = game.assetManager.getBackgroundImage();
        backgroundMusic = game.assetManager.getBackgroundMusic();

        backgroundMusic.setVolume(game.getMusicVolume());
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        initScreenElements();
        try {
            webSocketClient = new GameWebSocketClient("wss://egyptianratscrew.dev/ws", this);
            webSocketClient.connectBlocking();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initScreenElements() {

        Label.LabelStyle labelStyle = game.assetManager.getLabelStyle(2.0f);
        gameLabel = new Label("Game screen", labelStyle);
        gameLabel.setColor(Color.valueOf("#0f172a"));

        gameLabel.setPosition(viewport.getWorldWidth() / 2 - gameLabel.getWidth() / 2,
                viewport.getWorldHeight() / 2 - gameLabel.getHeight() / 2);

        // add all of it to the stage
        stage.addActor(gameLabel);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Draw background image
        game.batch.draw(backgroundImage, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
