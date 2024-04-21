package com.mygdx.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.Gdx;


public class RegistrationScreen implements Screen, MessageListener  {
    final EgyptianRatscrew game;
    private BitmapFont fontSmall;
    private final float ASPECT_RATIO = 16 / 9f;
    private Texture backgroundImage;
    private Music backgroundMusic;
    private OrthographicCamera camera;
    private String serverMessage;
    private GameWebSocketClient webSocketClient;

    private Stage stage;
    private TextField emailField, usernameField, passwordField;
    private Label emailLabel, usernameLabel, passwordLabel;
    private TextButton submitButton, exitButton;
    Viewport viewport;

    Color color = Color.valueOf("#e7e5e4");
    
    public void messageReceived(String message) {
    	this.serverMessage = message;
        System.out.println("Message received: " + serverMessage);
    }

    public RegistrationScreen(final EgyptianRatscrew game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(1600, 1600 / ASPECT_RATIO, camera);
        camera.setToOrtho(false, 800, 800 / ASPECT_RATIO); 

        fontSmall = game.assetManager.getFontSmall();
        backgroundImage = game.assetManager.getBackgroundImage();
        backgroundMusic = game.assetManager.getBackgroundMusic();
    
    }
    
    @Override
    public void show() {
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        stage = new Stage(new ScreenViewport()); 
        Gdx.input.setInputProcessor(stage);
    
        initFormElements();
    }

    private void initFormElements() {
        TextField.TextFieldStyle textFieldStyle = game.assetManager.getTextFieldStyle(1.0f);
        LabelStyle labelStyle = new LabelStyle(fontSmall, color);
        TextButtonStyle buttonStyle = game.assetManager.getTextButtonStyle(1.0f);
    
        emailLabel = new Label("Email", labelStyle);
        emailField = new TextField("", textFieldStyle);
        usernameLabel = new Label("Username", labelStyle);
        usernameField = new TextField("", textFieldStyle);
        passwordLabel = new Label("Password", labelStyle);
        passwordField = new TextField("", textFieldStyle);
        submitButton = new TextButton("Submit", buttonStyle);
        exitButton = new TextButton("Exit", buttonStyle);
    
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
    
        float y = viewport.getWorldHeight() / 2 + 100;
    
        emailField.pack();
        usernameField.pack();
        passwordField.pack();

        emailField.setPosition((viewport.getWorldWidth() - emailField.getPrefWidth()) / 2, y);
        y -= (emailField.getHeight() + 20);
        usernameField.setPosition((viewport.getWorldWidth() - usernameField.getPrefWidth()) / 2, y);
        y -= (usernameField.getHeight() + 20);
        passwordField.setPosition((viewport.getWorldWidth() - passwordField.getPrefWidth()) / 2, y);
        y -= (passwordField.getHeight() + 40); 
        submitButton.setPosition((viewport.getWorldWidth() - submitButton.getPrefWidth()) / 2, y);
        y -= (submitButton.getHeight() + 20);
        exitButton.setPosition((viewport.getWorldWidth() - exitButton.getPrefWidth()) / 2, y);
        
        // add all of it to the stage
        stage.addActor(emailLabel);
        stage.addActor(emailField);
        stage.addActor(usernameLabel);
        stage.addActor(usernameField);
        stage.addActor(passwordLabel);
        stage.addActor(passwordField);
        stage.addActor(submitButton);
        stage.addActor(exitButton);

        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String email = emailField.getText();
                String username = usernameField.getText();
                String password = passwordField.getText();
                System.out.println("Email: " + email + " Username: " + username + " Password: " + password);
                // webSocketClient = new GameWebSocketClient("ws://localhost:8080/registration", RegistrationScreen.this);
                // webSocketClient.connect();
                // webSocketClient.send("email=" + email + "&username=" + username + "&password=" + password);
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
        
        game.batch.begin();
        game.batch.draw(backgroundImage, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
