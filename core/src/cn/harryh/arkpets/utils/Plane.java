/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;

import static cn.harryh.arkpets.Const.*;


public class Plane {
    public final ArrayList<Vector3> barriers;
    public final ArrayList<Vector3> pointCharges;
    public final ArrayList<RectArea> world;
    private final Vector2 obj;
    private final Vector2 position;
    private final Vector2 speed;
    private final Vector2 speedLimit;
    private float gravity;
    private float resilience;
    private float airFrict;
    private float staticFrict;
    private boolean dropped = false;
    private float droppedHeight = 0;

    /** Initializes a plane with gravity field.
     */
    public Plane() {
        barriers        = new ArrayList<>();
        pointCharges    = new ArrayList<>();
        world           = new ArrayList<>();
        obj             = new Vector2(0, 0);
        position        = new Vector2(0, 0);
        speed           = new Vector2(0, 0);
        speedLimit      = new Vector2(0, 0);
        gravity         = 0;
        resilience      = 0;
        airFrict        = 0;
        staticFrict     = 0;
    }

    /** Sets the gravity acceleration.
     * @param gravity The acceleration of gravity (px/s^2).
     */
    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    /** Sets the bounce coefficient.
     * @param resilience The ratio of Ek to be reserved after the bounce.
     */
    public void setResilience(float resilience) {
        this.resilience = resilience > 1 ? 1 : (resilience < 0 ? 0 : resilience);
    }

    /** Sets the friction params.
     * @param airFrict The acceleration of air friction (px/s^2).
     * @param staticFrict The acceleration of static friction provided by the ground (px/s^2).
     */
    public void setFrict(float airFrict, float staticFrict) {
        this.airFrict    = Math.max(0, airFrict);
        this.staticFrict = Math.max(0, staticFrict);
    }

    /** Sets the size of the object.
     * @param objWidth The width of the object (px).
     * @param objHeight The height of the object (px).
     */
    public void setObjSize(float objWidth, float objHeight) {
        obj.set(objWidth, objHeight);
    }

    /** Sets the limitation of speed, 0=unlimited.
     * @param x Max speed in x-axis (px/s).
     * @param y Max speed in y-axis (px/s).
     */
    public void setSpeedLimit(float x, float y) {
        speedLimit.set(Math.max(0, x), Math.max(0, y));
    }

    /** Changes the position of the object forcibly,
     * which will cause velocity change.
     * @param deltaTime Delta time (s), set to 0 to avoid changing the velocity.
     * @param x New x-position (px).
     * @param y New y-position (px).
     */
    public void changePosition(float deltaTime, float x, float y) {
        if (deltaTime > 0)
            speed.set((x - position.x) / deltaTime, (y - position.y) / deltaTime);
        position.set(x, y);
        position.set(limitX(x), limitY(y));
    }

    /** Updates the position of the object.
     * @param deltaTime Delta time (s).
     */
    public void updatePosition(float deltaTime) {
        updateVelocity(deltaTime);
        float deltaX = speed.x * deltaTime;
        float deltaY = speed.y * deltaTime;
        final float bottom = borderBottom();
        droppedHeight = Math.max(Math.signum(gravity) * (position.y - bottom), droppedHeight);
        if (position.y != bottom && limitY(deltaY + position.y) == bottom) {
            // When it fell to the ground.
            if (Math.signum(gravity) * (position.y - bottom) > 0)
                dropped = true;
            speed.y = 0;
        }
        position.set(limitX(deltaX + position.x), limitY(deltaY + position.y));
    }

    /** Sets a line barrier that can support the object.
     * @param posTop The y-position of the barrier (px).
     * @param posLeft The x-position of the barrier's left edge (px).
     * @param width The width of the barrier (px).
     * @param overCover Whether to set the highest priority to this barrier.
     */
    public void setBarrier(float posTop, float posLeft, float width, boolean overCover) {
        if (overCover)
            barriers.add(0, new Vector3(posLeft, posTop, width));
        else
            barriers.add(new Vector3(posLeft, posTop, width));
    }

    /** Sets a point charge whose excited electric field can repulse the object.
     * The position and quantity of the charge are fixed.
     * @param posTop The y-position of the charge (px).
     * @param posLeft The x-position of the charge (px).
     * @param quantityProduct The product of the point's quantity and the object's quantity (C^2).
     */
    public void setPointCharge(float posTop, float posLeft, float quantityProduct) {
        pointCharges.add(new Vector3(posLeft, posTop, quantityProduct));
    }

    /** Gets the x-position of the object.
     * @return X (px).
     */
    public float getX() {
        return position.x;
    }

    /** Gets the y-position of the object.
     * @return Y (px).
     */
    public float getY() {
        return position.y;
    }

    /** Gets the dropped-status of the object.
     * @return true=dropped once.
     */
    public boolean getDropped() {
        if (dropped) {
            dropped = false; // Reset
            if (droppedHeight >= droppedThreshold) {
                droppedHeight = 0; // Reset
                return true;
            }
        }
        return false;
    }

    /** Gets the dropping-status of the object.
     * @return true=dropping.
     */
    public boolean getDropping() {
        return Math.abs(position.y - borderBottom()) > droppedThreshold;
    }

    /** Gets the debug message.
     * @return Debug message string.
     */
    public String getDebugMsg() {
        // Primary debug messages:
        String msg = this.toString();
        msg += "\nPosition:\t" + Math.round(position.x) + "\t" + Math.round(position.y) + (getDropping() ? "\t(dropping)" : "");
        msg += "\nVelocity:\t" + Math.round(speed.x) + "\t" + Math.round(speed.y);
        msg += "\nBorders:\t^" + Math.round(borderTop()) + "\t>" + Math.round(borderRight()) + "\tv" + Math.round(borderBottom()) + "\t<" + Math.round(borderLeft());
        msg += "\nAreas:\t" + world.size();
        // Additional debug messages:
        StringBuilder msgBuilder1 = new StringBuilder(msg);
        for (RectArea i : world)
            msgBuilder1.append("\n- ").append(i.toString());
        msg = msgBuilder1.toString();
        msg += "\nBarriers:\t" + barriers.size();
        StringBuilder msgBuilder2 = new StringBuilder(msg);
        for (Vector3 i : barriers)
            msgBuilder2.append("\n- Y = ").append(i.y).append(", X range = (").append(i.x).append(",").append(i.x + i.z).append(")");
        msg = msgBuilder2.toString();
        return msg;
    }

    /** Updates the velocity of the object.
     * @param deltaTime Delta time (s).
     */
    private void updateVelocity(float deltaTime) {
        final float top = borderTop();
        final float bottom = borderBottom();
        // Gravity
        speed.y -= gravity * deltaTime;
        if (position.y == bottom || (position.y + obj.y >= top && speed.y > 0))
            speed.y = 0;
        // Electrostatic forces
        for (Vector3 pc : pointCharges) {
            float dx = position.x + obj.x / 2f - pc.x;
            float dy = position.y + obj.y / 2f - pc.y;
            float hypot = (float)Math.hypot(dx, dy);
            speed.x = applyElectrostaticEffect(speed.x, pc.z, hypot, dx / hypot, deltaTime);
            speed.y = applyElectrostaticEffect(speed.y, pc.z, hypot, dy / hypot , deltaTime);
        }
        // Ground friction
        if (position.y == bottom)
            speed.x = applyFriction(speed.x, staticFrict, deltaTime);
        // Air friction
        speed.x = applyFriction(speed.x, airFrict, deltaTime);
        speed.y = applyFriction(speed.y, airFrict, deltaTime);
        // Limit
        if (speedLimit.x != 0 && Math.abs(speed.x) > speedLimit.x)
            speed.x = Math.signum(speed.x) * speedLimit.x;
        if (speedLimit.y != 0 && Math.abs(speed.y) > speedLimit.y)
            speed.y = Math.signum(speed.y) * speedLimit.y;
        // Bounce
        if (resilience != 0 && (position.x == borderLeft() || position.x == borderRight())) {
            speed.x = (float)(Math.sqrt(speed.x * speed.x * resilience) * Math.signum(-speed.x));
        }
    }

    /** Applies a friction to a velocity.
     * @param speed The original velocity (px/s).
     * @param frict The acceleration of friction (px/s^2).
     * @param deltaTime Delta time (s).
     * @return New velocity (px/s).
     */
    private float applyFriction(float speed, float frict, float deltaTime) {
        float delta = Math.signum(speed) * frict * deltaTime;
        float estimated = speed - delta;
        return delta * estimated < 0 ? 0 : estimated;
    }

    /** Applies the electrostatic effect of a point charge to a velocity.
     * @param speed The original velocity (px/s).
     * @param quantityProduct The product of the point's quantity and the object's quantity (C^2).
     * @param distance The absolute distance between the point charge and the object.
     * @param cosine The cosine of the included angel between the distance and its projection on the direction of speed.
     * @param deltaTime Delta time (s).
     * @return New velocity (px/s).
     */
    private float applyElectrostaticEffect(float speed, float quantityProduct, float distance, float cosine, float deltaTime){
        final float k = 2000 * (float)Math.hypot(obj.x, obj.y); // Electrostatic force constant
        final float dm = 20; // Min distance
        distance = Math.max(Math.abs(distance), dm); // Limit the distance
        cosine = Float.isNaN(cosine) ? 0 : Math.max(0, Math.min(1, cosine)); // Limit the cosine
        float delta = k * quantityProduct / distance / distance * cosine * deltaTime;
        return speed + delta;
    }

    /** Limits the x-position to avoid overstepping.
     * @param x X (px).
     * @return New x (px).
     */
    private float limitX(float x) {
        return Math.max(borderLeft(), Math.min(x, borderRight() - obj.x));
    }

    /** Limits the y-position to avoid overstepping.
     * @param y Y (px).
     * @return New y (px).
     */
    private float limitY(float y) {
        return Math.max(borderBottom(), Math.min(y, borderTop() - obj.y));
    }

    /** Gets the position of the top border.
     * @return Y (px).
     */
    public float borderTop() {
        float t = -Float.MAX_VALUE;
        for (RectArea a : world)
            if (a.isXInOrthographic(position.x, obj.x))
                if (a.top > t)
                    t = a.top;
        return t;
    }

    /** Gets the position of the bottom border.
     * @return Y (px).
     */
    public float borderBottom() {
        for (Vector3 i : barriers)
            if (i.x <= position.x + obj.x && position.x <= i.x + i.z)
                if (position.y + obj.y > i.y && borderTop() - obj.y > i.y)
                    return i.y;

        float t = Float.MAX_VALUE;
        for (RectArea a : world)
            if (a.isXInOrthographic(position.x, obj.x))
                if (a.bottom < t)
                    t = a.bottom;
        return t;
    }

    /** Gets the position of the right border.
     * @return X (px).
     */
    public float borderRight() {
        float t = -Float.MAX_VALUE;
        for (RectArea a : world)
            if (a.isYInOrthographic(position.y, obj.y))
                if (a.right > t)
                    t = a.right;
        return t;
    }

    /** Gets the position of the left border.
     * @return X (px).
     */
    public float borderLeft() {
        float t = Float.MAX_VALUE;
        for (RectArea a : world)
            if (a.isYInOrthographic(position.y, obj.y))
                if (a.left < t)
                    t = a.left;
        return t;
    }

    @Override
    public String toString() {
        return "Plane ^" + borderTop() + " >" + borderRight() + " v" + borderBottom() + " <" + borderLeft();
    }


    @SuppressWarnings("unused")
    public record RectArea(float left, float right, float top, float bottom) {
        public float getWidth() {
                return Math.abs(left - right);
            }

        public float getHeight() {
            return Math.abs(top - bottom);
        }

        public boolean isInArea(float x, float y) {
            return isXInOrthographic(x) && isYInOrthographic(y);
        }

        public boolean isInArea(float x, float y, float allowanceX, float allowanceY) {
            return isXInOrthographic(x, allowanceX) && isYInOrthographic(y, allowanceY);
        }

        public boolean isXInOrthographic(float x) {
            return x >= left && x <= right;
        }

        public boolean isYInOrthographic(float y) {
            return y >= bottom && y <= top;
        }

        public boolean isXInOrthographic(float x, float allowance) {
            return x >= left - allowance && x <= right + allowance;
        }

        public boolean isYInOrthographic(float y, float allowance) {
            return y >= bottom - allowance && y <= top + allowance;
        }

        @Override
        public String toString() {
                return "RectArea ^" + top + " >" + right + " v" + bottom + " <" + left;
            }
    }
}
