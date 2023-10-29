/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.easings;


abstract public class Easing {
    public float FROM;
    public float TO;
    public float DURATION;
    public float curDuration;
    public float curValue;

    /** Easing handler instance.
     * @param from The start value.
     * @param to The end value.
     * @param duration The duration(second) of the whole easing process.
     */
    public Easing(float from, float to, float duration) {
        FROM = from;
        TO = to;
        DURATION = duration;
        curDuration = 0;
        curValue = from;
    }

    /** Gets a specific value directly.
     * @param curDuration The x-position of the process.
     * @return The y-position of the process.
     */
    abstract float get(float curDuration);

    /** Updates the end value.
     * @param to The new end value.
     */
    public void update(float to) {
        if (to == TO)
            return;
        FROM = curValue;
        TO = to;
        curDuration = 0;
    }

    /** Updates the end value and the duration.
     * @param to The new end value.
     * @param duration The new duration(second).
     */
    public void update(float to, float duration) {
        if (to == TO)
            return;
        FROM = curValue;
        TO = to;
        DURATION = duration;
        curDuration = 0;
    }

    /** Steps the easing process.
     * @param deltaTime The delta time.
     * @return The current value.
     */
    public float step(float deltaTime) {
        curDuration += deltaTime;
        curValue = get(curDuration);
        return curValue;
    }

    @Override
    public String toString() {
        return String.valueOf((int)curValue);
    }
}
