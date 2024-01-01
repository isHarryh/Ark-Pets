/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.transitions;


/** The class represents a transition,
 * which controls a float number transit from its starting value to its ending value.
 */
public class TransitionFloat extends Transition<Float> {
    protected final TernaryFunction<Float, Float> function;

    public TransitionFloat(TernaryFunction<Float, Float> function, float totalProgress) {
        super(totalProgress);
        this.function = function;
        start = 0f;
        end = 0f;
    }

    @Override
    public Float atProgress(float progress) {
        return function.apply(start, end, currentProgress / totalProgress);
    }
}
