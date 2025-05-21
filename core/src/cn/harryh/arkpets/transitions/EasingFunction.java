/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.transitions;


/** The enumeration implements {@link TernaryFunction} with {@link Float} types, providing common easing functions.
 */
public enum EasingFunction implements TernaryFunction<Float, Float> {
    /** Linear function. */
    LINEAR((b, e, p) -> b + p * (e - b)),

    /** <a href="https://easings.net/#easeOutSine">Sine easing out function</a> */
    EASE_OUT_SINE((b, e, p) -> b + (float) Math.sin(p * Math.PI / 2) * (e - b)),

    /** <a href="https://easings.net/#easeOutCubic">Cubic easing out function</a> */
    EASE_OUT_CUBIC((b, e, p) -> b + (1 - (float) Math.pow(1 - p, 3)) * (e - b)),

    /** <a href="https://easings.net/#easeOutQuint">Quint easing out function</a> */
    EASE_OUT_QUINT((b, e, p) -> b + (1 - (float) Math.pow(1 - p, 5)) * (e - b));

    private final TernaryFunction<Float, Float> function;

    EasingFunction(TernaryFunction<Float, Float> function) {
        this.function = function;
    }

    @Override
    public Float apply(Float a, Float b, Float c) {
        return function.apply(a, b, c);
    }
}
