/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.animations.AnimClip;
import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.animations.GeneralBehavior;
import cn.harryh.arkpets.concurrent.SocketClient;
import cn.harryh.arkpets.platform.HWndCtrl;
import cn.harryh.arkpets.platform.WindowSystem;
import cn.harryh.arkpets.transitions.TransitionVector2;
import cn.harryh.arkpets.tray.MemberTrayImpl;
import cn.harryh.arkpets.utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static cn.harryh.arkpets.Const.coreTitleManager;


public class ArkPets extends InputApplicationAdaptor {
    /* RENDER PROCESS */
    public Plane plane;
    public ArkChar cha;
    public ArkConfig config;
    public MemberTrayImpl tray;
    public GeneralBehavior behavior;
    public TransitionVector2 windowPosition; // Window Position Easing

    private HWndCtrl hWndMine;
    private List<? extends HWndCtrl> hWndList;
    private final Cached<HWndCtrl> hWndTopmostGetter;

    private final String APP_TITLE;
    private int offsetY = 0;
    private boolean isToolwindowStyle = false;
    private boolean isAlwaysTransparent = false;
    private final Cached<Boolean> isFocused;

    public ArkPets(String title) {
        APP_TITLE = title;

        hWndTopmostGetter = new Cached<>();
        hWndTopmostGetter.setValueProducer(this::refreshWindowIndex);
        hWndTopmostGetter.setCacheAgeProducer(() -> 4.0 / getReducedFPS());

        isFocused = new Cached<>();
        isFocused.setValueProducer(() -> hWndMine.isForeground());
        isFocused.setCacheAgeProducer(() -> 4.0 / getReducedFPS());
    }

    @Override
    public void create() {
        // When the APP was created
        // 1.App setup
        Logger.info("App", "Create with title \"" + APP_TITLE + "\"");
        config = Objects.requireNonNull(ArkConfig.getConfig(), "ArkConfig returns a null instance, please check the config file.");
        Gdx.input.setInputProcessor(this);
        Gdx.graphics.setForegroundFPS(config.display_fps);
        registerDebugger();

        // 2.Character setup
        Logger.info("App", "Using model asset \"" + config.character_asset + "\"");
        cha = new ArkChar(config, config.display_scale);
        behavior = new GeneralBehavior(config, cha.animList);
        cha.adjustCanvas(behavior.defaultAnim().animClip().stage);
        cha.setAnimation(behavior.defaultAnim());
        cha.setAlpha(config.opacity_normal);
        Logger.info("Animation", "Available animation stages " + behavior.getStages());

        // 3.Plane setup
        plane = new Plane();
        plane.setGravity(config.physic_gravity_acc);
        plane.setResilience(0);
        plane.setFrict(config.physic_air_friction_acc, config.physic_static_friction_acc);
        plane.setObjSize(cha.camera.getWidth(), cha.camera.getHeight());
        plane.setSpeedLimit(config.physic_speed_limit_x, config.physic_speed_limit_y);
        Monitor primaryMonitor = refreshMonitorInfo();
        plane.changePosition(0,
                primaryMonitor.getWidth() * config.initial_position_x - cha.camera.getWidth() / 2f,
                -(primaryMonitor.getHeight() * config.initial_position_y + cha.camera.getHeight())
        );

        // 4.Window position setup
        windowPosition = new TransitionVector2(
                ArkConfig.getEasingFunctionFrom(config.transition_type),
                Math.max(0, config.transition_duration)
        );
        windowPosition.reset(plane.getX(), -(cha.camera.getHeight() + plane.getY()) + offsetY);
        windowPosition.setToEnd();

        // 5.Window style setup
        hWndMine = WindowSystem.findWindow(null, APP_TITLE);
        hWndMine.setLayered(true);
        if (config.window_style_topmost)
            hWndMine.setTopmost(true);
        updateWindow();

        // 6.Tray icon setup
        tray = new MemberTrayImpl(this, new SocketClient());

        // Setup complete
        Logger.info("App", "Render");
    }

    @Override
    public void render() {
        // 1.Render the next frame.
        cha.render();
        Gdx.graphics.setForegroundFPS((int) getReducedFPS());

        // 2.Select a new animation.
        AnimData newAnim;
        if (tray.keepAnim == null) {
            if (behavior.isAutoAnimExpired()) {
                newAnim = behavior.autoAnim(); // AI anim.
            } else {
                newAnim = null;
            }
        } else {
            newAnim = tray.keepAnim;
        }

        if (!isMouseDragging()) { // If no dragging:
            plane.updatePosition(Gdx.graphics.getDeltaTime());
            if (cha.getPlaying().mobility() != 0) {
                int mobility = cha.getPlaying().mobility();
                if (tray.keepAnim == null && willReachBorder(mobility)) {
                    // Turn around if auto-walk cause the collision from screen border.
                    newAnim = cha.getPlaying();
                    mobility = -mobility;
                    newAnim = new AnimData(newAnim.animClip(), null, newAnim.isLoop(), newAnim.isStrict(), mobility);
                    tray.keepAnim = tray.keepAnim == null ? null : newAnim;
                }
                walkWindow(config.behavior_walk_speed * (isCtrlPressed() ? 2 : 1) * mobility);
            }
        } else { // If dragging:
            newAnim = behavior.dragging();
        }
        if (plane.getDropping()) { // If dropping, do not change anim.
            newAnim = behavior.defaultAnim();
        } else if (plane.getDropped()) { // If dropped, play the dropped anim.
            newAnim = behavior.dropped();
        } else if (tray.keepAnim != null) { // If action-mode is enabled.
            if (isLeftPressed()) newAnim = behavior.walkAnim(-1);      // Left pressed
            else if (isRightPressed()) newAnim = behavior.walkAnim(1); // Right pressed
        }
        changeAnimation(newAnim); // Apply the new anim.

        // 3.Window properties.
        windowPosition.reset(plane.getX(), -(cha.camera.getHeight() + plane.getY()) + offsetY);
        windowPosition.addProgress(Gdx.graphics.getDeltaTime());
        updateWindow();

        // 4.Outline.
        boolean renderOutline = switch (ArkConfig.getRenderOutlineFrom(
                tray.keepAnim != null ? config.render_outline_emphasis : config.render_outline
        )) {
            case ALWAYS -> true;
            case PRESSING -> isMouseDown();
            case FOCUSED -> isFocused.getValue();
            case DRAGGING -> isMouseDragging();
            default -> false;
        };
        cha.setOutlineAlpha(renderOutline ? 1f : 0f);
        cha.setOutlineColor(ArkConfig.getGdxColorFrom(
                tray.keepAnim != null ? config.render_outline_emphasis_color : config.render_outline_color
        ));
    }

    @Override
    public void resize(int x, int y) {
        Logger.debug("Window", "Resized to " + x + " * " + y);
    }

    @Override
    public void dispose() {
        Logger.info("App", "Dispose");
    }

    /* INTERFACES */
    public boolean canChangeStage() {
        return behavior != null && behavior.getStages().size() > 1;
    }

    public void changeStage() {
        if (canChangeStage()) {
            behavior.nextStage();
            cha.adjustCanvas(behavior.getCurrentStage());
            plane.setObjSize(cha.camera.getWidth(), cha.camera.getHeight());
            Logger.info("Animation", "Changed to " + behavior.getCurrentStage());
            changeAnimation(behavior.defaultAnim());
        }
    }

    public void setTransparentMode(boolean enable) {
        isAlwaysTransparent = enable;
        cha.setAlpha(enable ? config.opacity_dim : config.opacity_normal);
    }

    private void changeAnimation(AnimData animData) {
        if (cha.setAnimation(animData))
            offsetY = (int) (animData.animClip().type.offsetY * config.display_scale);
    }

    /* INPUT PROCESS */
    @Override
    protected void onMouseDown() {
        if (!isMouseAtSolidPixel()) {
            // Transfer mouse event
            RelativeWindowPosition rwp = getRelativeWindowPositionAt(getMouseX(), getMouseY());
            if (rwp != null)
                rwp.sendMouseEvent(switch (getMouseButton()) {
                    case Input.Buttons.LEFT -> HWndCtrl.MouseEvent.LBUTTONDOWN;
                    case Input.Buttons.RIGHT -> HWndCtrl.MouseEvent.RBUTTONDOWN;
                    case Input.Buttons.MIDDLE -> HWndCtrl.MouseEvent.MBUTTONDOWN;
                    default -> HWndCtrl.MouseEvent.EMPTY;
                });
        } else {
            if (getMouseButton() == Input.Buttons.LEFT) {
                // Left Click: Play the specified animation
                changeAnimation(behavior.clickStart());
                tray.hideDialog();
            } else if (getMouseButton() == Input.Buttons.RIGHT) {
                // Right Click: Toggle the menu
                tray.toggleDialog((int) (plane.getX() + getMouseX()), (int) (-plane.getY() - cha.camera.getHeight()));
            }
        }
    }

    @Override
    protected void onMouseDrag() {
        if (getMouseButton() != Input.Buttons.RIGHT) {
            // Update window position
            int x = (int) (windowPosition.now().x + getMouseDeltaX());
            int y = (int) (windowPosition.now().y + getMouseDeltaY());
            plane.changePosition(Gdx.graphics.getDeltaTime(), x, -(cha.camera.getHeight() + y));
            windowPosition.setToEnd();
            tray.hideDialog();
        }
    }

    @Override
    protected void onMouseUp() {
        if (isMouseDragging()) {
            // Update the z-axis of the character
            cha.position.reset(cha.position.end().x, cha.position.end().y, getMouseIntention());
            if (cha.getPlaying() != null && cha.getPlaying().mobility() != 0) {
                AnimData anim = cha.getPlaying();
                cha.setAnimation(anim.derive(Math.abs(anim.mobility()) * getMouseIntention()));
            }
            if (tray.keepAnim != null && tray.keepAnim.mobility() != 0) {
                AnimData anim = tray.keepAnim;
                tray.keepAnim = anim.derive(Math.abs(anim.mobility()) * getMouseIntention());
            }
        } else if (!isMouseAtSolidPixel()) {
            // Transfer mouse event
            RelativeWindowPosition rwp = getRelativeWindowPositionAt(getMouseX(), getMouseY());
            if (rwp != null)
                rwp.sendMouseEvent(switch (getMouseButton()) {
                    case Input.Buttons.LEFT -> HWndCtrl.MouseEvent.LBUTTONUP;
                    case Input.Buttons.RIGHT -> HWndCtrl.MouseEvent.RBUTTONUP;
                    case Input.Buttons.MIDDLE -> HWndCtrl.MouseEvent.MBUTTONUP;
                    default -> HWndCtrl.MouseEvent.EMPTY;
                });
        } else if (getMouseButton() == Input.Buttons.LEFT) {
            // Left Click: Play the specified animation
            changeAnimation(behavior.clickEnd());
            tray.hideDialog();
        }
    }

    @Override
    protected void onKeyDown(int keycode) {
        if (tray.keepAnim != null) { // Switch animation in action mode
            AnimData data;
            if (isUpPressed()) {
                do {
                    data = behavior.prevAnim();
                } while (data.animClip().type == AnimClip.AnimType.MOVE); // Skip Move Animation
                tray.keepAnim = data;
                Logger.debug("Animation", "Switch to previous " + data);
            } else if (isDownPressed()) {
                do {
                    data = behavior.nextAnim();
                } while (data.animClip().type == AnimClip.AnimType.MOVE);
                tray.keepAnim = data;
                Logger.debug("Animation", "Switch to next " + data);
            }
        }
    }

    @Override
    protected void onKeyUp(int keycode) {
    }

    @Override
    protected void onMouseMoved() {
        if (!isMouseAtSolidPixel()) {
            // Transfer mouse event
            RelativeWindowPosition rwp = getRelativeWindowPositionAt(getMouseX(), getMouseY());
            if (rwp != null)
                rwp.sendMouseEvent(HWndCtrl.MouseEvent.MOUSEMOVE);
        }
    }

    private boolean isMouseAtSolidPixel() {
        int pixel = cha.getPixel(getMouseX(), cha.camera.getHeight() - getMouseY() - 1);
        return (pixel & 0x000000FF) > 0;
    }

    /* WINDOW OPERATIONS */
    private void updateWindow() {
        if (hWndMine == null)
            return;
        // Tool window style
        if (config.window_style_toolwindow && !isToolwindowStyle) {
            // Make sure ArkPets has been set as foreground window once
            for (int i = 0; i < 1; i++) {
                if (hWndMine.isForeground()) {
                    hWndMine.setTaskbar(false);
                    Logger.info("Window", "SetForegroundWindow succeeded");
                    isToolwindowStyle = true;
                    break;
                }
                hWndMine.setForeground();
            }
        }
        // Transparent style
        hWndMine.setTransparent(isAlwaysTransparent);
        // Window position
        hWndMine.setWindowPosition(hWndTopmostGetter.getValue(),
                (int) windowPosition.now().x, (int) windowPosition.now().y,
                cha.camera.getWidth(), cha.camera.getHeight());
    }

    private RelativeWindowPosition getRelativeWindowPositionAt(int x, int y) {
        if (hWndList == null)
            return null;
        int absX = x + (int) (windowPosition.now().x);
        int absY = y + (int) (windowPosition.now().y);
        for (HWndCtrl hWndCtrl : hWndList) {
            if (coreTitleManager.getNumber(hWndCtrl) < 0)
                if (hWndCtrl.posLeft <= absX && hWndCtrl.posRight > absX)
                    if (hWndCtrl.posTop <= absY && hWndCtrl.posBottom > absY) {
                        int relX = absX - hWndCtrl.posLeft;
                        int relY = absY - hWndCtrl.posTop;
                        return new RelativeWindowPosition(hWndCtrl, relX, relY);
                    }
        }
        return null;
    }

    private HWndCtrl refreshWindowIndex() {
        refreshMonitorInfo();
        hWndList = WindowSystem.getWindowList(true);
        HWndCtrl minWindow = null;
        HashMap<Integer, HWndCtrl> line = new HashMap<>();
        int myPos = (int) (windowPosition.now().x + cha.camera.getWidth() / 2f);
        int minNum = 2048;
        int myNum = coreTitleManager.getNumber(APP_TITLE);
        final float quantityProduct = 1;
        if (plane != null) {
            // Reset plane additions.
            plane.barriers.clear();
            plane.pointCharges.clear();
        }
        for (HWndCtrl hWndCtrl : hWndList) {
            int wndNum = coreTitleManager.getNumber(hWndCtrl);
            // Distinguish non-peer windows from peers.
            if (wndNum == -1) {
                boolean isBlackListWindow = false;
                for (Pattern pattern : Const.titleBlacklist) {
                    isBlackListWindow = pattern.matcher(hWndCtrl.windowText).matches();
                }
                if (isBlackListWindow) continue;
                if (hWndCtrl.posLeft <= myPos && myPos <= hWndCtrl.posRight) {
                    // This window and the app are share the same vertical line.
                    if (-hWndCtrl.posBottom < plane.borderTop() && -hWndCtrl.posTop > plane.borderBottom()) {
                        // This window is "under" the app.
                        for (int h = -hWndCtrl.posTop; h > -hWndCtrl.posBottom; h--) {
                            // Mark the window's y-position in the vertical line.
                            if (!line.containsKey(h))
                                line.put(h, (h == -hWndCtrl.posTop) ? hWndCtrl : null); // Record this window.
                        }
                    }
                }
            } else {
                if (config.behavior_do_peer_repulsion && wndNum != myNum && plane != null) {
                    // This window is peer window, set as point charges.
                    plane.setPointCharge(-hWndCtrl.getCenterY(), hWndCtrl.getCenterX(), quantityProduct);
                }
                // Find the last peer window to handle the z-index.
                if (wndNum > myNum && wndNum < minNum) {
                    minNum = coreTitleManager.getNumber(hWndCtrl);
                    minWindow = hWndCtrl;
                }
            }
            // Window iteration end.
        }
        if (minWindow == null) {
            // Set as the top window if there is no peer.
            minWindow = WindowSystem.getTopmostWindow();
        }
        if (plane != null) {
            // Set barriers according to the vertical line.
            for (int h = (int) plane.borderTop(); h > plane.borderBottom(); h--) {
                if (line.containsKey(h)) {
                    HWndCtrl temp = line.get(h);
                    if (temp != null)
                        plane.setBarrier(-temp.posTop, temp.posLeft, temp.windowWidth, false);
                }
            }
        }
        return config.window_style_topmost ? minWindow : null; // Return the last peer window.
    }

    private Monitor refreshMonitorInfo() {
        List<Monitor> monitors = Monitor.getMonitors();
        if (monitors.isEmpty()) {
            Logger.error("App", "Failed to get monitors information since no monitor has been found");
            throw new RuntimeException("Failed to refresh monitors config.");
        }
        plane.world.clear();
        boolean flag = true;
        for (Monitor m : monitors) {
            if (!flag) break;
            flag = config.display_multi_monitors;
            float left = m.getVirtualX();
            float right = left + m.getWidth();
            float top = -m.getVirtualY();
            float bottom = top - m.getHeight() + config.display_margin_bottom;
            plane.world.add(new Plane.RectArea(left, right, top, bottom));
        }
        return monitors.get(0); // Return the primary monitor.
    }

    /* WINDOW WALKING RELATED */
    private void walkWindow(float speed) {
        float distance = speed * config.display_scale * Gdx.graphics.getDeltaTime();
        int bias = Math.abs(distance - (int) distance) >= Math.random() ? (int) Math.signum(distance) : 0;
        int intDistance = (int) distance + bias;
        plane.changePosition(Gdx.graphics.getDeltaTime(), plane.getX() + intDistance, plane.getY());
    }

    private boolean willReachBorder(float speed) {
        if (plane == null)
            return false;
        if (speed > 0)
            return plane.getX() >= plane.borderRight() - cha.camera.getWidth();
        if (speed < 0)
            return plane.getX() <= plane.borderLeft();
        return false;
    }


    /* UTILS */
    private double getReducedFPS() {
        if (!config.eco_mode)
            return config.display_fps;
        double t = getLastActiveDeltaTime() / 60.0;
        double k = 1.0 + 0.5 * (Math.exp(-0.5 * t + 0.5) - 1.0);
        return Math.max(0.0, Math.min(1.0, k)) * config.display_fps;
    }

    private void registerDebugger() {
        Logger.debug("App", "OpenGL version is " + Gdx.gl.glGetString(GL20.GL_VERSION));
        Logger.debug("App", "OpenGL vendor is " + Gdx.gl.glGetString(GL20.GL_VENDOR));
        if (Const.isDebugEnabled) {
            registerKeyTyped('D', () -> {
                int heap = (int) Math.ceil((Gdx.app.getJavaHeap() >> 10) / 1024f);
                Logger.debug("Debugger", "FPS" + Gdx.graphics.getFramesPerSecond() + ", Heap" + heap + "MB");
            });
            registerKeyTyped('P', () -> Logger.debug("Debugger", "Showing plane info\n" + plane.getDebugMsg()));
            registerKeyTyped('S', () -> {
                String name = "temp/snapshot-" + System.currentTimeMillis() + ".png";
                Pixmap snapshot = Pixmap.createFromFrameBuffer(0, 0, cha.camera.getWidth(), cha.camera.getHeight());
                PixmapIO.writePNG(new FileHandle(name), snapshot);
                snapshot.dispose();
                Logger.debug("Debugger", "Snapshot saved to `" + name + "`");
            });
            registerKeyTyped('W', () -> {
                StringBuilder builder = new StringBuilder("Showing window list\n");
                WindowSystem.getWindowList(true).forEach(hWndCtrl -> builder.append(hWndCtrl).append("\n"));
                Logger.debug("Debugger", builder.toString());
            });
        }
    }

    private record RelativeWindowPosition(HWndCtrl hWndCtrl, int relX, int relY) {
        public void sendMouseEvent(HWndCtrl.MouseEvent msg) {
            if (msg == HWndCtrl.MouseEvent.EMPTY) return;
            //Logger.debug("Input", "Transfer mouse event " + msg + " to `" + hWndCtrl.windowText + "` @ " + relX + ", " + relY);
            hWndCtrl.updated().sendMouseEvent(msg, relX, relY);
        }
    }
}
