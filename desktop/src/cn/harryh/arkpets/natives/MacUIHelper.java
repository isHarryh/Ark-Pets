package cn.harryh.arkpets.natives;

import cn.harryh.arkpets.utils.Logger;
import com.sun.javafx.tk.TKStage;
import com.sun.jna.Pointer;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.lang.reflect.Field;


/**
 * Utilities for customizing the native {@code NSWindow} behind a JavaFX {@link Stage}.
 */
public class MacUIHelper {
    private static final String TAG = "MacUIHelper";

    // Titled(1) | Closable(2) | Miniaturizable(4) | FullSizeContentView(1 << 15)
    private static final long NS_WINDOW_STYLE_MASK = 32775L;
    private static final long NS_TITLE_VISIBILITY_HIDDEN = 1L;
    private static final long NS_WINDOW_BUTTON_CLOSE = 0L;
    private static final long NS_WINDOW_BUTTON_MINIATURIZE = 1L;
    private static final long NS_WINDOW_BUTTON_ZOOM = 2L;
    private static final long NS_LAYOUT_ATTRIBUTE_LEFT = 1L;
    private static final long NS_LAYOUT_RELATION_EQUAL = 0L;
    private static final double TITLE_BUTTON_LEFT_MARGIN = 15;
    private static final double TITLE_BUTTON_SPACING = 20;

    /**
     * Applies the launcher's transparent, borderless title bar style.
     *
     * @return {@code true} if the native traffic lights are ready to be used, or
     * {@code false} if the caller should fall back to the self-drawn title buttons.
     */
    public static boolean adjustNativeWindow(Stage stage) {
        Pointer window = null;
        try {
            ObjCHelper.init();
            window = getNSWindowHandle(stage);
            if (window == null) return false;
            applyStyle(window);
            return processTitle(window);
        } catch (RuntimeException | LinkageError e) {
            if (window != null) hideStandardButtons(window);
            Logger.warn(TAG, "Failed to adjust the native window, " +
                    "falling back to the self-drawn title buttons: " + e);
            return false;
        }
    }

    /**
     * Prevents macOS from restoring the window's previous state on the next launch.
     */
    public static void disableWindowRestoration(Stage stage) {
        Pointer window = getNSWindowHandle(stage);
        if (window == null) return;
        ObjCHelper.sendVoid(window, "setRestorable:", false);
    }

    /**
     * Obtains the native {@code NSWindow} pointer behind a JavaFX {@link Stage}.
     *
     * @return The window pointer, or {@code null} if it cannot be resolved.
     */
    public static Pointer getNSWindowHandle(Stage stage) {
        try {
            Field peerField = Window.class.getDeclaredField("peer");
            peerField.setAccessible(true);
            TKStage tkStage = (TKStage) peerField.get(stage);
            long handle = tkStage.getRawHandle();
            return handle == 0 ? null : new Pointer(handle);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            Logger.warn(TAG, "Failed to obtain the native NSWindow handle: " + e);
            return null;
        }
    }

    private static void applyStyle(Pointer window) {
        ObjCHelper.sendVoid(window, "setStyleMask:", NS_WINDOW_STYLE_MASK);
        ObjCHelper.sendVoid(window, "setTitleVisibility:", NS_TITLE_VISIBILITY_HIDDEN);
        ObjCHelper.sendVoid(window, "setTitlebarAppearsTransparent:", 1L);
        Pointer clearColor = ObjCHelper.sendPointer(ObjCHelper.cls("NSColor"), "clearColor");
        ObjCHelper.sendVoid(window, "setBackgroundColor:", clearColor);
    }

    private static boolean processTitle(Pointer window) {
        try {
            Pointer buttonZoom = ObjCHelper.sendPointer(
                    window, "standardWindowButton:", NS_WINDOW_BUTTON_ZOOM);
            Pointer buttonMini = ObjCHelper.sendPointer(
                    window, "standardWindowButton:", NS_WINDOW_BUTTON_MINIATURIZE);
            Pointer buttonClose = ObjCHelper.sendPointer(
                    window, "standardWindowButton:", NS_WINDOW_BUTTON_CLOSE);
            if (buttonZoom == null || buttonMini == null || buttonClose == null) {
                hideStandardButtons(window);
                return false;
            }
            Pointer superView = ObjCHelper.sendPointer(buttonZoom, "superview");
            if (superView == null) {
                hideStandardButtons(window);
                return false;
            }
            ObjCHelper.sendVoid(buttonZoom, "setHidden:", true);
            ObjCHelper.sendVoid(buttonClose, "setTranslatesAutoresizingMaskIntoConstraints:", false);
            ObjCHelper.sendVoid(buttonMini, "setTranslatesAutoresizingMaskIntoConstraints:", false);
            createConstraint(buttonClose, NS_LAYOUT_ATTRIBUTE_LEFT, superView,
                    NS_LAYOUT_ATTRIBUTE_LEFT, TITLE_BUTTON_LEFT_MARGIN);
            createConstraint(buttonMini, NS_LAYOUT_ATTRIBUTE_LEFT, buttonClose,
                    NS_LAYOUT_ATTRIBUTE_LEFT, TITLE_BUTTON_SPACING);
            return true;
        } catch (RuntimeException | LinkageError e) {
            Logger.warn(TAG, "Failed to process the native title buttons, " +
                    "falling back to the self-drawn title buttons: " + e);
            hideStandardButtons(window);
            return false;
        }
    }

    private static void hideStandardButtons(Pointer window) {
        try {
            long[] buttons = {
                    NS_WINDOW_BUTTON_CLOSE,
                    NS_WINDOW_BUTTON_MINIATURIZE,
                    NS_WINDOW_BUTTON_ZOOM
            };
            for (long buttonId : buttons) {
                Pointer button = ObjCHelper.sendPointer(window, "standardWindowButton:", buttonId);
                if (button != null) ObjCHelper.sendVoid(button, "setHidden:", true);
            }
        } catch (RuntimeException | LinkageError e) {
            Logger.warn(TAG, "Failed to hide the native window buttons: " + e);
        }
    }

    private static void createConstraint(Pointer item, long attribute, Pointer toItem,
                                         long toAttribute, double constant) {
        Pointer constraint = ObjCHelper.sendPointer(
                ObjCHelper.cls("NSLayoutConstraint"),
                "constraintWithItem:attribute:relatedBy:toItem:attribute:multiplier:constant:",
                item,
                attribute,
                NS_LAYOUT_RELATION_EQUAL,
                toItem,
                toAttribute,
                1.0,
                constant);
        ObjCHelper.sendVoid(constraint, "setActive:", true);
    }
}
