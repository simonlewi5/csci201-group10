package com.mygdx.game;

import java.net.URISyntaxException;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.Gson;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
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
    
    private Stage stage;
    private TextField  usernameField, passwordField;
    private TextButton submitButton, exitButton;
    Viewport viewport;

    
    public void messageReceived(String message) {
    	this.serverMessage = message;
        System.out.println("Message received: " + serverMessage);
    }

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
    public void show() {
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        Gdx.input.setInputProcessor(stage);

        initFormElements();

        try {
            webSocketClient = new GameWebSocketClient("wss://egyptianratscrew.dev/ws", this);
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initFormElements() {
        TextField.TextFieldStyle textFieldStyle = game.assetManager.getTextFieldStyle(1.0f);
        TextButtonStyle buttonStyle = game.assetManager.getTextButtonStyle(1.0f);

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


    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();
        game.batch.draw(backgroundImage, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

//        GlyphLayout layout = new GlyphLayout(fontLarge, "LOGIN SCREEN");
//        float x = (viewport.getWorldWidth() - layout.width) / 2;
//        float y = (viewport.getWorldHeight() - layout.height) / 2;
//        fontLarge.setColor(Color.WHITE);
//        fontLarge.draw(game.batch, layout, x, y);

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
