package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

public class GameScreen implements Screen, MessageListener {
    final EgyptianRatscrew game;
    private final float ASPECT_RATIO = 16 / 9f;
    private Texture backgroundImage;
    private ArrayList<Music> matchMusic;
    private int currentTrackIndex = 0;
    private OrthographicCamera camera;
    private String serverMessage;
    private GameWebSocketClient webSocketClient;
    private Viewport viewport;
    private Stage stage;

    private Table gameBoard;
    private TextButton playButton;
    private TextButton slapButton;
    private Match match;
    private Map<String, Texture> cardTextures;

    public GameScreen(final EgyptianRatscrew game) {
        this.game = game;
        this.match = game.getCurrentMatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800 / ASPECT_RATIO);
        viewport = new FitViewport(1600, 1600 / ASPECT_RATIO, camera);
        backgroundImage = game.assetManager.getBackgroundMatch();
        loadMatchMusic();
        cardTextures = game.assetManager.getCardTextures();

        // Add a resize listener to handle window resizing
        Gdx.graphics.setResizable(true);

        stage = new Stage(viewport, game.batch);
    }

    @Override
    public void messageReceived(String message) {
        this.serverMessage = message;
        System.out.println("Message received: " + serverMessage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

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

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
    
        game.batch.begin();
        game.batch.draw(backgroundImage, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.batch.end();
    
        if (!matchMusic.get(currentTrackIndex).isPlaying()) {
            playNextTrack();
        }
    
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
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

    private void initScreenElements() {
        gameBoard = new Table();
        gameBoard.setFillParent(true);


        TextButtonStyle buttonStyle = game.assetManager.getTextButtonStyle(2.0f);
        
        playButton = new TextButton("Play", buttonStyle);
        playButton.setPosition(800, 800 / ASPECT_RATIO - 100);

        
        slapButton = new TextButton("Slap", buttonStyle);
        slapButton.setPosition(800, 800 / ASPECT_RATIO - 150);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Play button clicked");
            }
        });
        slapButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Slap button clicked");
            }
        });
        gameBoard.add(playButton).expand().bottom().pad(20).left().pad(40);
        gameBoard.add(slapButton).expand().bottom().pad(20).right().pad(40);
        stage.addActor(gameBoard);
    }

    public void loadMatchMusic() {
        matchMusic = new ArrayList<Music>();
        for (int i = 1; i <= 3; i++) {
            matchMusic.add(game.assetManager.getMatchMusic(i));
        }
    }
    public void playNextTrack() {
        if (matchMusic.get(currentTrackIndex).isPlaying()) {
            return;
        }
        if (matchMusic.get(currentTrackIndex) != null) {
            matchMusic.get(currentTrackIndex).stop();
        }
        
        currentTrackIndex = (currentTrackIndex + 1) % matchMusic.size();
        Music nextTrack = matchMusic.get(currentTrackIndex);
        if (nextTrack != null) {
            nextTrack.setLooping(false);
            nextTrack.play();
        }
    }
}
