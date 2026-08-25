package cn.harryh.arkpets.telemetry.wal;

import java.io.*;


/** Encodes and decodes exceptions via Java serialization.
 */
public final class WalExceptionCodec implements WalCodec<Exception> {
    public static final WalExceptionCodec INSTANCE = new WalExceptionCodec();

    private WalExceptionCodec() {
    }

    @Override
    public String type() {
        return "exception";
    }

    @Override
    public byte[] encode(Exception value) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(value);
            oos.flush();
            return bos.toByteArray();
        }
    }

    @Override
    public Exception decode(byte[] payload) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(payload);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (Exception) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Cannot load exception class", e);
        }
    }
}
