/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.utils;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;


public class Plane {
    public ArrayList<Vector3> barriers;
    public ArrayList<Vector3> pointCharges;
    private final Vector2 obj;
    private final Vector2 world;
    private final Vector2 position;
    private final Vector2 speed;
    private final Vector2 speedLimit;
    private float bounce;
    private float gravity;
    private float airFrict;
    private float staticFrict;
    private boolean dropped = false;
    private float droppedHeight = 0;
    static float droppedThreshold = 10f;

    /** Initialize a plane with gravity field.
     * The origin of coordinates (0,0) is the left-bottom point.
     * Minus(-) are allowed, but may not be compatible.
     * @param $worldWidth The width of the plane (px).
     * @param $worldHeight The height of the plane (px).
     * @param $gravity The acceleration of gravity (px/s^2).
     */
    public Plane(int $worldWidth, int $worldHeight, float $gravity) {
        position = new Vector2(0, 0);
        speed = new Vector2(0, 0);
        speedLimit = new Vector2(0, 0);
        barriers = new ArrayList<>();
        pointCharges = new ArrayList<>();
        world = new Vector2($worldWidth, $worldHeight);
        obj = new Vector2(0, 0);
        bounce = 0;
        gravity = $gravity;
        airFrict = 0;
        staticFrict = 0;
    }

    /** Set the bounce coefficient.
     * @param $bounce The ratio of Ek to be reserved after the bounce.
     */
    public void setBounce(float $bounce) {
        bounce = $bounce > 1 ? 1 : ($bounce < 0 ? 0 : $bounce);
    }

    /** Set the friction params.
     * @param $airFrict The acceleration of air friction (px/s^2).
     * @param $staticFrict The acceleration of static friction provided by the groud (px/s^2).
     */
    public void setFrict(float $airFrict, float $staticFrict) {
        airFrict = $airFrict;
        staticFrict = $staticFrict;
    }

    /** Set the size of the object.
     * Minus(-) are allowed, but may not be compatible.
     * @param $objWidth The width of the object (px).
     * @param $objHeight The height of the object (px).
     */
    public void setObjSize(float $objWidth, float $objHeight) {
        obj.set($objWidth, $objHeight);
    }

    /** Set the limitation of speed, 0=unlimited.
     * @param $x Max speed in x-direction (px/s).
     * @param $y Max speed in y-direction (px/s).
     */
    public void setSpeedLimit(float $x, float $y) {
        speedLimit.set($x, $y);
    }

    /** Forcibly change the position of the object,
     * which will cause velocity change.
     * @param $deltaTime Delta time (s).
     * @param $x New x-position (px).
     * @param $y New y-position (px).
     */
    public void changePosition(float $deltaTime, float $x, float $y) {
        speed.set(($x-position.x)/$deltaTime, ($y-position.y)/$deltaTime);
        position.set($x, $y);
        position.set(limitX($x), limitY($y));
    }

    /** Update the position of the object.
     * @param $deltaTime Delta time (s).
     */
    public void updatePosition(float $deltaTime) {
        updateVelocity($deltaTime);
        float deltaX = speed.x * $deltaTime;
        float deltaY = speed.y * $deltaTime;
        final float borderBottom = borderBottom();
        droppedHeight = Math.max(Math.signum(gravity) * (position.y - borderBottom), droppedHeight);
        if (position.y != borderBottom && limitY(deltaY + position.y) == borderBottom) {
            // When it fell to the ground.
            if (Math.signum(gravity) * (position.y - borderBottom) > 0)
                dropped = true;
            speed.y = 0;
        }
        position.set(limitX(deltaX + position.x), limitY(deltaY + position.y));
    }

    /** Set a line barrier that can support the object.
     * @param $posTop The y-position of the barrier (px).
     * @param $posLeft The x-position of the barrier's left edge (px).
     * @param $width The width of the barrier (px).
     * @param $overCover Whether to set the highest priority to this barrier.
     */
    public void setBarrier(float $posTop, float $posLeft, float $width, boolean $overCover) {
        if ($overCover)
            barriers.add(0, new Vector3($posLeft, $posTop, $width));
        else
            barriers.add(new Vector3($posLeft, $posTop, $width));
    }

    /** Set a point charge whose excited electric field can repulse the object.
     * The position and quantity of the charge are fixed.
     * @param $posTop The y-position of the charge (px).
     * @param $posLeft The x-position of the charge (px).
     * @param $quantityProduct The product of the point's quantity and the object's quantity (C^2).
     */
    public void setPointCharge(float $posTop, float $posLeft, float $quantityProduct) {
        pointCharges.add(new Vector3($posLeft, $posTop, $quantityProduct));
    }

    /** Get the x-position of the object.
     * @return X (px).
     */
    public float getX() {
        return position.x;
    }

    /** Get the y-position of the object.
     * @return Y (px).
     */
    public float getY() {
        return position.y;
    }

    /** Get the dropped-status of the object.
     * @return true=dropped.
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

    /** Get the dropping-status of the object.
     * @return true=dropping.
     */
    public boolean getDropping() {
        return Math.abs(position.y - borderBottom()) > droppedThreshold;
    }

    /** Update the velocity of the object.
     * @param $deltaTime Delta time (s).
     */
    private void updateVelocity(float $deltaTime) {
        final float BOTTOM = borderBottom();
        final float TOP = borderTop();
        // Gravity
        speed.y -= gravity * $deltaTime;
        if (position.y == BOTTOM) {
            speed.y = 0;
        }
        else if (position.y == TOP && speed.y > 0)
            speed.y = 0;
        // Electrostatic forces
        for (Vector3 pc : pointCharges) {
            float dx = position.x + obj.x / 2f - pc.x;
            float dy = position.y + obj.y / 2f - pc.y;
            float hypot = (float)Math.hypot(dx, dy);
            speed.x = applyElectrostaticEffect(speed.x, pc.z,  hypot, dx / hypot, $deltaTime);
            speed.y = applyElectrostaticEffect(speed.y, pc.z, hypot, dy / hypot , $deltaTime);
        }
        // Ground friction
        if (position.y == BOTTOM)
            speed.x = applyFriction(speed.x, staticFrict, $deltaTime);
        // Air friction
        speed.x = applyFriction(speed.x, airFrict, $deltaTime);
        speed.y = applyFriction(speed.y, airFrict, $deltaTime);
        // Limit
        if (speedLimit.x != 0 && Math.abs(speed.x) > speedLimit.x)
            speed.x = Math.signum(speed.x) * speedLimit.x;
        if (speedLimit.y != 0 && Math.abs(speed.y) > speedLimit.y)
            speed.y = Math.signum(speed.y) * speedLimit.y;
        // Bounce
        if (bounce != 0 && (position.x == borderLeft() || position.x == borderRight())) {
            speed.x = (float)(Math.sqrt(speed.x * speed.x * bounce) * Math.signum(-speed.x));
        }
    }

    /** Apply a friction to a velocity.
     * @param $speed The original velocity (px/s).
     * @param $frict The acceleration of friction (px/s^2).
     * @param $deltaTime Delta time (s).
     * @return New velocity (px/s).
     */
    private float applyFriction(float $speed, float $frict, float $deltaTime) {
        float delta = Math.signum($speed) * $frict * $deltaTime;
        return Math.signum($speed - delta) == Math.signum(delta) ? $speed - delta : 0;
    }

    /** Apply the electrostatic effect of a point charge to a velocity.
     * @param $speed The original velocity (px/s).
     * @param $quantityProduct The product of the point's quantity and the object's quantity (C^2).
     * @param $distance The absolute distance between the point charge and the object.
     * @param $cosine The cosine of the included angel between the distance and its projection on the direction of speed.
     * @param $deltaTime Delta time (s).
     * @return New velocity (px/s).
     */
    private float applyElectrostaticEffect(float $speed, float $quantityProduct, float $distance, float $cosine, float $deltaTime){
        final float k = 2000 * (float)Math.hypot(obj.x, obj.y); // Electrostatic force constant
        final float dm = 20; // Min distance
        $distance = Math.max(Math.abs($distance), dm); // Limit the distance
        float delta = k * $quantityProduct / $distance / $distance * $cosine * $deltaTime;
        //System.out.println("speed+="+delta);
        return $speed + delta;
    }

    /** Limit the x-position to avoid overstepping.
     * @param $x X (px).
     * @return New x (px).
     */
    private float limitX(float $x) {
        return $x<borderLeft() ? borderLeft() : Math.min($x, borderRight());
    }

    /** Limit the y-position to avoid overstepping.
     * @param $y Y (px).
     * @return New y (px).
     */
    private float limitY(float $y) {
        return $y<borderBottom() ? borderBottom() : Math.min($y, borderTop());
    }

    /** Get the border position of the top.
     * @return Y (px).
     */
    private float borderTop() {
        if (world.y > 0)
            return obj.y < 0 ? world.y : world.y - obj.y;
        else
            return obj.y < 0 ? 0 : -obj.y;
    }

    /** Get the border position of the bottom.
     * @return Y (px).
     */
    private float borderBottom() {
        for (Vector3 i : barriers) {
            if (i.x <= position.x-obj.x && position.x <= i.x+i.z)
                if (Math.abs(position.y) <= Math.abs(i.y))
                    return obj.y < 0 ? i.y - obj.y : i.y;
        }
        if (world.y > 0)
            return obj.y < 0 ? -obj.y : 0;
        else
            return obj.y < 0 ? world.y - obj.y : world.y;
    }

    /** Get the border position of the right.
     * @return X (px).
     */
    private float borderRight() {
        if (world.x > 0)
            return obj.x < 0 ? world.x : world.x - obj.x;
        else
            return obj.x < 0 ? 0 : -obj.x;
    }

    /** Get the border position of the left.
     * @return X (px).
     */
    private float borderLeft() {
        if (world.x > 0)
            return obj.x < 0 ? -obj.x : 0;
        else
            return obj.x < 0 ? world.x - obj.x : world.x;
    }
}
