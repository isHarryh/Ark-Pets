/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.utils;


public class AICtrl {
    public final String NAME;
    public final float DURATION_MIN;
    public final int WEIGHT;
    public final AnimCtrl ANIM;

    /** AI Controller Instance.
     * @param $animCtrl The AnimCtrl.
     * @param $duration_min The minimal loop-action duration(seconds).
     * @param $weight The weight to call this action.
     */
    public AICtrl(AnimCtrl $animCtrl, float $duration_min, int $weight) {
        NAME = $animCtrl.ANIM_NAME;
        DURATION_MIN = $duration_min;
        WEIGHT = $weight;
        ANIM = $animCtrl;
    }
    
}
