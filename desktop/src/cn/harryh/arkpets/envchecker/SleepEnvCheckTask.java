package cn.harryh.arkpets.envchecker;

public class SleepEnvCheckTask extends EnvCheckTask {
    private final int time;

    public SleepEnvCheckTask(int time) {
        super();
        this.time = time;
    }

    @Override
    public boolean run() {
        try {
            Thread.sleep(time);
        } catch (Exception ignored) {
        }
        return true;
    }

    @Override
    public String getFailureReason() {
        return "";
    }

    @Override
    public String getFailureDetail() {
        return "";
    }

    @Override
    public boolean tryFix() {
        return true;
    }

    @Override
    public boolean canFix() {
        return true;
    }
}
