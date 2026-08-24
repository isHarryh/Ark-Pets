package cn.harryh.arkpets.wal;

import java.io.*;


/** Encodes and decodes desktop session heartbeats.
 */
public final class WalDesktopHeartbeatCodec implements WalCodec<WalDesktopHeartbeatEvent> {
    public static final WalDesktopHeartbeatCodec INSTANCE = new WalDesktopHeartbeatCodec();

    private WalDesktopHeartbeatCodec() {
    }

    @Override
    public String type() {
        return "desktop_heartbeat";
    }

    @Override
    public byte[] encode(WalDesktopHeartbeatEvent value) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeLong(value.startTime());
            dos.writeBoolean(value.stopped());
        }
        return bos.toByteArray();
    }

    @Override
    public WalDesktopHeartbeatEvent decode(byte[] payload) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(payload))) {
            long startTime = dis.readLong();
            boolean stopped = dis.readBoolean();
            return new WalDesktopHeartbeatEvent(startTime, stopped);
        }
    }
}
