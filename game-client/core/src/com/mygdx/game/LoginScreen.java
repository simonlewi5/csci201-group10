package com.mygdx.game;

import java.net.URISyntaxException;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class LoginScreen implements Screen, MessageListener {
    final EgyptianRatscrew game;
    private final float ASPECT_RATIO = 16 / 9f;
    private Texture backgroundImage;
    private Music backgroundMusic;
    private OrthographicCamera camera;
    private String serverMessage;
    private GameWebSocketClient webSocketClient;
    private Viewport viewport;
    private Label errorMessageLabel;
    private Stage stage;
    private TextField  usernameField, passwordField;
    private TextButton submitButton, exitButton;

    public LoginScreen(final EgyptianRatscrew game) {
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
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                serverMessage = message;
                System.out.println("Message received: " + serverMessage);
                Gson gson = new Gson();
                Response response = gson.fromJson(serverMessage, Response.class);
                String type = response.getType();
    
                if (type.equals("AUTH_SUCCESS")) {
                    JsonObject dataObject = response.getData().getAsJsonObject();
                    JsonObject playerJson = dataObject.getAsJsonObject("player");
                    Player player = gson.fromJson(playerJson, Player.class);
    
                    game.player = player;
                    System.out.println("Login successful for player: " + player.getUsername());
                    game.setScreen(new UserMenuScreen(game, player));
                } else if (type.equals("AUTH_ERROR")) {
                    System.out.println("Authentication failed");
                    int startIndex = serverMessage.indexOf("\"data\":\"") + "\"data\":\"".length();
                    int endIndex = serverMessage.indexOf("\"", startIndex);
                    String dataValue = serverMessage.substring(startIndex, endIndex);
                    showErrorMessage(dataValue);
                }
            }
        });
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
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initScreenElements() {
        TextField.TextFieldStyle textFieldStyle = game.assetManager.getTextFieldStyle(1.0f);
        TextButtonStyle buttonStyle = game.assetManager.getTextButtonStyle(1.0f);
        Label.LabelStyle labelStyle = game.assetManager.getLabelStyle(1.0f, "FF0000");

        // set up error message display
        errorMessageLabel = new Label("", labelStyle);
        errorMessageLabel.setSize(300, 100);
        errorMessageLabel.setPosition(600, 600);

        usernameField = new TextField("", textFieldStyle);
        usernameField.setMessageText("Username");
        passwordField = new TextField("", textFieldStyle);
        passwordField.setMessageText("Password");
        submitButton = new TextButton("Submit", buttonStyle);
        exitButton = new TextButton("Exit", buttonStyle);

        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');

        float fieldWidth = 400;
        float y = viewport.getWorldHeight() / 2 + 100;

        usernameField.pack();
        passwordField.pack();

        usernameField.setSize(fieldWidth, usernameField.getHeight());
        usernameField.setPosition((viewport.getWorldWidth() - fieldWidth) / 2, y);
        y -= (usernameField.getHeight() + 20);

        passwordField.setSize(fieldWidth, passwordField.getHeight());
        passwordField.setPosition((viewport.getWorldWidth() - fieldWidth) / 2, y);
        y -= (passwordField.getHeight() + 20);

        submitButton.setPosition((viewport.getWorldWidth() - submitButton.getPrefWidth()) / 2, y);
        y -= (submitButton.getHeight() + 20);
        exitButton.setPosition((viewport.getWorldWidth() - exitButton.getPrefWidth()) / 2, y);

        // add all of it to the stage
        stage.addActor(usernameField);
        stage.addActor(passwordField);
        stage.addActor(submitButton);
        stage.addActor(exitButton);
        stage.addActor(errorMessageLabel);

        submitButton.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               if (webSocketClient != null && webSocketClient.isOpen()) {
                   String username = usernameField.getText();
                   String password = passwordField.getText();
                   HashMap<String, Object> data = new HashMap<>();

                   data.put("action", "login");
                   data.put("username", username);
                   data.put("password", password);

                   String json = new Gson().toJson(data);
                   webSocketClient.send(json);
                   usernameField.setText("");
                   passwordField.setText("");
               } else {
                   System.out.println("WebSocket is not open");
               }
           }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });


    }

    public void showErrorMessage(String text){
        errorMessageLabel.setText(text);
        errorMessageLabel.setVisible(true);

        float fadeInDuration = 0.25f;
        float visibleDuration = 1f;
        float fadeOutDuration = 0.5f;

        errorMessageLabel.addAction(Actions.sequence(
                Actions.fadeIn(fadeInDuration),
                Actions.delay(visibleDuration),
                Actions.fadeOut(fadeOutDuration),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        errorMessageLabel.setVisible(false);
                    }
                })));
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
    	webSocketClient.close();
        stage.dispose();
    }

}
