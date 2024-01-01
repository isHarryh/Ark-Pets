/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.transitions;

import com.badlogic.gdx.math.Vector2;


/** The class represents a transition,
 * which controls a vector-2 (x,y) transit from its starting value to its ending value.
 */
public class TransitionVector2 extends Transition<Vector2> {
    protected final TernaryFunction<Float, Float> function;

    public TransitionVector2(TernaryFunction<Float, Float> function, float totalProgress) {
        super(totalProgress);
        this.function = function;
        start = new Vector2(0, 0);
        end = new Vector2(0, 0);
    }

    @Override
    public Vector2 atProgress(float progress) {
        float ratio = currentProgress / totalProgress;
        return new Vector2(
                function.apply(start.x, end.x, ratio),
                function.apply(start.y, end.y, ratio)
        );
    }

    public void reset(float x, float y) {
        reset(new Vector2(x, y));
    }
}
