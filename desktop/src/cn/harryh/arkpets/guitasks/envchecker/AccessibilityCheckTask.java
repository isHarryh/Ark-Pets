package cn.harryh.arkpets.guitasks.envchecker;

import cn.harryh.arkpets.natives.HIServices;
import cn.harryh.arkpets.utils.Logger;

import java.awt.*;
import java.net.URI;


public class AccessibilityCheckTask extends EnvCheckTask{
    @Override
    public String getFailureReason() {
        return "启用辅助功能";
    }

    @Override
    public String getFailureDetail() {
        return "ArkPets 的部分功能需要启用辅助功能权限才能使用。\n请在打开的“辅助功能”窗口中找到 ArkPets，并启用辅助功能权限。";
    }

    @Override
    public boolean tryFix() {
        return false;
    }

    @Override
    public boolean canFix() {
        return false;
    }

    @Override
    public boolean run() {
        if (!HIServices.INSTANCE.AXIsProcessTrusted()) {
            try {
                Desktop.getDesktop().browse(new URI("x-apple.systempreferences:com.apple.preference.security?Privacy_Accessibility"));
            } catch (Exception e) {
                Logger.error("Launcher", "Failed to open System Preferences Page");
            }
            return false;
        } else {
            return true;
        }
    }
}
