/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.platform.WindowSystem;
import cn.harryh.arkpets.utils.ArgPending;
import cn.harryh.arkpets.utils.Logger;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Objects;

import static cn.harryh.arkpets.Const.*;


/** The bootstrap for ArkPets the libGDX app.
 * @see ArkPets
 */
public class EmbeddedLauncher {
    public static File customConfig;
    // Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument

    public static void main(String[] args) {
        // Disable assistive technologies
        System.setProperty("javax.accessibility.assistive_technologies", "");
        ArgPending.argCache = args;
        // Config
        new ArgPending("--config", args) {
            protected void process(String command, String addition) {
                customConfig = new File(addition);
            }
        };
        ArkConfig appConfig = Objects.requireNonNull(customConfig == null ? ArkConfig.getConfig() : ArkConfig.getConfig(customConfig));
        // Logger
        Logger.initialize(LogConfig.logCorePath, LogConfig.logCoreMaxKeep);
        try {
            Logger.setLevel(appConfig.logging_level);
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
                Logger.info("System", "Enable the debug feature");
                isDebugEnabled = true;
            }
        };
        new ArgPending("--load-lib", args) {
            @Override
            protected void process(String command, String addition) {
                Logger.info("System", "Loading the specified library \"" + addition + "\"");
                try {
                    System.load(addition);
                } catch (UnsatisfiedLinkError e) {
                    Logger.error("System", "Failed to load the specified library, details see below.", e);
                }
            }
        };
        Logger.info("System", "Entering the app of EmbeddedLauncher");
        Logger.info("System", "ArkPets version is " + appVersion);
        Logger.debug("System", "Default charset is " + Charset.defaultCharset());
        // Init temp folder
        File temp = new File(PathConfig.tempDirPath);
        if (!(temp.exists() || temp.mkdir())) {
            Logger.error("System", "Failed to create the temporary directory.");
        }
        try {
            WindowSystem.init();
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            // Configure ANGLE
            if (appConfig.render_enable_angle) {
                Logger.info("System", "Using ANGLE renderer");
                config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 2, 0);
                Configuration.OPENGL_EXPLICIT_INIT.set(true);
            }
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
            Lwjgl3Application app = new Lwjgl3Application(new ArkPets(TITLE, appConfig), config);
        } catch (Exception e) {
            WindowSystem.free();
            Logger.error("System", "A fatal error occurs in the runtime of Lwjgl3Application, details see below.", e);
            System.exit(-1);
        }
        WindowSystem.free();
        Logger.info("System", "Exited from EmbeddedLauncher successfully");
        System.exit(0);
    }
}
