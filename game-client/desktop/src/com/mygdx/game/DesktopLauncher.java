package com.mygdx.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		float ASPECT_RATIO = 16 / 9f;
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(1600, (int)(1600 / ASPECT_RATIO));
		config.setForegroundFPS(60);
		config.setTitle("Egyptian Ratscrew");
		new Lwjgl3Application(new EgyptianRatscrew(), config);
	}
}
