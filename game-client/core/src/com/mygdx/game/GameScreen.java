package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen implements Screen, MessageListener {
    final EgyptianRatscrew game;
    private final float ASPECT_RATIO = 16 / 9f;
    private final float CARD_SIZE_X = 154f, CARD_SIZE_Y = 212f; // size of custom pixel art assets

    private Texture backgroundImage;
    private ArrayList<Music> matchMusic;
    private int currentTrackIndex = 0;
    private OrthographicCamera camera;
    private String serverMessage;
    private GameWebSocketClient webSocketClient;
    private Viewport viewport;
    private Stage stage;
    private Table gameBoard, playerTop, playerLeft, playerRight, playerBottom;
    private TextButton playButton;
    private TextButton slapButton;
    private Match match;
    private Map<String, Texture> cardTextures;
    private Map<String, Label> cardCountLabels = new HashMap<>(); // map username to cardsRemaining labels
    private int playerIndex;
    private String color = "#e7e5e4";
    private String currentTurnColor = "#fde047";

    public GameScreen(final EgyptianRatscrew game, GameWebSocketClient webSocketClient) {
        this.game = game;
        this.webSocketClient = webSocketClient;
        this.match = game.getCurrentMatch();

        // set up render/bg stuff
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 800 / ASPECT_RATIO);
        viewport = new FitViewport(1600, 1600 / ASPECT_RATIO, camera);
        backgroundImage = game.assetManager.getBackgroundMatch();
        loadMatchMusic();

        // set up game board
        cardTextures = game.assetManager.getCardTextures();
        gameBoard = new Table();
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);
        Gdx.graphics.setResizable(true);
        initScreenElements();
    }

    @Override
    public void messageReceived(String message) {
        serverMessage = message;
        // System.out.println("Message received: " + serverMessage);
        Gson gson = new Gson();
        Response response = gson.fromJson(serverMessage, Response.class);
        String type = response.getType();
        if ("MATCH_UPDATE".equals(type)) {
            System.out.println("Match update received");
            JsonElement dataElement = response.getData();
            updateMatch(gson.fromJson(dataElement, Match.class));
        } else if ("AUTH_ERROR".equals(type)) {
            JsonElement dataElement = response.getData();
            String dataString = dataElement.getAsString();
            System.out.println(dataString);
        } else if ("SLAP_FAILURE".equals(type)) {
            // TODO: display some sort of graphic by the failure of a user, same for slap success
            JsonElement dataElement = response.getData();
            String dataString = dataElement.getAsString();
            System.out.println(dataString + " FAILED");
        } else if ("SLAP_SUCCESS".equals(type)) {
            JsonElement dataElement = response.getData();
            String dataString = dataElement.getAsString();
            System.out.println(dataString + " SUCCEEDED");
        } else if ("MATCH_END".equals(type)) {
            System.out.println("Match end received");
            JsonElement dataElement = response.getData();
            updateMatch(gson.fromJson(dataElement, Match.class));
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    game.setScreen(new EndScreen(game, webSocketClient));
                }
            });
        }
    }

    private void updateMatch(Match m) {
        updateCenterPile(m.getCenterPile());
        match.setHands(m.getHands());
        match.setTurnIndex(m.getTurnIndex());

//        System.out.println("Updated center pile: " + match.getCenterPile().getCards());
//        System.out.println("Updated pile count: " + match.getCenterPile().getCards().size());
//        System.out.print("Updated hand count: ");
//        for (Player p : match.getPlayers()) {
//            System.out.print(p.getUsername() + "-" + match.getHands().get(p.getUsername()).getCards().size() + " ");
//        }
//        System.out.println();
    }

    private void updateHandCountLabels() {
        // update the labels to reflect new hand counts
        for (String username : match.getHands().keySet()) {
            Label l = cardCountLabels.get(username);
            l.setText("Cards remaining: " + match.getHands().get(username).getCards().size());
        }
    }

    private void updateTurnIndicator() {
        // update the turn indicator to reflect the current player's turn
        int turnIndex = match.getTurnIndex();
        if (turnIndex == playerIndex) {
            playButton.setDisabled(false);
        } else {
            playButton.setDisabled(true);
        }
        for (int i = 0; i < match.getPlayers().size(); i++) {
            if (i == turnIndex) 
                // highlight the current player's turn
                cardCountLabels.get(match.getPlayers().get(i).getUsername()).setColor(Color.valueOf(currentTurnColor));
            else 
                cardCountLabels.get(match.getPlayers().get(i).getUsername()).setColor(Color.valueOf(color));
        }
    }

    private void updateCenterPile(CenterPile updatedPile) {
        // if updated size bigger, cards were added to existing pile -> set random rotation only for new cards
        // if updated size smaller, the pile was reset -> set all cards
        // TODO: implement a resetCenterPile() for when slaps succeed (not in this func)
        CenterPile centerPile = match.getCenterPile();

        int i = 0;
        if (centerPile.getCards().size() < updatedPile.getCards().size()) {
            i = centerPile.getCards().size();
        } else {
            resetCenterPile();
        }
        
        while (i < updatedPile.getCards().size()) {
            Card c = updatedPile.getCards().get(i);
            c.rotateRandomly();
            c.offsetRandomly();
            centerPile.addCard(updatedPile.getCards().get(i));
            i++;
        }
    
        match.setCenterPile(centerPile);
    }

    private void resetCenterPile() {
        CenterPile centerPile = match.getCenterPile();
        centerPile.clear();
        match.setCenterPile(centerPile);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        // draw bg
        game.batch.begin();
        game.batch.draw(backgroundImage, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.batch.end();

        // check for music update
        if (!matchMusic.get(currentTrackIndex).isPlaying()) {
            playNextTrack();
        }

        // update stage actors
        updateHandCountLabels();
        updateTurnIndicator();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        // draw center pile separately on top of stage and stage.draw()
        float x = Gdx.graphics.getWidth() / 2 - CARD_SIZE_X / 2;
        float y = Gdx.graphics.getHeight() / 2 - CARD_SIZE_Y / 2;
        game.batch.begin();
        List<Card> cardsToRender = new ArrayList<>(match.getCenterPile().getCards());
        for (Card card : cardsToRender) {
            String key = card.getValue() + "_" + card.getSuit();

            // parameters: Texture texture, float x, float y,
            //             float originX, float originY, float width, float height,
            //             float scaleX, float scaleY, float rotation,
            //             int srcX, int srcY, int srcWidth, int srcHeight,
            //             boolean flipX, boolean flipY
            game.batch.draw(game.assetManager.getCardTextures().get(key), x + card.getX(), y + card.getY(),
                            CARD_SIZE_X / 2, CARD_SIZE_Y / 2, CARD_SIZE_X, CARD_SIZE_Y,
                            1, 1, card.getRotation(),
                            0, 0, 500, 726, // later, change srcWidth/Height to match pixel asset dimensions
                            false, false);
        }
        game.batch.end();
    }

    private void initScreenElements() {
        gameBoard.setFillParent(true);
        gameBoard.defaults().expand();

        // play/slap buttons
        TextButtonStyle buttonStyle = game.assetManager.getTextButtonStyle(2.0f);
        playButton = new TextButton("Play", buttonStyle);
        slapButton = new TextButton("Slap", buttonStyle);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String playerId = game.getPlayer().getId();
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
                String playerId = game.getPlayer().getId();
                HashMap<String, Object> data = new HashMap<>();

                data.put("action", "slap");
                data.put("player_id", playerId);

                String json = new Gson().toJson(data);
                webSocketClient.send(json);
            }
        });

        Map<String, Hand> hands = match.getHands();
        ArrayList<Player> players = match.getPlayers();

        // determine what index the user is in the players list
        int numPlayers = players.size();
        for (int i = 0; i < numPlayers; i++) {
            if (players.get(i).getUsername().equals(game.player.getUsername())) {
                playerIndex = i;
                break;
            }
        }

        // some convoluted logic for finding the index of who to display on the top/left/right
        String playerTopUsername = "", playerLeftUsername = "", playerRightUsername = "";
        if (numPlayers == 2) {
            playerTopUsername = (playerIndex == 0) ? players.get(1).getUsername() : players.get(0).getUsername();
        }
        else if (numPlayers == 3) {
            playerTopUsername = (playerIndex == 0) ? players.get(2).getUsername() : players.get(playerIndex - 1).getUsername();
            playerLeftUsername = (playerIndex == players.size() - 1) ? players.get(0).getUsername() : players.get(playerIndex + 1).getUsername();
        }
        else {
            playerTopUsername = (playerIndex <= 1) ? players.get(playerIndex + 2).getUsername() : players.get(playerIndex - 2).getUsername();
            playerLeftUsername = (playerIndex == players.size() - 1) ? players.get(0).getUsername() : players.get(playerIndex + 1).getUsername();
            playerRightUsername = (playerIndex == 0) ? players.get(players.size() - 1).getUsername() : players.get(playerIndex - 1).getUsername();
        }

        // create top row
        playerTop = createPlayerTable(playerTopUsername, hands.get(playerTopUsername).getCards().size(), 3);
        gameBoard.add().uniform().fill();
        gameBoard.add(playerTop).uniform().fill();
        gameBoard.add().uniform().fill();
        gameBoard.row();

        // create middle row
        if (numPlayers >= 3) {
            // left player to left col, empty middle col
            playerLeft = createPlayerTable(playerLeftUsername, hands.get(playerLeftUsername).getCards().size(), 2);
            gameBoard.add(playerLeft).uniform().fill();
            gameBoard.add().uniform().fill();

            // right player to right col
            if (numPlayers == 4) {
                playerRight = createPlayerTable(playerRightUsername, hands.get(playerRightUsername).getCards().size(), 4);
                gameBoard.add(playerRight).uniform().fill();
            }
            else gameBoard.add().uniform().fill();

            gameBoard.row();
        }
        else {
            gameBoard.add().uniform().fill();
            gameBoard.add().uniform().fill();
            gameBoard.add().uniform().fill();
            gameBoard.row();
        }

        // create bottom row
        playerBottom = createPlayerTable(game.player.getUsername(), hands.get(game.player.getUsername()).getCards().size(), 1);
        gameBoard.add(playButton).uniform().right();
        gameBoard.add(playerBottom).uniform().fill();
        gameBoard.add(slapButton).uniform().left();
        gameBoard.row();

        stage.addActor(gameBoard);
        // stage.setDebugAll(true);
    }

    private Table createPlayerTable(String username, int cardsRemaining, int playerPosition) {
        Image cardBackImage = new Image(cardTextures.get("card_back"));
        Table playerTable = new Table();
        playerTable.defaults().expand();
        Label.LabelStyle playerLabelStyle = game.assetManager.getLargeLabelStyle(1.5f);
        Label.LabelStyle cardLabelStyle = game.assetManager.getLabelStyle(1.0f);

        Label usernameLabel = new Label(username, playerLabelStyle);
        Label remaining = new Label("Cards remaining: " + cardsRemaining, cardLabelStyle);
        cardCountLabels.put(username, remaining);

        // player positions look like this:
        //    3
        // 2     4
        //    1
        if (playerPosition >= 2) {
            // text above cards
            playerTable.add(usernameLabel);
            playerTable.row();
            playerTable.add(remaining);
            playerTable.row();

            // positions 2 and 4 have horizontal card backs
            // TODO: fix card back rotation this shit stretched
            if (playerPosition == 2 || playerPosition == 4) {
                cardBackImage.setOrigin(CARD_SIZE_X / 2, CARD_SIZE_Y / 2);
                if (playerPosition == 2) cardBackImage.setRotation(-90);
                else cardBackImage.setRotation(90);
                cardBackImage.setSize(CARD_SIZE_Y, CARD_SIZE_X);

                playerTable.add(cardBackImage).size(CARD_SIZE_Y, CARD_SIZE_X).uniform().fill();
            }
            else {
                playerTable.add(cardBackImage).size(CARD_SIZE_X, CARD_SIZE_Y).uniform().fill();
            }
            playerTable.row();
        }
        else {
            // text below cards
            playerTable.add(cardBackImage).size(CARD_SIZE_X, CARD_SIZE_Y).uniform().fill();
            playerTable.row();
            playerTable.add(usernameLabel);
            playerTable.row();
            playerTable.add(remaining);
            playerTable.row();
        }

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
            nextTrack.setVolume(game.getMusicVolume());
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
