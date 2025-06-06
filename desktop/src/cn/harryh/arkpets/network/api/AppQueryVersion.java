/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.network.api;

import cn.harryh.arkpets.utils.Version;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;


public class AppQueryVersion extends BaseModel<AppQueryVersion.AppQueryVersionData> {
    public static class AppQueryVersionData implements Serializable {
        public int[] stableVersion;
        public int[] betaVersion;
    }


    @JSONField(serialize = false, deserialize = false)
    public Version getStableVersion() {
        if (data.stableVersion == null || data.stableVersion.length == 0)
            return null;
        return new Version(data.stableVersion);
    }

    @JSONField(serialize = false, deserialize = false)
    public Version getBetaVersion() {
        if (data.betaVersion == null || data.betaVersion.length == 0)
            return null;
        return new Version(data.betaVersion);
    }
}
