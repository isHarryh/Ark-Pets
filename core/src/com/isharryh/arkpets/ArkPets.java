/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import java.util.ArrayList;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinDef.HWND;

import com.isharryh.arkpets.behaviors.*;
import com.isharryh.arkpets.utils.AnimCtrl;
import com.isharryh.arkpets.utils.HWndCtrl;
import com.isharryh.arkpets.utils.LoopCtrl;
import com.isharryh.arkpets.utils.Plane;
import com.isharryh.arkpets.easings.EasingLinear;
import com.isharryh.arkpets.easings.EasingLinearVector2;


public class ArkPets extends ApplicationAdapter implements InputProcessor {
	/* RENDER PROCESS */
	public Plane plane;
	public ArkChar cha;
	public ArkConfig config;
	public ArkTray tray;
	public Behavior behavior;

	private HWND HWND_TOPMOST;
	private LoopCtrl getHWndLoopCtrl;

	private int APP_FPS = 30;
	private float WD_SCALE; // Window Scale
	private int WD_W; // Window Real Width
	private int WD_H; // Window Real Height
	private int SCR_W; // Screen Width
	private int SCR_H; // Screen Height

	public Vector2 WD_poscur; // Window Current Position
	public Vector2 WD_postar; // Window Target Position
	public EasingLinearVector2 WD_poseas; // Window Postion Easing
	private int OFFSET_Y = 0;

	public ArkPets(String $title) {
		APP_TITLE = $title;
	}

	@Override
	public void create() {
		// When the APP was created
		// 1.App setup
		Gdx.app.setLogLevel(3);
		Gdx.app.log("event", "AP:Create");
		Gdx.input.setInputProcessor(this);
		config = ArkConfig.init();
		ScreenUtils.clear(0, 0, 0, 0);
		// 2.Window setup
		WD_poscur = new Vector2(0, 0);
		WD_postar = new Vector2(0, 0);
		WD_poseas = new EasingLinearVector2(new EasingLinear(0, 1, 0.2f));
		WD_SCALE = config.display_scale;
		int WD_ORI_W = 140; // Window Origin Width
		int WD_ORI_H = 160; // Window Origin Height
		WD_W = (int) (WD_SCALE * WD_ORI_W);
		WD_H = (int) (WD_SCALE * WD_ORI_H);
		SCR_W = config.display_monitor_info[0];
		SCR_H = config.display_monitor_info[1];
		APP_FPS = config.display_fps;
		getHWndLoopCtrl = new LoopCtrl(1f / APP_FPS * 4);
		intiWindow(100, SCR_H / 2);
		setWindowPosTar(100, SCR_H / 2f);
		Gdx.graphics.setForegroundFPS(APP_FPS);
		// 3.Plane setup
		plane = new Plane(SCR_W, config.display_margin_bottom-SCR_H, SCR_H * 0.75f);
		plane.setFrict(SCR_W * 0.05f, SCR_W * 0.25f);
		plane.setBounce(0);
		plane.setObjSize(WD_W, -WD_H);
		plane.setSpeedLimit(SCR_W * 0.5f, SCR_H * 1f);
		plane.changePosition(0, WD_postar.x, -WD_postar.y);
		// 4.Character setup
		cha = new ArkChar(config.character_recent+".atlas", config.character_recent+".skel", 0.33f);
		cha.setCanvas(WD_ORI_W, WD_ORI_H, APP_FPS);
		// 5.Behavior setup
		if (BehaviorOperBuild2.match(cha.anim_list))
			behavior = new BehaviorOperBuild2(config);
		else if (BehaviorOperBuild.match(cha.anim_list))
			behavior = new BehaviorOperBuild(config);
		else if (BehaviorOperBuild3.match(cha.anim_list))
			behavior = new BehaviorOperBuild3(config);
		else {
			Gdx.app.error("error", "AP:No suitable ArkPets behavior instance found.");
			Gdx.app.exit();
		}
		Gdx.app.log("info", "AP:Use "+behavior.getClass().getName());
		cha.setAnimation(behavior.defaultAnim());
		// 6.Tray icon setup
		tray = new ArkTray(this);
		// Setup complete
		Gdx.app.log("event", "AP:Render");
	}

	@Override
	public void render() {
		// 1.Render the next frame.
		cha.next();
		if (cha.anim_frame.F_CUR == cha.anim_frame.F_MAX) {
			Gdx.app.log("info", "FPS" + Gdx.graphics.getFramesPerSecond() + ", Heap" + (int) (Gdx.app.getJavaHeap() / 1024) + "KB");
		}

		// 2.Select a new anim.
		AnimCtrl newAnim = behavior.autoCtrl(Gdx.graphics.getDeltaTime()); // AI anim.
		if (!mouse_drag) { // If no dragging:
			plane.updatePosition(Gdx.graphics.getDeltaTime());
			setWindowPosTar(plane.getX(), -plane.getY());
			setWindowPosCur(Gdx.graphics.getDeltaTime());
			if (cha.anim_queue[0].MOBILITY != 0) {
				if (willReachBorder(cha.anim_queue[0].MOBILITY)) {
					// Turn around if auto-walk cause the collision from screen border.
					newAnim = cha.anim_queue[0];
					newAnim = new AnimCtrl(newAnim.ANIM_NAME, newAnim.LOOP, newAnim.INTERRUPTABLE, newAnim.OFFSET_Y, -newAnim.MOBILITY);
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
	}

	@Override
	public void dispose() {
		Gdx.app.log("event", "AP:Dispose");
		tray.remove();
	}

	private void changeAnimation(AnimCtrl animCtrl) {
		if (animCtrl != null) {
			// If it is needed to change animation:
			if (cha.setAnimation(animCtrl))
				OFFSET_Y = (int)(animCtrl.OFFSET_Y * config.display_scale);
		}
	}


	/* INPUT PROCESS */
	private final Vector2 mouse_pos = new Vector2();
	private int mouse_intention_x = 1;
	private boolean mouse_drag = false;

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		mouse_pos.set(screenX, screenY);
		if (button != Input.Buttons.LEFT || pointer > 0)
			return false;
		Gdx.app.debug("debug", "C???: "+screenX + ", " + screenY);
		cha.setAnimation(behavior.clickStart());
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		mouse_drag = true;
		int t = (int)Math.signum(screenX - mouse_pos.x);
		mouse_intention_x = t == 0 ? mouse_intention_x : t;
		int x = (int)(WD_poscur.x + screenX - mouse_pos.x);
		int y = (int)(WD_poscur.y + screenY - mouse_pos.y);
		setWindowPos(x, y, true);
		plane.changePosition(Gdx.graphics.getDeltaTime(), x, -y);
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {	
		if (!mouse_drag)
			changeAnimation(behavior.clickEnd());
		else {
			cha.setPositionTar(cha.positionTar.x, cha.positionTar.y, mouse_intention_x);
			if (tray.keepAnim != null && cha.anim_queue[0].MOBILITY != 0) {
				AnimCtrl newAnim = cha.anim_queue[0];
				newAnim = new AnimCtrl(newAnim.ANIM_NAME, newAnim.LOOP, newAnim.INTERRUPTABLE, newAnim.OFFSET_Y, Math.abs(newAnim.MOBILITY) * mouse_intention_x);
				tray.keepAnim = newAnim;
			}
		}
		mouse_drag = false;
		if (button != Input.Buttons.LEFT || pointer > 0)
			return false;
		Gdx.app.debug("debug", "C???: "+screenX + ", " + screenY);
		return true;
	}

	@Override
	public boolean keyDown(int keycode) {
		Gdx.app.debug("debug", "K???: "+keycode);
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		Gdx.app.debug("debug", "K???: "+keycode);
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean scrolled(float a, float b) {
		return false;
	}

	/* WINDOW API */
	private final String APP_TITLE;
	public HWND HWND_MINE;

	private boolean intiWindow(int x, int y) {
		if (HWND_MINE == null)
            HWND_MINE = User32.INSTANCE.FindWindow(null, APP_TITLE);
		if (HWND_MINE == null)
			return false;
		HWND_TOPMOST = refreshWindowIdx();
		//final int WL_TRAN_ON = 262160;
		//final int WL_TRAN_OFF = User32.INSTANCE.GetWindowLong(HWND_MINE, WinUser.GWL_EXSTYLE)
		//		| WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
		//System.out.println(User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE));
		//User32.INSTANCE.SetWindowLong(HWND_MINE, WinUser.GWL_EXSTYLE, enable ? WL_TRAN_ON : WL_TRAN_OFF);
		User32.INSTANCE.SetWindowLong(HWND_MINE, WinUser.GWL_EXSTYLE, 0x00000088);
		User32.INSTANCE.SetWindowPos(HWND_MINE, HWND_TOPMOST, x, y,
				WD_W, WD_H, WinUser.SWP_FRAMECHANGED);
		User32.INSTANCE.SetWindowPos(HWND_MINE, HWND_TOPMOST, x, y,
				WD_W, WD_H, WinUser.SWP_NOSIZE);
		return true;
	}

	private boolean setWindowPos(int x, int y, boolean override) {
		if (HWND_MINE == null)
			return false;
		if (getHWndLoopCtrl.isExecutable(Gdx.graphics.getDeltaTime())) {
			HWND new_hwnd_topmost = refreshWindowIdx();
			if (new_hwnd_topmost != HWND_TOPMOST) {
				HWND_TOPMOST = new_hwnd_topmost;
				if (x == WD_poscur.x && y == WD_poscur.y)
					return false;
			}
		}
		WD_poscur.set(
				x > 0 ? Math.min(x, SCR_W - WD_W) : 0,
				y > 0 ? Math.min(y, SCR_H - WD_H + OFFSET_Y) : 0
		);
		if (override) {
			setWindowPosTar(WD_poscur.x, WD_poscur.y);
			WD_poseas.eX.curValue = WD_poscur.x;
			WD_poseas.eY.curValue = WD_poscur.y;
			WD_poseas.eX.curDuration = WD_poseas.eX.DURATION;
			WD_poseas.eY.curDuration = WD_poseas.eY.DURATION;
		}
		User32.INSTANCE.SetWindowPos(HWND_MINE, HWND_TOPMOST, (int)WD_poscur.x, (int)WD_poscur.y,
			0, 0, WinUser.SWP_NOSIZE);
		return true;
	}

	private HWND refreshWindowIdx() {
		ArrayList<HWndCtrl> windowList = HWndCtrl.getWindowList(true);
		HWND minWindow = null;
		HWndCtrl[] line = new HWndCtrl[SCR_H];
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
					// This window "is" in the vertical line that the app lies.
					if (hWndCtrl.posBottom > 0 && hWndCtrl.posTop < SCR_H) {
						// This window is "under" the app.
						for (int h = Math.max(hWndCtrl.posTop, 0); h < Math.min(hWndCtrl.posBottom, SCR_H); h++) {
							if (line[h] == null)
								line[h] = (h == hWndCtrl.posTop) ? hWndCtrl : new HWndCtrl(); // Record this window.
						}
					}
				}
			} else {
				if (config.behavior_do_peer_repulsion && wndNum != myNum && plane != null) {
					// Set point charges.
					plane.setPointCharge(-hWndCtrl.getCenterY(), hWndCtrl.getCenterX(), quantityProduct);
				}
				// Find the last peer window.
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
			// Set barriers.
			for (int h = WD_H; h < SCR_H; h++) {
				HWndCtrl temp = line[h];
				if (temp != null && temp.hWnd != null) {
					plane.setBarrier(-temp.posTop, 0, SCR_W, false);
					//System.out.println("Barrier at "+(-temp.posTop)+" is "+temp.windowText+" pos "+temp.posLeft+","+temp.posRight);
				}
			}
		}
		return minWindow; // Return the last peer window.
	}

	private int getArkPetsWindowNum(String title) {
		final String prefix = "ArkPets";
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
				Gdx.app.log("warning", e.toString());
				return -1;
			}
		}
		return -1;
	}

	/* WINDOW OPERATION RELATED */
	private void walkWindow(float len) {
		float expectedLen = len * WD_SCALE * (30f / APP_FPS);
		int realLen = randomRound(expectedLen);
		plane.changePosition(Gdx.graphics.getDeltaTime(), WD_postar.x + realLen, -WD_postar.y);
	}

	private int randomRound(float val) {
		int integer = (int)val;
		float decimal = val - integer;
		int offset = Math.abs(decimal) >= Math.random() ? (val >= 0 ? 1 : -1) : 0;
		return integer + offset;
	}

	private boolean willReachBorder(float len) {
		if (plane == null) return false;
		return (plane.getX() >= SCR_W-WD_W && len > 0) || (plane.getX() <= 0 && len < 0);
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
}
