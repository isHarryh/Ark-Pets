package cn.harryh.arkpets.tray.model;

import java.util.UUID;

public class View {
    public enum OperateType {
        LOGIN,
        LOGOUT
    }
    public final UUID uuid;
    public final OperateType operateType;
    public View(UUID uuid, OperateType operateType) {
        this.uuid = uuid;
        this.operateType = operateType;
    }
}
