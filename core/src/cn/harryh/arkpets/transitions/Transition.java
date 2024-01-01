/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.transitions;

import java.util.Objects;


/** The class represents a transition,
 * which controls a value transit from its starting value to its ending value.
 * @param <E> The type of the value.
 * @since ArkPets 2.3
 */
abstract public class Transition<E> {
    protected E start;
    protected E end;
    protected float currentProgress;
    protected float totalProgress;

    /** Initializes a transition.
     * @param totalProgress The total progress of the transition.
     */
    public Transition(float totalProgress) {
        setTotalProgress(totalProgress);
    }

    /** Gets the certain value at the specific progress.
     * @param progress The given progress
     * @return The value at the given progress.
     */
    abstract public E atProgress(float progress);

    /** Gets the starting value.
     * @return The starting value.
     */
    public final E start() {
        return start;
    }

    /** Gets the ending value.
     * @return The ending value.
     */
    public final E end() {
        return end;
    }

    /** Gets the current value.
     * @return The current value.
     */
    public final E now() {
        return atProgress(currentProgress);
    }

    /** Returns {@code true} if the transition is at its ending point.
     * @return true=ended, false=not-yet-ended.
     */
    public final boolean isEnded() {
        return Objects.equals(now(), end());
    }

    /** Adds the given progress to the transition progress.
     * @param progress The given progress to add.
     */
    public final void addProgress(float progress) {
        setCurrentProgress(currentProgress + progress);
    }

    /** Updates the ending value of the transition and resets the current progress to 0.
     * @param end The new ending value.
     */
    public final void reset(E end) {
        if (Objects.equals(this.end, end))
            return;
        this.start = now();
        this.end = end;
        currentProgress = 0;
    }

    /** Sets the current progress of the transition.
     * @param currentProgress The new current progress.
     */
    public final void setCurrentProgress(float currentProgress) {
        this.currentProgress = Math.max(0, Math.min(totalProgress, currentProgress));
    }

    /** Sets the total progress of the transition.
     * @param totalProgress The new total progress.
     */
    public final void setTotalProgress(float totalProgress) {
        if (totalProgress <= 0)
            throw new IllegalArgumentException("Total progress must be greater than 0");
        currentProgress = 0;
        this.totalProgress = totalProgress;
    }

    /** Sets the transition to its ending point where {@code currentProgress = totalProgress}.
     */
    public final void setToEnd() {
        this.currentProgress = totalProgress;
    }

    /** Sets the transition to its starting point where {@code currentProgress = 0}.
     */
    public final void setToStart() {
        this.currentProgress = 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + start + ", " + end + ']';
    }
}
