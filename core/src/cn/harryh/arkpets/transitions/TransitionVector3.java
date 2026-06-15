/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.transitions;

import com.badlogic.gdx.math.Vector3;


/** The class represents a transition,
 * which controls a vector-3 (x,y,z) transit from its starting value to its ending value.
 */
public class TransitionVector3 extends Transition<Vector3> {
    protected final EasingFunction easing;

    public TransitionVector3(EasingFunction easingFunction, float totalProgress) {
        super(totalProgress);
        easing = easingFunction;
        start = new Vector3(0, 0, 0);
        end = new Vector3(0, 0, 0);
    }

    @Override
    public Vector3 atProgress(float progress) {
        if (totalProgress <= 0)
            return end;
        float ratio = currentProgress / totalProgress;
        return new Vector3(
                easing.apply(start.x, end.x, ratio),
                easing.apply(start.y, end.y, ratio),
                easing.apply(start.z, end.z, ratio)
        );
    }

    public void reset(float x, float y, float z) {
        reset(new Vector3(x, y, z));
    }
}
