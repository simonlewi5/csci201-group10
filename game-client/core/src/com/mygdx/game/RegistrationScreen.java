package com.mygdx.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.Gson;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;


import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;


public class RegistrationScreen implements Screen, MessageListener  {
    final EgyptianRatscrew game;
    private final float ASPECT_RATIO = 16 / 9f;
    private Texture backgroundImage;
    private Music backgroundMusic;
    private OrthographicCamera camera;
    private String serverMessage;
    private GameWebSocketClient webSocketClient;
    private Label errorMessageLabel;
    private Stage stage;
    private TextField emailField, usernameField, passwordField, confirmPasswordField;
    private TextButton submitButton, exitButton;
    Viewport viewport;
    private boolean authSuccess = false;
    Color color = Color.valueOf("#e7e5e4");


    @Override
    public void messageReceived(String message) {
        serverMessage = message;
        System.out.println("Message received: " + serverMessage);
        Gson gson = new Gson();
        Response response = gson.fromJson(serverMessage, Response.class);
        if (response.getType().equals("AUTH_ERROR")) {
            int startIndex = serverMessage.indexOf("\"data\":\"") + "\"data\":\"".length();
            int endIndex = serverMessage.indexOf("\"", startIndex);
            String dataValue = serverMessage.substring(startIndex, endIndex);
            showErrorMessage(dataValue);
        }
        else{
            authSuccess = true;
        }
            
    }

    public RegistrationScreen(final EgyptianRatscrew game) {
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
        backgroundMusic.setVolume(game.getMusicVolume());
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
        Label.LabelStyle labelStyle = game.assetManager.getLabelStyle(1.0f, "FF0000");

        // set up email/username/password fields and buttons
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

        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');
    
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

        // set up error message display
        errorMessageLabel = new Label("", labelStyle);
        errorMessageLabel.setSize(300, 100);
        errorMessageLabel.setPosition(650, 600);
        
        // add all of it to the stage
        stage.addActor(emailField);
        stage.addActor(usernameField);
        stage.addActor(passwordField);
        stage.addActor(confirmPasswordField);
        stage.addActor(submitButton);
        stage.addActor(exitButton);
        stage.addActor(errorMessageLabel);
        
        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (webSocketClient != null && webSocketClient.isOpen()) {
                    String email = emailField.getText();
                    String username = usernameField.getText();
                    String password = passwordField.getText();
                    String confirmPassword = confirmPasswordField.getText();
                    System.out.println("Email: " + email + " Username: " + username + " Password: " + password + " Confirm Password: " + confirmPassword);

                    if (inputValidationCheck(email, username, password, confirmPassword)) {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("action", "register");
                        data.put("email", email);
                        data.put("username", username);
                        data.put("password", password);

                        Gson gson = new Gson();
                        String jsonData = gson.toJson(data);
                        webSocketClient.send(jsonData);

                        emailField.setText("");
                        usernameField.setText("");
                        passwordField.setText("");
                        confirmPasswordField.setText("");
                        
                    }
                } else {
                    System.out.println("WebSocket is not open");
                }
                if (authSuccess) {
                    showErrorMessage("Successfully registered!");
                    game.setScreen(new MainMenuScreen(game));
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

    private boolean inputValidationCheck(String email, String username, String password, String confirmPassword) {
        // perform client side input validation
        // check null inputs
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showErrorMessage("All fields are required.");
            return false;
        }

        // email format check
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
        if (!emailPattern.matcher(email).matches()) {
            showErrorMessage("Invalid email format.");
            return false;
        }

        // pw check
        if (!password.equals(confirmPassword)) {
            showErrorMessage("Passwords do not match.");
            return false;
        }

        
        return true;
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
