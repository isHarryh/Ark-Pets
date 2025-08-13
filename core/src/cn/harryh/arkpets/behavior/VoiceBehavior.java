package cn.harryh.arkpets.behavior;


import cn.harryh.arkpets.utils.Logger;

import java.util.Iterator;


public class VoiceBehavior {
    private final StateStore state;

    public VoiceBehavior(StateStore data) {
        this.state = data;
    }

    public String run() {
        for (Iterator<State> it = state.getStateIter(); it.hasNext(); ) {
            State s = it.next();
            Logger.debug("Audio","Running State "+s);
            String voice = s.run();
            if(s.isOneShot()) state.mask(s);
            state.clear(s);
            Logger.debug("Audio","Select Voice "+voice);
            return voice;
        }
        return null;
    }
}
