/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.easings;


public class EasingLinearVector2 {
    public EasingLinear eX;
    public EasingLinear eY;

    public EasingLinearVector2(EasingLinear $easing) {
        eX = new EasingLinear($easing.FROM, $easing.TO, $easing.DURATION);
        eY = new EasingLinear($easing.FROM, $easing.TO, $easing.DURATION);
    }
}
