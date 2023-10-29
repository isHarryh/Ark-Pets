/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.easings;


public class EasingLinearVector3 {
    public final EasingLinear eX;
    public final EasingLinear eY;
    public final EasingLinear eZ;

    public EasingLinearVector3(EasingLinear easing) {
        eX = new EasingLinear(easing.FROM, easing.TO, easing.DURATION);
        eY = new EasingLinear(easing.FROM, easing.TO, easing.DURATION);
        eZ = new EasingLinear(easing.FROM, easing.TO, easing.DURATION);
    }
}
