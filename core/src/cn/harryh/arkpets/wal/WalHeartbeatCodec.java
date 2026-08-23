package cn.harryh.arkpets.wal;

import java.io.*;


/** Encodes and decodes model session heartbeats.
 */
public final class WalHeartbeatCodec implements WalCodec<WalHeartbeatEvent> {
    public static final WalHeartbeatCodec INSTANCE = new WalHeartbeatCodec();

    private WalHeartbeatCodec() {
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
            dos.writeUTF(value.label());
            dos.writeLong(value.startTime());
            dos.writeBoolean(value.stopped());
        }
        return bos.toByteArray();
    }

    @Override
    public WalHeartbeatEvent decode(byte[] payload) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(payload))) {
            String asset = dis.readUTF();
            String label = dis.readUTF();
            long startTime = dis.readLong();
            boolean stopped = dis.readBoolean();
            return new WalHeartbeatEvent(asset, label, startTime, stopped);
        }
    }
}
