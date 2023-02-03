/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.utils;


public class AnimAutoData {
    public final AnimData ANIM;
    public final String NAME;
    public final float DURATION_MIN;
    public final int WEIGHT;

    /** Autonomic-Animation Data Object.
     * @param $animData The Animation Data.
     * @param $duration_min The minimal loop-action duration(seconds).
     * @param $weight The weight to call this action.
     */
    public AnimAutoData(AnimData $animData, float $duration_min, int $weight) {
        NAME = $animData.ANIM_NAME;
        DURATION_MIN = $duration_min;
        WEIGHT = $weight;
        ANIM = $animData;
    }
}
