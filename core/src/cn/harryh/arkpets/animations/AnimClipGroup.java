/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import cn.harryh.arkpets.animations.AnimClip.*;
import com.esotericsoftware.spine.Animation;

import java.util.*;


/** The class implements the Collection of {@link AnimClip}.
 * @since ArkPets 2.3
 */
public class AnimClipGroup implements Collection<AnimClip> {
    protected final ArrayList<AnimClip> animClipList;

    public AnimClipGroup(Animation[] animList) {
        this.animClipList = new ArrayList<>();
        for (Animation a : animList)
            this.animClipList.add(new AnimClip(a));
        sortStages();
    }

    protected AnimClipGroup(Collection<AnimClip> animClipList) {
        this.animClipList = new ArrayList<>(animClipList);
    }

    /** Finds the animations that match the given type.
     * @param type The specified animation type.
     * @return A group of matched animation.
     */
    public AnimClipGroup findAnimations(AnimType type) {
        ArrayList<AnimClip> found = new ArrayList<>();
        for (AnimClip a : animClipList) {
            if (a.type == type) {
                found.add(a);
            }
        }
        return new AnimClipGroup(found);
    }

    /** Finds the animations that match the given modifier.
     * @param modifier The specified animation modifier.
     * @return A group of matched animation.
     */
    public AnimClipGroup findAnimations(AnimModifier modifier) {
        ArrayList<AnimClip> found = new ArrayList<>();
        for (AnimClip a : animClipList) {
            if (a.modifier == modifier) {
                found.add(a);
            }
        }
        return new AnimClipGroup(found);
    }

    /** Finds the animations that match the given stage.
     * @param stage The specified animation stage.
     * @return A group of matched animation.
     */
    public AnimClipGroup findAnimations(AnimStage stage) {
        ArrayList<AnimClip> found = new ArrayList<>();
        for (AnimClip a : animClipList) {
            if (a.stage.id() == stage.id()) {
                found.add(a);
            }
        }
        return new AnimClipGroup(found);
    }

    /** Finds all the animations by their stages.
     * @return A map of {@code AnimStage} -> {@code AnimClipGroup}.
     */
    public HashMap<AnimStage, AnimClipGroup> clusterByStage() {
        HashMap<AnimStage, AnimClipGroup> result = new HashMap<>();
        for (AnimClip a : animClipList) {
            if (result.containsKey(a.stage))
                result.get(a.stage).add(a);
            else
                result.put(a.stage, new AnimClipGroup(List.of(a)));
        }
        return result;
    }

    /** Gets the animation clip at the given index.
     * @param index The specified index.
     * @return The animation clip.
     */
    public AnimClip get(int index) {
        if (animClipList.size() < index + 1 || index < 0)
            return null;
        return animClipList.get(index);
    }

    /** Draws a streamed animation data from this animation clip group.
     * <hr>
     * A steamed animation is a series of animation which may consist of the {@code BEGIN} animation, the {@code LOOP}
     * animation and the {@code END} animation.
     * @param type The specified animation type.
     * @return The animation data whose animation clip will be none if not found.
     */
    public AnimData getStreamedAnimData(AnimType type) {
        AnimClipGroup found = this.findAnimations(type);
        AnimClip begin = found.findAnimations(AnimModifier.BEGIN).get(0);
        AnimClip end = found.findAnimations(AnimModifier.END).get(0);
        AnimClip loop = found.findAnimations(AnimModifier.LOOP).get(0);
        AnimClip none = found.findAnimations(AnimModifier.NONE).get(0);
        AnimClip center = loop != null ? loop : none;
        if (center != null) {
            AnimData result = new AnimData(center);
            if (begin != null)
                result = new AnimData(begin).join(result);
            if (end != null)
                result = result.join(new AnimData(end));
            return result;
        }
        return new AnimData(null);
    }

    /** Draws a loop animation data from this animation clip group.
     * <hr>
     * A loop animation is a single animation which could be played in loop and typically could be interrupted.
     * @param type The specified animation type.
     * @return The animation data whose animation clip will be none if not found.
     */
    public AnimData getLoopAnimData( AnimType type) {
        AnimClipGroup found = this.findAnimations(type);
        AnimClip loop = found.findAnimations(AnimModifier.LOOP).get(0);
        AnimClip none = found.findAnimations(AnimModifier.NONE).get(0);
        AnimClip center = loop != null ? loop : none;
        if (center != null)
            return new AnimData(center, null, true, false, 0, 0);
        return new AnimData(null);
    }

    /** Draws a strict animation data from this animation clip group.
     * <hr>
     * A strict animation is a single animation which couldn't be interrupted and typically should be played once.
     * @param type The specified animation type.
     * @return The animation data whose animation clip will be none if not found.
     */
    public AnimData getStrictAnimData(AnimType type) {
        AnimClipGroup found = this.findAnimations(type);
        AnimClip loop = found.findAnimations(AnimModifier.LOOP).get(0);
        AnimClip none = found.findAnimations(AnimModifier.NONE).get(0);
        AnimClip center = loop != null ? loop : none;
        if (center != null)
            return new AnimData(center, null, false, true);
        return new AnimData(null);
    }

    protected void sortStages() {
        HashSet<Integer> existing = new HashSet<>();
        for (AnimClip a : animClipList)
            existing.add(a.stage.id());
        ArrayList<Integer> ordered = new ArrayList<>(existing.stream().sorted().toList());
        ordered.removeIf(n -> n < 0);
        for (AnimClip a : animClipList) {
            int id = a.stage.id();
            a.stage.updateId(ordered.contains(id) ? ordered.indexOf(id) : -1);
        }
    }

    @Override
    public Iterator<AnimClip> iterator() {
        return animClipList.iterator();
    }

    @Override
    public boolean add(AnimClip animClip) {
        return animClipList.add(animClip);
    }

    @Override
    public boolean addAll(Collection<? extends AnimClip> c) {
        return animClipList.addAll(c);
    }

    @Override
    public boolean contains(Object o) {
        return animClipList.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return animClipList.containsAll(c);
    }

    @Override
    public boolean remove(Object o) {
        return animClipList.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return animClipList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return animClipList.retainAll(c);
    }

    @Override
    public void clear() {
        animClipList.clear();
    }

    @Override
    public boolean isEmpty() {
        return animClipList.isEmpty();
    }

    @Override
    public int size() {
        return animClipList.size();
    }

    @Override
    public Object[] toArray() {
        return animClipList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return animClipList.toArray(a);
    }
}
