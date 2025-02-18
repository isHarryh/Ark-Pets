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
    private static Method lwtOnMain;

    public static void init() {
        msgSend = Function.getFunction("objc.A", "objc_msgSend");
        if (Platform.isIntel()) {
            msgSend_stret = Function.getFunction("objc.A", "objc_msgSend_stret");
        }
        try {
            Class<?> lwckit = Class.forName("sun.lwawt.macosx.LWCToolkit");
            lwtOnMain = lwckit.getDeclaredMethod("performOnMainThreadAfterDelay", Runnable.class, long.class);
            lwtOnMain.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    public static void runOnAppKit(Runnable r) {
        try {
            lwtOnMain.invoke(null, r, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private interface Runtime extends Library {
        Runtime INSTANCE = Native.load("objc.A", Runtime.class);

        Pointer sel_getUid(String msg);

        Pointer objc_lookUpClass(String name);
    }
}
