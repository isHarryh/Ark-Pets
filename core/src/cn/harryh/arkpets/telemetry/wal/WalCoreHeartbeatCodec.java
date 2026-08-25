package cn.harryh.arkpets.telemetry.wal;

import java.io.*;


/** Encodes and decodes core session heartbeats.
 */
public final class WalCoreHeartbeatCodec implements WalCodec<WalCoreHeartbeatCodec.WalHeartbeatEvent> {
    public static final WalCoreHeartbeatCodec INSTANCE = new WalCoreHeartbeatCodec();

    private WalCoreHeartbeatCodec() {
    }

    @Override
    public String type() {
        return "heartbeat";
    }

    @Override
    public byte[] encode(WalHeartbeatEvent value) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeUTF(value.asset());
            dos.writeLong(value.startTime());
            dos.writeBoolean(value.stopped());
        }
        return bos.toByteArray();
    }

    @Override
    public WalHeartbeatEvent decode(byte[] payload) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(payload))) {
            String asset = dis.readUTF();
            long startTime = dis.readLong();
            boolean stopped = dis.readBoolean();
            return new WalHeartbeatEvent(asset, startTime, stopped);
        }
    }

    /** A heartbeat snapshot of a model session.
     * @param asset The model asset id.
     * @param startTime The session start time in epoch milliseconds.
     * @param stopped Whether this heartbeat is written on a normal exit.
     */
    public record WalHeartbeatEvent(String asset, long startTime, boolean stopped) {
    }
}
