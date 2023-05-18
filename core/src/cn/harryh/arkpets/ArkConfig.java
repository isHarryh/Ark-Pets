/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static cn.harryh.arkpets.Const.*;


public class ArkConfig {
    public static final String configCustomPath = configExternal;
    public static final String configDefaultPath = configInternal;
    private static final File configCustom =
            new File(configCustomPath);
    private static final InputStream configDefault =
            Objects.requireNonNull(ArkConfig.class.getResourceAsStream(configDefaultPath));

    // The following is the config items
    public float display_scale;
    public int   display_fps;
    public int   display_margin_bottom;
    public int[] display_monitor_info;
    public String  character_asset;
    public String  character_label;
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
            IOUtils.FileUtil.writeString(configCustom, charsetDefault, readConfig(), false);
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
                Logger.info("Config", "Default config was copied successfully.");
            } catch (IOException e) {
                Logger.error("Config", "Default config copying failed, details see below.", e);
            }
        }
        try {
            return JSONObject.parseObject(IOUtils.FileUtil.readString(configCustom, charsetDefault), ArkConfig.class);
        } catch (IOException e) {
            Logger.error("Config", "Default config reading failed, details see below.", e);
            return null;
        }
    }


    /** Only available in Windows OS.
     */
    public static class StartupConfig {
        public static File startupDir;
        public static File startupFile;

        static {
            try {
                startupDir = new File(System.getProperty("user.home") + "/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup");
                if (!startupDir.isDirectory())
                    throw new FileNotFoundException("No such directory " + startupDir.getAbsolutePath());
                startupFile = new File(startupDir.getAbsolutePath(), startUpScript);
            } catch (Exception e) {
                startupDir = null;
                startupFile = null;
                Logger.error("Config", "Auto-startup config may be unavailable, details see below.", e);
            }
        }

        public static boolean addStartup() {
            try {
                String script = generateScript();
                if (script == null || startupDir == null)
                    throw new IOException("Generate script failed.");
                IOUtils.FileUtil.writeString(startupFile, charsetVBS, script, false);
                Logger.info("Config", "Auto-startup was added: " + startupFile.getAbsolutePath());
                return true;
            } catch (Exception e) {
                Logger.error("Config", "Auto-startup adding failed, details see below.", e);
                return false;
            }
        }

        public static void removeStartup() {
            try {
                IOUtils.FileUtil.delete(startupFile.toPath(), false);
                Logger.info("Config", "Auto-startup was removed: " + startupFile.getAbsolutePath());
            } catch (Exception e) {
                Logger.error("Config", "Auto-startup removing failed, details see below.", e);
            }
        }

        public static boolean isSetStartup() {
            try {
                if (!Files.exists(startupFile.toPath()))
                    return false;
                String script = generateScript();
                if (script == null || startupDir == null)
                    throw new IOException("Generate script failed.");
                String checksum1 = IOUtils.FileUtil.getMD5(Objects.requireNonNull(script).getBytes(charsetVBS));
                String checksum2 = IOUtils.FileUtil.getMD5(startupFile);
                return checksum1.equals(checksum2);
            } catch (Exception e) {
                return false;
            }
        }

        /** Get a content of a VBS script which can start ArkPets.
         * @return The script's content.
         */
        public static String generateScript() {
            if (!Files.exists(new File(startupTarget).toPath()))
                return null;
            String cd = System.getProperty("user.dir");
            cd = cd.replaceAll("\"", "\"\"");
            cd = cd + (cd.endsWith("\\") ? "" : "\\");
            String run = startupTarget + " --direct-start";
            run = run.replaceAll("\"", "\"\"");
            return "rem *** This is an auto-startup script, you can delete it if you want. ***\n" +
                    "const cd = \"" + cd + "\"\n" +
                    "const ex = \"" + startupTarget + "\"\n" +
                    "set fso=WScript.CreateObject(\"Scripting.FileSystemObject\")\n" +
                    "if fso.FileExists(cd & ex) then\n" +
                    "  set s = WScript.CreateObject(\"WScript.shell\")\n" +
                    "  s.CurrentDirectory = cd\n" +
                    "  s.Run \"" + run + "\"\n" +
                    "end if\n";
        }
    }
}
