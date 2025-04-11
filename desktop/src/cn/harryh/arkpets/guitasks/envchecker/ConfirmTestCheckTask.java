package cn.harryh.arkpets.guitasks.envchecker;

import cn.harryh.arkpets.ArkConfig;


public class ConfirmTestCheckTask extends EnvCheckTask {
    @Override
    public String getFailureReason() {
        return "";
    }

    @Override
    public String getFailureDetail() {
        return "";
    }

    @Override
    public boolean tryFix(ArkConfig cfg) {
        return true;
    }

    @Override
    public boolean needConfirmFix() {
        return true;
    }

    @Override
    public boolean canFix() {
        return true;
    }

    @Override
    public boolean run() {
        return false;
    }

    @Override
    public String getFixReason() {
        return "TestConfirm";
    }

    @Override
    public String getFixDetail() {
        return "TestConfirm";
    }
}
