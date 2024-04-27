package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
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
    
    private Table gameBoard, centerPile;
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
        centerPile = new Table();
        gameBoard = new Table();
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);
        Gdx.graphics.setResizable(true);
        initScreenElements();
    }

    @Override
    public void messageReceived(String message) {
        serverMessage = message;
        System.out.println("Message received: " + serverMessage);
        Gson gson = new Gson();
        Response response = gson.fromJson(serverMessage, Response.class);
        String type = response.getType();
        if ("MATCH_UPDATE".equals(type)) {
            System.out.println("Match update received");
            JsonElement dataElement = response.getData();
            Match updatedMatch = gson.fromJson(dataElement, Match.class);
            match.setCenterPile(updatedMatch.getCenterPile());
            System.out.println("Updated center pile: " + match.getCenterPile().getCards());
            System.out.println("Updated pile count: " + match.getCenterPile().getCards().size());
            System.out.println("centerPile size: " + centerPile.getChildren().size);

        } else if ("AUTH_ERROR".equals(type)) {
            JsonElement dataElement = response.getData();
            String dataString = dataElement.getAsString();
            System.out.println(dataString);
        }
    }


    @Override
    public void show() {

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
            if (centerPile.getChildren().size != this.match.getCenterPile().getCards().size()) {
                Card lastCardPlayed = match.getCenterPile().getCards().get(match.getCenterPile().getCards().size() - 1);
                String cardKey = lastCardPlayed.getValue() + "_" + lastCardPlayed.getSuit();
                Image cardImage = new Image(cardTextures.get(cardKey));
                centerPile.add(cardImage).size(120, 200).uniform().fill();
            }
        stage.draw();
    }


    private void initScreenElements() {
        gameBoard.setFillParent(true);
        gameBoard.defaults().expand();

        TextButtonStyle buttonStyle = game.assetManager.getTextButtonStyle(2.0f);
        playButton = new TextButton("Play", buttonStyle);
        slapButton = new TextButton("Slap", buttonStyle);
        
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Play button clicked");
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

        // the center pile of cards
        // Image testCard = new Image(cardTextures.get("2_CLUBS"));
        // centerPile.add(testCard).size(120,200).uniform().fill();
        gameBoard.add(centerPile).size(120,200).uniform().fill();
        centerPile.setDebug(true);
        

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
        stage.setDebugAll(true);
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
        webSocketClient.close();
    }

}
