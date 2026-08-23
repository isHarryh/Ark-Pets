package cn.harryh.arkpets.wal;

import cn.harryh.arkpets.Const;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;


/** Appends records to a write-ahead log file.
 */
public final class WalWriter implements AutoCloseable {
    private static final byte[] MAGIC = "WAL1".getBytes(StandardCharsets.US_ASCII);
    private static final byte VERSION = 0x01;

    private final DataOutputStream out;
    private long seq = 0;

    private WalWriter(File file) throws IOException {
        boolean writeHeader = !file.exists() || file.length() == 0;
        out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)));
        if (writeHeader) {
            out.write(MAGIC);
            out.writeByte(VERSION);
        }
    }

    /** Opens a WAL file for the process with the given id.
     * @param pid The id of the writing process.
     * @return A new writer.
     * @throws IOException If the file cannot be opened.
     */
    public static WalWriter open(long pid) throws IOException {
        File file = new File(Const.LogConfig.logDir, Const.LogConfig.logWalPattern.formatted(pid));
        File parent = file.getParentFile();
        if (parent != null && !parent.exists())
            parent.mkdirs();
        return new WalWriter(file);
    }

    /** Appends a record encoded by the given codec.
     * @param codec The codec used to encode the value.
     * @param value The value to append.
     * @throws IOException If the record cannot be written.
     */
    public synchronized <T> void append(WalCodec<T> codec, T value) throws IOException {
        byte[] payload = codec.encode(value);
        out.writeUTF(codec.type());
        out.writeLong(seq++);
        out.writeLong(System.currentTimeMillis());
        out.writeInt(payload.length);
        out.write(payload);
        CRC32 crc = new CRC32();
        crc.update(payload);
        out.writeInt((int) crc.getValue());
    }

    /** Flushes buffered records to the underlying file.
     * @throws IOException If the flush fails.
     */
    public synchronized void flush() throws IOException {
        out.flush();
    }

    @Override
    public synchronized void close() throws IOException {
        out.close();
    }
}
