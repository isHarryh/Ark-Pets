package cn.harryh.arkpets.tray.model;

import java.util.UUID;

public class SocketData {
    public enum OperateType {
        LOGIN,
        LOGOUT,
        KEEP_ACTION,
        NO_KEEP_ACTION,
        TRANSPARENT_MODE,
        NO_TRANSPARENT_MODE,
        CHANGE_STAGE,
        SUCCESS,
        FAILURE
    }

    public UUID uuid;
    public OperateType operateType;

    public String name;

    public SocketData(UUID uuid, OperateType operateType) {
        this.uuid = uuid;
        this.operateType = operateType;
    }

    public SocketData(UUID uuid, OperateType operateType, String name) {
        this(uuid, operateType);
        this.name = name;
    }
}
