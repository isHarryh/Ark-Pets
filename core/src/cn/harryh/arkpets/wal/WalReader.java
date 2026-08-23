package cn.harryh.arkpets.wal;

import cn.harryh.arkpets.Const;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.CRC32;


/** Reads records from a write-ahead log file.
 */
public final class WalReader implements AutoCloseable {
    private static final byte[] MAGIC = "WAL1".getBytes(StandardCharsets.US_ASCII);

    private final DataInputStream in;

    private WalReader(File file) throws IOException {
        DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        byte[] magic = new byte[MAGIC.length];
        stream.readFully(magic);
        if (!Arrays.equals(magic, MAGIC)) {
            stream.close();
            throw new IOException("Invalid WAL file: " + file.getAbsolutePath());
        }
        stream.readByte(); // version, currently unused
        this.in = stream;
    }

    /** Opens the WAL file of the process with the given id.
     * @param pid The id of the writing process.
     * @return A new reader.
     * @throws IOException If the file cannot be opened.
     */
    public static WalReader open(long pid) throws IOException {
        return new WalReader(new File(Const.LogConfig.logDir, Const.LogConfig.logWalPattern.formatted(pid)));
    }

    /** Opens the given WAL file.
     * @param file The WAL file to read.
     * @return A new reader.
     * @throws IOException If the file cannot be opened.
     */
    public static WalReader open(File file) throws IOException {
        return new WalReader(file);
    }

    /** Lists all pending WAL files in the log directory, ordered by name.
     * @return The list of WAL files.
     */
    public static List<File> listWalFiles() {
        File dir = new File(Const.LogConfig.logDir);
        File[] files = dir.listFiles(f -> f.getName().matches("core\\.\\d+\\.wal"));
        if (files == null)
            return List.of();
        return Arrays.stream(files).sorted(Comparator.comparing(File::getName)).toList();
    }

    /** Parses the process id from a WAL file name.
     * @param file The WAL file.
     * @return The process id.
     */
    public static long parsePid(File file) {
        String name = file.getName();
        return Long.parseLong(name.substring("core.".length(), name.lastIndexOf(".wal")));
    }

    /** Reads all intact records in the file.
     * Trailing torn records and records with a bad checksum are dropped.
     * @return The list of records.
     * @throws IOException If the file cannot be read.
     */
    public List<WalRecord> readAll() throws IOException {
        List<WalRecord> records = new ArrayList<>();
        while (true) {
            WalRecord record;
            try {
                record = readRecord();
            } catch (EOFException e) {
                break; // torn record at the end of the file
            }
            if (record != null)
                records.add(record);
        }
        return records;
    }

    private WalRecord readRecord() throws IOException {
        String type = in.readUTF();
        long seq = in.readLong();
        long timestamp = in.readLong();
        int payloadLength = in.readInt();
        byte[] payload = new byte[payloadLength];
        in.readFully(payload);
        int checksum = in.readInt();
        CRC32 crc = new CRC32();
        crc.update(payload);
        if ((int) crc.getValue() != checksum)
            return null; // corrupted record, skip it
        return new WalRecord(type, seq, timestamp, payload);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
