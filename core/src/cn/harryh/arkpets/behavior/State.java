package cn.harryh.arkpets.behavior;

import java.util.function.Supplier;


public enum State {
    // todo adjust by trust
    FIRST_START(() -> "CN_011",true),
    SECOND_START(() -> "CN_033",true),
    AFTER_SCHEDULED_START(() -> Math.random() > 0.6f ? "CN_017" : "CN_018",true),
    AFTER_FAVORITE_START(() -> "CN_001",true),
    FIRST_LANDED(() -> Math.random() > 0.5f ? "CN_042" : "CN_037",false),
    CLICKED(() -> Math.random() > 0.7f ? "CN_036" : "CN_034",false),
    SLEEPING(() -> Math.random() > 0.6f ? "CN_010" : null,false);

    private final Supplier<String> action;
    private final boolean oneShot;

    State(Supplier<String> action,boolean oneShot) {
        this.action = action;
        this.oneShot = oneShot;
    }

    public String run() {
        return action.get();
    }

    public boolean isOneShot() {
        return oneShot;
    }
}
