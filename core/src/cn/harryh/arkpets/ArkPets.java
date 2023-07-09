/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.behaviors.*;
import cn.harryh.arkpets.utils.AnimData;
import cn.harryh.arkpets.utils.HWndCtrl;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.Plane;
import cn.harryh.arkpets.easings.EasingLinear;
import cn.harryh.arkpets.easings.EasingLinearVector2;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import java.util.*;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinDef.HWND;

import static cn.harryh.arkpets.Const.*;


public class ArkPets extends ApplicationAdapter implements InputProcessor {
	/* RENDER PROCESS */
	public Plane plane;
	public ArkChar cha;
	public ArkConfig config;
	public ArkTray tray;
	public Behavior behavior;

	private HWND HWND_TOPMOST;
	private LoopCtrl getHWndLoopCtrl;

	private int APP_FPS = fpsDefault;
	private float WD_SCALE; // Window Scale
	private int WD_W; // Window Real Width
	private int WD_H; // Window Real Height

	public Vector2 WD_poscur; // Window Current Position
	public Vector2 WD_postar; // Window Target Position
	public EasingLinear WD_alpha; // Window Opacity Easing
	public EasingLinearVector2 WD_poseas; // Window Position Easing
	private int OFFSET_Y = 0;

	public ArkPets(String $title) {
		APP_TITLE = $title;
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
		getHWndLoopCtrl = new LoopCtrl(1.0f / APP_FPS * 4);
		// 2.Character setup
		Logger.info("App", "Using model asset \"" + config.character_asset + "\"");
		cha = new ArkChar(config.character_asset+".atlas", config.character_asset+".skel", skelBaseScale);
		cha.setCanvas(APP_FPS);
		// 3.Window params setup
		WD_poscur = new Vector2(0, 0);
		WD_postar = new Vector2(0, 0);
		WD_poseas = new EasingLinearVector2(new EasingLinear(0, 1, linearEasingDuration));
		WD_alpha = new EasingLinear(0, 1, linearEasingDuration);
		WD_SCALE = config.display_scale;
		WD_W = (int)(WD_SCALE * cha.flexibleLayout.getWidth());
		WD_H = (int)(WD_SCALE * cha.flexibleLayout.getHeight());
		// 4.Plane setup
		plane = new Plane(new ArrayList<>(), config.physic_gravity_acc);
		plane.setFrict(config.physic_air_friction_acc, config.physic_static_friction_acc);
		plane.setBounce(0);
		plane.setObjSize(WD_W, WD_H);
		plane.setSpeedLimit(config.physic_speed_limit_x, config.physic_speed_limit_y);
		ArkConfig.Monitor primaryMonitor = ArkConfig.Monitor.getMonitors()[0];
		intiWindow((int)(primaryMonitor.size[0] * 0.1f), (int)(primaryMonitor.size[0] * 0.1f));
		// 5.Behavior setup
		behavior = Behavior.selectBehavior(cha.anim_list, new Behavior[] {
				new BehaviorOperBuild2(config, cha.anim_list),
				new BehaviorOperBuild(config, cha.anim_list),
				new BehaviorOperBuild3(config, cha.anim_list),
				new BehaviorBattleGeneral(config, cha.anim_list),
				new BehaviorBattleGeneral2(config, cha.anim_list),
				new BehaviorBattleGeneral3(config, cha.anim_list),
				new BehaviorBattleGeneral4(config, cha.anim_list),
		});
		if (behavior == null) {
			Logger.error("App", "No suitable ArkPets behavior instance found, you can contact the developer, details see below.");
			Logger.error("App", "This model only contains these animations: " + Arrays.toString(cha.anim_list));
			throw new RuntimeException("Launch ArkPets failed due to unsupported model.");
		}
		Logger.info("App", "Using behavior class \"" + behavior.getClass().getSimpleName() + "\"");
		cha.setAnimation(behavior.defaultAnim());
		// 6.Tray icon setup
		tray = new ArkTray(this);
		// Setup complete
		Logger.info("App", "Render");
	}

	@Override
	public void render() {
		// 1.Render the next frame.
		cha.next();
		if (cha.anim_frame.isLoopEnd()) {
			// When an animation's loop ends:
			Logger.debug("Monitor", "FPS" + Gdx.graphics.getFramesPerSecond() + ", Heap" + (int)Math.ceil((Gdx.app.getJavaHeap() >> 10) / 1024f) + "MB");
		}

		// 2.Select a new animation.
		AnimData newAnim = behavior.autoCtrl(Gdx.graphics.getDeltaTime()); // AI anim.
		if (!mouse_drag) { // If no dragging:
			plane.updatePosition(Gdx.graphics.getDeltaTime());
			if (cha.anim_queue[0].MOBILITY != 0) {
				if (willReachBorder(cha.anim_queue[0].MOBILITY)) {
					// Turn around if auto-walk cause the collision from screen border.
					newAnim = cha.anim_queue[0];
					newAnim = new AnimData(newAnim.ANIM_NAME, newAnim.LOOP, newAnim.INTERRUPTABLE, newAnim.OFFSET_Y, -newAnim.MOBILITY);
					tray.keepAnim = tray.keepAnim == null ? null : newAnim;
				}
				walkWindow(0.85f * cha.anim_queue[0].MOBILITY);
			}
		} else { // If dragging:
			newAnim = behavior.dragStart();
		}
		if (plane.getDropping()) { // If dropping, do not change anim.
			newAnim = behavior.defaultAnim();
		} else if (plane.getDropped()) { // If dropped, play the dropped anim.
			newAnim = behavior.drop();
		} else if (tray.keepAnim != null) { // If keep-anim is enabled.
			newAnim = tray.keepAnim;
		}
		changeAnimation(newAnim); // Apply the new anim.

		// 3.Window activities.
		setWindowPosTar(plane.getX(), - (WD_H + plane.getY()));
		setWindowPosCur(Gdx.graphics.getDeltaTime());
		setWindowAlphaCur(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void resize(int x, int y) {
		Logger.debug("Window", "Resized to " + x + " * " + y);
	}

	@Override
	public void dispose() {
		Logger.info("App", "Dispose");
		tray.remove();
	}

	private void changeAnimation(AnimData animData) {
		if (animData != null) {
			// If it is needed to change animation:
			if (cha.setAnimation(animData))
				OFFSET_Y = (int)(animData.OFFSET_Y * config.display_scale);
		}
	}


	/* INPUT PROCESS */
	private final Vector2 mouse_pos = new Vector2();
	private int mouse_intention_x = 1;
	private boolean mouse_drag = false;

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Logger.debug("Input", "Click+ @ " + screenX + ", " + screenY);
		mouse_pos.set(screenX, screenY);
		if (pointer <= 0) {
			if (button == Input.Buttons.LEFT) {
				cha.setAnimation(behavior.clickStart());
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// Logger.debug("Input", "Dragged to " + screenX + ", " + screenY);
		mouse_drag = true;
		int t = (int)Math.signum(screenX - mouse_pos.x);
		mouse_intention_x = t == 0 ? mouse_intention_x : t;
		int x = (int)(WD_poscur.x + screenX - mouse_pos.x);
		int y = (int)(WD_poscur.y + screenY - mouse_pos.y);
		plane.changePosition(Gdx.graphics.getDeltaTime(), x, - (WD_H + y));
		setWindowPos((int)plane.getX(),  - (WD_H + (int)plane.getY()), true);
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Logger.debug("Input", "Click- @ " + screenX + ", " + screenY);
		Logger.debug("Plane Debug Message", plane.getDebugMsg());
		if (!mouse_drag)
			changeAnimation(behavior.clickEnd());
		else {
			cha.setPositionTar(cha.positionTar.x, cha.positionTar.y, mouse_intention_x);
			if (tray.keepAnim != null && cha.anim_queue[0].MOBILITY != 0) {
				AnimData newAnim = cha.anim_queue[0];
				newAnim = new AnimData(newAnim.ANIM_NAME, newAnim.LOOP, newAnim.INTERRUPTABLE, newAnim.OFFSET_Y, Math.abs(newAnim.MOBILITY) * mouse_intention_x);
				tray.keepAnim = newAnim;
			}
		}
		mouse_drag = false;
		return true;
	}

	@Override
	public boolean keyDown(int keycode) {
		Logger.debug("Input", "Key+ @ " + keycode);
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		Logger.debug("Input", "Key- @ " + keycode);
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float a, float b) {
		return false;
	}

	/* WINDOW API */
	private final String APP_TITLE;
	public HWND HWND_MINE;

	private void intiWindow(int x, int y) {
		if (HWND_MINE == null)
            HWND_MINE = User32.INSTANCE.FindWindow(null, APP_TITLE);
        refreshMonitorInfo();
		HWND_TOPMOST = refreshWindowIdx();
		setWindowPos(x, y, true);
		setWindowTransparent(false);
		plane.changePosition(0, WD_postar.x, - (WD_postar.y + WD_H));
	}

	public void setWindowTransparent(boolean $transparent) {
		Logger.debug("Window", "JNA SetWindowLong returns " +
				Integer.toHexString(User32.INSTANCE.SetWindowLong(HWND_MINE, WinUser.GWL_EXSTYLE,
						windowLongDefault | ($transparent ? WinUser.WS_EX_TRANSPARENT : 0))));
	}

	private void setWindowAlpha(float $alpha) {
		$alpha = Math.max(0, Math.min(1, $alpha));
		User32.INSTANCE.SetLayeredWindowAttributes(HWND_MINE, 0, (byte)((int)($alpha * 255) & 0xFF), User32.LWA_ALPHA);
	}

	private void setWindowPos(int x, int y, boolean override) {
		if (HWND_MINE == null) return;
		if (getHWndLoopCtrl.isExecutable(Gdx.graphics.getDeltaTime())) {
			refreshMonitorInfo();
			HWND new_hwnd_topmost = refreshWindowIdx();
			if (new_hwnd_topmost != HWND_TOPMOST) {
				HWND_TOPMOST = new_hwnd_topmost;
				if (x == WD_poscur.x && y == WD_poscur.y)
					return;
			}
		}
		if (override) {
			setWindowPosTar(x, y);
			WD_poscur.x = WD_postar.x;
			WD_poscur.y = WD_postar.y;
			WD_poseas.eX.curDuration = WD_poseas.eX.DURATION;
			WD_poseas.eY.curDuration = WD_poseas.eY.DURATION;
		}
		User32.INSTANCE.SetWindowPos(HWND_MINE, HWND_TOPMOST,
				(int) WD_poscur.x, (int) WD_poscur.y,
				WD_W, WD_H, WinUser.SWP_NOACTIVATE
		);
	}

	private HWND refreshWindowIdx() {
		ArrayList<HWndCtrl> windowList = HWndCtrl.getWindowList(true);
		//Logger.debug("HWND", windowList.toString());
		HWND minWindow = null;
		HashMap<Integer, HWndCtrl> line = new HashMap<>();
		int myPos = (int)(WD_poscur.x + WD_W / 2);
		int minNum = 2048;
		int myNum = getArkPetsWindowNum(APP_TITLE);
		final float quantityProduct = 1;
		if (plane != null) {
			// Reset plane additions.
			plane.barriers.clear();
			plane.pointCharges.clear();
		}
		for (HWndCtrl hWndCtrl : windowList) {
			int wndNum = getArkPetsWindowNum(hWndCtrl.windowText);
			// Distinguish non-peer windows from peers.
			if (wndNum == -1){
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
					minWindow = hWndCtrl.hWnd;
				}
			}
			// Window iteration end.
		}
		if (minWindow == null) {
			// Set as the top window if there is no peer.
			minWindow = new HWND(Pointer.createConstant(-1));
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

	/* WINDOW OPERATION RELATED */
	private void walkWindow(float len) {
		float expectedLen = len * WD_SCALE * (30f / APP_FPS);
		int realLen = randomRound(expectedLen);
		plane.changePosition(Gdx.graphics.getDeltaTime(), WD_postar.x + realLen, - (WD_postar.y + WD_H));
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

	private void setWindowPosTar(float $pos_x, float $pos_y) {
		WD_postar.set($pos_x, $pos_y);
		WD_poseas.eX.update($pos_x);
		WD_poseas.eY.update($pos_y + OFFSET_Y);
	}

	private void setWindowPosCur(float $deltaTime) {
		WD_poscur.set(WD_poseas.eX.step($deltaTime), WD_poseas.eY.step($deltaTime));
		setWindowPos((int)WD_poscur.x, (int)WD_poscur.y, false);
	}

	public void setWindowAlphaTar(float $alpha) {
		WD_alpha.update($alpha);
	}

	private void setWindowAlphaCur(float $deltaTime) {
		if (WD_alpha.curValue == WD_alpha.TO) return;
		WD_alpha.step($deltaTime);
		setWindowAlpha(WD_alpha.curValue);
	}


	/* UTILS */
	public static class LoopCtrl {
		private final float minIntervalTime;
		private float accumTime;

		/** Loop Controller instance.
		 * @param $minIntervalTime The minimal interval time for each loop.
		 */
		public LoopCtrl(float $minIntervalTime) {
			minIntervalTime = $minIntervalTime;
		}

		/** Query whether the loop is executable now.
		 * @param $deltaTime The delta time.
		 * @return true=okay.
		 */
		public boolean isExecutable(float $deltaTime) {
			accumTime += $deltaTime;
			if (accumTime >= minIntervalTime) {
				accumTime = 0;
				return true;
			} else {
				return false;
			}
		}
	}
}
