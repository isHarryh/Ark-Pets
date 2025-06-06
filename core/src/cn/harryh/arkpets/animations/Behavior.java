/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import cn.harryh.arkpets.utils.Cached;

import java.util.Arrays;


abstract public class Behavior {
    protected AnimClipGroup animList;
    protected AnimDataWeight[] actionList;
    protected Cached<AnimData> actionAutoGetter;
    private int idxRec;

    private static final double minAnimCacheAge = 0.5;

    /** Character Behavior Controller Instance.
     * @param animList The animation clip list.
     */
    public Behavior(AnimClipGroup animList) {
        actionList = null;
        this.animList = animList;
        actionAutoGetter = new Cached<>() {
            @Override
            protected AnimData produce() {
                return getRandomAction();
            }

            @Override
            protected double cacheAge() {
                if (getCachedValue() == null)
                    return minAnimCacheAge;
                return Math.max(minAnimCacheAge, getCachedValue().animClip().duration);
            }
        };
        idxRec = 0;
    }

    /** Gets a random animation.
     * @return AnimData object.
     */
    public final AnimData autoAnim() {
        return actionAutoGetter.getValue();
    }

    /** Gets the next animation.
     * @return AnimData object.
     */
    public final AnimData nextAnim() {
        if (actionList.length > 0) {
            idxRec = idxRec >= actionList.length - 1 ? 0 : idxRec + 1;
            return actionList[idxRec].anim();
        }
        return new AnimData(null);
    }

    /** Gets the previous animation.
     * @return AnimData object.
     */
    public final AnimData prevAnim() {
        if (actionList.length > 0) {
            idxRec = idxRec <= 0 ? actionList.length - 1 : idxRec - 1;
            return actionList[idxRec].anim();
        }
        return new AnimData(null);
    }

    private AnimData getRandomAction() {
        if (actionList.length > 0) {
            // Calculate the sum of all action's weight
            int weightSum = Arrays.stream(actionList).mapToInt(AnimDataWeight::weight).sum();
            // Random select a weight
            int weightSelect = (int) Math.ceil(Math.random() * weightSum);
            // Figure out which action is selected
            int weight = 0;
            for (AnimDataWeight animDataWeight : actionList) {
                weight += animDataWeight.weight();
                if (weightSelect <= weight)
                    return animDataWeight.anim();
            }
        }
        return new AnimData(null);
    }

    /** Gets the default animation.
     * @return AnimData object.
     */
    public AnimData defaultAnim() {
        return new AnimData(null);
    }

    /** Gets the walk animation.
     * @param mobility 1=GoRight, -1=GoLeft.
     * @return AnimData object.
     */
    public AnimData walkAnim(int mobility) {
        return new AnimData(null);
    }

    /** Gets the animation when mouse-down.
     * @return AnimData object.
     */
    public AnimData clickStart() {
        return new AnimData(null);
    }

    /** Gets the animation when mouse-up.
     * @return AnimData object.
     */
    public AnimData clickEnd() {
        return new AnimData(null);
    }

    /** Gets the animation when the user starts dragging.
     * @return AnimData object.
     */
    public AnimData dragging() {
        return new AnimData(null);
    }

    /** Gets the animation when character dropped.
     * @return AnimData object.
     */
    public AnimData dropped() {
        return new AnimData(null);
    }
}
