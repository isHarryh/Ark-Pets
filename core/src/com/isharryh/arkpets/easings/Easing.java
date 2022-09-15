package com.isharryh.arkpets.easings;


public class Easing {
    public float FROM;
    public float TO;
    public float DURATION;
    public float curDuration;
    public float curValue;

    /** Easing tool. (Basic class)
     * @param $from The start value.
     * @param $to The end value.
     * @param $duration The duration(second) of the whole easing process.
     */
    public Easing(float $from, float $to, float $duration) {
        FROM = $from;
        TO = $to;
        DURATION = $duration;
        curDuration = 0;
        curValue = $from;
    }

    /** Update the end value.
     * @param $to The new end value.
     */
    public void update(float $to) {
        if ($to == TO)
            return;
        FROM = curValue;
        TO = $to;
        curDuration = 0;
    }

    /** Update the end value and the duration.
     * @param $to The new end value.
     * @param $duration The new duration(second).
     */
    public void update(float $to, float $duration) {
        if ($to == TO)
            return;
        FROM = curValue;
        TO = $to;
        DURATION = $duration;
        curDuration = 0;
    }

    /** Step the easing process.
     * @param $deltaTime The delta time.
     * @return The current value.
     */
    public float step(float $deltaTime) {
        curDuration += $deltaTime;
        curValue = get(curDuration);
        return curValue;
    }

    /** Directly get a specific value.
     * @param $curDuration The x-position of the process.
     * @return The y-position of the process.
     */
    public float get(float $curDuration) {
        if ($curDuration >= DURATION)
            return TO;
        return curValue;
    }
}
