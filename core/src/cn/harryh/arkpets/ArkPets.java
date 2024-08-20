/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.animations.GeneralBehavior;
import cn.harryh.arkpets.concurrent.SocketClient;
import cn.harryh.arkpets.transitions.TernaryFunction;
import cn.harryh.arkpets.transitions.TransitionFloat;
import cn.harryh.arkpets.transitions.TransitionVector2;
import cn.harryh.arkpets.tray.MemberTrayImpl;
import cn.harryh.arkpets.hwnd.HWndCtrl;
import cn.harryh.arkpets.hwnd.HWndCtrlFactory;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.Plane;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static cn.harryh.arkpets.Const.*;


public class ArkPets extends ApplicationAdapter implements InputProcessor {
	/* RENDER PROCESS */
	public Plane plane;
	public ArkChar cha;
	public ArkConfig config;
	public MemberTrayImpl tray;
	public GeneralBehavior behavior;

	public TransitionFloat windowAlpha; // Window Opacity Easing
	public TransitionVector2 windowPosition; // Window Position Easing

	private HWndCtrl<?> hWndMine;
	private HWndCtrl<?> hWndTopmost;
	private LoopCtrl getHWndLoopCtrl;
	private List<? extends HWndCtrl<?>> hWndList;

	private final String APP_TITLE;
	private final MouseStatus mouseStatus = new MouseStatus();
	private int width; // Window Real Width
	private int height; // Window Real Height
	private int offsetY = 0;
	private boolean isToolwindowStyle = false;
	private boolean isAlwaysTransparent = false;

	public ArkPets(String title) {
		APP_TITLE = title;
	}

	@Override
	public void create() {
		// When the APP was created
		// 1.App setup
		Logger.info("App", "Create with title \"" + APP_TITLE + "\"");
		config = Objects.requireNonNull(ArkConfig.getConfig(), "ArkConfig returns a null instance, please check the config file.");
		Gdx.input.setInputProcessor(this);
		Gdx.graphics.setForegroundFPS(config.display_fps);

		// 2.Character setup
		Logger.info("App", "Using model asset \"" + config.character_asset + "\"");
		cha = new ArkChar(config, skelBaseScale);
		behavior = new GeneralBehavior(config, cha.animList);
		cha.adjustCanvas(behavior.defaultAnim().animClip().stage);
		cha.setAnimation(behavior.defaultAnim());
		Logger.info("Animation", "Available animation stages " + behavior.getStages());

		// 3.Plane setup
		width = (int)(config.display_scale * cha.camera.getWidth());
		height = (int)(config.display_scale * cha.camera.getHeight());
		plane = new Plane();
		plane.setGravity(config.physic_gravity_acc);
		plane.setResilience(0);
		plane.setFrict(config.physic_air_friction_acc, config.physic_static_friction_acc);
		plane.setObjSize(width, height);
		plane.setSpeedLimit(config.physic_speed_limit_x, config.physic_speed_limit_y);
		ArkConfig.Monitor primaryMonitor = refreshMonitorInfo();
		plane.changePosition(0,
				primaryMonitor.size[0] * config.initial_position_x - width / 2f,
				-(primaryMonitor.size[1] * config.initial_position_y + height)
		);

		// 4.Window position setup
		getHWndLoopCtrl = new LoopCtrl(1f / config.display_fps * 4);
		windowPosition = new TransitionVector2(TernaryFunction.EASE_OUT_CUBIC, easingDuration);
		windowPosition.reset(plane.getX(), - (height + plane.getY()) + offsetY);
		windowPosition.setToEnd();
		setWindowPos();

		// 5.Window style setup
		windowAlpha = new TransitionFloat(TernaryFunction.EASE_OUT_CUBIC, easingDuration);
		windowAlpha.reset(1f);
		hWndMine = HWndCtrlFactory.find(null, APP_TITLE);
		hWndMine.setLayered(true);
		if(config.window_style_topmost){
			hWndMine.setTopMost(true);
		}
		//hWndMine.setWindowExStyle(HWndCtrl.WS_EX_LAYERED | (config.window_style_topmost ? HWndCtrl.WS_EX_TOPMOST : 0));
		promiseToolwindowStyle(1000);

		// 6.Tray icon setup
		tray = new MemberTrayImpl(this, new SocketClient());

		// Setup complete
		Logger.info("App", "Render");
	}

	@Override
	public void render() {
		// 1.Render the next frame.
		cha.renderToBatch();

		// 2.Select a new animation.
		AnimData newAnim = behavior.autoCtrl(Gdx.graphics.getDeltaTime()); // AI anim.
		if (!mouseStatus.dragging) { // If no dragging:
			plane.updatePosition(Gdx.graphics.getDeltaTime());
			if (cha.getPlaying().mobility() != 0) {
				if (willReachBorder(cha.getPlaying().mobility())) {
					// Turn around if auto-walk cause the collision from screen border.
					newAnim = cha.getPlaying();
					newAnim = new AnimData(newAnim.animClip(), null, newAnim.isLoop(), newAnim.isStrict(), newAnim.offsetY(), -newAnim.mobility());
					tray.keepAnim = tray.keepAnim == null ? null : newAnim;
				}
				walkWindow(0.85f * cha.getPlaying().mobility());
			}
		} else { // If dragging:
			newAnim = behavior.dragging();
		}
		if (plane.getDropping()) { // If dropping, do not change anim.
			newAnim = behavior.defaultAnim();
		} else if (plane.getDropped()) { // If dropped, play the dropped anim.
			newAnim = behavior.dropped();
		} else if (tray.keepAnim != null) { // If keep-anim is enabled.
			newAnim = tray.keepAnim;
		}
		changeAnimation(newAnim); // Apply the new anim.

		// 3.Window properties.
		windowPosition.reset(plane.getX(), - (height + plane.getY()) + offsetY);
		windowPosition.addProgress(Gdx.graphics.getDeltaTime());
		setWindowPos();
		if (!windowAlpha.isEnded()) {
			windowAlpha.addProgress(Gdx.graphics.getDeltaTime());
			hWndMine.setWindowAlpha(windowAlpha.now());
		}
		promiseToolwindowStyle(1);
	}

	@Override
	public void resize(int x, int y) {
		Logger.debug("Window", "Resized to " + x + " * " + y);
	}

	@Override
	public void dispose() {
		Logger.info("App", "Dispose");
		HWndCtrlFactory.free();
	}

	/* INTERFACES */
	public boolean canChangeStage() {
		return behavior != null && behavior.getStages().size() > 1;
	}

	public void changeStage() {
		if (canChangeStage()) {
			behavior.nextStage();
			cha.adjustCanvas(behavior.getCurrentStage());
			width = (int)(config.display_scale * cha.camera.getWidth());
			height = (int)(config.display_scale * cha.camera.getHeight());
			plane.setObjSize(width, height);
			Logger.info("Animation", "Changed to " + behavior.getCurrentStage());
			changeAnimation(behavior.defaultAnim());
		}
	}

	public void setAlwaysTransparent(boolean alwaysTransparent) {
		isAlwaysTransparent = alwaysTransparent;
	}

	private void changeAnimation(AnimData animData) {
		if (cha.setAnimation(animData))
			offsetY = (int)(animData.offsetY() * config.display_scale);
	}

	/* INPUT PROCESS */
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Logger.debug("Input", "Click+ Btn " + button +" @ " + screenX + ", " + screenY);
		if (pointer <= 0) {
			mouseStatus.updatePosition(screenX, screenY, button);
			if (!isMouseAtSolidPixel()) {
				// Transfer mouse event
				RelativeWindowPosition rwp = getRelativeWindowPositionAt(screenX, screenY);
				if (rwp != null)
					rwp.sendMouseEvent(switch (button) {
						case Input.Buttons.LEFT -> HWndCtrl.MouseEvent.LBUTTONDOWN;
						case Input.Buttons.RIGHT -> HWndCtrl.MouseEvent.RBUTTONDOWN;
						case Input.Buttons.MIDDLE -> HWndCtrl.MouseEvent.MBUTTONDOWN;
						default -> HWndCtrl.MouseEvent.EMPTY;
					});
			} else {
				if (button == Input.Buttons.LEFT) {
					// Left Click: Play the specified animation
					changeAnimation(behavior.clickStart());
					tray.hideDialog();
				} else if (button == Input.Buttons.RIGHT) {
					// Right Click: Toggle the menu
					tray.toggleDialog((int)(plane.getX() + screenX), (int)(-plane.getY() - height));
				}
			}
		}
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		//Logger.debug("Input", "Dragged to " + screenX + ", " + screenY);
		if (pointer <= 0) {
			if (mouseStatus.button != Input.Buttons.RIGHT && isMouseAtSolidPixel()) {
				mouseStatus.dragging = true;
				mouseStatus.updateIntentionX(screenX);
				// Update window position
				int x = (int)(windowPosition.now().x + screenX - mouseStatus.x);
				int y = (int)(windowPosition.now().y + screenY - mouseStatus.y);
				plane.changePosition(Gdx.graphics.getDeltaTime(), x, -(height + y));
				windowPosition.setToEnd();
				tray.hideDialog();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Logger.debug("Input", "Click- Btn " + button +" @ " + screenX + ", " + screenY);
		if (pointer <= 0) {
			mouseStatus.updatePosition(screenX, screenY, button);
			if (mouseStatus.dragging) {
				// Update the z-axis of the character
				cha.position.reset(cha.position.end().x, cha.position.end().y, mouseStatus.intentionX);
				if (cha.getPlaying() != null && cha.getPlaying().mobility() != 0) {
					AnimData anim = cha.getPlaying();
					cha.setAnimation(anim.derive(anim.offsetY(), Math.abs(anim.mobility()) * mouseStatus.intentionX));
				}
                if (tray.keepAnim != null && tray.keepAnim.mobility() != 0) {
                    AnimData anim = tray.keepAnim;
					tray.keepAnim = anim.derive(anim.offsetY(), Math.abs(anim.mobility()) * mouseStatus.intentionX);
                }
            } else if (!isMouseAtSolidPixel()) {
				// Transfer mouse event
				RelativeWindowPosition rwp = getRelativeWindowPositionAt(screenX, screenY);
				if (rwp != null)
					rwp.sendMouseEvent(switch (button) {
						case Input.Buttons.LEFT -> HWndCtrl.MouseEvent.LBUTTONUP;
						case Input.Buttons.RIGHT -> HWndCtrl.MouseEvent.RBUTTONUP;
						case Input.Buttons.MIDDLE -> HWndCtrl.MouseEvent.MBUTTONUP;
						default -> HWndCtrl.MouseEvent.EMPTY;
					});
			} else if (button == Input.Buttons.LEFT) {
				// Left Click: Play the specified animation
				changeAnimation(behavior.clickEnd());
				tray.hideDialog();
			}
        }
		mouseStatus.dragging = false;
		return true;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		Logger.debug("Plane Debug Msg", plane.getDebugMsg());
		Logger.debug("Status Msg", "FPS" + Gdx.graphics.getFramesPerSecond() + ", Heap" + (int) Math.ceil((Gdx.app.getJavaHeap() >> 10) / 1024f) + "MB");
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		mouseStatus.updatePosition(screenX, screenY);
		if (!isMouseAtSolidPixel()) {
			// Transfer mouse event
			RelativeWindowPosition rwp = getRelativeWindowPositionAt(screenX, screenY);
			if (rwp != null)
				rwp.sendMouseEvent(HWndCtrl.MouseEvent.MOUSEMOVE);
		}
		return false;
	}

	@Override
	public boolean scrolled(float a, float b) {
		return false;
	}

	private boolean isMouseAtSolidPixel() {
		int pixel = cha.getPixel(mouseStatus.x, height - mouseStatus.y - 1);
		return (pixel & 0x000000FF) > 0;
	}

	/* WINDOW OPERATIONS */
	private void setWindowPos() {
		if (hWndMine == null) return;
		if (getHWndLoopCtrl.isExecutable(Gdx.graphics.getDeltaTime())) {
			refreshMonitorInfo();
			HWndCtrl<?> new_hwnd_topmost = refreshWindowIndex();
			hWndTopmost = new_hwnd_topmost != hWndTopmost ? new_hwnd_topmost : hWndTopmost;
			hWndMine.setWindowTransparent(isAlwaysTransparent);
		}
		hWndMine.setWindowPosition((HWndCtrl)hWndTopmost, (int)windowPosition.now().x, (int)windowPosition.now().y, width, height);
	}

	private RelativeWindowPosition getRelativeWindowPositionAt(int x, int y) {
		if (hWndList == null)
			return null;
		int absX = x + (int)(windowPosition.now().x);
		int absY = y + (int)(windowPosition.now().y);
		for (HWndCtrl<?> hWndCtrl : hWndList) {
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

	private HWndCtrl<?> refreshWindowIndex() {
		hWndList = HWndCtrlFactory.getWindowList(true);
		HWndCtrl<?> minWindow = null;
		HashMap<Integer, HWndCtrl<?>> line = new HashMap<>();
		int myPos = (int)(windowPosition.now().x + width / 2);
		int minNum = 2048;
		int myNum = coreTitleManager.getNumber(APP_TITLE);
		final float quantityProduct = 1;
		if (plane != null) {
			// Reset plane additions.
			plane.barriers.clear();
			plane.pointCharges.clear();
		}
		for (HWndCtrl<?> hWndCtrl : hWndList) {
			int wndNum = coreTitleManager.getNumber(hWndCtrl);
			// Distinguish non-peer windows from peers.
			if (wndNum == -1) {
				if (hWndCtrl.posLeft <= myPos && myPos <= hWndCtrl.posRight) {
					// This window and the app are share the same vertical line.
					if (-hWndCtrl.posBottom < plane.borderTop() && -hWndCtrl.posTop > plane.borderBottom()) {
						// This window is "under" the app.
						for (int h = -hWndCtrl.posTop; h > -hWndCtrl.posBottom; h--) {
							// Mark the window's y-position in the vertical line.
							if (!line.containsKey(h))
								line.put(h, (h == -hWndCtrl.posTop) ? hWndCtrl : HWndCtrlFactory.EMPTY); // Record this window.
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
		if (minWindow == null || minWindow.isEmpty()) {
			// Set as the top window if there is no peer.
			minWindow = HWndCtrlFactory.getTopMost();
		}
		if (plane != null) {
			// Set barriers according to the vertical line.
			for (int h = (int)plane.borderTop(); h > plane.borderBottom(); h--) {
				if (line.containsKey(h)) {
					HWndCtrl<?> temp = line.get(h);
					if (temp != null && temp.hWnd != null)
						plane.setBarrier(-temp.posTop, temp.posLeft, temp.windowWidth, false);
				}
			}
		}
		return config.window_style_topmost ? minWindow : HWndCtrlFactory.EMPTY; // Return the last peer window.
	}

	private ArkConfig.Monitor refreshMonitorInfo() {
		ArkConfig.Monitor[] monitors = ArkConfig.Monitor.getMonitors();
		if (monitors.length == 0) {
			Logger.error("App", "Failed to get monitors information since no monitor has been found");
			throw new RuntimeException("Failed to refresh monitors config.");
		}
		plane.world.clear();
		boolean flag = true;
		for (ArkConfig.Monitor i : monitors) {
			if (!flag) break;
			flag = config.display_multi_monitors;
			float left = i.virtual[0];
			float right = left + i.size[0];
			float top = -i.virtual[1];
			float bottom = top - i.size[1] + config.display_margin_bottom;
			plane.world.add(new Plane.RectArea(left, right, top, bottom));
		}
		return monitors[0];
	}

	private void promiseToolwindowStyle(int maxRetries) {
		if (config.window_style_toolwindow && !isToolwindowStyle) {
			// Make sure ArkPets has been set as foreground window once
			for (int i = 0; ; i++) {
				if (hWndMine.isForeground()) {
					hWndMine.setToolWindow(true);
					//hWndMine.setWindowExStyle(hWndMine.getWindowExStyle() | HWndCtrl.WS_EX_TOOLWINDOW);
					Logger.info("Window", "SetForegroundWindow succeeded");
					isToolwindowStyle = true;
					break;
				} else if (i > maxRetries) {
					return;
				}
				hWndMine.setForeground();
			}
		}
	}

	/* WINDOW WALKING RELATED */
	private void walkWindow(float len) {
		float expectedLen = len * config.display_scale * (30f / config.display_fps);
		int realLen = randomRound(expectedLen);
		float newPlaneX = plane.getX() + realLen;
		plane.changePosition(Gdx.graphics.getDeltaTime(), newPlaneX, plane.getY());
	}

	private int randomRound(float val) {
		int integer = (int)val;
		float decimal = val - integer;
		int offset = Math.abs(decimal) >= Math.random() ? (val >= 0 ? 1 : -1) : 0;
		return integer + offset;
	}

	private boolean willReachBorder(float len) {
		if (plane == null) return false;
		return (plane.getX() >= plane.borderRight() - width && len > 0) || (plane.getX() <= plane.borderLeft() && len < 0);
	}


	/* UTILS */
	private static class LoopCtrl {
		private final float minIntervalTime;
		private float accumTime;

		/** Loop Controller instance.
		 * @param minIntervalTime The minimal interval time for each loop.
		 */
		public LoopCtrl(float minIntervalTime) {
			this.minIntervalTime = minIntervalTime;
			this.accumTime = minIntervalTime;
		}

		/** Returns true if the loop is executable now.
		 * @param deltaTime The updated delta time.
		 */
		public boolean isExecutable(float deltaTime) {
			accumTime += deltaTime;
			if (accumTime >= minIntervalTime) {
				accumTime = 0;
				return true;
			} else {
				return false;
			}
		}
	}


	private static class MouseStatus {
		private int x = 0;
		private int y = 0;
		private int button = 0;
		private int intentionX = 0;
		private boolean dragging = false;

		public MouseStatus() {
		}

		public void updateIntentionX(int newX) {
			int t = (int)Math.signum(newX - x);
			intentionX = t == 0 ? intentionX : t;
		}

		public void updatePosition(int newX, int newY) {
			x = newX;
			y = newY;
		}

		public void updatePosition(int newX, int newY, int button) {
			updatePosition(newX, newY);
			this.button = button;
		}
	}


	private record RelativeWindowPosition(HWndCtrl<?> hWndCtrl, int relX, int relY) {
		public void sendMouseEvent(HWndCtrl.MouseEvent msg) {
			if(msg == HWndCtrl.MouseEvent.EMPTY) return;
			//Logger.debug("Input", "Transfer mouse event " + msg + " to `" + hWndCtrl.windowText + "` @ " + relX + ", " + relY);
			HWndCtrlFactory.update(hWndCtrl).sendMouseEvent(msg, relX, relY);
		}
	}
}
