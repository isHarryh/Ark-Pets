/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import java.util.Objects;


/** Animation weight record.
 * @param anim The Animation Data.
 * @param weight The weight to call this action.
 * @see AnimData
 */
public record AnimDataWeight(AnimData anim, int weight) {
    public float duration() {
        return anim.animClip().duration;
    }

    @Override
    public String toString() {
        return "AnimDataWeight {" + anim + "} " +
                "Weight=" + weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimDataWeight that = (AnimDataWeight) o;
        return weight == that.weight && Objects.equals(anim, that.anim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anim, weight);
    }
}
