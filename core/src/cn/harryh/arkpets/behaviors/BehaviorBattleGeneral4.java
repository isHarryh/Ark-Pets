/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.behaviors;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.utils.AnimData;

import static cn.harryh.arkpets.Const.*;


public class BehaviorBattleGeneral4 extends Behavior {

    public BehaviorBattleGeneral4(ArkConfig $config, String[] $animList) {
        super($config, $animList);
        action_list = new AnimData.AnimAutoData[] {
            new AnimData.AnimAutoData(new AnimData(getProperAnimName("Idle"), true, true, 0, 0),
                    behaviorMinTimeLv2, (int)(behaviorBaseWeight / Math.sqrt(config.behavior_ai_activation)))
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
        int flag = 0b1;
        for (String s : animList) {
            if (s != null) {
                if (s.contains("Idle"))
                    flag &= 0b0;
            }
        }
        return flag == 0;
    }

    public AnimData defaultAnim() {
        return new AnimData(getProperAnimName("Idle"), true, true);
    }

    public AnimData clickEnd() {
        return action_list[0].ANIM;
    }

    public AnimData dragStart() {
        return action_list[0].ANIM;
    }

    public AnimData dragEnd() {
        return clickEnd();
    }
}
