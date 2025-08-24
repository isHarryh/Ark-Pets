package cn.harryh.arkpets.behavior;

import java.util.function.Supplier;

import static cn.harryh.arkpets.Const.VoiceConfig.*;


public enum State {
    // todo adjust by trust
    FIRST_START(() -> reporting,true),
    SECOND_START(() -> enterFacility,true),
    AFTER_SCHEDULED_START(() -> Math.random() > 0.6f ? deployToTeam : appointLeader,true),
    AFTER_FAVORITE_START(() -> appointAssistant,true),
    FIRST_LANDED(() -> Math.random() > 0.5f ? greeting : title,false),
    CLICKED(() -> Math.random() > 0.7f ? trustTouch : tapOnce,false),
    SLEEPING(() -> Math.random() > 0.75f ? idle : null,false),
    IDLE(() -> {
        if (Math.random() > 0.85f)
            return talkLv1[(int) Math.floor((Math.random() * (talkLv1.length - 1)))];
        else return null;
    },false);

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
