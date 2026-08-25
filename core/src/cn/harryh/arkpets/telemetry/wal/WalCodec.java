package cn.harryh.arkpets.telemetry.wal;

import java.io.IOException;


/** Encodes and decodes the payload of a WAL record.
 * @param <T> The type of the payload value.
 */
public interface WalCodec<T> {

    /** Gets the type tag used to identify this codec in a WAL record.
     * @return The type tag.
     */
    String type();

    /** Encodes a value into a byte array.
     * @param value The value to encode.
     * @return The encoded payload.
     * @throws IOException If the value cannot be encoded.
     */
    byte[] encode(T value) throws IOException;

    /** Decodes a payload back into a value.
     * @param payload The payload to decode.
     * @return The decoded value.
     * @throws IOException If the payload cannot be decoded.
     */
    T decode(byte[] payload) throws IOException;
}
