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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.Gdx;


public class RegistrationScreen implements Screen, MessageListener  {
    final EgyptianRatscrew game;
    private final float ASPECT_RATIO = 16 / 9f;
    private Texture backgroundImage;
    private Music backgroundMusic;
    private OrthographicCamera camera;
    private String serverMessage;
    private GameWebSocketClient webSocketClient;

    private Stage stage;
    private TextField emailField, usernameField, passwordField, confirmPasswordField;
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
        TextButtonStyle buttonStyle = game.assetManager.getTextButtonStyle(1.0f);
    
        emailField = new TextField("", textFieldStyle);
        emailField.setMessageText("Email Address");
        usernameField = new TextField("", textFieldStyle);
        usernameField.setMessageText("Username");
        passwordField = new TextField("", textFieldStyle);
        passwordField.setMessageText("Password");
        confirmPasswordField = new TextField("", textFieldStyle);
        confirmPasswordField.setMessageText("Confirm Password");
        submitButton = new TextButton("Submit", buttonStyle);
        exitButton = new TextButton("Exit", buttonStyle);
    
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
    
        float fieldWidth = 400;
        float y = viewport.getWorldHeight() / 2 + 100;
    
        emailField.pack();
        usernameField.pack();
        passwordField.pack();

        emailField.setSize(fieldWidth, emailField.getHeight());
        emailField.setPosition((viewport.getWorldWidth() - fieldWidth) / 2, y);
        y -= (emailField.getHeight() + 20);
    
        usernameField.setSize(fieldWidth, usernameField.getHeight());
        usernameField.setPosition((viewport.getWorldWidth() - fieldWidth) / 2, y);
        y -= (usernameField.getHeight() + 20);

        passwordField.setSize(fieldWidth, passwordField.getHeight());
        passwordField.setPosition((viewport.getWorldWidth() - fieldWidth) / 2, y);
        y -= (passwordField.getHeight() + 20);

        confirmPasswordField.setSize(fieldWidth, confirmPasswordField.getHeight());
        confirmPasswordField.setPosition((viewport.getWorldWidth() - fieldWidth) / 2, y);
        y -= (confirmPasswordField.getHeight() + 20);

        submitButton.setPosition((viewport.getWorldWidth() - submitButton.getPrefWidth()) / 2, y);
        y -= (submitButton.getHeight() + 20);
        exitButton.setPosition((viewport.getWorldWidth() - exitButton.getPrefWidth()) / 2, y);
        
        // add all of it to the stage
        stage.addActor(emailField);
        stage.addActor(usernameField);
        stage.addActor(passwordField);
        stage.addActor(confirmPasswordField);
        stage.addActor(submitButton);
        stage.addActor(exitButton);

        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String email = emailField.getText();
                String username = usernameField.getText();
                String password = passwordField.getText();
                String confirmPassword = confirmPasswordField.getText();
                System.out.println("Email: " + email + " Username: " + username + " Password: " + password + " Confirm Password: " + confirmPassword);
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
