package com.isharryh.arkpets.behaviors;

import java.lang.reflect.InvocationTargetException;

import com.isharryh.arkpets.ArkConfig;
import com.isharryh.arkpets.utils.AICtrl;
import com.isharryh.arkpets.utils.AnimCtrl;


public class Behavior {
    public AICtrl[] action_list;
    protected ArkConfig config;
    protected float deltaMin;
    protected float timeRec;
    protected float duraRec;
    protected int idxRec;
    
    /** Character Behavior Controller Instance.
     * @param $config ArkConfig object.
     */
    public Behavior(ArkConfig $config) {
        action_list = null;
        config = $config;
        deltaMin = 0.5f;
        timeRec = 0f;
        duraRec = 0f;
        idxRec = 0;
    }

    /** Whether the animation list match this behavior class.
     * @param animList The animation list.
     * @return true=match, false=mismatch.
     */
    public static boolean match(String[] animList) {
        return false;
    }

    /** Get a random animation.
     * @param $deltaTime The delta time.
     * @return AnimCtrl object.
     */
    public AnimCtrl autoCtrl(float $deltaTime) {
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
    public int getRandomAction() {
        // Calculate the sum of all action's weight
        int weight_sum = 0;
        for (AICtrl i: action_list) {
            weight_sum += i.WEIGHT;
        }
        // Random select a weight
        int weight_select = (int) Math.round((Math.random() * weight_sum) + 0.5);
        // Figure out which action the weight refered
        weight_sum = 0;
        for (int j = 0; j < action_list.length; j++) {
            weight_sum += action_list[j].WEIGHT;
            if (weight_select <= weight_sum)
                return j;
        }
        return -1;
    }

    /** Select a matched behavior object from a behavior-list.
     * @param $animList
     * @param $candidateBehaviors
     * @return Behavior object.
     */
    final public static Behavior selectBehavior(String[] $animList, Behavior[] $candidateBehaviors) {
        for (int i = 0; i < $candidateBehaviors.length; i++) {
            try {
                if ($candidateBehaviors[i].getClass().getMethod("match", String[].class)
                        .invoke(null, (Object)$animList).equals(true))
                    return $candidateBehaviors[i];
            } catch (IllegalAccessException e) {
                continue;
            } catch (InvocationTargetException e) {
                continue;
            } catch (NoSuchMethodException e) {
                continue;
            } catch (SecurityException e) {
                continue;
            }
        }
        return null;
    }

    /** Get the default animation.
     * @return AnimCtrl object.
     */
    public AnimCtrl defaultAnim() {
        return null;
    }

    /** Get the animation when mouse-down.
     * @return AnimCtrl object.
     */
    public AnimCtrl clickStart() {
        return null;
    }

    /** Get the animation when mouse-up.
     * @return
     */
    public AnimCtrl clickEnd() {
        return null;
    }

    /** Get the animation when user start dragging.
     * @return
     */
    public AnimCtrl dragStart() {
        return null;
    }

    /** Get the animation when user end dragging.
     * @return
     */
    public AnimCtrl dragEnd() {
        return null;
    }
}
