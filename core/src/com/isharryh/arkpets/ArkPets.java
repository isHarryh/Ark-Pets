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

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.isharryh.arkpets.behaviors.*;
import com.isharryh.arkpets.utils.AnimCtrl;
import com.isharryh.arkpets.easings.EasingLinear;
import com.isharryh.arkpets.easings.EasingLinearVector2;


public class ArkPets extends ApplicationAdapter implements InputProcessor {
	/* RENDER PROCESS */
	public ArkChar cha;
	public ArkConfig config;
	public Behavior behavior;

	public final String APP_TITLE;
	public HWND HWND_MINE;
	private int APP_FPS = 30;
	private final int WD_ORI_W = 140; // Window Origin Width
	private final int WD_ORI_H = 160; // Window Origin Height
	private float WD_SCALE; // Window Scale
	public int WD_W; // Window Real Width
	public int WD_H; // Window Real Height
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
		ScreenUtils.clear(0, 0, 0, 0);
		ArkConfig config = ArkConfig.init();
		// Window Setup
		WD_poscur = new Vector2(0, 0);
		WD_postar = new Vector2(0, 0);
		WD_poseas = new EasingLinearVector2(new EasingLinear(0, 1, 0.2f));
		WD_SCALE = config.display_scale;
		WD_W = (int) (WD_SCALE * WD_ORI_W);
		WD_H = (int) (WD_SCALE * WD_ORI_H);
		APP_FPS = config.display_fps;
		intiWindow(100, config.display_monitor_info[1] / 2);
		setWindowPosTar(100, config.display_monitor_info[1] / 2);
		Gdx.graphics.setForegroundFPS(APP_FPS);
		Gdx.input.setInputProcessor(this);
		// Character setup
		cha = new ArkChar(config.character_recent+".atlas", config.character_recent+".skel", 0.36f);
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
		if (cha.anim_frame.F_CUR == cha.anim_frame.F_MAX)
			Gdx.app.log("info", String.valueOf("FPS"+Gdx.graphics.getFramesPerSecond()+", Heap"+(int)(Gdx.app.getJavaHeap()/1024)+"KB"));
		AnimCtrl newAnim = behavior.autoCtrl(Gdx.graphics.getDeltaTime());
		if (!mouse_drag) {
			// If no dragging:
			setWindowPosTar(WD_postar.x, WD_postar.y);
			setWindowPosCur(Gdx.graphics.getDeltaTime());
			if (cha.anim_queue[0].MOBILITY != 0)
				walkWindow(0.85f * cha.anim_queue[0].MOBILITY);
		} else {
			newAnim = behavior.dragStart();
		}
		
		if (newAnim != null) {
			// If need to change animation:
			if (newAnim.OFFSET_Y != OFFSET_Y) {
				OFFSET_Y = newAnim.OFFSET_Y;
			}
			cha.setAnimation(newAnim);
		}
	}

	@Override
	public void dispose() {
		Gdx.app.log("event", "AP:Dispose");
	}

	/* INPUT PROCESS */
	private Vector2 mouse_pos = new Vector2();
	private boolean mouse_drag = false;

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT || pointer > 0)
			return false;
		Gdx.app.debug("debug", "C↓: "+screenX + ", " + screenY);
		mouse_pos.set(screenX, screenY);
		mouse_drag = true;
		cha.setAnimation(behavior.clickStart());
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (!mouse_drag)
			return false;
		setWindowPos((int)(WD_poscur.x + screenX - mouse_pos.x), (int)(WD_poscur.y + screenY - mouse_pos.y), true);
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (button != Input.Buttons.LEFT || pointer > 0)
			return false;
			Gdx.app.debug("debug", "C↑: "+screenX + ", " + screenY);
		mouse_drag = false;
		cha.setAnimation(behavior.clickEnd());
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
	private boolean intiWindow(int x, int y) {
		if (HWND_MINE == null)
            HWND_MINE = User32.INSTANCE.FindWindow(null, APP_TITLE);
            if (HWND_MINE == null)
                return false;
		final HWND HWND_TOPMOST = new HWND(Pointer.createConstant(-1));
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

	private void walkWindow(float len) {
		float expectedLen = len * WD_SCALE * (30f / APP_FPS);
		int realLen = randomRound(expectedLen);
		//Gdx.app.debug("walk", expectedLen+" -> "+realLen);
		setWindowPosTar(WD_postar.x + realLen, WD_postar.y);
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

	private boolean setWindowPos(int x, int y, boolean override) {
		final HWND HWND_TOPMOST = new HWND(Pointer.createConstant(-1));
		if (HWND_MINE == null || HWND_TOPMOST == null)
			return false;
		WD_poscur.set(x > 0 ? x : 0, y > 0 ? y : 0);
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
}
