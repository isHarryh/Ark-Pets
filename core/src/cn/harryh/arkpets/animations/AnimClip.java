/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.animations;

import cn.harryh.arkpets.utils.Logger;
import com.esotericsoftware.spine.Animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** One Animation Clip is corresponding to one certain Spine {@link Animation}.
 * @since ArkPets 2.3
 */
public class AnimClip {
    public final String fullName;
    public final String baseName;
    public final AnimType type;
    public final AnimModifier modifier;
    public final AnimStage stage;
    public final float duration;

    /** An animation type represents a series of identical animation.
     */
    public enum AnimType {
        NONE("", 0),
        DEFAULT("^Default.?$", 0),
        IDLE("^((Idle)|(Relax)).?$", 0),
        MOVE("^Move.?$", 0),
        SIT("^Sit$", 50),
        SLEEP("^Sleep$", 50),
        SPECIAL("^Special$", 0),
        INTERACT("^Interact$", 0),
        ATTACK("^((Attack)|(Combat)).?$", 0),
        SKILL("^Skill.?$", 0),
        START("^Start.?$", 0),
        DIE("^Die.?$", 0),
        REVIVE("^((Revive)|(Reborn)).?$", 0);

        /** The regex pattern of this type of animation, which is case-insensitive. */
        public final Pattern pattern;
        /** The y-axis offset that should be applied on this type of animation when rendering. */
        public final int offsetY;

        AnimType(String pattern, int offsetY) {
            this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            this.offsetY = offsetY;
        }

        public Matcher matcher(String input) {
            return pattern.matcher(input);
        }
    }

    /** An animation modifier represents the attributes of a certain animation.
     */
    public enum AnimModifier {
        NONE(""),
        BEGIN("^((Begin)|(Start)|(Up)|(Appear))$"),
        LOOP("^Loop$"),
        END("^((End)|(Down)|(Disappear))$");

        public final Pattern pattern;

        public Matcher matcher(String input) {
            return pattern.matcher(input);
        }

        AnimModifier(String regex) {
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }
    }

    /** An animation stage represents the character's stage to which the animation belongs.
     */
    public static final class AnimStage {
        private int id;

        private enum AnimCommonStage {
            NONE(""),
            C_NUMBER("^C\\d$"),
            ALPHABET("^[A-Z]$");

            public final Pattern pattern;

            public Matcher matcher(String input) {
                return pattern.matcher(input);
            }

            AnimCommonStage(String regex) {
                pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            }
        }

        public AnimStage(int id) {
            this.id = id;
        }

        public AnimStage(String name) {
            this(0);
            try {
                if (AnimCommonStage.C_NUMBER.matcher(name).matches()) {
                    updateId(Integer.parseInt(name.substring(1)));
                } else if (AnimCommonStage.ALPHABET.matcher(name).matches()) {
                    int valueOfA = Character.getNumericValue('A');
                    int valueOfThis = Character.getNumericValue(name.toUpperCase().charAt(0));
                    int alphabetIndex = valueOfThis - valueOfA + 1;
                    if (alphabetIndex < 1)
                        throw new IllegalArgumentException("Unexpected numeric value.");
                    updateId(alphabetIndex);
                }
            } catch (RuntimeException e) {
                Logger.warn("Animation", "Failed to recognized the stage name \"" + name + "\".");
                updateId(-1);
            }
        }

        public int id() {
            return id;
        }

        public void updateId(int id) {
            this.id = Math.max(-1, id);
        }

        @Override
        public String toString() {
            return "AnimStage C" + id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AnimStage animStage = (AnimStage)o;
            return id == animStage.id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    /** Initializes an animation clip.
     * @param name The full name of the animation.
     * @param duration The duration of the animation (second).
     */
    public AnimClip(String name, float duration) {
        ArrayList<String> elements = split(name);
        RecognitionResult<AnimType> temp = recognizeType(elements);
        this.fullName = name;
        this.baseName = temp.according;
        this.type = temp.result;
        this.modifier = recognizeModifier(elements).result;
        this.stage = recognizeStage(elements).result;
        this.duration = duration;
    }

    /** Initializes an animation clip.
     * @param anim The Spine {@link Animation} instance.
     */
    public AnimClip(Animation anim) {
        this(anim.getName(), anim.getDuration());
    }

    private record SeparationResult(String result, List<String> fragments) {
    }

    private record RecognitionResult<T>(T result, String according) {
    }

    private ArrayList<String> split(String string) {
        return new ArrayList<>(List.of(string.split("_")));
    }

    private SeparationResult separateNumber(String string) {
        char[] chars = string.toCharArray();
        ArrayList<String> fragment = new ArrayList<>();
        int headTo = -1;
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isDigit(chars[i])) {
                headTo = i - 1;
                break;
            }
        }
        if (headTo > -1)
            fragment.add(String.valueOf(chars, 0, headTo + 1));
        int tailBegin = chars.length;
        for (int i = chars.length - 1; i >= 0; i--){
            if (!Character.isDigit(chars[i])) {
                tailBegin = i + 1;
                break;
            }
        }
        if (tailBegin < chars.length)
            fragment.add(String.valueOf(chars, tailBegin, chars.length - tailBegin));
        String core = String.valueOf(chars, headTo + 1, tailBegin - headTo - 1);
        return new SeparationResult(core, fragment);
    }

    private RecognitionResult<AnimModifier> recognizeModifier(ArrayList<String> elements) {
        for (var iterator = elements.listIterator(elements.size()); iterator.hasPrevious(); ) {
            String s = iterator.previous();
            for (AnimModifier a : AnimModifier.values()) {
                if (a.matcher(s).matches()) {
                    iterator.remove();
                    return new RecognitionResult<>(a, s);
                }
            }
        }
        return new RecognitionResult<>(AnimModifier.NONE, "");
    }

    private RecognitionResult<AnimType> recognizeType(ArrayList<String> elements) {
        for (var iterator = elements.listIterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            for (AnimType a : AnimType.values()) {
                if (a.matcher(s).matches()) {
                    // NO iterator.remove();
                    return new RecognitionResult<>(a, s);
                }
            }
        }
        return new RecognitionResult<>(AnimType.NONE, "");
    }

    private RecognitionResult<AnimStage> recognizeStage(ArrayList<String> elements) {
        final AnimType[] exMatchingTypes = new AnimType[]{AnimType.IDLE, AnimType.MOVE/*, AnimType.ATTACK*/};
        final Pattern exMatchingPattern = Pattern.compile("\\d", Pattern.CASE_INSENSITIVE);
        for (var iterator = elements.listIterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            /* Simple matching */
            for (AnimStage.AnimCommonStage a : AnimStage.AnimCommonStage.values()) {
                if (a.matcher(s).matches()) {
                    iterator.remove();
                    return new RecognitionResult<>(new AnimStage(s), s);
                }
            }
            /* Extensive matching : Since the naming system of Arknights' animation is so f**king a mess,
            it is necessary to do additional matching in order to figure out their stage's info correctly. */
            SeparationResult separation = separateNumber(s);
            for (AnimType t : exMatchingTypes) {
                if (t.matcher(separation.result()).matches()) {
                    String s1;
                    if (!separation.fragments().isEmpty()) s1 = separation.fragments().get(0);
                    else if (iterator.hasNext()) s1 = elements.get(iterator.nextIndex());
                    else break;
                    if (exMatchingPattern.matcher(s1).matches()) {
                        // NO iterator.remove();
                        return new RecognitionResult<>(new AnimStage('C' + s1), s1);
                    }
                }
            }
        }
        return new RecognitionResult<>(new AnimStage(0), "");
    }

    @Override
    public String toString() {
        ArrayList<String> stringArray = new ArrayList<>();
        stringArray.add("AnimClip");
        stringArray.add('\"' + fullName + '\"');
        if (stage != null)
            stringArray.add('C' + String.valueOf(stage.id));
        if (type != null)
            stringArray.add('<' + type.toString().toUpperCase() + '>');
        if (modifier != null && !modifier.equals(AnimModifier.NONE))
            stringArray.add('(' + modifier.toString().toLowerCase() + ')');

        StringBuilder string = new StringBuilder();
        for (var iterator = stringArray.listIterator(); iterator.hasNext(); ) {
            string.append(iterator.next());
            if (iterator.hasNext())
                string.append(' ');
        }
        return string.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimClip animClip = (AnimClip) o;
        return Float.compare(duration, animClip.duration) == 0 && Objects.equals(fullName, animClip.fullName) && Objects.equals(baseName, animClip.baseName) && type == animClip.type && modifier == animClip.modifier && Objects.equals(stage, animClip.stage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, baseName, type, modifier, stage, duration);
    }
}
