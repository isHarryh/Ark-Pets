/** Copyright (c) 2022-2024, Harry Huang, Half Nothing
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.concurrent;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.UUID;


public class SocketData implements Serializable {
    /** ArkPets cross-process-communication operations.
     */
    public enum Operation {
        LOGIN,
        LOGOUT,
        KEEP_ACTION,
        NO_KEEP_ACTION,
        TRANSPARENT_MODE,
        NO_TRANSPARENT_MODE,
        CAN_CHANGE_STAGE,
        CHANGE_STAGE,
        HANDSHAKE_REQUEST,
        HANDSHAKE_RESPONSE,
        ACTIVATE_LAUNCHER
    }

    /** The UUID for identification.
     */
    public UUID uuid;

    /** The {@link Operation} of this request.
     */
    public Operation operation;

    /** The optional message string.
     * Note that using {@link #getMsgString()} is recommended.
     */
    public StringDTO msg;

    private SocketData(UUID uuid, Operation operation, StringDTO msg) {
        this.uuid       = uuid;
        this.operation  = operation;
        this.msg        = msg;
    }

    @JSONField(serialize = false, deserialize = false)
    public String getMsgString() {
        return msg.toString();
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    public static SocketData of(String jsonString) {
        return JSONObject.parseObject(jsonString, SocketData.class);
    }

    public static SocketData ofLogin(UUID uuid, String name) {
        return new SocketData(uuid, Operation.LOGIN, StringDTO.of(name));
    }

    public static SocketData ofOperation(UUID uuid, Operation operation) {
        return new SocketData(uuid, operation, null);
    }


    private static class StringDTO {
        public byte[] bytes;
        public String encoding;

        private StringDTO(byte[] bytes, String encoding) {
            this.bytes = bytes;
            this.encoding = encoding;
        }

        @Override
        public String toString() {
            return new String(bytes, Charset.forName(encoding));
        }

        public static StringDTO of(String string) {
            return new StringDTO(string.getBytes(Charset.defaultCharset()), Charset.defaultCharset().name());
        }
    }
}
