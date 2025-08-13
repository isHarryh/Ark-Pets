package cn.harryh.arkpets.behavior;


import cn.harryh.arkpets.utils.Logger;


public class VoiceBehavior {
    public StateStore state;

    public VoiceBehavior(StateStore data) {
        this.state =data;
    }

    public String run() {
        String voice = null;
        // Special Event
        if (state.get(State.FIRST_LANDED)) {
            state.clear(State.FIRST_LANDED);
            state.mask(State.FIRST_LANDED);
            voice = Math.random() > 0.5f ? "CN_042" : "CN_037";
        }
        // Interact Event
        if (state.get(State.CLICKED)) {
            state.clear(State.CLICKED); // clean
            voice = Math.random() > 0.7f ? "CN_036" : "CN_034";
        }
        // Idle Event
        if (state.get(State.SLEEPING) && Math.random() > 0.65f) {
            state.clear(State.SLEEPING);
            state.mask(State.SLEEPING);
            voice = "CN_010";
        }
        Logger.debug("Audio","Select Voice "+voice);
        return voice;
    }
}
