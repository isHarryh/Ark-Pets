package cn.harryh.arkpets.concurrent;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.UUID;


public class SocketData implements Serializable {
    public enum Operation {
        LOGIN,
        LOGOUT,
        KEEP_ACTION,
        NO_KEEP_ACTION,
        TRANSPARENT_MODE,
        NO_TRANSPARENT_MODE,
        CHANGE_STAGE,
        VERIFY,
        SERVER_ONLINE,
        ACTIVATE_LAUNCHER
    }

    public UUID uuid;
    public Operation operation;
    public byte[] name;
    public boolean canChangeStage;

    public SocketData(UUID uuid, Operation operation) {
        this(uuid, operation, "", false);
    }

    public SocketData(UUID uuid, Operation operation, String name, boolean canChangeStage) {
        this.uuid = uuid;
        this.operation = operation;
        this.name = name.getBytes(Charset.forName("GBK"));
        this.canChangeStage = canChangeStage;
    }
}
