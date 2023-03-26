/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.behaviors;

import com.isharryh.arkpets.utils.AnimData;
import com.isharryh.arkpets.utils.AnimData.AnimAutoData;
import com.isharryh.arkpets.ArkConfig;


public class BehaviorBattleGeneral3 extends Behavior {

    public BehaviorBattleGeneral3(ArkConfig $config, String[] $animList) {
        super($config, $animList);
        action_list = new AnimAutoData[] {
            new AnimAutoData(new AnimData(getProperAnimName("Idle"), true, true, 0, 0),
                    4f, (int) (256 / Math.sqrt(config.behavior_ai_activation)))
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
        int flag = 0b11;
        for (String s : animList) {
            if (s != null) {
                if (s.contains("Idle"))
                    flag &= 0b01;
                if (s.contains("Attack"))
                    flag &= 0b10;
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
}
