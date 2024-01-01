/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import cn.harryh.arkpets.ArkConfig;


abstract public class Behavior {
    protected AnimDataWeight[] action_list;
    protected AnimClipGroup anim_list;
    protected ArkConfig config;
    protected float deltaMin;
    protected float timeRec;
    protected float duraRec;
    protected int idxRec;

    /** Character Behavior Controller Instance.
     * @param config ArkConfig object.
     * @param animList The animation name list.
     */
    public Behavior(ArkConfig config, AnimClipGroup animList) {
        action_list = null;
        anim_list = animList;
        this.config = config;
        deltaMin = 0.5f;
        autoCtrlReset();
    }

    /** Gets a random animation.
     * @param deltaTime The delta time.
     * @return AnimData object.
     */
    public final AnimData autoCtrl(float deltaTime) {
        duraRec += deltaTime;
        timeRec += deltaTime;
        if (timeRec >= deltaMin) {
            timeRec = 0f;
            if (duraRec >= action_list[idxRec].duration()) {
                // Now try to change action
                duraRec = 0f;
                idxRec = getRandomAction();
                return action_list[idxRec].anim();
            }
        }
        return null;
    }

    /** Resets the random animation getter.
     */
    protected final void autoCtrlReset() {
        timeRec = 0;
        duraRec = 0;
        idxRec = 0;
    }

    /** Selects an action to play randomly.
     * @return The index of the action.
     */
    protected final int getRandomAction() {
        // Calculate the sum of all action's weight
        int weight_sum = 0;
        for (AnimDataWeight i: action_list) {
            weight_sum += i.weight();
        }
        // Random select a weight
        int weight_select = (int)Math.ceil(Math.random() * weight_sum);
        // Figure out which action the weight referred
        weight_sum = 0;
        for (int j = 0; j < action_list.length; j++) {
            weight_sum += action_list[j].weight();
            if (weight_select <= weight_sum)
                return j;
        }
        return -1;
    }

    /** Gets the default animation.
     * @return AnimData object.
     */
    public AnimData defaultAnim() {
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
