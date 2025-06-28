/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.network.api;

import cn.harryh.arkpets.utils.Version;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.time.Instant;


/** The response model for MirrorChyan query version operation.
 *
 * <ul>
 *     <li>BaseURL: {@code https://mirrorchyan.com/api}</li>
 *     <li>Endpoint: {@code /resources/{RID}/latest}</li>
 * </ul>
 *
 * @see <a href="https://apifox.com/apidoc/shared/ffdc8453-597d-4ba6-bd3c-5e375c10c789/253583257e0">Apifox</a>
 */
public class McQueryVersion extends BaseModel<McQueryVersion.McQueryVersionData> {
    @SuppressWarnings("unused")
    public static class McQueryVersionData implements Serializable {
        // The follows are always visible:
        @JSONField
        public String version_name;
        @JSONField
        public String version_number;
        @JSONField
        public String os;
        @JSONField
        public String arch;
        @JSONField
        public String channel;
        @JSONField
        public String release_note;

        // The follows are visible only if provided CDK is valid:
        @JSONField
        public String url;
        @JSONField
        public long filesize;
        @JSONField
        public String sha256;
        @JSONField
        public String update_type;
        @JSONField
        public long cdk_expired_time;
    }


    @JSONField(serialize = false, deserialize = false)
    public Version getVersion() {
        if (data.version_name == null || data.version_name.isEmpty())
            return null;
        return new Version(data.version_name);
    }

    @JSONField(serialize = false, deserialize = false)
    public Instant getCDKExpiredTime() {
        if (data.cdk_expired_time <= 0)
            return null;
        return Instant.ofEpochSecond(data.cdk_expired_time);
    }

    @JSONField(serialize = false, deserialize = false)
    public void raiseForCode() throws McException {
        switch (code) {
            case 0 -> {
                // Success, do nothing
            }
            case 7001 -> throw new McException(code, msg, "cdk expired", "此 CDK 已经过期");
            case 7002 -> throw new McException(code, msg, "cdk incorrect", "提供的 CDK 不正确");
            case 7003 -> throw new McException(code, msg, "cdk exceeded usage limit", "此 CDK 已达到每日用量上限");
            case 7004 -> throw new McException(code, msg, "cdk resource type mismatch", "此 CDK 与所下载的资源类型不匹配");
            case 7005 -> throw new McException(code, msg, "cdk banned", "此 CDK 已经被封禁");
            default -> throw new McException(code, msg, "unknown", "意外错误");
        }
    }


    public static class McException extends Exception {
        private final String localizedMessage;

        public McException(int code, String msg, String category, String localizedCategory) {
            super(code + " " + category + ": " + msg);
            localizedMessage = "(" + code + ")" + localizedCategory;
        }

        @Override
        public String getLocalizedMessage() {
            return localizedMessage;
        }
    }
}
