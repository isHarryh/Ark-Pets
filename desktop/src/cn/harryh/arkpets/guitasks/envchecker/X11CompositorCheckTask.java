package cn.harryh.arkpets.guitasks.envchecker;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.natives.X11Extension;
import cn.harryh.arkpets.natives.X11Helper;
import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.platform.unix.X11;


public class X11CompositorCheckTask extends EnvCheckTask{
    private String reason;
    private String detail;

    @Override
    public String getFailureReason() {
        return reason;
    }

    @Override
    public String getFailureDetail() {
        return detail;
    }

    @Override
    public boolean tryFix(ArkConfig cfg) {
        return false;
    }

    @Override
    public boolean canFix() {
        return false;
    }

    @Override
    public boolean run() {
        X11Extension x11 = X11Extension.INSTANCE;
        X11.Display dis = x11.XOpenDisplay(null);
        if (dis == null) {
            Logger.error("EnvCheck","Cannot open X display");
            reason = "无法连接到 X 显示";
            detail = "连接到 X 显示时失败。";
            return false;
        } else {
            Logger.info("EnvCheck", "Connected to X display");
        }
        int scr = x11.XDefaultScreen(dis);
        // todo netwm check
        X11.Atom a = X11Helper.getAtom(dis,String.format("_NET_WM_CM_S%d",scr));
        X11.Window w = x11.XGetSelectionOwner(dis,a);
        if (w == null) {
            reason = "未找到 X 合成器";
            detail = "当前系统中找不到运行中的 X 合成器，这将导致桌宠背景不透明。\n请尝试在窗口管理器的设置中启用合成功能，或运行一个合成器。";
            x11.XCloseDisplay(dis);
            return false;
        }
        x11.XCloseDisplay(dis);
        return true;
    }
}
