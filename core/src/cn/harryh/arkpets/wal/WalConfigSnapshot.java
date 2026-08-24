package cn.harryh.arkpets.wal;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.PrivacyField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;


/** Collects and (de)serializes a snapshot of ArkConfig fields.
 */
public final class WalConfigSnapshot {
    private WalConfigSnapshot() {
    }

    /** Collects all public scalar ArkConfig fields into a snapshot map.
     * Fields annotated with {@link PrivacyField} are skipped.
     * Only scalar values (Boolean/Integer/Double/String) are retained; floats are widened to double.
     * @param config The config instance to read, may be null.
     * @return The collected snapshot.
     */
    public static Map<String, Object> collect(ArkConfig config) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        if (config == null)
            return snapshot;
        for (Field field : ArkConfig.class.getFields()) {
            if (field.isAnnotationPresent(PrivacyField.class))
                continue;
            try {
                Object value = field.get(config);
                if (value == null)
                    continue;
                if (value instanceof Boolean || value instanceof Integer || value instanceof String) {
                    snapshot.put(field.getName(), value);
                } else if (value instanceof Float || value instanceof Double) {
                    snapshot.put(field.getName(), ((Number) value).doubleValue());
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return snapshot;
    }

    /** Writes a snapshot to the given stream.
     * @param out The output stream.
     * @param snapshot The snapshot to write.
     * @throws IOException If the snapshot cannot be written.
     */
    public static void writeTo(DataOutputStream out, Map<String, Object> snapshot) throws IOException {
        out.writeInt(snapshot.size());
        for (Map.Entry<String, Object> entry : snapshot.entrySet()) {
            out.writeUTF(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Boolean) {
                out.writeByte(0);
                out.writeBoolean((Boolean) value);
            } else if (value instanceof Integer) {
                out.writeByte(1);
                out.writeInt((Integer) value);
            } else if (value instanceof Double) {
                out.writeByte(2);
                out.writeDouble((Double) value);
            } else {
                out.writeByte(3);
                out.writeUTF((String) value);
            }
        }
    }

    /** Reads a snapshot from the given stream.
     * @param in The input stream.
     * @return The snapshot.
     * @throws IOException If the snapshot cannot be read.
     */
    public static Map<String, Object> readFrom(DataInputStream in) throws IOException {
        int size = in.readInt();
        Map<String, Object> snapshot = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            String key = in.readUTF();
            byte type = in.readByte();
            Object value = switch (type) {
                case 0 -> in.readBoolean();
                case 1 -> in.readInt();
                case 2 -> in.readDouble();
                default -> in.readUTF();
            };
            snapshot.put(key, value);
        }
        return snapshot;
    }
}
