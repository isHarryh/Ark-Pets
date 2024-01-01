/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import java.util.Objects;


/** Animation data record.
 * @param animClip The animation clip of THIS animation data.
 * @param animNext The NEXT animation data, which would be applied after this animation ended.
 * @param isLoop {@code true} indicates that this animation could be played in loop.
 * @param isStrict {@code true} indicates that this animation couldn't be interrupted.
 * @param offsetY The root offset in the Y-axis coordinate.
 * @param mobility The root motion. 0=None, 1=GoRight, -1=GoLeft.
 */
public record AnimData(
        AnimClip animClip,
        AnimData animNext,
        boolean isLoop,
        boolean isStrict,
        int offsetY,
        int mobility
) {
    /** Animation data record (simplified constructor).
     * @param animClip The animation clip of THIS animation data.
     */
    public AnimData(AnimClip animClip) {
        this(animClip, null, false, false,0, 0);
    }

    /** Animation data record (simplified constructor).
     * @param animClip The animation clip of THIS animation data.
     * @param animNext The NEXT animation data, which would be applied after this animation ended.
     * @param isLoop {@code true} indicates that this animation could be played in loop.
     * @param isStrict {@code true} indicates that this animation couldn't be interrupted.
     */
    public AnimData(AnimClip animClip, AnimData animNext, boolean isLoop, boolean isStrict) {
        this(animClip, animNext, isLoop, isStrict, 0, 0);
    }

    /** Derives a variation of this animation data.
     * @param isLoop New value for {@code isLoop}.
     * @param isStrict New value for {@code isStrict}.
     * @return New animation data.
     */
    public AnimData derive(boolean isLoop, boolean isStrict) {
        return new AnimData(this.animClip, this.animNext, isLoop, isStrict, this.offsetY, this.mobility);
    }

    /** Derives a variation of this animation data.
     * @param offsetY New value for {@code offsetY}.
     * @param mobility New value for {@code mobility}.
     * @return New animation data.
     */
    public AnimData derive(int offsetY, int mobility) {
        return new AnimData(this.animClip, this.animNext, this.isLoop, this.isStrict, offsetY, mobility);
    }

    /** Joins another animation data, which would be applied after this animation ended, to this animation data.
     * @param animNext The given animation data.
     * @return New animation data.
     */
    public AnimData join(AnimData animNext) {
        if (this.animNext == null)
            return new AnimData(this.animClip, animNext, this.isLoop, this.isStrict, this.offsetY, this.mobility);
        else
            return new AnimData(this.animClip, this.animNext.join(animNext), this.isLoop, this.isStrict, this.offsetY, this.mobility);
    }

    public boolean isEmpty() {
        return animClip == null;
    }

    public String name() {
        return isEmpty() ? null : animClip.fullName;
    }

    @Override
    public String toString() {
        return "AnimData {" + animClip + "}" +
                (isLoop ? " Loop" : "") +
                (isStrict ? " Strict" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimData animData = (AnimData) o;
        return isLoop == animData.isLoop && isStrict == animData.isStrict && offsetY == animData.offsetY && mobility == animData.mobility && Objects.equals(animClip, animData.animClip) && Objects.equals(animNext, animData.animNext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animClip, animNext, isLoop, isStrict, offsetY, mobility);
    }
}
