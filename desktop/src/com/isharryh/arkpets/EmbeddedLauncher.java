package com.isharryh.arkpets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class EmbeddedLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		// Configure FPS
		config.setForegroundFPS(30);
		config.setIdleFPS(30);
		// Configure window size
		final int SCR_W = 140;
		final int SCR_H = 160;
		config.setWindowedMode(SCR_W, SCR_H);
		config.setWindowSizeLimits(SCR_W, SCR_H, SCR_W, SCR_H);
		config.setResizable(false);
		// Configure window title
		final String TITLE = applyWindowTitle();
		config.setTitle(TITLE);
		config.setDecorated(false);
		// Configure window display
		config.setInitialVisible(true);
		config.setTransparentFramebuffer(true);
		config.setInitialBackgroundColor(new Color(0,0,0,0));
		// Configure window position
		config.setWindowPosition(0, 0);
		// Instantiate the App
		Lwjgl3Application app = new Lwjgl3Application(new ArkPets(TITLE), config);
	}

	private static String applyWindowTitle() {
		final String prefix = "ArkPets";
		int cnt = 1;
		String tmp = "";
		HWND hwnd_test = User32.INSTANCE.FindWindow(null, prefix);
		while (hwnd_test != null) {
			cnt++;
			tmp = " (" + String.valueOf(cnt) + ")";
			hwnd_test = User32.INSTANCE.FindWindow(null, prefix + tmp);
		}
		return (prefix + tmp);
	}
}
