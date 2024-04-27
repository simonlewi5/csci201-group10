package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
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
    
    private Stack centerPile;
    private Table gameBoard;
    private TextButton playButton;
    private TextButton slapButton;
    private Match match;
    private Map<String, Texture> cardTextures;

    public GameScreen(final EgyptianRatscrew game, GameWebSocketClient webSocketClient) {
        this.game = game;
        this.webSocketClient = webSocketClient;
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
        serverMessage = message;
        // System.out.println("Message received: " + serverMessage);
        System.out.println(serverMessage);
        Gson gson = new Gson();
        Response response = gson.fromJson(serverMessage, Response.class);
        System.out.println("Response: " + response.toString());
        String type = response.getType();
        System.out.println("Type: " + type);
        if ("MATCH_UPDATE".equals(type)) {
            System.out.println("Match update received");
            JsonElement dataElement = response.getData();
            Match updatedMatch = gson.fromJson(dataElement, Match.class);
            game.setCurrentMatch(updatedMatch);
            System.out.println("Center pile: " + match.getCenterPile().getCards().toString());
            updateCenterPile();
            gameBoard.invalidateHierarchy();

        } else if ("AUTH_ERROR".equals(type)) {
            JsonElement dataElement = response.getData();
            String dataString = dataElement.getAsString();
            System.out.println(dataString);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        initScreenElements();
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
        gameBoard.defaults().expand();
        // gameBoard.setDebug(true);

        centerPile = createCenterPile();

        TextButtonStyle buttonStyle = game.assetManager.getTextButtonStyle(2.0f);
        playButton = new TextButton("Play", buttonStyle);
        slapButton = new TextButton("Slap", buttonStyle);
        
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Play button clicked");
                // String cardKey = debugRandomCard();
                // updateCenterPile(cardKey, centerPile);
                // gameBoard.invalidateHierarchy();
                String playerId = game.getPlayer1().getId();
                HashMap<String, Object> data = new HashMap<>();

                data.put("action", "play_card");
                data.put("player_id", playerId);

                String json = new Gson().toJson(data);
                webSocketClient.send(json);

            }
        });
        slapButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Slap button clicked");
            }
        });

        int numPlayers = match.getPlayers().size();
        Map <String, Hand> hands = match.getHands();

        // int numPlayers = 2;

        // ArrayList<String> debugPlayerNames = new ArrayList<String>();
        // debugPlayerNames.add("Player 1");
        // debugPlayerNames.add("Player 2");
        // debugPlayerNames.add("Player 3");
        // debugPlayerNames.add("Player 4");

        // ArrayList<Integer> debugCardsRemaining = new ArrayList<Integer>();
        // debugCardsRemaining.add(7);
        // debugCardsRemaining.add(11);
        // debugCardsRemaining.add(12);
        // debugCardsRemaining.add(4);


        Image cardBackImage1 = new Image(cardTextures.get("card_back"));
        Image cardBackImage2 = new Image(cardTextures.get("card_back"));
        Image cardBackImage3 = new Image(cardTextures.get("card_back"));
        Image cardBackImage4 = new Image(cardTextures.get("card_back"));

        String player1Username = match.getPlayers().get(0).getUsername();
        int player1CardsRemaining = hands.get(player1Username).getCards().size();
        String player2Username = match.getPlayers().get(1).getUsername();
        int player2CardsRemaining = hands.get(player2Username).getCards().size();
        String player3Username = "";
        int player3CardsRemaining = 0;
        String player4Username = "";
        int player4CardsRemaining = 0;
        if (numPlayers >= 3) {
            player3Username = match.getPlayers().get(2).getUsername();
            player3CardsRemaining = hands.get(player3Username).getCards().size();
        }
        if (numPlayers >= 4) {
            player4Username = match.getPlayers().get(3).getUsername();
            player4CardsRemaining = hands.get(player4Username).getCards().size();
        }

        //top row
        gameBoard.add().uniform().fill();
        if (numPlayers >= 3) {
            Table playerTable3 = createPlayerTable(player3Username, player3CardsRemaining, 3, numPlayers, cardBackImage3);
            gameBoard.add(playerTable3).uniform().fill();
        } else {
            Table playerTable2 = createPlayerTable(player2Username, player2CardsRemaining, 2, numPlayers, cardBackImage2);
            gameBoard.add(playerTable2).uniform().fill();
        }
        gameBoard.add().uniform().fill();
        gameBoard.row();
        
        // middle row - only show card backs if there are 3 or 4 players
        if (numPlayers >= 3) {
            Table playerTable2 = createPlayerTable(player2Username, player2CardsRemaining, 2, numPlayers, cardBackImage2);
            gameBoard.add(playerTable2).uniform().fill();
        } else {
            gameBoard.add().uniform().fill();
        }
        gameBoard.add(centerPile).size(120,200).uniform().fill();

        if (numPlayers >= 4) {
            Table playerTable4 = createPlayerTable(player4Username, player4CardsRemaining, 4, numPlayers, cardBackImage4);
            gameBoard.add(playerTable4).uniform().fill();
        } else {
            gameBoard.add().uniform().fill();
        }
        gameBoard.row();
        
        // bottom row
        gameBoard.add(playButton).uniform();
        Table playerTable1 = createPlayerTable(player1Username, player1CardsRemaining, 1, numPlayers, cardBackImage1);
        gameBoard.add(playerTable1).uniform().fill();
        gameBoard.add(slapButton).uniform();
        gameBoard.row();

        stage.addActor(gameBoard);
    }

    // playerNumber is the index of the player in the match.getPlayers() list, except you start with 
    // player1 (the person playing locally) as the starting point and cycle through the list, so it's offset
    // numPlayers is the total number of players in the match
    private Table createPlayerTable(String username, int cardsRemaining, int playerNumber, int numPlayers, Image cardBackImage) {
        Table playerTable = new Table();
        // playerTable.setDebug(true);
        playerTable.defaults().expand();
        Label.LabelStyle playerLabelStyle = game.assetManager.getLargeLabelStyle(1.5f);
        Label.LabelStyle cardLabelStyle = game.assetManager.getLabelStyle(1.0f);
        playerTable.add(new Label(username, playerLabelStyle));
        playerTable.row();
        playerTable.add(new Label("Cards remaining: " + cardsRemaining, cardLabelStyle));
        playerTable.row();
        if (numPlayers >=3) {
            if (playerNumber == 2 || playerNumber == 4) {
                cardBackImage.setOrigin(Align.center);
                playerTable.add(cardBackImage).size(200, 120).uniform().fill();
            }
            else
                playerTable.add(cardBackImage).size(120, 200).uniform().fill();    
        } else {
            playerTable.add(cardBackImage).size(120, 200).uniform().fill();
        }
        playerTable.row();
        return playerTable;
    }

    private Stack createCenterPile() {
        Stack centerPile = new Stack();
        // centerPile.setDebug(true);
        return centerPile;
    }

    private void updateCenterPile() {
        ArrayList<Card> centerPileCards = match.getCenterPile().getCards();
        if (centerPileCards.size() == 0) {
            centerPile.clear();
            return;
        }
        Card lastCard = centerPileCards.get(centerPileCards.size() - 1);
        String cardKey = lastCard.getValue() + "_" + lastCard.getSuit();
        Image cardImage = new Image(cardTextures.get(cardKey));
        cardImage.setSize(120, 200);
        centerPile.add(cardImage);
    }

    private String debugRandomCard() {
        String[] suits = {"hearts", "diamonds", "clubs", "spades"};
        String[] values = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"};
        int suitIndex = (int) (Math.random() * 4);
        int valueIndex = (int) (Math.random() * 13);
        return values[valueIndex] + "_" + suits[suitIndex].toUpperCase();
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
