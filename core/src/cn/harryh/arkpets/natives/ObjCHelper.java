package cn.harryh.arkpets.natives;

import cn.harryh.arkpets.utils.Logger;
import com.sun.jna.Callback;
import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A thin, type-aware bridge to the macOS Objective-C runtime.
 * <p>
 * This helper is the only place that talks to {@code libobjc} directly. All message sends
 * are normalized so that integer arguments are always passed as 64-bit values, matching
 * the {@code NSInteger} ABI on both x86_64 and arm64.
 */
public final class ObjCHelper {
    private static final String OBJC_LIB = "objc.A"; // Resolved by JNA as libobjc.A.dylib
    private static final String TAG = "ObjCHelper";

    private static volatile boolean initialized;
    private static Function msgSend;
    private static Function dispatchSync;
    private static Function pthreadMainNp;
    private static Pointer mainQueue;
    private static final Map<String, Pointer> SEL_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Pointer> CLS_CACHE = new ConcurrentHashMap<>();

    private ObjCHelper() {
    }

    /**
     * Initializes the Objective-C runtime bindings.
     * <p>
     * This method is idempotent and thread-safe, so it may be called from any entry point.
     */
    public static void init() {
        if (initialized) return;
        synchronized (ObjCHelper.class) {
            if (initialized) return;
            msgSend = Function.getFunction(OBJC_LIB, "objc_msgSend");
            NativeLibrary system = NativeLibrary.getInstance("System");
            dispatchSync = system.getFunction("dispatch_sync_f");
            mainQueue = system.getGlobalVariableAddress("_dispatch_main_q");
            try {
                pthreadMainNp = system.getFunction("pthread_main_np");
            } catch (UnsatisfiedLinkError e) {
                pthreadMainNp = null;
                Logger.warn(TAG, "pthread_main_np is unavailable; main queue tasks may deadlock on the main thread");
            }
            initialized = true;
        }
    }

    /**
     * Looks up an Objective-C class by name.
     *
     * @param name The class name.
     * @return The class pointer.
     * @throws IllegalArgumentException If the class cannot be found.
     */
    public static Pointer cls(String name) {
        return CLS_CACHE.computeIfAbsent(name, key -> {
            Pointer ptr = Runtime.INSTANCE.objc_lookUpClass(key);
            if (ptr == null) throw new IllegalArgumentException("Objective-C class not found: " + key);
            return ptr;
        });
    }

    /**
     * Looks up an Objective-C selector by name.
     *
     * @param name The selector name.
     * @return The selector pointer.
     * @throws IllegalArgumentException If the selector cannot be resolved.
     */
    public static Pointer sel(String name) {
        return SEL_CACHE.computeIfAbsent(name, key -> {
            Pointer ptr = Runtime.INSTANCE.sel_getUid(key);
            if (ptr == null) throw new IllegalArgumentException("Objective-C selector not found: " + key);
            return ptr;
        });
    }

    /**
     * Sends a message whose return type is {@code void}.
     */
    public static void sendVoid(Pointer receiver, String selector, Object... args) {
        ensureInit();
        msgSend.invokeVoid(buildArgs(receiver, selector, args));
    }

    /**
     * Sends a message whose return type is a C {@code int} or Objective-C {@code BOOL}.
     */
    public static int sendInt(Pointer receiver, String selector, Object... args) {
        ensureInit();
        return msgSend.invokeInt(buildArgs(receiver, selector, args));
    }

    /**
     * Sends a message whose return type is a C {@code long} or {@code NSInteger}.
     */
    public static long sendLong(Pointer receiver, String selector, Object... args) {
        ensureInit();
        return msgSend.invokeLong(buildArgs(receiver, selector, args));
    }

    /**
     * Sends a message whose return type is an object pointer.
     */
    public static Pointer sendPointer(Pointer receiver, String selector, Object... args) {
        ensureInit();
        return msgSend.invokePointer(buildArgs(receiver, selector, args));
    }

    /**
     * Sends a message returning a structure by value.
     * <p>
     * Only structures of 16 bytes or less are supported. Larger structures would require
     * an ABI-specific {@code objc_msgSend_stret} path on x86_64.
     *
     * @param type The structure type to return.
     */
    public static <T> T sendStruct(Class<T> type, Pointer receiver, String selector, Object... args) {
        ensureInit();
        return type.cast(msgSend.invoke(type, buildArgs(receiver, selector, args)));
    }

    /**
     * Runs a task on the Cocoa main queue.
     * <p>
     * If the calling thread is already the Cocoa main thread, the task runs immediately to
     * avoid a {@code dispatch_sync} self-deadlock. Exceptions thrown by tasks dispatched
     * to the main queue are logged instead of crossing the JNA callback boundary.
     */
    public static void runOnMain(Runnable action) {
        ensureInit();
        if (isMainThread()) {
            action.run();
            return;
        }
        ThreadCallback callback = ctx -> {
            try {
                action.run();
            } catch (Throwable t) {
                Logger.error(TAG, "Uncaught exception in a task dispatched to the Cocoa main queue", t);
            }
        };
        dispatchSync.invoke(Void.class, new Object[]{mainQueue, Pointer.NULL, callback});
    }

    private static void ensureInit() {
        if (!initialized) init();
    }

    private static boolean isMainThread() {
        // Fall back to dispatch_sync when the guard is unavailable, as documented in init().
        if (pthreadMainNp == null) return false;
        return pthreadMainNp.invokeInt(new Object[0]) != 0;
    }

    private static Object[] buildArgs(Pointer receiver, String selector, Object[] args) {
        Object[] out = new Object[args.length + 2];
        out[0] = receiver;
        out[1] = sel(selector);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof Boolean b) out[i + 2] = b ? 1L : 0L;
            else if (arg instanceof Integer n) out[i + 2] = n.longValue();
            else if (arg instanceof Short n) out[i + 2] = n.longValue();
            else if (arg instanceof Byte n) out[i + 2] = n.longValue();
            else out[i + 2] = arg; // Pointer, Structure, Long or Double
        }
        return out;
    }

    private interface Runtime extends Library {
        Runtime INSTANCE = Native.load(OBJC_LIB, Runtime.class);

        Pointer sel_getUid(String msg);

        Pointer objc_lookUpClass(String name);
    }

    public interface ThreadCallback extends Callback {
        void callback(Pointer ctx);
    }
}
