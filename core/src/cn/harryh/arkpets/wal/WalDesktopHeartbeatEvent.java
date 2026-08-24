package cn.harryh.arkpets.wal;


/** A heartbeat snapshot of a desktop session.
 * @param startTime The session start time in epoch milliseconds.
 * @param stopped Whether this heartbeat is written on a normal exit.
 */
public record WalDesktopHeartbeatEvent(long startTime, boolean stopped) {
}
