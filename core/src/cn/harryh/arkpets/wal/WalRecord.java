package cn.harryh.arkpets.wal;


/** A single, immutable record read from a WAL file.
 */
public record WalRecord(String type, long seq, long timestamp, byte[] payload) {
}
