/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.easings;


public class EasingLinearVector3 {
    public EasingLinear eX;
    public EasingLinear eY;
    public EasingLinear eZ;

    public EasingLinearVector3(EasingLinear $easing) {
        eX = new EasingLinear($easing.FROM, $easing.TO, $easing.DURATION);
        eY = new EasingLinear($easing.FROM, $easing.TO, $easing.DURATION);
        eZ = new EasingLinear($easing.FROM, $easing.TO, $easing.DURATION);
    }
}
