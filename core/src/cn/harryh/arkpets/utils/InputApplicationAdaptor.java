/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;

import java.util.HashMap;


abstract public class InputApplicationAdaptor extends ApplicationAdapter implements InputProcessor {
    // Mouse
    private int mouseX = 0;
    private int mouseY = 0;
    private int mouseDeltaX = 0;
    private int mouseDeltaY = 0;
    private int mouseButton = 0;
    private int lastDragDeltaX = 0;
    private int lastDragDeltaY = 0;
    private int lastMoveDeltaX = 0;
    private int lastMoveDeltaY = 0;
    private boolean isMouseDragging = false;
    private boolean isMouseDown = false;

    // Keyboard
    private byte isAltPressed = 0b00;
    private byte isCtrlPressed = 0b00;
    private byte isShiftPressed = 0b00;
    private boolean isLeftPressed = false;
    private boolean isRightPressed = false;
    private boolean isUpPressed = false;
    private boolean isDownPressed = false;

    private final HashMap<Character, Runnable> keyHandlerMap = new HashMap<>();

    private long lastActiveNanoTime = System.nanoTime();

    abstract protected void onMouseDown();

    abstract protected void onMouseDrag();

    abstract protected void onMouseUp();

    abstract protected void onMouseMoved();

    abstract protected void onKeyDown(int keycode);

    abstract protected void onKeyUp(int keycode);

    @Override
    abstract public void create();

    @Override
    abstract public void render();

    @Override
    abstract public void resize(int w, int h);

    @Override
    abstract public void dispose();

    public final int getMouseX() {
        return mouseX;
    }

    public final int getMouseY() {
        return mouseY;
    }

    public final int getMouseDeltaX() {
        return mouseDeltaX;
    }

    public final int getMouseDeltaY() {
        return mouseDeltaY;
    }

    public final int getMouseButton() {
        return mouseButton;
    }

    public final int getLastDragDeltaX() {
        return lastDragDeltaX;
    }

    public final int getLastDragDeltaY() {
        return lastDragDeltaY;
    }

    public final int getLastMoveDeltaX() {
        return lastMoveDeltaX;
    }

    public final int getLastMoveDeltaY() {
        return lastMoveDeltaY;
    }

    public final double getLastActiveDeltaTime() {
        return (System.nanoTime() - lastActiveNanoTime) / 1_000_000_000.0;
    }

    public final boolean isMouseDragging() {
        return isMouseDragging;
    }

    public final boolean isMouseDown() {
        return isMouseDown;
    }

    public final void registerKeyTyped(char character, Runnable handler) {
        keyHandlerMap.put(Character.toLowerCase(character), handler);
    }

    public final boolean isAltPressed() {
        return isAltPressed != 0b00;
    }

    public final boolean isCtrlPressed() {
        return isCtrlPressed != 0b00;
    }

    public final boolean isShiftPressed() {
        return isShiftPressed != 0b00;
    }

    public final boolean isLeftPressed() {
        return isLeftPressed;
    }

    public final boolean isRightPressed() {
        return isRightPressed;
    }

    public final boolean isUpPressed() {
        return isUpPressed;
    }

    public final boolean isDownPressed() {
        return isDownPressed;
    }

    @Deprecated
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        lastActiveNanoTime = System.nanoTime();
        if (pointer <= 0) {
            Logger.debug("Input", "Click+ Btn " + button + " @ " + screenX + ", " + screenY);
            mouseX = screenX;
            mouseY = screenY;
            mouseDeltaX = 0;
            mouseDeltaY = 0;
            lastDragDeltaX = 0;
            lastDragDeltaY = 0;
            lastMoveDeltaX = 0;
            lastMoveDeltaY = 0;
            mouseButton = button;
            isMouseDown = true;
            onMouseDown();
        }
        return true;
    }

    @Deprecated
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        lastActiveNanoTime = System.nanoTime();
        if (pointer <= 0) {
            Logger.debug("Input", "Click- Btn " + button + " @ " + screenX + ", " + screenY);
            mouseX = screenX;
            mouseY = screenY;
            mouseDeltaX = 0;
            mouseDeltaY = 0;
            mouseButton = button;
            isMouseDown = false;
            onMouseUp();
        }
        isMouseDragging = false;
        return false;
    }

    @Deprecated
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        lastActiveNanoTime = System.nanoTime();
        if (pointer <= 0) {
            mouseDeltaX = screenX - mouseX;
            mouseDeltaY = screenY - mouseY;
            lastDragDeltaX = mouseDeltaX * lastDragDeltaX <= 0 ? mouseDeltaX : lastDragDeltaX + mouseDeltaX;
            lastDragDeltaY = mouseDeltaY * lastDragDeltaY <= 0 ? mouseDeltaY : lastDragDeltaY + mouseDeltaY;
            isMouseDragging = true;
            onMouseDrag();
        }
        return false;
    }

    @Deprecated
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        int dx = screenX - mouseX;
        int dy = screenY - mouseY;
        mouseX = screenX;
        mouseY = screenY;
        lastMoveDeltaX = dx * lastMoveDeltaX <= 0 ? dx : lastMoveDeltaX + dx;
        lastMoveDeltaY = dy * lastMoveDeltaY <= 0 ? dy : lastMoveDeltaY + dy;
        onMouseMoved();
        return true;
    }

    @Deprecated
    @Override
    public boolean scrolled(float a, float b) {
        return false;
    }

    @Deprecated
    @Override
    public boolean keyDown(int keycode) {
        lastActiveNanoTime = System.nanoTime();
        switch (keycode) {
            case Keys.ALT_LEFT -> isAltPressed |= 0b10;
            case Keys.ALT_RIGHT -> isAltPressed |= 0b01;
            case Keys.CONTROL_LEFT -> isCtrlPressed |= 0b10;
            case Keys.CONTROL_RIGHT -> isCtrlPressed |= 0b01;
            case Keys.SHIFT_LEFT -> isShiftPressed |= 0b10;
            case Keys.SHIFT_RIGHT -> isShiftPressed |= 0b01;
            case Keys.LEFT -> isLeftPressed = true;
            case Keys.RIGHT -> isRightPressed = true;
            case Keys.UP -> isUpPressed = true;
            case Keys.DOWN -> isDownPressed = true;
        }
        onKeyDown(keycode);
        return true;
    }

    @Deprecated
    @Override
    public boolean keyUp(int keycode) {
        lastActiveNanoTime = System.nanoTime();
        switch (keycode) {
            case Keys.ALT_LEFT -> isAltPressed &= 0b01;
            case Keys.ALT_RIGHT -> isAltPressed &= 0b10;
            case Keys.CONTROL_LEFT -> isCtrlPressed &= 0b01;
            case Keys.CONTROL_RIGHT -> isCtrlPressed &= 0b10;
            case Keys.SHIFT_LEFT -> isShiftPressed &= 0b01;
            case Keys.SHIFT_RIGHT -> isShiftPressed &= 0b10;
            case Keys.LEFT -> isLeftPressed = false;
            case Keys.RIGHT -> isRightPressed = false;
            case Keys.UP -> isUpPressed = false;
            case Keys.DOWN -> isDownPressed = false;
        }
        onKeyUp(keycode);
        return true;
    }

    @Deprecated
    @Override
    public boolean keyTyped(char character) {
        lastActiveNanoTime = System.nanoTime();
        Runnable handler = keyHandlerMap.get(Character.toLowerCase(character));
        if (handler != null)
            handler.run();
        return true;
    }
}
