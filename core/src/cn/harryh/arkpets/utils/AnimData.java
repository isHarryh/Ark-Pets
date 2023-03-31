/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

public class AnimData {
    public final String ANIM_NAME;
    public final boolean LOOP;
    public final boolean INTERRUPTABLE;
    public final AnimData ANIM_NEXT;
    public final int OFFSET_Y;
    public final int MOBILITY;

    /** Animation Data Object. (loop)
     * @param $anim_name The name of the animation.
     * @param $loop Allow to loop the animation.
     * @param $interruptable Allow to be interrupted by another animation.
     */
    public AnimData(String $anim_name, boolean $loop, boolean $interruptable)  {
        ANIM_NAME = $anim_name;
        LOOP = $loop;
        INTERRUPTABLE = $interruptable;
        ANIM_NEXT = null;
        OFFSET_Y = 0;
        MOBILITY = 0;
    }

    /** Animation Data Object. (usually play-once)
     * @param $anim_name The name of the animation.
     * @param $loop Allow to loop the animation.
     * @param $interruptable Allow to be interrupted by another animation.
     * @param $anim_next If defined, $anim_next will be played after the animation.
     */
    public AnimData(String $anim_name, boolean $loop, boolean $interruptable, AnimData $anim_next)  {
        ANIM_NAME = $anim_name;
        LOOP = $loop;
        INTERRUPTABLE = $interruptable;
        ANIM_NEXT = $anim_next;
        OFFSET_Y = 0;
        MOBILITY = 0;
    }

    /** Animation Data Object. (loop, full-version constructor)
     * @param $anim_name The name of the animation.
     * @param $loop Allow to loop the animation.
     * @param $interruptable Allow to be interrupted by another animation.
     * @param $offset_y The offset of the bottom of the animation.
     * @param $mobility 0=No, 1=GoRight, -1=GoLeft.
     */
    public AnimData(String $anim_name, boolean $loop, boolean $interruptable, int $offset_y, int $mobility)  {
        ANIM_NAME = $anim_name;
        LOOP = $loop;
        INTERRUPTABLE = $interruptable;
        ANIM_NEXT = null;
        OFFSET_Y = $offset_y;
        MOBILITY = $mobility;
    }

    public static class AnimAutoData {
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
}
