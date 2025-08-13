package cn.harryh.arkpets.behavior;

import java.util.EnumSet;


public class StateStore {
    private final EnumSet<State> state;
    private final EnumSet<State> mask;

    public StateStore() {
        this.state = EnumSet.noneOf(State.class);
        this.mask = EnumSet.noneOf(State.class);
    }

    public void set(State state) {
        if(getMask(state)) return;
        this.state.add(state);
    }

    public void clear(State state) {
        this.state.remove(state);
    }

    public boolean get(State state) {
        return this.state.contains(state);
    }

    public void mask(State state) {
        this.mask.add(state);
    }

    public void unmask(State state) {
        this.mask.remove(state);
    }

    public boolean getMask(State state) {
        return this.mask.contains(state);
    }

    @Override
    public String toString() {
        return "State state=%s mask=%s".formatted(state.toString(), mask.toString());
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof StateStore that)) return false;

        return state.equals(that.state) && mask.equals(that.mask);
    }

    @Override
    public int hashCode() {
        int result = state.hashCode();
        result = 31 * result + mask.hashCode();
        return result;
    }
}
