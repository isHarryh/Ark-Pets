package cn.harryh.arkpets.wal;


/** A heartbeat snapshot of a model session.
 * @param asset The model asset id.
 * @param label The model label.
 * @param startTime The session start time in epoch milliseconds.
 * @param stopped Whether this heartbeat is written on a normal exit.
 */
public record WalHeartbeatEvent(String asset, String label, long startTime, boolean stopped) {
}
