package cn.harryh.arkpets.natives;

import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.Pointer;


/** @see <a href="https://github.com/isHarryh/Ark-Pets/issues/182">ArkPets Issue #182</a>
 */
public class MacHelper {
    public static void tryFixLayers(Pointer view) {
        if (view == null || Pointer.nativeValue(view) == 0) {
            Logger.error("System", "view is null");
            return;
        }

        try {
            Pointer layers = ObjCHelper.sendPointer(view, "layer");
            if (layers == null) {
                Logger.error("System", "view.layer is null");
                return;
            }

            Pointer sublayers = ObjCHelper.sendPointer(layers, "sublayers");
            if (sublayers == null) {
                Logger.error("System", "layer.sublayers is null");
                return;
            }

            Pointer firstLayer = ObjCHelper.sendPointer(sublayers, "firstObject");
            if (firstLayer == null) {
                Logger.error("System", "sublayers[0] is null");
                return;
            }

            ObjCHelper.sendVoid(firstLayer, "setOpaque:", false);
        } catch (RuntimeException e) {
            Logger.error("System", "Failed to fix Metal layer opacity, details see below.", e);
        }
    }
}
