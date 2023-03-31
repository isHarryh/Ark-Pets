/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.utils.ArgPending;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import static cn.harryh.arkpets.Const.*;


/** The bootstrap for ArkPets the libGDX app.
 * @see ArkPets
 */
public class EmbeddedLauncher {
	// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument

	public static void main (String[] args) {
		ArgPending.argCache = args;
		// Logger
		Logger.initialize("logs/core", 32);
		new ArgPending(LogLevels.errorArg, args) {
			protected void process(String command, String addition) {
				Logger.setLevel(Logger.ERROR);
			}
		};
		new ArgPending(LogLevels.warnArg, args) {
			protected void process(String command, String addition) {
				Logger.setLevel(Logger.WARN);
			}
		};
		new ArgPending(LogLevels.infoArg, args) {
			protected void process(String command, String addition) {
				Logger.setLevel(Logger.INFO);
			}
		};
		new ArgPending(LogLevels.debugArg, args) {
			protected void process(String command, String addition) {
				Logger.setLevel(Logger.DEBUG);
			}
		};

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		// Configure FPS
		config.setForegroundFPS(30);
		config.setIdleFPS(30);
		// Configure window size
		config.setWindowedMode(coreWidthDefault, coreHeightDefault);
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
		final String prefix = coreTitle;
		int cnt = 1;
		String tmp = "";
		HWND hwnd_test = User32.INSTANCE.FindWindow(null, prefix);
		while (hwnd_test != null) {
			cnt++;
			tmp = " (" + cnt + ")";
			hwnd_test = User32.INSTANCE.FindWindow(null, prefix + tmp);
		}
		return (prefix + tmp);
	}
}
