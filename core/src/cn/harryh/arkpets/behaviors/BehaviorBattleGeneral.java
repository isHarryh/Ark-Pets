/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.behaviors;

import cn.harryh.arkpets.utils.AnimData;
import cn.harryh.arkpets.ArkConfig;

import static cn.harryh.arkpets.Const.*;


public class BehaviorBattleGeneral extends Behavior {
    private final String[] notWantedList = {"Begin", "End", "Die"};

    public BehaviorBattleGeneral(ArkConfig $config, String[] $animList) {
        super($config, $animList);
        action_list = new AnimData.AnimAutoData[] {
            new AnimData.AnimAutoData(new AnimData(getProperAnimName("Idle", notWantedList), true, true, 0, 0),
                    behaviorMinTimeLv2, (int)(behaviorBaseWeight / Math.sqrt(config.behavior_ai_activation))),
            new AnimData.AnimAutoData(new AnimData(getProperAnimName("Move", notWantedList), true, true, 0, 1),
                    behaviorMinTimeLv1, behaviorWeightLv1 * (config.behavior_allow_walk?1:0)),
            new AnimData.AnimAutoData(new AnimData(getProperAnimName("Move", notWantedList), true, true, 0, -1),
                    behaviorMinTimeLv1, behaviorWeightLv1 * (config.behavior_allow_walk?1:0))
        };
    }

    public boolean match(String[] animList) {
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
        return new AnimData(getProperAnimName("Idle", notWantedList), true, true);
    }

    public AnimData clickEnd() {
        return config.behavior_allow_interact?new AnimData(getProperAnimName("Attack", notWantedList), false, false,
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
