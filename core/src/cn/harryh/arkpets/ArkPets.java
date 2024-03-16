/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.animations.AnimData;
import cn.harryh.arkpets.animations.GeneralBehavior;
import cn.harryh.arkpets.assets.AssetItem;
import cn.harryh.arkpets.concurrent.SocketClient;
import cn.harryh.arkpets.transitions.TernaryFunction;
import cn.harryh.arkpets.transitions.TransitionFloat;
import cn.harryh.arkpets.transitions.TransitionVector2;
import cn.harryh.arkpets.tray.MemberTrayImpl;
import cn.harryh.arkpets.utils.HWndCtrl;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.Plane;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

import java.util.*;

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

	private HWndCtrl hWndMine;
	private HWndCtrl hWndTopmost;
	private LoopCtrl getHWndLoopCtrl;
	private List<HWndCtrl> hWndList;

	private final String APP_TITLE;
	private int APP_FPS = fpsDefault;
	private float WD_SCALE; // Window Scale
	private int WD_W; // Window Real Width
	private int WD_H; // Window Real Height
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
		Logger.info("App", "Create");
		Gdx.input.setInputProcessor(this);
		config = Objects.requireNonNull(ArkConfig.getConfig());
		APP_FPS = config.display_fps;
		Gdx.graphics.setForegroundFPS(APP_FPS);
		getHWndLoopCtrl = new LoopCtrl(1f / APP_FPS * 4);
		// 2.Character setup
		Logger.info("App", "Using model asset \"" + config.character_asset + "\"");
		cha = new ArkChar(config.character_asset, new AssetItem.AssetAccessor(config.character_files), skelBaseScale);
		behavior = new GeneralBehavior(config, cha.animList);
		cha.adjustCanvas(behavior.defaultAnim().animClip().stage);
		cha.setAnimation(behavior.defaultAnim());
		Logger.info("Animation", "Available animation stages " + behavior.getStages());
		// 3.Window params setup
		windowPosition = new TransitionVector2(TernaryFunction.EASE_OUT_CUBIC, easingDuration);
		windowAlpha = new TransitionFloat(TernaryFunction.EASE_OUT_CUBIC, easingDuration);
		windowAlpha.reset(1f);
		WD_SCALE = config.display_scale;
		WD_W = (int)(WD_SCALE * cha.camera.getWidth());
		WD_H = (int)(WD_SCALE * cha.camera.getHeight());
		// 4.Plane setup
		plane = new Plane(new ArrayList<>());
		plane.setGravity(config.physic_gravity_acc);
		plane.setResilience(0);
		plane.setFrict(config.physic_air_friction_acc, config.physic_static_friction_acc);
		plane.setObjSize(WD_W, WD_H);
		plane.setSpeedLimit(config.physic_speed_limit_x, config.physic_speed_limit_y);
		ArkConfig.Monitor primaryMonitor = ArkConfig.Monitor.getMonitors()[0];
		initWindow((int)(primaryMonitor.size[0] * 0.1f), (int)(primaryMonitor.size[0] * 0.1f));
		// 5.Tray icon setup
		tray = new MemberTrayImpl(this, new SocketClient(), UUID.randomUUID());
		// Setup complete
		Logger.info("App", "Render");
	}

	@Override
	public void render() {
		// 1.Render the next frame.
		cha.renderToBatch();

		// 2.Select a new animation.
		AnimData newAnim = behavior.autoCtrl(Gdx.graphics.getDeltaTime()); // AI anim.
		if (!mouse_drag) { // If no dragging:
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
		windowPosition.reset(plane.getX(), - (WD_H + plane.getY()) + offsetY);
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
	}

	/* INTERFACES */
	public boolean canChangeStage() {
		return behavior != null && behavior.getStages().size() > 1;
	}

	public void changeStage() {
		if (canChangeStage()) {
			behavior.nextStage();
			cha.adjustCanvas(behavior.getCurrentStage());
			WD_W = (int)(WD_SCALE * cha.camera.getWidth());
			WD_H = (int)(WD_SCALE * cha.camera.getHeight());
			plane.setObjSize(WD_W, WD_H);
			Logger.info("Animation", "Changed to " + behavior.getCurrentStage());
			changeAnimation(behavior.defaultAnim());
		}
	}

	public void setAlwaysTransparent(boolean alwaysTransparent) {
		isAlwaysTransparent = alwaysTransparent;
	}

	private void changeAnimation(AnimData animData) {
		if (animData != null && !animData.isEmpty()) {
			// If it is needed to change animation:
			if (cha.setAnimation(animData))
				offsetY = (int)(animData.offsetY() * config.display_scale);
		}
	}

	/* INPUT PROCESS */
	private final Vector2 mouse_pos = new Vector2();
	private int mouse_intention_x = 1;
	private boolean mouse_drag = false;

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Logger.debug("Input", "Click+ Btn " + button +" @ " + screenX + ", " + screenY);
		mouse_pos.set(screenX, screenY);
		if (pointer <= 0) {
			if (!isMouseAtSolidPixel()) {
				// Transfer mouse event
				RelativeWindowPosition rwp = getRelativeWindowPositionAt(screenX, screenY);
				if (rwp != null)
					rwp.sendMouseEvent(switch (button) {
						case Input.Buttons.LEFT -> HWndCtrl.WM_LBUTTONDOWN;
						case Input.Buttons.RIGHT -> HWndCtrl.WM_RBUTTONDOWN;
						case Input.Buttons.MIDDLE -> HWndCtrl.WM_MBUTTONDOWN;
						default -> 0;
					});
			} else {
				if (button == Input.Buttons.LEFT) {
					// Left Click: Play the specified animation
					changeAnimation(behavior.clickStart());
					tray.hideDialog();
				} else if (button == Input.Buttons.RIGHT) {
					// Right Click: Toggle the menu
					tray.toggleDialog((int)(plane.getX() + screenX), (int)(-plane.getY() - WD_H));
				}
			}
		}
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		Logger.debug("Input", "Dragged to " + screenX + ", " + screenY);
		if (pointer <= 0) {
			if (isMouseAtSolidPixel()) {
				mouse_drag = true;
				int t = (int)Math.signum(screenX - mouse_pos.x);
				mouse_intention_x = t == 0 ? mouse_intention_x : t;
				// Update window position
				int x = (int)(windowPosition.now().x + screenX - mouse_pos.x);
				int y = (int)(windowPosition.now().y + screenY - mouse_pos.y);
				plane.changePosition(Gdx.graphics.getDeltaTime(), x, -(WD_H + y));
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
		mouse_pos.set(screenX, screenY);
		if (pointer <= 0) {
			if (mouse_drag) {
				// Update the z-axis of the character
				cha.position.reset(cha.position.end().x, cha.position.end().y, mouse_intention_x);
				if (cha.getPlaying() != null && cha.getPlaying().mobility() != 0) {
					AnimData anim = cha.getPlaying();
					cha.setAnimation(anim.derive(anim.offsetY(), Math.abs(anim.mobility()) * mouse_intention_x));
				}
                if (tray.keepAnim != null && tray.keepAnim.mobility() != 0) {
                    AnimData anim = tray.keepAnim;
					tray.keepAnim = anim.derive(anim.offsetY(), Math.abs(anim.mobility()) * mouse_intention_x);
                }
            } else if (!isMouseAtSolidPixel()) {
				// Transfer mouse event
				RelativeWindowPosition rwp = getRelativeWindowPositionAt(screenX, screenY);
				if (rwp != null)
					rwp.sendMouseEvent(switch (button) {
						case Input.Buttons.LEFT -> HWndCtrl.WM_LBUTTONUP;
						case Input.Buttons.RIGHT -> HWndCtrl.WM_RBUTTONUP;
						case Input.Buttons.MIDDLE -> HWndCtrl.WM_MBUTTONUP;
						default -> 0;
					});
			} else if (button == Input.Buttons.LEFT) {
				// Left Click: Play the specified animation
				changeAnimation(behavior.clickEnd());
				tray.hideDialog();
			}
        }
		mouse_drag = false;
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
		mouse_pos.set(screenX, screenY);
		if (!isMouseAtSolidPixel()) {
			// Transfer mouse event
			RelativeWindowPosition rwp = getRelativeWindowPositionAt(screenX, screenY);
			if (rwp != null)
				rwp.sendMouseEvent(HWndCtrl.WM_MOUSEMOVE);
		}
		return false;
	}

	@Override
	public boolean scrolled(float a, float b) {
		return false;
	}

	private boolean isMouseAtSolidPixel() {
		int pixel = cha.getPixel((int)mouse_pos.x, (int)(WD_H - mouse_pos.y - 1));
		return (pixel & 0x000000FF) > 0;
	}

	/* WINDOW OPERATIONS */
	private void initWindow(int x, int y) {
		// Initialize HWnd
		if (hWndMine == null || hWndMine.isEmpty())
			hWndMine = new HWndCtrl(null, APP_TITLE);
		hWndTopmost = refreshWindowIdx();
		// Set the initial style of the window
		hWndMine.setWindowExStyle(HWndCtrl.WS_EX_LAYERED | HWndCtrl.WS_EX_TOPMOST);
		hWndMine.setWindowTransparent(false);
		refreshMonitorInfo();
		promiseToolwindowStyle(1000);
		// Set the initial position of the window
		plane.changePosition(0, x, - (y + WD_H));
		windowPosition.reset(plane.getX(), - (WD_H + plane.getY()) + offsetY);
		windowPosition.setToEnd();
		setWindowPos();
	}

	private void setWindowPos() {
		if (hWndMine == null) return;
		if (getHWndLoopCtrl.isExecutable(Gdx.graphics.getDeltaTime())) {
			refreshMonitorInfo();
			HWndCtrl new_hwnd_topmost = refreshWindowIdx();
			if (new_hwnd_topmost != hWndTopmost)
				hWndTopmost = new_hwnd_topmost;
			hWndMine.setWindowTransparent(isAlwaysTransparent);
		}
		hWndMine.setWindowPosition(hWndTopmost, (int)windowPosition.now().x, (int)windowPosition.now().y, WD_W, WD_H);
	}

	private RelativeWindowPosition getRelativeWindowPositionAt(int x, int y) {
		if (hWndList == null)
			return null;
		int absX = x + (int)(windowPosition.now().x);
		int absY = y + (int)(windowPosition.now().y);
		for (HWndCtrl hWndCtrl : hWndList) {
			if (getArkPetsWindowNum(hWndCtrl.windowText) < 0)
				if (hWndCtrl.posLeft <= absX && hWndCtrl.posRight > absX)
					if (hWndCtrl.posTop <= absY && hWndCtrl.posBottom > absY) {
						int relX = absX - hWndCtrl.posLeft;
						int relY = absY - hWndCtrl.posTop;
						return new RelativeWindowPosition(hWndCtrl, relX, relY);
					}
		}
		return null;
	}

	private HWndCtrl refreshWindowIdx() {
		hWndList = HWndCtrl.getWindowList(true);
		//Logger.debug("HWND", windowList.toString());
		HWndCtrl minWindow = null;
		HashMap<Integer, HWndCtrl> line = new HashMap<>();
		int myPos = (int)(windowPosition.now().x + WD_W / 2);
		int minNum = 2048;
		int myNum = getArkPetsWindowNum(APP_TITLE);
		final float quantityProduct = 1;
		if (plane != null) {
			// Reset plane additions.
			plane.barriers.clear();
			plane.pointCharges.clear();
		}
		for (HWndCtrl hWndCtrl : hWndList) {
			int wndNum = getArkPetsWindowNum(hWndCtrl.windowText);
			// Distinguish non-peer windows from peers.
			if (wndNum == -1) {
				if (hWndCtrl.posLeft <= myPos && myPos <= hWndCtrl.posRight) {
					// This window and the app are share the same vertical line.
					if (-hWndCtrl.posBottom < plane.borderTop() && -hWndCtrl.posTop > plane.borderBottom()) {
						// This window is "under" the app.
						for (int h = -hWndCtrl.posTop; h > -hWndCtrl.posBottom; h--) {
							// Mark the window's y-position in the vertical line.
							if (!line.containsKey(h))
								line.put(h, (h == -hWndCtrl.posTop) ? hWndCtrl : HWndCtrl.EMPTY); // Record this window.
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
					minNum = getArkPetsWindowNum(hWndCtrl.windowText);
					minWindow = hWndCtrl;
				}
			}
			// Window iteration end.
		}
		if (minWindow == null || minWindow.isEmpty()) {
			// Set as the top window if there is no peer.
			minWindow = new HWndCtrl(-1);
		}
		if (plane != null) {
			// Set barriers according to the vertical line.
			for (int h = (int)plane.borderTop(); h > plane.borderBottom(); h--) {
				if (line.containsKey(h)) {
					HWndCtrl temp = line.get(h);
					if (temp != null && temp.hWnd != null)
						plane.setBarrier(-temp.posTop, temp.posLeft, temp.windowWidth, false);
				}
			}
		}
		return minWindow; // Return the last peer window.
	}

	private void refreshMonitorInfo() {
		ArkConfig.Monitor[] monitors = ArkConfig.Monitor.getMonitors();
		if (monitors.length == 0) {
			Logger.error("App", "Failed to get monitors information");
			throw new RuntimeException("Launch ArkPets failed due to monitors config error.");
		}
		plane.world.clear();
		boolean flag = true;
		for (ArkConfig.Monitor i : ArkConfig.Monitor.getMonitors()) {
			if (!flag) break;
			flag = config.display_multi_monitors;
			float left = i.virtual[0];
			float right = left + i.size[0];
			float top = -i.virtual[1];
			float bottom = top - i.size[1] + config.display_margin_bottom;
			plane.world.add(new Plane.RectArea(left, right, top, bottom));
		}
	}

	private void promiseToolwindowStyle(int maxRetries) {
		if (!isToolwindowStyle) {
			// Make sure ArkPets has been set as foreground window once
			for (int i = 0; ; i++) {
				if (hWndMine.isForeground()) {
					hWndMine.setWindowExStyle(hWndMine.getWindowExStyle() | HWndCtrl.WS_EX_TOOLWINDOW);
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

	public static int getArkPetsWindowNum(String title) {
		final String prefix = coreTitle;
		final String prefix2 = " (";
		final String suffix = ")";
		if (title != null && !title.isEmpty()) {
			try {
				if (title.indexOf(prefix) == 0) {
					if (title.equals(prefix))
						return 0;
					if (title.indexOf(prefix+prefix2) == 0)
						if (title.lastIndexOf(suffix) == title.length()-suffix.length())
							return Integer.parseInt(title.substring(prefix.length()+prefix2.length(), title.length()-suffix.length()));
				}
			} catch (Exception e) {
				Logger.error("Window", "Unable to get ArkPets window number, details see below.", e);
				return -1;
			}
		}
		return -1; // Not ArkPets window
	}

	/* WINDOW WALKING RELATED */
	private void walkWindow(float len) {
		float expectedLen = len * WD_SCALE * (30f / APP_FPS);
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
		return (plane.getX() >= plane.borderRight() - WD_W && len > 0) || (plane.getX() <= plane.borderLeft() && len < 0);
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


	private record RelativeWindowPosition(HWndCtrl hWndCtrl, int relX, int relY) {
		public void sendMouseEvent(int msg) {
			if (msg == 0)
				return;
			//Logger.debug("Input", "Transfer mouse event " + msg + " to `" + hWndCtrl.windowText + "` @ " + relX + ", " + relY);
			hWndCtrl.updated().sendMouseEvent(msg, relX, relY);
		}
	}
}
