package cn.harryh.arkpets.wal;

import java.io.*;
import java.util.Map;


/** Encodes and decodes ArkConfig snapshots.
 */
public final class WalConfigCodec implements WalCodec<Map<String, Object>> {
    public static final WalConfigCodec INSTANCE = new WalConfigCodec();

    private WalConfigCodec() {
    }

    @Override
    public String type() {
        return "config";
    }

    @Override
    public byte[] encode(Map<String, Object> value) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            WalConfigSnapshot.writeTo(dos, value);
        }
        return bos.toByteArray();
    }

    @Override
    public Map<String, Object> decode(byte[] payload) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(payload))) {
            return WalConfigSnapshot.readFrom(dis);
        }
    }
}
