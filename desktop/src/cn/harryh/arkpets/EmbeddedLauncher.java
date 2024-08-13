/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.utils.ArgPending;
import cn.harryh.arkpets.utils.HWndCtrlFactory;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryUtil;

import java.nio.charset.Charset;
import java.util.Objects;

import static cn.harryh.arkpets.Const.*;


/** The bootstrap for ArkPets the libGDX app.
 * @see ArkPets
 */
public class EmbeddedLauncher {
    // Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument

    public static void main (String[] args) {
        ArgPending.argCache = args;
        // Logger
        Logger.initialize(LogConfig.logCorePath, LogConfig.logCoreMaxKeep);
        try {
            Logger.setLevel(Objects.requireNonNull(ArkConfig.getConfig()).logging_level);
        } catch (Exception ignored) {
        }
        new ArgPending(LogConfig.errorArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.ERROR);
            }
        };
        new ArgPending(LogConfig.warnArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.WARN);
            }
        };
        new ArgPending(LogConfig.infoArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.INFO);
            }
        };
        new ArgPending(LogConfig.debugArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.DEBUG);
            }
        };
        Logger.info("System", "Entering the app of EmbeddedLauncher");
        Logger.info("System", "ArkPets version is " + appVersion);
        Logger.debug("System", "Default charset is " + Charset.defaultCharset());

        try {
            HWndCtrlFactory.init();
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            // Configure FPS
            config.setForegroundFPS(fpsDefault);
            config.setIdleFPS(fpsDefault);
            // Configure window layout
            config.setDecorated(false);
            config.setResizable(false);
            config.setWindowedMode(coreWidthDefault, coreHeightDefault);
            config.setWindowPosition(0, 0);
            // Configure window title
            final String TITLE = coreTitleManager.getIdleTitle();
            config.setTitle(TITLE);
            // Configure window display
            config.setInitialVisible(true);
            config.setTransparentFramebuffer(true);
            config.setInitialBackgroundColor(Color.CLEAR);
            // Handle GLFW error
            GLFW.glfwSetErrorCallback(new GLFWErrorCallback() {
                @Override
                public void invoke(int error, long description) {
                    if (error != GLFW.GLFW_NO_ERROR) {
                        String descriptionString = MemoryUtil.memUTF8(description);
                        Logger.error("System", "Detected a GLFW error: (Code " + error + ") " + descriptionString);
                    }
                }
            });
            // Instantiate the App
            Lwjgl3Application app = new Lwjgl3Application(new ArkPets(TITLE), config);
        } catch (Exception e) {
            Logger.error("System", "An fatal error occurs in the runtime of Lwjgl3Application, details see below.", e);
            System.exit(-1);
        }
        Logger.info("System", "Exited from EmbeddedLauncher successfully");
        System.exit(0);
    }
}
