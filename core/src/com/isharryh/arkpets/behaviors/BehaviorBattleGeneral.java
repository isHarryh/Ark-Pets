/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.behaviors;

import com.isharryh.arkpets.utils.AnimData;
import com.isharryh.arkpets.utils.AnimData.AnimAutoData;
import com.isharryh.arkpets.ArkConfig;


public class BehaviorBattleGeneral extends Behavior {

    public BehaviorBattleGeneral(ArkConfig $config, String[] $animList) {
        super($config, $animList);
        action_list = new AnimAutoData[] {
            new AnimAutoData(new AnimData(getProperAnimName("Idle"), true, true, 0, 0),
                    4f, (int) (256 / Math.sqrt(config.behavior_ai_activation))),
            new AnimAutoData(new AnimData(getProperAnimName("Move"), true, true, 0, 1),
                    2f, 32*(config.behavior_allow_walk?1:0)),
            new AnimAutoData(new AnimData(getProperAnimName("Move"), true, true, 0, -1),
                    2f, 32*(config.behavior_allow_walk?1:0))
        };
    }

    private String getProperAnimName(String $wanted) {
        for (String s : anim_list)
            if (s.contains($wanted) && s.contains("Loop"))
                return s;
        for (String s : anim_list)
            if (s.contains($wanted) && !s.contains("End") && !s.contains("Begin"))
                return s;
        for (String s : anim_list)
            if (s.contains($wanted))
                return s;
        return "";
    }

    public static boolean match(String[] animList) {
        int flag = 0b111;
        for (String s : animList) {
            if (s != null) {
                if (s.contains("Idle"))
                    flag &= 0b011;
                if (s.contains("Move"))
                    flag &= 0b101;
                if (s.contains("Attack"))
                    flag &= 0b110;
            }
        }
        return flag == 0;
    }

    public AnimData defaultAnim() {
        return new AnimData(getProperAnimName("Idle"), true, true);
    }

    public AnimData clickEnd() {
        return config.behavior_allow_interact?new AnimData(getProperAnimName("Attack"), false, false,
                action_list[0].ANIM) : null;
    }

    public AnimData dragStart() {
        return action_list[0].ANIM;
    }

    public AnimData dragEnd() {
        return clickEnd();
    }

    public AnimData drop() {
        return clickEnd();
    }
}
