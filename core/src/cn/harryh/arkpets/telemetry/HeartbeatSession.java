package cn.harryh.arkpets.telemetry;

import cn.harryh.arkpets.telemetry.wal.WalCodec;
import cn.harryh.arkpets.telemetry.wal.WalExceptionCodec;
import cn.harryh.arkpets.telemetry.wal.WalWriter;
import cn.harryh.arkpets.utils.Logger;

import java.io.IOException;


/** Manages the heartbeat lifecycle of a process session.
 * Writes an initial heartbeat, spawns a daemon heartbeat thread, and supports a normal finish or a crash.
 * @param <T> The heartbeat snapshot type.
 */
public final class HeartbeatSession<T> {
    private static final long HEARTBEAT_INTERVAL = 30_000L;

    private final WalCodec<T> codec;
    private final HeartbeatFactory<T> factory;
    private final long startTime;
    private volatile boolean running = true;

    /** Creates a heartbeat snapshot for a given session state.
     * @param <T> The heartbeat snapshot type.
     */
    @FunctionalInterface
    public interface HeartbeatFactory<T> {
        T create(long startTime, boolean stopped);
    }

    public HeartbeatSession(WalCodec<T> codec, HeartbeatFactory<T> factory) {
        this.codec = codec;
        this.factory = factory;
        this.startTime = System.currentTimeMillis();
        write(factory.create(startTime, false));
        startHeartbeatThread();
    }

    private synchronized void write(T value) {
        try (WalWriter writer = WalWriter.open(ProcessHandle.current().pid())) {
            writer.append(codec, value);
        } catch (IOException e) {
            Logger.warn("System", "Failed to write heartbeat");
        }
    }

    @SuppressWarnings("BusyWait")
    private void startHeartbeatThread() {
        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
                if (running)
                    write(factory.create(startTime, false));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /** Marks the session as normally finished. */
    public void finish() {
        running = false;
        write(factory.create(startTime, true));
    }

    /** Records a crash exception for the session. */
    public void crash(Exception e) {
        running = false;
        synchronized (this) {
            try (WalWriter writer = WalWriter.open(ProcessHandle.current().pid())) {
                writer.append(WalExceptionCodec.INSTANCE, e);
            } catch (IOException ignore) {
                Logger.warn("System", "Failed to write crash exception");
            }
        }
    }
}
