package cn.harryh.arkpets.telemetry.wal;

import java.io.*;


public class WalSystemInfoCodec implements WalCodec<WalSystemInfoCodec.SystemInfo>{
    public static final WalSystemInfoCodec INSTANCE = new WalSystemInfoCodec();

    @Override
    public String type() {
        return "system_info";
    }

    @Override
    public byte[] encode(WalSystemInfoCodec.SystemInfo value) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeUTF(value.gpuName());
            dos.writeUTF(value.gpuVersion());
            dos.writeUTF(value.osName());
            dos.writeUTF(value.osArch());
        }
        return bos.toByteArray();
    }

    @Override
    public WalSystemInfoCodec.SystemInfo decode(byte[] payload) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(payload))) {
            String gpuInfo = dis.readUTF();
            String gpuVersion = dis.readUTF();
            String osName = dis.readUTF();
            String osArch = dis.readUTF();
            return new SystemInfo(gpuInfo, gpuVersion, osName, osArch);
        }
    }

    public record SystemInfo(String gpuName, String gpuVersion, String osName, String osArch) {
        public SystemInfo(String gpuInfo, String gpuVersion) {
            this(gpuInfo, gpuVersion, System.getProperty("os.name"), System.getProperty("os.arch"));
        }
    }
}
