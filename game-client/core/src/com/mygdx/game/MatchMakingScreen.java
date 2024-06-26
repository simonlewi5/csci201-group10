package com.mygdx.game;

import java.net.URISyntaxException;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class MatchMakingScreen implements Screen, MessageListener {
    final EgyptianRatscrew game;
    private final float ASPECT_RATIO = 16 / 9f;
    private Texture backgroundImage;
    private Music backgroundMusic;
    private OrthographicCamera camera;
    private String serverMessage;
    private GameWebSocketClient webSocketClient;
    private Viewport viewport;
    private Stage stage;
    
    private Label searchingLabel;
    private float ellipsisTimer = 0;
    private final float ellipsisInterval = 0.5f;
    private int ellipsisCount = 0;



    public MatchMakingScreen(final EgyptianRatscrew game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800 / ASPECT_RATIO);
        viewport = new FitViewport(1600, 1600 / ASPECT_RATIO, camera);
        backgroundImage = game.assetManager.getBackgroundImage();
        backgroundMusic = game.assetManager.getBackgroundMusic();
        stage = new Stage(viewport, game.batch);

        Gdx.graphics.setResizable(true);
    }

    @Override
    public void messageReceived(String message) {
        serverMessage = message;
        System.out.println("Message received: " + serverMessage);
        Gson gson = new Gson();
        Response response = gson.fromJson(serverMessage, Response.class);
        String type = response.getType();
        if (type.equals("QUEUE_UPDATE")) {
            JsonElement dataElement = response.getData();
            String dataString = dataElement.getAsString();
            System.out.println(dataString);
        } else if (type.equals("MATCH_FOUND")) {
            try {
                game.setCurrentMatch(gson.fromJson(response.getData(), Match.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
            fadeOut();
        }
    }
    
    @Override
    public void show() {
        backgroundMusic.setVolume(game.getMusicVolume());
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        Gdx.input.setInputProcessor(stage);

        initScreenElements();
        try {
            webSocketClient = new GameWebSocketClient("wss://egyptianratscrew.dev/ws", this);
            webSocketClient.connectBlocking();
            searchForMatch();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    private void initScreenElements() {

        Label.LabelStyle labelStyle = game.assetManager.getLabelStyle(2.0f);
        searchingLabel = new Label("Searching for a match", labelStyle);
        searchingLabel.setColor(Color.valueOf("#0f172a"));

        searchingLabel.setPosition(viewport.getWorldWidth() / 2 - searchingLabel.getWidth() / 2,
                viewport.getWorldHeight() / 2 - searchingLabel.getHeight() / 2);

        // add all of it to the stage
        stage.addActor(searchingLabel);
    }

    private void searchForMatch() {
        String playerId = game.player.getId();
        HashMap<String, Object> data = new HashMap<>();

        data.put("action", "search_for_match");
        data.put("player_id", playerId);

        String json = new Gson().toJson(data);
        webSocketClient.send(json);
    }

    private void fadeOut() {
        final float fadeOutDuration = 1f;
        final float fadeOutStep = 0.01f;
        final float initialVolume = backgroundMusic.getVolume();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                float volume = backgroundMusic.getVolume();
                volume -= fadeOutStep;
                if (volume <= 0) {
                    volume = 0;
                    backgroundMusic.stop();
                    this.cancel(); 
                }
                backgroundMusic.setVolume(volume);
            }
        }, 0, fadeOutDuration / (initialVolume / fadeOutStep));


        stage.addAction(Actions.sequence(
            Actions.fadeOut(fadeOutDuration),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    GameScreen gameScreen = new GameScreen(game, webSocketClient);
                    webSocketClient.setListener(gameScreen);
                    game.setScreen(new GameScreen(game, webSocketClient));
                }
            })
        ));
    }



    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        ellipsisTimer += delta;
        if (ellipsisTimer >= ellipsisInterval) {
            ellipsisTimer = 0;
            ellipsisCount = (ellipsisCount + 1) % 4;
            StringBuilder sb = new StringBuilder("Searching for a match");
            for (int i = 0; i < ellipsisCount; i++) {
                sb.append('.');
            }
            searchingLabel.setText(sb.toString());
        }

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.setColor(1, 1, 1, stage.getRoot().getColor().a);
        game.batch.begin();
        game.batch.draw(backgroundImage, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.batch.end();
        stage.act(delta);
        stage.draw();
        game.batch.setColor(1, 1, 1, 1);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
        stage.dispose();
    }

}
