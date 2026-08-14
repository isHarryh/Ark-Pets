/** Copyright (c) 2022-2026, Harry Huang, Litwak913
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import java.util.stream.IntStream;


/** The stochastic matrix (markov matrix) to manage the transition of auto-played animations in a natural way.
 */
public class StochasticMatrix {
    protected final StochasticMatrixRow[] weights;
    protected final boolean[] disabled;
    protected final AnimData[] binds;

    public static int[][] DEFAULT_WEIGHTS = new int[][]{
            // IDLE SIT SLEEP MOVE_L MOVE_R SPECIAL
            {40, 20, 10, 10, 10, 10}, // IDLE -> ?
            {30, 40, 20, 10, 10, 10}, // SIT -> ?
            {20, 20, 60, 0, 0, 0}, // SLEEP -> ?
            {40, 10, 0, 20, 20, 10}, // MOVE_L -> ?
            {40, 10, 0, 20, 20, 10}, // MOVE_R -> ?
            {50, 20, 10, 10, 10, 0} // SPECIAL -> ?
    };

    /** One stochastic state corresponding to an auto-played animation.
     */
    public enum StochasticState {
        IDLE,
        SIT,
        SLEEP,
        MOVE_L,
        MOVE_R,
        SPECIAL;

        public StochasticState next() {
            int ord = (ordinal() + 1) % StochasticState.values().length;
            return StochasticState.values()[ord];
        }

        public StochasticState prev() {
            int ord = (ordinal() - 1 + StochasticState.values().length) % StochasticState.values().length;
            return StochasticState.values()[ord];
        }
    }

    /** One row of the stochastic matrix. Each element represents the weight of transition to the corresponding state.
     * @param weights The weights array of the states.
     * @param disabledRef The reference to the disabled states array. If a state is disabled, its weight is ignored.
     */
    public record StochasticMatrixRow(int[] weights, boolean[] disabledRef) {
        public StochasticMatrixRow {
            if (weights.length != StochasticState.values().length)
                throw new IllegalArgumentException("Weights length mismatch");
        }

        public StochasticState random() {
            int sum = IntStream.range(0, weights.length).filter(i -> !disabledRef[i]).map(i -> weights[i]).sum();
            int rnd = (int) (Math.random() * sum);
            int acc = 0;
            for (int i = 0; i < weights.length; i++) {
                if (disabledRef[i])
                    continue;
                acc += weights[i];
                if (rnd < acc)
                    return StochasticState.values()[i];
            }
            return null;
        }
    }

    /** Initializes the stochastic matrix with given weights.
     * @param weights The weights 2D array. Each row corresponds to a state,
     *                and each column corresponds to the weight of transition to another state.
     */
    public StochasticMatrix(int[][] weights) {
        if (weights.length != StochasticState.values().length)
            throw new IllegalArgumentException("Weights length mismatch");
        this.weights = new StochasticMatrixRow[weights.length];
        this.disabled = new boolean[StochasticState.values().length];
        this.binds = new AnimData[StochasticState.values().length];
        for (int i = 0; i < weights.length; i++)
            this.weights[i] = new StochasticMatrixRow(weights[i], this.disabled);
    }

    public AnimData nextAnimOf(StochasticState state) {
        StochasticState newState = state;
        for (int i = 0; i < StochasticState.values().length; i++) {
            newState = newState.next();
            if (!disabled[newState.ordinal()])
                return binds[newState.ordinal()];
        }
        return binds[state.ordinal()];
    }

    public AnimData prevAnimOf(StochasticState state) {
        StochasticState newState = state;
        for (int i = 0; i < StochasticState.values().length; i++) {
            newState = newState.prev();
            if (!disabled[newState.ordinal()])
                return binds[newState.ordinal()];
        }
        return binds[state.ordinal()];
    }

    public StochasticState transitedAnimOf(StochasticState state) {
        return weights[state.ordinal()].random();
    }

    public AnimData getStateAnim(StochasticState state) {
        return binds[state.ordinal()];
    }

    public void bind(StochasticState state, AnimData anim) {
        binds[state.ordinal()] = anim;
    }

    public void scale(StochasticState state, float factor) {
        if (factor < 0)
            throw new IllegalArgumentException("Scale factor cannot be positive");
        int ord = state.ordinal();
        for (int i = 0; i < weights[ord].weights.length; i++)
            weights[ord].weights[i] = Math.round(weights[ord].weights[i] * factor);
    }

    public void disable(StochasticState state) {
        disabled[state.ordinal()] = true;
    }

    public boolean isAllDisabled() {
        for (boolean d : disabled)
            if (!d)
                return false;
        return true;
    }

    public int[][] getDebugMatrix() {
        int[][] matrix = new int[StochasticState.values().length][StochasticState.values().length];
        for (int i = 0; i < weights.length; i++) {
            StochasticMatrixRow row = weights[i];
            for (int j = 0; j < row.weights.length; j++) {
                if (row.disabledRef[j]) matrix[i][j] = -row.weights[j];
                else matrix[i][j] = row.weights[j];
            }
        }
        return matrix;
    }
}
