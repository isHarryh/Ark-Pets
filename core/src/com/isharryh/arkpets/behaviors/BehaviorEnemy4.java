/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.behaviors;

import com.badlogic.gdx.utils.Array;
import com.isharryh.arkpets.ArkConfig;
import com.isharryh.arkpets.utils.AnimAutoData;
import com.isharryh.arkpets.utils.AnimData;


public class BehaviorEnemy4 extends Behavior {

    public BehaviorEnemy4(ArkConfig $config) {
        super($config);
        action_list = new AnimAutoData[] {
            new AnimAutoData(new AnimData("W_Idle", true, true, 0, 0),
                    4f, (int) (256 / Math.sqrt(config.behavior_ai_activation))),
            new AnimAutoData(new AnimData("W_Move", true, true, 0, 1),
                    2f, 32*(config.behavior_allow_walk?1:0)),
            new AnimAutoData(new AnimData("W_Move", true, true, 0, -1),
                    2f, 32*(config.behavior_allow_walk?1:0))
        };
    }

    public static boolean match(String[] animList) {
        Array<String> arr = new Array<>(animList);
        if (!arr.contains("W_Idle", false))
            return false;
        if (!arr.contains("W_Attack", false))
            return false;
        return true;
    }

    public AnimData defaultAnim() {
        return new AnimData("W_Idle", true, true);
    }

    public AnimData clickEnd() {
        return config.behavior_allow_interact?new AnimData("W_Attack", false, false,
                new AnimData("W_Idle", true, true)) : null;
    }

    public AnimData dragStart() {
        return action_list[0].ANIM;
    }

    public AnimData dragEnd() {
        return clickEnd();
    }
}
