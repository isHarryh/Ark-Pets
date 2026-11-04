package cn.harryh.arkpets.natives;

import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.ptr.PointerByReference;


/** @see <a href="https://github.com/isHarryh/Ark-Pets/issues/182">ArkPets Issue #182</a>
 */
public class MacHelper {
    private static final CoreFoundation.CFStringRef AX_CHILDREN = CoreFoundation.CFStringRef.createCFString("AXChildren");
    private static final CoreFoundation.CFStringRef AX_ROLE = CoreFoundation.CFStringRef.createCFString("AXRole");
    private static final CoreFoundation.CFStringRef AX_FRAME = CoreFoundation.CFStringRef.createCFString("AXFrame");
    private static final String AX_ROLE_LIST = "AXList";

    /** Whether the missing accessibility permission has been reported, to avoid log flooding. */
    private static volatile boolean accessibilityUntrustedReported = false;

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

    public static long findDockPid() {
        var dock = ProcessHandle
                .allProcesses()
                .filter((e) -> e.info().command().isPresent() && e.info().command().get().contains("Dock.app/Contents/MacOS/Dock"))
                .findFirst();
        return dock.map(ProcessHandle::pid).orElse(-1L);
    }

    /**
     * Gets the frame of the Dock.
     * <p>
     * The Dock only exposes its geometry through the {@code AXList} child of its application
     * element; the application element itself always reports a zero-sized {@code AXSize}.
     * The frame follows the slide animation of an auto-hidden Dock, so an origin that is
     * moved off-screen means that the Dock is currently hidden.
     * <p>
     * When multiple displays are configured with separate Docks, the first {@code AXList}
     * element is used.
     *
     * @param pid The process ID of the Dock.
     * @return The frame in screen coordinates whose origin is at the top-left corner,
     * or {@code null} if the accessibility API is unavailable or the frame cannot be read.
     */
    public static CoreGraphics.CGRect getDockRealFrame(int pid) {
        if (pid <= 0) {
            Logger.debug("System", "Dock process is not available, skip reading the Dock frame");
            return null;
        }

        if (HIServices.INSTANCE.AXIsProcessTrusted() == 0) {
            if (!accessibilityUntrustedReported) {
                accessibilityUntrustedReported = true;
                Logger.warn("System", "Accessibility permission is not granted, cannot read the Dock frame");
            }
            return null;
        }
        accessibilityUntrustedReported = false;

        Pointer app = HIServices.INSTANCE.AXUIElementCreateApplication(pid);
        if (app == null || Pointer.nativeValue(app) == 0) {
            Logger.warn("System", "Failed to create the accessibility element of the Dock");
            return null;
        }

        CoreFoundation.CFArrayRef children = null;
        try {
            PointerByReference ref = new PointerByReference();
            int err = HIServices.INSTANCE.AXUIElementCopyAttributeValue(app, AX_CHILDREN, ref);
            if (err != HIServices.kAXErrorSuccess || ref.getValue() == null) {
                Logger.debug("System", "Failed to copy AXChildren of the Dock, error: " + err);
                return null;
            }
            children = new CoreFoundation.CFArrayRef(ref.getValue());
            for (int i = 0; i < children.getCount(); i++) {
                Pointer element = children.getValueAtIndex(i);
                if (element != null && AX_ROLE_LIST.equals(getStringAttribute(element, AX_ROLE)))
                    return getFrameAttribute(element, AX_FRAME);
            }
            Logger.debug("System", "No AXList element found in the Dock");
            return null;
        } finally {
            if (children != null)
                children.release();
            new CoreFoundation.CFTypeRef(app).release();
        }
    }

    private static String getStringAttribute(Pointer element, CoreFoundation.CFStringRef attribute) {
        PointerByReference ref = new PointerByReference();
        if (HIServices.INSTANCE.AXUIElementCopyAttributeValue(element, attribute, ref) != HIServices.kAXErrorSuccess
                || ref.getValue() == null)
            return null;
        CoreFoundation.CFStringRef value = new CoreFoundation.CFStringRef(ref.getValue());
        try {
            return value.stringValue();
        } finally {
            value.release();
        }
    }

    private static CoreGraphics.CGRect getFrameAttribute(Pointer element, CoreFoundation.CFStringRef attribute) {
        PointerByReference ref = new PointerByReference();
        if (HIServices.INSTANCE.AXUIElementCopyAttributeValue(element, attribute, ref) != HIServices.kAXErrorSuccess
                || ref.getValue() == null)
            return null;
        CoreFoundation.CFTypeRef value = new CoreFoundation.CFTypeRef(ref.getValue());
        try {
            int type = HIServices.INSTANCE.AXValueGetType(ref.getValue());
            if (type != HIServices.kAXValueTypeCGRect) {
                Logger.warn("System", "Unexpected AXValue type of the Dock frame: " + type);
                return null;
            }
            CoreGraphics.CGRect frame = new CoreGraphics.CGRect();
            if (HIServices.INSTANCE.AXValueGetValue(ref.getValue(), HIServices.kAXValueTypeCGRect, frame.getPointer()) == 0)
                return null;
            frame.read();
            if (frame.size.width <= 0 || frame.size.height <= 0) {
                Logger.debug("System", "Invalid Dock frame size: " + frame.size.width + "x" + frame.size.height);
                return null;
            }
            return frame;
        } finally {
            value.release();
        }
    }
}
