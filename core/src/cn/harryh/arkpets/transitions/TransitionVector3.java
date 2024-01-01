/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.transitions;

import com.badlogic.gdx.math.Vector3;


/** The class represents a transition,
 * which controls a vector-3 (x,y,z) transit from its starting value to its ending value.
 */
public class TransitionVector3 extends Transition<Vector3> {
    protected final TernaryFunction<Float, Float> function;

    public TransitionVector3(TernaryFunction<Float, Float> function, float totalProgress) {
        super(totalProgress);
        this.function = function;
        start = new Vector3(0, 0, 0);
        end = new Vector3(0, 0, 0);
    }

    @Override
    public Vector3 atProgress(float progress) {
        float ratio = currentProgress / totalProgress;
        return new Vector3(
                function.apply(start.x, end.x, ratio),
                function.apply(start.y, end.y, ratio),
                function.apply(start.z, end.z, ratio)
        );
    }

    public void reset(float x, float y, float z) {
        reset(new Vector3(x, y, z));
    }
}
