package cn.harryh.arkpets.natives;

import cn.harryh.arkpets.utils.Logger;


/** @see <a href="https://github.com/isHarryh/Ark-Pets/issues/182">ArkPets Issue #182</a>
 */
public class MacHelper {
    public static void tryFixLayers(long view) {
        var layers = ObjCHelper.msgSend.invokePointer(new Object[]{view, ObjCHelper.sel("layer")});
        if (layers == null) {
            Logger.error("System", "view.layer is null");
            return;
        }

        var sublayers = ObjCHelper.msgSend.invokePointer(new Object[]{layers, ObjCHelper.sel("sublayers")
        });
        if (sublayers == null) {
            Logger.error("System", "layer.sublayers is null");
            return;
        }

        var firstLayer = ObjCHelper.msgSend.invokePointer(new Object[]{sublayers, ObjCHelper.sel("firstObject")});
        if (firstLayer == null) {
            Logger.error("System", "sublayers[0] is null");
            return;
        }

        ObjCHelper.msgSend.invoke(new Object[]{firstLayer, ObjCHelper.sel("setOpaque:"), 0});
    }
}
