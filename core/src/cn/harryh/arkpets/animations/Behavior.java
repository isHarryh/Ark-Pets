/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import cn.harryh.arkpets.animations.StochasticMatrix.StochasticState;
import cn.harryh.arkpets.utils.Cached;


abstract public class Behavior {
    protected final Cached<AnimData> actionAutoGetter;
    protected StochasticMatrix currentMatrix;
    protected StochasticState currentState;

    private static final double minAnimCacheAge = 0.5;

    /** Character Behavior Controller Instance.
     */
    public Behavior() {
        actionAutoGetter = new Cached<>();
        actionAutoGetter.setValueProducer(() -> {
            StochasticState newState = currentMatrix.transitedAnimOf(currentState);
            if (newState == null) {
                return currentMatrix.getStateAnim(currentState);
            } else {
                currentState = newState;
                return currentMatrix.getStateAnim(newState);
            }
        });
        actionAutoGetter.setCacheAgeProducer(() -> {
            AnimData cache = actionAutoGetter.getCachedValue();
            return cache == null ? minAnimCacheAge : Math.max(minAnimCacheAge, cache.animClip().duration);
        });
        currentMatrix = null;
        currentState = null;
    }

    /** Checks whether the random animation is expired or empty.
     * @return True if expired or empty, false otherwise.
     */
    public final boolean isAutoAnimExpired() {
        return actionAutoGetter.isExpired();
    }

    /** Gets a random animation. This method has caching mechanism.
     * @return AnimData object.
     */
    public final AnimData autoAnim() {
        return actionAutoGetter.getValue();
    }

    /** Gets the next animation.
     * @return AnimData object.
     */
    public final AnimData nextAnim() {
        if (currentMatrix.isAllDisabled()) return null;
        AnimData newAnim = currentMatrix.nextAnimOf(currentState);
        currentState = currentState.next();
        return newAnim;
    }

    /** Gets the previous animation.
     * @return Animation data, or {@code null} if not available.
     */
    public final AnimData prevAnim() {
        if (currentMatrix.isAllDisabled()) return null;
        AnimData newAnim = currentMatrix.prevAnimOf(currentState);
        currentState = currentState.prev();
        return newAnim;
    }

    /** Gets the default animation.
     * @return Animation data, or {@code null} if not available.
     */
    public AnimData defaultAnim() {
        return null;
    }

    /** Gets the walk animation.
     * @param mobility 1=GoRight, -1=GoLeft.
     * @return Animation data, or {@code null} if not available.
     */
    public AnimData walkAnim(int mobility) {
        return null;
    }

    /** Gets the animation when mouse-down.
     * @return Animation data, or {@code null} if not available.
     */
    public AnimData clickStart() {
        return null;
    }

    /** Gets the animation when mouse-up.
     * @return Animation data, or {@code null} if not available.
     */
    public AnimData clickEnd() {
        return null;
    }

    /** Gets the animation when the user starts dragging.
     * @return Animation data, or {@code null} if not available.
     */
    public AnimData dragging() {
        return null;
    }

    /** Gets the animation when character dropped.
     * @return Animation data, or {@code null} if not available.
     */
    public AnimData dropped() {
        return null;
    }

    public int[][] getDebugMatrix() {
        return currentMatrix.getDebugMatrix();
    }

    public StochasticState getCurrentMatrixState() {
        return currentState;
    }
}
