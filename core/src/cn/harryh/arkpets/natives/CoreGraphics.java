package cn.harryh.arkpets.natives;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;


public interface CoreGraphics extends Library {
    CFStringRef kCGWindowNumber = CFStringRef.createCFString("kCGWindowNumber");
    CFStringRef kCGWindowBounds = CFStringRef.createCFString("kCGWindowBounds");
    CFStringRef kCGWindowLayer = CFStringRef.createCFString("kCGWindowLayer");
    CFStringRef kCGWindowName = CFStringRef.createCFString("kCGWindowName");
    CFStringRef kCGWindowOwnerName = CFStringRef.createCFString("kCGWindowOwnerName");

    int kCGWindowListExcludeDesktopElements = (1 << 4);
    int kCGWindowListOptionOnScreenOnly = 1;

    int NSStatusWindowLevel = 25;
    int NSNormalWindowLevel = 0;

    CoreGraphics INSTANCE = Native.load("CoreGraphics", CoreGraphics.class);

    CFArrayRef CGWindowListCopyWindowInfo(int option, int relativeToWindow);

    CFArrayRef CGWindowListCreateDescriptionFromArray(CFArrayRef windowArray);

    boolean CGRectMakeWithDictionaryRepresentation(CFDictionaryRef dict, CGRect.ByReference rect);

    CFDictionaryRef CGSessionCopyCurrentDictionary();


    @Structure.FieldOrder({"origin", "size"})
    class CGRect extends Structure {
        public CGPoint origin;
        public CGSize size;

        public static class ByReference extends CGRect implements Structure.ByReference {
        }

        public static class ByValue extends CGRect implements Structure.ByValue {
        }
    }

    @Structure.FieldOrder({"x", "y"})
    class CGPoint extends Structure {
        public double x;
        public double y;
    }

    @Structure.FieldOrder({"width", "height"})
    class CGSize extends Structure {
        public double width;
        public double height;
    }
}
