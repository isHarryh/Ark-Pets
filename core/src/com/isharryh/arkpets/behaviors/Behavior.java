/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.behaviors;

import java.lang.reflect.InvocationTargetException;

import com.isharryh.arkpets.ArkConfig;
import com.isharryh.arkpets.utils.AnimData;
import com.isharryh.arkpets.utils.AnimData.AnimAutoData;


abstract public class Behavior {
    public AnimAutoData[] action_list;
    public String[] anim_list;
    protected ArkConfig config;
    protected float deltaMin;
    protected float timeRec;
    protected float duraRec;
    protected int idxRec;
    
    /** Character Behavior Controller Instance.
     * @param $config ArkConfig object.
     * @param $animList The animation name list.
     */
    public Behavior(ArkConfig $config, String[] $animList) {
        action_list = null;
        anim_list = $animList;
        config = $config;
        deltaMin = 0.5f;
        timeRec = 0;
        duraRec = 0;
        idxRec = 0;
    }

    /** Get a random animation.
     * @param $deltaTime The delta time.
     * @return AnimData object.
     */
    public AnimData autoCtrl(float $deltaTime) {
        duraRec += $deltaTime;
        timeRec += $deltaTime;
        if (timeRec >= deltaMin) {
            timeRec = 0f;
            if (duraRec >= action_list[idxRec].DURATION_MIN) {
                // Now try to change action
                duraRec = 0f;
                idxRec = getRandomAction();
                return action_list[idxRec].ANIM;
            }
        }
        return null;
    }

    /** Randomly select an action to play.
     * @return The index of the action.
     */
    private int getRandomAction() {
        // Calculate the sum of all action's weight
        int weight_sum = 0;
        for (AnimAutoData i: action_list) {
            weight_sum += i.WEIGHT;
        }
        // Random select a weight
        int weight_select = (int) Math.round((Math.random() * weight_sum) + 0.5);
        // Figure out which action the weight referred
        weight_sum = 0;
        for (int j = 0; j < action_list.length; j++) {
            weight_sum += action_list[j].WEIGHT;
            if (weight_select <= weight_sum)
                return j;
        }
        return -1;
    }

    /** Whether the provided animation list match this behavior class.
     * @param animList The animation name list.
     * @return true=match, false=mismatch.
     */
    public static boolean match(String[] animList) {
        return false;
    }

    /** Select a matched behavior object from a behavior-list.
     * @param $animList A list contains the name of animations.
     * @param $candidateBehaviors A list contains the Behavior objects to be selected.
     * @return Behavior object.
     */
    public static Behavior selectBehavior(String[] $animList, Behavior[] $candidateBehaviors) {
        for (Behavior $candidateBehavior : $candidateBehaviors) {
            try {
                if ($candidateBehavior.getClass().getMethod("match", String[].class)
                        .invoke(null, (Object) $animList).equals(true))
                    return $candidateBehavior;
            } catch (IllegalAccessException | SecurityException | NoSuchMethodException |
                     InvocationTargetException ignored) {
            }
        }
        return null;
    }

    /** Get the default animation.
     * @return AnimData object.
     */
    public AnimData defaultAnim() {
        return null;
    }

    /** Get the animation when mouse-down.
     * @return AnimData object.
     */
    public AnimData clickStart() {
        return null;
    }

    /** Get the animation when mouse-up.
     * @return AnimData object.
     */
    public AnimData clickEnd() {
        return null;
    }

    /** Get the animation when user start dragging.
     * @return AnimData object.
     */
    public AnimData dragStart() {
        return null;
    }

    /** Get the animation when user end dragging.
     * @return AnimData object.
     */
    public AnimData dragEnd() {
        return null;
    }

    /** Get the animation when character dropped.
     * @return AnimData object.
     */
    public AnimData drop() {
        return null;
    }
}
