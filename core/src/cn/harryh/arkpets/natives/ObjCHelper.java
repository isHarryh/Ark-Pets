package cn.harryh.arkpets.natives;

import com.sun.jna.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class ObjCHelper {
    private static final Map<String, Pointer> selMap = new HashMap<>();
    private static final Map<String, Pointer> clsMap = new HashMap<>();
    public static Function msgSend;
    public static Function msgSend_stret;

    public static void init() {
        msgSend = Function.getFunction("objc.A", "objc_msgSend");
        if (Platform.isIntel()) {
            msgSend_stret = Function.getFunction("objc.A", "objc_msgSend_stret");
        }
    }

    public static Pointer cls(String cls) {
        Pointer ptr;
        ptr = clsMap.get(cls);
        if (ptr == null) {
            ptr = Runtime.INSTANCE.objc_lookUpClass(cls);
            if (ptr != null) clsMap.put(cls, ptr);
        }
        return ptr;
    }

    public static Pointer sel(String sel) {
        Pointer ptr;
        ptr = selMap.get(sel);
        if (ptr == null) {
            ptr = Runtime.INSTANCE.sel_getUid(sel);
            if (ptr != null) selMap.put(sel, ptr);
        }
        return ptr;
    }

    private interface Runtime extends Library {
        Runtime INSTANCE = Native.load("objc.A", Runtime.class);

        Pointer sel_getUid(String msg);

        Pointer objc_lookUpClass(String name);

        boolean class_addMethod(Pointer cls, Pointer name, Callback imp, String types);
    }

    public static void addRunOnAppKitMethod(Pointer targetCls, ThreadCallback cb, String name) {
        Runtime.INSTANCE.class_addMethod(targetCls, sel("apRunOnAppKit" + name), cb, "v@:");
    }

    public interface ThreadCallback extends Callback {
        void callback(Pointer id, Pointer _cmd);
    }
}
