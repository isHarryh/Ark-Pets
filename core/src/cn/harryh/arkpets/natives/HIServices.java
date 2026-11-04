package cn.harryh.arkpets.natives;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.ptr.PointerByReference;


/**
 * The macOS Accessibility API (HIServices), used to query the geometry of system UI elements.
 *
 * @see <a href="https://developer.apple.com/documentation/applicationservices/axuielement_h">AXUIElement</a>
 * @see <a href="https://developer.apple.com/documentation/applicationservices/axvalue_h">AXValue</a>
 */
public interface HIServices extends Library {
    HIServices INSTANCE = Native.load("ApplicationServices", HIServices.class);

    /** @see <a href="https://developer.apple.com/documentation/applicationservices/axerror">AXError</a> */
    int kAXErrorSuccess = 0;

    /** @see <a href="https://developer.apple.com/documentation/applicationservices/axvaluetype">AXValueType</a> */
    int kAXValueTypeCGSize = 2;
    int kAXValueTypeCGRect = 3;

    Pointer AXUIElementCreateSystemWide();

    Pointer AXUIElementCreateApplication(int pid);

    /** Returns a CFBoolean, so the raw byte is exposed instead of a 32-bit Java boolean. */
    byte AXIsProcessTrusted();

    /**
     * Copies the value of an attribute of an accessibility element.
     *
     * @param value The owner of the returned reference, which must be released by the caller.
     */
    int AXUIElementCopyAttributeValue(Pointer element, CoreFoundation.CFStringRef attribute, PointerByReference value);

    /**
     * Copies a range of values of an attribute of an accessibility element.
     *
     * @param values The owner of the returned array, which must be released by the caller.
     */
    int AXUIElementCopyAttributeValues(Pointer element, CoreFoundation.CFStringRef attribute,
                                       CoreFoundation.CFIndex index, CoreFoundation.CFIndex maxValues,
                                       PointerByReference values);

    /**
     * Copies the names of all attributes supported by an accessibility element.
     *
     * @param names The owner of the returned array, which must be released by the caller.
     */
    int AXUIElementCopyAttributeNames(Pointer element, PointerByReference names);

    int AXValueGetType(Pointer value);

    /**
     * Extracts the value wrapped by an {@code AXValueRef}.
     *
     * @param valuePtr The buffer to fill, whose size must match the requested type.
     * @return A CFBoolean, so the raw byte is exposed instead of a 32-bit Java boolean.
     */
    byte AXValueGetValue(Pointer value, int theType, Pointer valuePtr);
}
