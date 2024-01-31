package cn.harryh.arkpets.socket;

import java.util.UUID;

public class SocketData {
    public enum OperateType {
        LOGIN,
        LOGOUT,
        KEEP_ACTION,
        NO_KEEP_ACTION,
        TRANSPARENT_MODE,
        NO_TRANSPARENT_MODE,
        CHANGE_STAGE
    }

    public UUID uuid;
    public OperateType operateType;

    public String name;
    public boolean canChangeStage;

    public SocketData(UUID uuid, OperateType operateType) {
        this(uuid, operateType, "", false);
    }

    public SocketData(UUID uuid, OperateType operateType, String name, boolean canChangeStage) {
        this.uuid = uuid;
        this.operateType = operateType;
        this.name = name;
        this.canChangeStage = canChangeStage;
    }
}
