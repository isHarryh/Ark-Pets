package cn.harryh.arkpets.telemetry.wal;

import java.io.*;


/** Encodes and decodes system information.
 */
public final class WalSystemInfoCodec implements WalCodec<WalSystemInfoCodec.SystemInfo> {
    public static final WalSystemInfoCodec INSTANCE = new WalSystemInfoCodec();

    private WalSystemInfoCodec() {
    }

    @Override
    public String type() {
        return "system_info";
    }

    @Override
    public byte[] encode(SystemInfo value) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeUTF(value.gpuName());
            dos.writeUTF(value.gpuVersion());
            dos.writeUTF(value.osName());
            dos.writeUTF(value.osArch());
            dos.writeUTF(value.gpuRenderer());
            dos.writeInt(value.gpuMaxTexture());
            dos.writeInt(value.gpuMaxRenderbuffer());
        }
        return bos.toByteArray();
    }

    @Override
    public SystemInfo decode(byte[] payload) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(payload))) {
            String gpuInfo = dis.readUTF();
            String gpuVersion = dis.readUTF();
            String osName = dis.readUTF();
            String osArch = dis.readUTF();
            String gpuRenderer = dis.readUTF();
            int textureSize = dis.readInt();
            int renderbufferSize = dis.readInt();
            return new SystemInfo(gpuInfo, gpuVersion, osName, osArch, gpuRenderer, textureSize, renderbufferSize);
        }
    }

    public record SystemInfo(String gpuName, String gpuVersion, String osName, String osArch, String gpuRenderer,
                             int gpuMaxTexture, int gpuMaxRenderbuffer) {
        public SystemInfo(String gpuInfo, String gpuVersion, String gpuRenderer, int gpuMaxTexture, int gpuMaxRenderbuffer) {
            this(gpuInfo, gpuVersion, System.getProperty("os.name"), System.getProperty("os.arch"), gpuRenderer, gpuMaxTexture, gpuMaxRenderbuffer);
        }
    }
}
