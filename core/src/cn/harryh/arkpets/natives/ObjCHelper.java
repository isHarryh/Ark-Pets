package cn.harryh.arkpets.natives;

import cn.harryh.arkpets.Const;
import com.sun.jna.*;

import java.util.HashMap;
import java.util.Map;


public class ObjCHelper {
    private static final Map<String, Pointer> selMap = new HashMap<>();
    private static final Map<String, Pointer> clsMap = new HashMap<>();
    public static Function msgSend;
    public static Function msgSend_stret;
    private static Pointer MAIN_QUEUE;
    private static Function DISPATCH_SYNC;

    public static void init() {
        msgSend = Function.getFunction("objc.A", "objc_msgSend");
        if (Const.isIntel) {
            msgSend_stret = Function.getFunction("objc.A", "objc_msgSend_stret");
        }
        NativeLibrary sys = NativeLibrary.getInstance("System");
        DISPATCH_SYNC = sys.getFunction("dispatch_sync_f");
        MAIN_QUEUE = sys.getGlobalVariableAddress("_dispatch_main_q");
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

    public static void runOnMain(ThreadCallback cb) {
        DISPATCH_SYNC.invoke(Void.class, new Object[]{MAIN_QUEUE, Pointer.NULL, cb});
    }

    private interface Runtime extends Library {
        Runtime INSTANCE = Native.load("objc.A", Runtime.class);

        Pointer sel_getUid(String msg);

        Pointer objc_lookUpClass(String name);
    }

    public interface ThreadCallback extends Callback {
        void callback(Pointer ctx);
    }
}
