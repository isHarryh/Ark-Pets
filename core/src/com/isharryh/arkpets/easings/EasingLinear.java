/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.easings;


public class EasingLinear extends Easing {
    /** Easing tool. (Linear mode)
     * @param $from The start value.
     * @param $to The end value.
     * @param $duration The duration(second) of the whole easing process.
     */
    public EasingLinear(float $from, float $to, float $duration) {
        super($from, $to, $duration);
    }

    /** Directly get a specific value.
     * @param $curDuration The x-position of the process.
     * @return The y-position of the process.
     */
    public float get(float $curDuration) {
        if ($curDuration >= DURATION)
            return TO;
        return (FROM + (TO - FROM) * (curDuration / DURATION));
    }
}
