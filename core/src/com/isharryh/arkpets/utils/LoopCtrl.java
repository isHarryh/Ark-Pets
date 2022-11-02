/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.utils;

public class LoopCtrl {
    private float minIntervalTime;
    private float accumTime;

    /** Loop Controller instance.
     * @param $minIntervalTime The minimal interval time for the loop.
     */
    public LoopCtrl(float $minIntervalTime) {
        minIntervalTime = $minIntervalTime;
    }

    /** Query whether the loop is executable now.
     * @param $deltaTime The delta time.
     * @return true=okay.
     */
    public boolean isExecutable(float $deltaTime) {
        accumTime += $deltaTime;
        if (accumTime >= minIntervalTime) {
            accumTime = 0;
            return true;
        } else {
            return false;
        }
    }
}
