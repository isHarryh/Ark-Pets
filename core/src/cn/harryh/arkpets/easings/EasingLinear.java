/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.easings;


public class EasingLinear extends Easing {
    /** Easing handler instance. (Linear mode)
     * @param $from The start value.
     * @param $to The end value.
     * @param $duration The duration(second) of the whole easing process.
     */
    public EasingLinear(float $from, float $to, float $duration) {
        super($from, $to, $duration);
    }

    public float get(float $curDuration) {
        if ($curDuration >= DURATION)
            return TO;
        return (FROM + (TO - FROM) * (curDuration / DURATION));
    }
}
