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
	public Behavior behavior;

	private HWND HWND_TOPMOST;
	private LoopCtrl regetHWndLoopCtrl;

	private int APP_FPS = 30;
	private final int WD_ORI_W = 140; // Window Origin Width
	private final int WD_ORI_H = 160; // Window Origin Height
	private float WD_SCALE; // Window Scale
	private int WD_W; // Window Real Width
	private int WD_H; // Window Real Height
	private int SCR_W; // Screen Width
	private int SCR_H; // Screen Height

	public Vector2 WD_poscur; // Window Current Position
	public Vector2 WD_postar; // Window Target Position
	public EasingLinearVector2 WD_poseas; // Window Postion Easing
	public int OFFSET_Y = 0;

	public ArkPets(String $title) {
		APP_TITLE = $title;
	}

	@Override
	public void create() {
		// When the APP was created
		Gdx.app.setLogLevel(3);
		Gdx.app.log("event", "AP:Create");
		config = ArkConfig.init();
		ScreenUtils.clear(0, 0, 0, 0);
		// Window setup
		WD_poscur = new Vector2(0, 0);
		WD_postar = new Vector2(0, 0);
		WD_poseas = new EasingLinearVector2(new EasingLinear(0, 1, 0.2f));
		WD_SCALE = config.display_scale;
		WD_W = (int) (WD_SCALE * WD_ORI_W);
		WD_H = (int) (WD_SCALE * WD_ORI_H);
		SCR_W = (int) config.display_monitor_info[0];
		SCR_H = (int) config.display_monitor_info[1];
		APP_FPS = config.display_fps;
		regetHWndLoopCtrl = new LoopCtrl(1 / APP_FPS * 4);
		intiWindow(100, SCR_H / 2);
		setWindowPosTar(100, SCR_H / 2);
		// Plane setup
		plane = new Plane(SCR_W, config.display_margin_bottom-SCR_H, SCR_H * 0.75f);
		plane.setFrict(SCR_W * 0.05f, SCR_W * 0.25f);
		plane.setBounce(0);
		plane.setObjSize(WD_W, -WD_H);
		plane.setSpeedLimit(SCR_W * 0.5f, SCR_H * 1f);
		plane.changePosition(0, WD_postar.x, -WD_postar.y);
		Gdx.graphics.setForegroundFPS(APP_FPS);
		Gdx.input.setInputProcessor(this);
		// Character setup
		cha = new ArkChar(config.character_recent+".atlas", config.character_recent+".skel", 0.33f);
		cha.setCanvas(WD_ORI_W, WD_ORI_H, APP_FPS);
		// Behavior setup
		if (BehaviorOperBuild2.match(cha.anim_list))
			behavior = new BehaviorOperBuild2(config);
		else if (BehaviorOperBuild.match(cha.anim_list))
			behavior = new BehaviorOperBuild(config);
		else
			behavior = null; // TODO Throw an error.
		Gdx.app.log("info", "AP:Use "+behavior.getClass().getName());
		cha.setAnimation(behavior.defaultAnim());
		Gdx.app.log("event", "AP:Render");
	}

	@Override
	public void render() {
		// When render graphics
		cha.next();
		if (cha.anim_frame.F_CUR == cha.anim_frame.F_MAX) {
			Gdx.app.log("info", String.valueOf("FPS"+Gdx.graphics.getFramesPerSecond()+", Heap"+(int)(Gdx.app.getJavaHeap()/1024)+"KB"));
		}
		AnimCtrl newAnim = behavior.autoCtrl(Gdx.graphics.getDeltaTime());
		if (!mouse_drag) {
			// If no dragging:
			plane.updatePosition(Gdx.graphics.getDeltaTime());
			// System.out.println((int)plane.getX()+"\t"+(int)-plane.getY());
			setWindowPosTar(plane.getX(), -plane.getY());
			setWindowPosCur(Gdx.graphics.getDeltaTime());
			if (cha.anim_queue[0].MOBILITY != 0)
				walkWindow(0.85f * cha.anim_queue[0].MOBILITY);
		} else {
			newAnim = behavior.dragStart();
		}
		if (plane.getDropping())
			newAnim = behavior.defaultAnim();
		if (plane.getDropped())
			newAnim = behavior.drop();
		changeAnimation(newAnim);
	}

	@Override
	public void dispose() {
		Gdx.app.log("event", "AP:Dispose");
	}

	private void changeAnimation(AnimCtrl animCtrl) {
		if (animCtrl != null) {
			// If need to change animation:
			if (cha.setAnimation(animCtrl))
				OFFSET_Y = (int)(animCtrl.OFFSET_Y * config.display_scale);
		}
	}


	/* INPUT PROCESS */
	private Vector2 mouse_pos = new Vector2();
	private boolean mouse_drag = false;

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		mouse_pos.set(screenX, screenY);
		if (button != Input.Buttons.LEFT || pointer > 0)
			return false;
		Gdx.app.debug("debug", "C↓: "+screenX + ", " + screenY);
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
		int x = (int)(WD_poscur.x + screenX - mouse_pos.x);
		int y = (int)(WD_poscur.y + screenY - mouse_pos.y);
		setWindowPos(x, y, true);
		plane.changePosition(Gdx.graphics.getDeltaTime(), x, -y);
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {	
		if (!mouse_drag) {
			changeAnimation(behavior.clickEnd());
		}
		mouse_drag = false;
		if (button != Input.Buttons.LEFT || pointer > 0)
			return false;
		Gdx.app.debug("debug", "C↑: "+screenX + ", " + screenY);
		return true;
	}

	@Override
	public boolean keyDown(int keycode) {
		Gdx.app.debug("debug", "K↓: "+keycode);
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		Gdx.app.debug("debug", "K↑: "+keycode);
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
	private HWND HWND_MINE;

	private boolean intiWindow(int x, int y) {
		if (HWND_MINE == null)
            HWND_MINE = User32.INSTANCE.FindWindow(null, APP_TITLE);
            if (HWND_MINE == null)
                return false;
		HWND_TOPMOST = refreshWindowIdx();
		//final int WL_TRAN_ON = 262160;
		//final int WL_TRAN_OFF = User32.INSTANCE.GetWindowLong(HWND_MINE, WinUser.GWL_EXSTYLE)
		//		| WinUser.WS_EX_LAYERED | WinUser.WS_EX_TRANSPARENT;
		if (HWND_MINE == null || HWND_TOPMOST == null)
			return false;
		//System.out.println(User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_EXSTYLE));
		//User32.INSTANCE.SetWindowLong(HWND_MINE, WinUser.GWL_EXSTYLE, enable ? WL_TRAN_ON : WL_TRAN_OFF);
		User32.INSTANCE.SetWindowPos(HWND_MINE, HWND_TOPMOST, x, y,
				WD_W, WD_H, WinUser.SWP_FRAMECHANGED);
		User32.INSTANCE.SetWindowPos(HWND_MINE, HWND_TOPMOST, x, y,
				WD_W, WD_H, WinUser.SWP_NOSIZE);
		return true;
	}

	private boolean setWindowPos(int x, int y, boolean override) {
		if (HWND_MINE == null)
			return false;
		if (regetHWndLoopCtrl.isExecutable(Gdx.graphics.getDeltaTime())) {
			HWND new_hwnd_topmost = refreshWindowIdx();
			if (new_hwnd_topmost != HWND_TOPMOST || HWND_TOPMOST == null) {
				HWND_TOPMOST = new_hwnd_topmost;
				if (x == WD_poscur.x && y == WD_poscur.y)
					return false;
			}
		}
		WD_poscur.set(
				x > 0 ? (x < SCR_W - WD_W ? x : SCR_W - WD_W) : 0,
				y > 0 ? (y < SCR_H - WD_H + OFFSET_Y ? y : SCR_H - WD_H + OFFSET_Y) : 0
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
		for (HWndCtrl hWndCtrl : windowList) {
			// Find windows as ground
			if ((getArkPetsWindowNum(hWndCtrl.windowText) == -1) && hWndCtrl.posLeft <= myPos && myPos <= hWndCtrl.posRight) {
				// This window IS in the vertical line that the app lies.
				if (hWndCtrl.posBottom > 0 && hWndCtrl.posTop < SCR_H) {
					for (int h = hWndCtrl.posTop<0?0:hWndCtrl.posTop; h < (hWndCtrl.posBottom>SCR_H?SCR_H:hWndCtrl.posBottom); h++) {
						if (line[h] == null)
							line[h] = (h == hWndCtrl.posTop) ? hWndCtrl : new HWndCtrl();
					}
				}
			}
			// Find the last peer window.
			if (getArkPetsWindowNum(hWndCtrl.windowText) > myNum && getArkPetsWindowNum(hWndCtrl.windowText) < minNum) {
				minNum = getArkPetsWindowNum(hWndCtrl.windowText);
				minWindow = hWndCtrl.hWnd;
			}
		}
		minWindow = (minWindow == null) ? new HWND(Pointer.createConstant(-1)) : minWindow; // Set as the top window if there is no peer.
		if (plane != null) {
			// Reset barriers
			plane.barriers.clear();
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
							return Integer.valueOf(title.substring(prefix.length()+prefix2.length(), title.length()-suffix.length()));
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
		//Gdx.app.debug("walk", expectedLen+" -> "+realLen);
		plane.changePosition(Gdx.graphics.getDeltaTime(), WD_postar.x + realLen, -WD_postar.y);
	}

	private int randomRound(float val) {
		int integer = (int)val;
		float decimal = val - integer;
		int offset = Math.abs(decimal) >= Math.random() ? (val >= 0 ? 1 : -1) : 0;
		return integer + offset;
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
