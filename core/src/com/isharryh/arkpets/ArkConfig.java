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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;


public class ArkConfig {
    public static final String configCustomPath;
    public static final String configDefaultPath;
    private static final File configCustom;
    private static final File configDefault;
    static {
        configCustomPath = "ArkPetsCustom.config";
        configDefaultPath = "/ArkPetsDefault.config";
        configCustom = new File(configCustomPath);
        configDefault = new File(Objects.requireNonNull(ArkConfig.class.getResource(configDefaultPath)).toExternalForm());
    }

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
     * @return The text.
     */
    public String readConfig() {
        return JSON.toJSONString(this, true);
    }

    /** Save the config to the custom file.
     */
    public void saveConfig() {
        try {
            IOUtils.FileUtil.writeString(configCustom, "UTF-8", readConfig(), false);
        } catch (IOException e) {
            Logger.error("Config", "Config saving failed, details see below.", e);
        }
    }

    /** Instantiate an ArkConfig.
     * @return ArkConfig object.
     */
    public static ArkConfig getConfig() {
        if (!configCustom.isFile()) {
            try {
                Files.copy(Objects.requireNonNull(ArkConfig.class.getResourceAsStream(configDefaultPath)), configCustom.toPath(), StandardCopyOption.REPLACE_EXISTING);
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

    /** Compare two object whether their values are the same,
     * no matter what types they are.
     * @return true=same, false=diff.
     */
    public static boolean compare(Object $a, Object $b) {
        if ($a == null || $b == null)
            return false;
        return String.valueOf($a).equals(String.valueOf($b));
    }
}
