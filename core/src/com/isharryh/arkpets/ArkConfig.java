/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package com.isharryh.arkpets;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.badlogic.gdx.Gdx;
import com.isharryh.arkpets.utils.IOUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
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
        //configDefault = SrcUtil.getInternalFile(ArkConfig.class, "/ArkPetsDefault.config");
        configDefault = new File(Objects.requireNonNull(ArkConfig.class.getResource(configDefaultPath)).toExternalForm());
    }

    // The following is the config items
    public float display_scale;
    public int   display_fps;
    public int   display_margin_bottom;
    public int[] display_monitor_info;
    public String character_recent;
    public int     behavior_ai_activation;
    public boolean behavior_allow_sleep;
    public boolean behavior_allow_walk;
    public boolean behavior_allow_sit;
    public boolean behavior_allow_interact;
    public boolean behavior_do_peer_repulsion;

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
            FileUtil.writeString(configCustom, "UTF-8", readConfig(), false);
        } catch (IOException e) {
            System.err.println("[error] Config saving failed");
            e.printStackTrace();
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
                System.err.println("[error] Config copying failed");
                e.printStackTrace();
            }
        }
        try {
            return JSONObject.parseObject(FileUtil.readString(configCustom, "UTF-8"), ArkConfig.class);
        } catch (IOException e) {
            System.err.println("[error] Config reading failed");
            e.printStackTrace();
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
