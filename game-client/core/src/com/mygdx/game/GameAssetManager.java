package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.audio.Music;

public class GameAssetManager {
    public final AssetManager manager = new AssetManager();

    // Paths to resources
    private static final String FONT_PATH = "AlegreyaSans-Bold.ttf";
    private static final String BACKGROUND_IMAGE = "main_menu_background.png";
    private static final String BACKGROUND_MUSIC = "Taj_Mahal.ogg";
    private static final String BUTTON_IMAGE = "buttons.png";
    private static final String CURSOR_IMAGE = "cursor.png";
    private static final String TEXT_CURSOR_IMAGE = "text_cursor.png";
    private static final String TEXT_FIELD_BACKGROUND_UP = "button-up.9.png";
    private static final String TEXT_FIELD_BACKGROUND_DOWN = "button-down.9.png";

    String color = "#e7e5e4";

    // Loading assets
    public void loadAssets() {
        // Set loaders for the FreeTypeFont
        FreeTypeFontLoaderParameter fontParams = new FreeTypeFontLoaderParameter();
        fontParams.fontFileName = FONT_PATH;

        fontParams.fontParameters.size = 36;
        fontParams.fontParameters.minFilter = Texture.TextureFilter.Linear;
        fontParams.fontParameters.magFilter = Texture.TextureFilter.Linear;
        fontParams.fontParameters.genMipMaps = true;
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(new InternalFileHandleResolver()));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(new InternalFileHandleResolver()));
        manager.load("font36.ttf", BitmapFont.class, fontParams);

        fontParams.fontParameters.size = 72;
        manager.load("font72.ttf", BitmapFont.class, fontParams);

        fontParams.fontParameters.size = 24;
        manager.load("font24.ttf", BitmapFont.class, fontParams);

        // Load other assets
        manager.load(BACKGROUND_IMAGE, Texture.class);
        manager.load(BACKGROUND_MUSIC, Music.class);
        manager.load(BUTTON_IMAGE, Texture.class);
        manager.load(CURSOR_IMAGE, Texture.class);
        manager.load(TEXT_CURSOR_IMAGE, Texture.class);
        manager.load(TEXT_FIELD_BACKGROUND_UP, Texture.class);
        manager.load(TEXT_FIELD_BACKGROUND_DOWN, Texture.class);
    }

    // Retrieving assets
    public BitmapFont getFontSmall() {
        return manager.get("font24.ttf", BitmapFont.class);
    }

    public BitmapFont getFontMedium() {
        return manager.get("font36.ttf", BitmapFont.class);
    }

    public BitmapFont getFontLarge() {
        return manager.get("font72.ttf", BitmapFont.class);
    }

    public Texture getBackgroundImage() {
        return manager.get(BACKGROUND_IMAGE, Texture.class);
    }

    public Music getBackgroundMusic() {
        return manager.get(BACKGROUND_MUSIC, Music.class);
    }

    public Button.ButtonStyle getButtonStyle() {
        Texture buttonTex = manager.get(BUTTON_IMAGE, Texture.class);
        TextureRegion buttonUpRegion = new TextureRegion(buttonTex, 0, 0, buttonTex.getWidth(), buttonTex.getHeight() / 2);
        TextureRegion buttonDownRegion = new TextureRegion(buttonTex, 0, buttonTex.getHeight() / 2, buttonTex.getWidth(), buttonTex.getHeight() / 2);
        Drawable buttonUpDrawable = new TextureRegionDrawable(buttonUpRegion);
        Drawable buttonDownDrawable = new TextureRegionDrawable(buttonDownRegion);

        Button.ButtonStyle buttonStyle = new Button.ButtonStyle(buttonUpDrawable, buttonDownDrawable, null);

        return buttonStyle;
    }

    public TextButton.TextButtonStyle getTextButtonStyle(float scale) {
        BitmapFont buttonFont = getFontMedium();
        Texture buttonTex = manager.get(BUTTON_IMAGE, Texture.class);
        Drawable upDrawable = new TextureRegionDrawable(new TextureRegion(manager.get(BUTTON_IMAGE, Texture.class), 0, 0, buttonTex.getWidth(), buttonTex.getHeight() / 2));
        Drawable downDrawable = new TextureRegionDrawable(new TextureRegion(manager.get(BUTTON_IMAGE, Texture.class), 0, buttonTex.getHeight() / 2, buttonTex.getWidth(), buttonTex.getHeight() / 2));
        
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = upDrawable;
        textButtonStyle.down = downDrawable;
        textButtonStyle.font = buttonFont;

        // styling
        textButtonStyle.font.getData().setScale(scale);
        // textButtonStyle.font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        // textButtonStyle.font.getData().markupEnabled = true;
        // textButtonStyle.font.getData().setLineHeight(40);
        textButtonStyle.font.getData().padTop = -10;
        textButtonStyle.font.getData().padBottom = -10;
        textButtonStyle.font.getData().padLeft = -50;
        textButtonStyle.font.getData().padRight = -50;
        
        return textButtonStyle;
    }

    public TextField.TextFieldStyle getTextFieldStyle(float scale) {
        TextureRegion upRegion = new TextureRegion(manager.get(TEXT_FIELD_BACKGROUND_UP, Texture.class));
        TextureRegion downRegion = new TextureRegion(manager.get(TEXT_FIELD_BACKGROUND_DOWN, Texture.class));
        NinePatch upPatch = new NinePatch(upRegion, 35, 25, 0, 0);
        NinePatch downPatch = new NinePatch(downRegion, 35, 25, 0, 0); 
    
        NinePatchDrawable backgroundUpDrawable = new NinePatchDrawable(upPatch);
        NinePatchDrawable backgroundDownDrawable = new NinePatchDrawable(downPatch);
    
        BitmapFont buttonFont = getFontSmall();
        buttonFont.getData().setScale(scale);
    
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = buttonFont;
        textFieldStyle.fontColor = Color.valueOf(color);
        textFieldStyle.background = backgroundUpDrawable;
        textFieldStyle.focusedBackground = backgroundDownDrawable;
        textFieldStyle.cursor = new TextureRegionDrawable(new TextureRegion(manager.get(TEXT_CURSOR_IMAGE, Texture.class)));
    
        return textFieldStyle;
    }

    public Texture getCursorImage() {
        return manager.get(CURSOR_IMAGE, Texture.class);
    }

    public Texture getTextCursorImage() {
        return manager.get(TEXT_CURSOR_IMAGE, Texture.class);
    }

    // Dispose of assets and manager
    public void dispose() {
        manager.dispose();
    }
}
