package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import java.util.List;
import java.util.ArrayList;
import com.badlogic.gdx.utils.Timer;

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


    private List<String> slapHistory;
    private List<String> centerPile;
    private String bottomCard;
    private List<String> hand;

    private Boolean mustFace = false;
    private int turns = 0;

    private int numPlayers;
    private List<String> players;



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

        slapHistory = new ArrayList<String>();

        centerPile = new ArrayList<String>();

        bottomCard = "";

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

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Draw background image
        game.batch.draw(backgroundImage, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Draw game elements
        drawGameElements();

        game.batch.end();

        // Something like this...
//        if (currentPlayer() == this.username) {
//            duringTurn();
//        } else {
//            offTurn();
//        }
    }

    private void duringTurn() {
        // Draw buttons
        TextButton.TextButtonStyle textButtonStyle = game.assetManager.getTextButtonStyle(1.0f);

        playButton = new TextButton("Play", textButtonStyle);
        slapButton = new TextButton("Slap", textButtonStyle);

        playButton.getLabel().setAlignment(Align.center);
        slapButton.getLabel().setAlignment(Align.center);

        float playButtonWidth = 50; // fix later
        float slapButtonWidth = 50; // fix later

        playButton.setPosition(50,50); // fix later
        slapButton.setPosition(50,50); // fix later

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playCard();
            }
        });

        slapButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                slap();
            }
        });

//        if ( ""message received"" == "center grabbed") {
//            mustFace = false;
//            turns = 0;
//            nextTurn();
//            centerPile.clear(); // smth like this
//        }

    }

    private void offTurn() {
        // Draw buttons
        TextButton.TextButtonStyle textButtonStyle = game.assetManager.getTextButtonStyle(1.0f);

        slapButton = new TextButton("Slap", textButtonStyle);

        slapButton.getLabel().setAlignment(Align.center);

        float slapButtonWidth = 50; // do later

        slapButton.setPosition(50,50); // fix later

        slapButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                slap();
            }
        });

//        if ( ""message received"" == "center grabbed") {
//            centerPile.clear(); // smth like this
//        }
    }

    private void drawGameElements() {
        // Draw game table
        game.batch.draw(game.assetManager.getTableImage(), 200, (viewport.getWorldHeight() - 600) / 2, 600, 600);

        // Draw slap history column
        BitmapFont fontMedium = game.assetManager.getFontMedium();
        float historyX = 600;
        float historyY = 450;
        float lineHeight = 20;
        int historySize = slapHistory.size();
        int numToDisplay = Math.min(historySize, 5); // Display up to the top 5 items
        for (int i = 0; i < numToDisplay; i++) {
            fontMedium.draw(game.batch, slapHistory.get(historySize - i - 1), historyX, historyY - i * lineHeight);
        }

        // Draw players based on numPlayers and player turn list
        // render turns if they are greater than 1
        // render arrow showing whose turn it is

        // Draw cards stuff
    }

    private void playCard() {
        // something like this:
        String topCard = hand.get(hand.size() - 1);
        hand.remove(hand.size() - 1);

        centerPile.add(topCard);
        turns--;

        // Send new centerPile to all players
        // Send new hand number to all players

//        if (mustFace) {
//            if (topCard is a faceCard) {
//                mustFace = false;
//                turns = 0;
//                nextTurn();
//            }
//
//            else if (turns == 0) {
//                mustFace = false;
//                // tell previous to grab center
//            }
//        }

//        else {
//            if (turns == 0) {
//                nextTurn();
//            }
//        }
    }

    private void stashCard() {
        // something like this:
        String topCard = hand.get(hand.size() - 1);
        hand.remove(hand.size() - 1);

        centerPile.add(0, topCard);
        bottomCard = topCard;

        // Send new centerPile and bottomCard to all players
        // Send new hand number to all players
    }

    private void grabCanter() {
        hand.addAll(centerPile);

        // something like this:
        centerPile.clear();

        // Send new centerPile and bottomCard to all players
        // Send new hand number to all players
    }

    private void slap(){
        if (isValidSlap()){
            // send message to server to see who got there first
            // if this player got there first, call grabCenter
            turns = 0;
        }
        else {
            stashCard();
        }
    }

    // finish later
    private Boolean isValidSlap() {
        // add different possible combos of first three cards
        return false;
    }

    private void nextTurn() {
        // get username of next player in the list with hand.size() > 0
        // if last card played was a face card and not complete set
                // assign apropriate turns
        // else assign 1 turn

        Timer.schedule(new Timer.Task(){
            @Override
            public void run() {
                // send message to update current turn
            }
        }, 1);
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
