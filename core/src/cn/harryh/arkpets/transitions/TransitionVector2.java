/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.transitions;

import com.badlogic.gdx.math.Vector2;


/** The class represents a transition,
 * which controls a vector-2 (x,y) transit from its starting value to its ending value.
 */
public class TransitionVector2 extends Transition<Vector2> {
    protected final EasingFunction easing;

    public TransitionVector2(EasingFunction easingFunction, float totalProgress) {
        super(totalProgress);
        easing = easingFunction;
        start = new Vector2(0, 0);
        end = new Vector2(0, 0);
    }

    @Override
    public Vector2 atProgress(float progress) {
        if (totalProgress <= 0)
            return end;
        float ratio = currentProgress / totalProgress;
        return new Vector2(
                easing.apply(start.x, end.x, ratio),
                easing.apply(start.y, end.y, ratio)
        );
    }

    public void reset(float x, float y) {
        reset(new Vector2(x, y));
    }
}
