/** Copyright (c) 2022, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets.behaviors;

import com.badlogic.gdx.utils.Array;
import com.isharryh.arkpets.ArkConfig;
import com.isharryh.arkpets.utils.AICtrl;
import com.isharryh.arkpets.utils.AnimCtrl;


public class BehaviorOperBuild3 extends Behavior {

    public BehaviorOperBuild3(ArkConfig $config) {
        super($config);
        action_list = new AICtrl[] {
            new AICtrl(new AnimCtrl("Relax", true, true, 0, 0),
                    4f, (int) (256 / Math.sqrt(config.behavior_ai_activation))),
            new AICtrl(new AnimCtrl("Move", true, true, 0, 1),
                    2f, 32*(config.behavior_allow_walk?1:0)),
            new AICtrl(new AnimCtrl("Move", true, true, 0, -1),
                    2f, 32*(config.behavior_allow_walk?1:0))
        };
    }

    public static boolean match(String[] animList) {
        Array<String> arr = new Array<>(animList);
        if (!arr.contains("Interact", false))
            return false;
        if (!arr.contains("Relax", false))
            return false;
        if (!arr.contains("Move", false))
            return false;
        return true;
    }

    public AnimCtrl defaultAnim() {
        return new AnimCtrl("Relax", true, true);
    }

    public AnimCtrl clickEnd() {
        return config.behavior_allow_interact?new AnimCtrl("Interact", false, false,
                new AnimCtrl("Relax", true, true)) : null;
    }

    public AnimCtrl dragStart() {
        return action_list[0].ANIM;
    }

    public AnimCtrl drop() {
        return config.behavior_allow_interact?new AnimCtrl("Interact", false, false,
                new AnimCtrl("Relax", true, true)) : null;
    }
}
