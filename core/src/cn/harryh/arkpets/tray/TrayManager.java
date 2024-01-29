package cn.harryh.arkpets.tray;

import cn.harryh.arkpets.ArkTray;
import cn.harryh.arkpets.process_pool.ProcessPool;
import cn.harryh.arkpets.process_pool.TaskStatus;
import cn.harryh.arkpets.utils.Logger;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static cn.harryh.arkpets.Const.fontFileRegular;

public abstract class TrayManager {
    protected volatile SystemTray tray;
    protected volatile TrayIcon trayIcon;
    protected final static ProcessPool threadPool = new ProcessPool();
    protected boolean initialized = false;
    protected Map<UUID, Tray> arkPetTrays = new HashMap<>();
    public static Font font;

    static {
        try {
            InputStream inputStream = Objects.requireNonNull(ArkTray.class.getResourceAsStream(fontFileRegular));
            font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            if (font != null) {
                UIManager.put("Label.font", font.deriveFont(9f).deriveFont(Font.ITALIC));
                UIManager.put("MenuItem.font", font.deriveFont(11f));
            }
        } catch (FontFormatException | IOException e) {
            Logger.error("Tray", "Failed to load tray menu font, details see below.", e);
            font = null;
        }
    }

    public abstract void showDialog(int x, int y);

    public abstract void listen(Stage stage);

    public abstract void hide(Stage stage);

    public abstract void addArkPets(UUID uuid, Tray tray);

    public abstract void removeArkPets(UUID uuid, Tray tray);

    public void shutdown() {
        threadPool.shutdown();
    }

    public SystemTray getTray() {
        return tray;
    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    private void sendMessage(TrayIcon.MessageType messageType, String title, String content, Object... args) {
        if (!initialized)
            return;
        trayIcon.displayMessage(title, content.formatted(args), messageType);
    }

    public void sendInfoMessage(String title, String content, Object... args) {
        sendMessage(TrayIcon.MessageType.INFO, title, content, args);
    }

    public void sendErrorMessage(String title, String content, Object... args) {
        sendMessage(TrayIcon.MessageType.ERROR, title, content, args);
    }

    public void sendWarnMessage(String title, String content, Object... args) {
        sendMessage(TrayIcon.MessageType.WARNING, title, content, args);
    }

    public void sendMessage(String title, String content, Object... args) {
        sendMessage(TrayIcon.MessageType.NONE, title, content, args);
    }

    public FutureTask<TaskStatus> submit(Class<?> clazz, java.util.List<String> jvmArgs, List<String> args) {
        return threadPool.submit(clazz, jvmArgs, args);
    }

    public Future<?> submit(Runnable task) {
        return threadPool.submit(task);
    }
}
