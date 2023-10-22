/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import java.util.Objects;


/** Animation data record.
 * @param animClip The current animation clip.
 * @param animNext The next animation data.
 * @param loop Allows to loop the animation.
 * @param interruptable Allows to be interrupted by another animation.
 * @param offsetY The offset of the bottom of the animation.
 * @param mobility 0=None, 1=GoRight, -1=GoLeft.
 */
public record AnimData(
        AnimClip animClip,
        AnimData animNext,
        boolean loop,
        boolean interruptable,
        int offsetY,
        int mobility
) {
    /** Animation data record (simplified constructor).
     * @param animClip The animation clip.
     */
    public AnimData(AnimClip animClip) {
        this(animClip, null, false, true, 0, 0);
    }

    /** Derives a variation of this animation data.
     * @param loop New value for {@code loop}.
     * @param interruptable New value for {@code interruptable}.
     * @return New animation data.
     */
    public AnimData derive(boolean loop, boolean interruptable) {
        return new AnimData(this.animClip, this.animNext, loop, interruptable, this.offsetY, this.mobility);
    }

    /** Derives a variation of this animation data.
     * @param offset_y New value for {@code offsetY}.
     * @param mobility New value for {@code mobility}.
     * @return New animation data.
     */
    public AnimData derive(int offset_y, int mobility) {
        return new AnimData(this.animClip, this.animNext, this.loop, this.interruptable, offset_y, mobility);
    }

    /** Joins a animation data to this animation data.
     * @param animNext The given animation data.
     * @return New animation data.
     */
    public AnimData join(AnimData animNext) {
        if (this.animNext == null)
            return new AnimData(this.animClip, animNext, this.loop, this.interruptable, this.offsetY, this.mobility);
        else
            return new AnimData(this.animClip, this.animNext.join(animNext), this.loop, this.interruptable, this.offsetY, this.mobility);
    }

    public boolean isEmpty() {
        return animClip == null;
    }

    public String name() {
        return isEmpty() ? null : animClip.fullName;
    }

    @Override
    public String toString() {
        return "AnimData {" + animClip + "} " +
                (loop ? "Loop" : "NotLoop") + ' ' +
                (interruptable ? "Interruptable" : "NotInterruptable");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimData animData = (AnimData) o;
        return loop == animData.loop && interruptable == animData.interruptable && offsetY == animData.offsetY && mobility == animData.mobility && Objects.equals(animClip, animData.animClip) && Objects.equals(animNext, animData.animNext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animClip, animNext, loop, interruptable, offsetY, mobility);
    }
}
