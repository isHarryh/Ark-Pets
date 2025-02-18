package cn.harryh.arkpets.natives;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.mac.CoreFoundation;


public class CoreGraphicsHelper {
    public static CoreFoundation.CFStringRef kCGWindowNumber;
    public static CoreFoundation.CFStringRef kCGWindowLayer;
    public static CoreFoundation.CFStringRef kCGWindowBounds;
    public static CoreFoundation.CFStringRef kCGWindowName;
    public static CoreFoundation.CFStringRef kCGWindowOwnerName;

    public static final int kCGWindowListExcludeDesktopElements = (1 << 4);
    public static final int kCGWindowListOptionOnScreenOnly = 1;

    public static final int NSStatusWindowLevel = 25;
    public static final int NSNormalWindowLevel = 0;

    public static void initCG() {
        kCGWindowNumber = CoreFoundation.CFStringRef.createCFString("kCGWindowNumber");
        kCGWindowBounds = CoreFoundation.CFStringRef.createCFString("kCGWindowBounds");
        kCGWindowLayer = CoreFoundation.CFStringRef.createCFString("kCGWindowLayer");
        kCGWindowName = CoreFoundation.CFStringRef.createCFString("kCGWindowName");
        kCGWindowOwnerName = CoreFoundation.CFStringRef.createCFString("kCGWindowOwnerName");
    }

    public static void freeCG() {
        kCGWindowNumber.release();
        kCGWindowLayer.release();
        kCGWindowName.release();
        kCGWindowBounds.release();
        kCGWindowOwnerName.release();
    }

    public interface CGInterface extends Library {
        CGInterface INSTANCE = Native.load("CoreGraphics", CGInterface.class);

        CoreFoundation.CFArrayRef CGWindowListCopyWindowInfo(int option, int relativeToWindow);

        CoreFoundation.CFArrayRef CGWindowListCreateDescriptionFromArray(CoreFoundation.CFArrayRef windowArray);

        boolean CGRectMakeWithDictionaryRepresentation(CoreFoundation.CFDictionaryRef dict, CGRect.ByReference rect);

        CoreFoundation.CFDictionaryRef CGSessionCopyCurrentDictionary();
    }

    @Structure.FieldOrder({"origin", "size"})
    public static class CGRect extends Structure {
        public CGPoint origin;
        public CGSize size;

        public static class ByReference extends CGRect implements Structure.ByReference {
        }

        public static class ByValue extends CGRect implements Structure.ByValue {
        }
    }

    @Structure.FieldOrder({"x", "y"})
    public static class CGPoint extends Structure {
        public double x;
        public double y;
    }

    @Structure.FieldOrder({"width", "height"})
    public static class CGSize extends Structure {
        public double width;
        public double height;
    }
}
