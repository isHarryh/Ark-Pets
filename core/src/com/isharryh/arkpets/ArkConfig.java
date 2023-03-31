/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.isharryh.arkpets.utils.IOUtils;
import com.isharryh.arkpets.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;


public class ArkConfig {
    public static final String configCustomPath = "ArkPetsConfig.json";
    public static final String configDefaultPath = "/ArkPetsConfigDefault.json";
    private static final File configCustom =
            new File(configCustomPath);
    private static final InputStream configDefault =
            Objects.requireNonNull(ArkConfig.class.getResourceAsStream(configDefaultPath));

    // The following is the config items
    public float display_scale;
    public int   display_fps;
    public int   display_margin_bottom;
    public int[] display_monitor_info;
    public String  character_recent;
    public int     behavior_ai_activation;
    public boolean behavior_allow_sleep;
    public boolean behavior_allow_walk;
    public boolean behavior_allow_sit;
    public boolean behavior_allow_interact;
    public boolean behavior_do_peer_repulsion;
    public String  logging_level;

    private ArkConfig() {
    }

    /** Get the config in String format.
     * @return All the content in the config file.
     */
    public String readConfig() {
        return JSON.toJSONString(this, true);
    }

    /** Save the config into the custom file.
     */
    public void saveConfig() {
        try {
            IOUtils.FileUtil.writeString(configCustom, "UTF-8", readConfig(), false);
        } catch (IOException e) {
            Logger.error("Config", "Config saving failed, details see below.", e);
        }
    }

    /** Instantiate an ArkConfig object.
     * @return ArkConfig object.
     */
    public static ArkConfig getConfig() {
        if (!configCustom.isFile()) {
            try {
                Files.copy(configDefault, configCustom.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Logger.error("Config", "Config copying failed, details see below.", e);
            }
        }
        try {
            return JSONObject.parseObject(IOUtils.FileUtil.readString(configCustom, "UTF-8"), ArkConfig.class);
        } catch (IOException e) {
            Logger.error("Config", "Config reading failed, details see below.", e);
            return null;
        }
    }
}
