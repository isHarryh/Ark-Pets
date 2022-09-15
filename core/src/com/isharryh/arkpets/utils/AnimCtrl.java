package com.isharryh.arkpets.utils;

public class AnimCtrl {
    public final String ANIM_NAME;
    public final boolean LOOP;
    public final boolean INTERRUPTABLE;
    public final AnimCtrl ANIM_NEXT;
    public final int OFFSET_Y;
    public final int MOBILITY;

    /** Animation Data Controller instance. (loop)
     * @param $anim_name The name of the animation.
     * @param $loop Allow to loop the animation.
     * @param $interruptable Allow to be interrupted by another animation.
     */
    public AnimCtrl(String $anim_name, boolean $loop, boolean $interruptable)  {
        ANIM_NAME = $anim_name;
        LOOP = $loop;
        INTERRUPTABLE = $interruptable;
        ANIM_NEXT = null;
        OFFSET_Y = 0;
        MOBILITY = 0;
    }

    /** Animation Data Controller instance. (usually play-once)
     * @param $anim_name The name of the animation.
     * @param $loop Allow to loop the animation.
     * @param $interruptable Allow to be interrupted by another animation.
     * @param $anim_next If defined, $anim_next will be played after the animation.
     */
    public AnimCtrl(String $anim_name, boolean $loop, boolean $interruptable, AnimCtrl $anim_next)  {
        ANIM_NAME = $anim_name;
        LOOP = $loop;
        INTERRUPTABLE = $interruptable;
        ANIM_NEXT = $anim_next;
        OFFSET_Y = 0;
        MOBILITY = 0;
    }

    /** Animation Data Controller instance. (loop, full-ver)
     * @param $anim_name The name of the animation.
     * @param $loop Allow to loop the animation.
     * @param $interruptable Allow to be interrupted by another animation.
     * @param $offset_y The offset of the bottom of the animation.
     * @param $mobility 0=No, 1=GoRight, -1=GoLeft.
     */
    public AnimCtrl(String $anim_name, boolean $loop, boolean $interruptable, int $offset_y, int $mobility)  {
        ANIM_NAME = $anim_name;
        LOOP = $loop;
        INTERRUPTABLE = $interruptable;
        ANIM_NEXT = null;
        OFFSET_Y = $offset_y;
        MOBILITY = $mobility;
    }
}
