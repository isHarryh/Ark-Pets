/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;

import static cn.harryh.arkpets.Const.*;


public class ArkConfig {
    public static final String configCustomPath = configExternal;
    public static final String configDefaultPath = configInternal;
    public static final ArkConfig defaultConfig;
    private static final File configCustom = new File(configCustomPath);
    private static boolean isNewcomer = false;

    static {
        ArkConfig defaultConfig_ = null;
        try {
            defaultConfig_ = JSONObject.parseObject(
                    IOUtils.FileUtil.readString(getDefaultConfigInputStream(), charsetDefault),
                    ArkConfig.class
            );
        } catch (IOException e) {
            Logger.error("Config", "Default config parsing failed, details see below.", e);
        }
        defaultConfig = defaultConfig_;
    }

    // The following is the config items
    public int        behavior_ai_activation;
    public boolean    behavior_allow_interact;
    public boolean    behavior_allow_sit;
    public boolean    behavior_allow_walk;
    public boolean    behavior_do_peer_repulsion;
    public String     character_asset;
    public JSONObject character_files;
    public String     character_label;
    public int        display_fps;
    public int        display_margin_bottom;
    public boolean    display_multi_monitors;
    public float      display_scale;
    public boolean    launcher_solid_exit;
    public String     logging_level;
    public float      physic_gravity_acc;
    public float      physic_air_friction_acc;
    public float      physic_static_friction_acc;
    public float      physic_speed_limit_x;
    public float      physic_speed_limit_y;

    private ArkConfig() {
    }

    /** Saves the config into the custom file.
     */
    @JSONField(serialize = false)
    public void saveConfig() {
        try {
            IOUtils.FileUtil.writeString(configCustom, charsetDefault, JSON.toJSONString(this, true), false);
        } catch (IOException e) {
            Logger.error("Config", "Config saving failed, details see below.", e);
        }
    }

    /** Instantiates an ArkConfig object.
     * @return ArkConfig object. null if failed.
     */
    @JSONField(serialize = false)
    public static ArkConfig getConfig() {
        // Duplicate the default config file if the custom config file is not existed.
        try {
            if (!configCustom.isFile()) {
                Files.copy(getDefaultConfigInputStream(), configCustom.toPath(), StandardCopyOption.REPLACE_EXISTING);
                isNewcomer = true;
                Logger.info("Config", "Default config was copied successfully.");
            }
        } catch (IOException e) {
            Logger.error("Config", "Default config copying failed, details see below.", e);
        }
        // Read and parse the custom config file.
        try {
            return Objects.requireNonNull(
                    JSONObject.parseObject(IOUtils.FileUtil.readString(configCustom, charsetDefault), ArkConfig.class),
                    "JSON parsing returns null."
            );
        } catch (IOException e) {
            Logger.error("Config", "Config reading failed, details see below.", e);
        } catch (NullPointerException e) {
            Logger.error("Config", "Config parsing failed, details see below.", e);
        }
        return null;
    }

    private static InputStream getDefaultConfigInputStream() {
        return Objects.requireNonNull(ArkConfig.class.getResourceAsStream(configDefaultPath));
    }

    /** Returns true if the config file was newly-generated.
     */
    @JSONField(serialize = false)
    public boolean isNewcomer() {
        return isNewcomer;
    }

    /** Returns true if all the physic params are set to 0.
     * <hr>
     * Note: Once users upgraded ArkPets to v2.2+ where physic params can be modified manually,
     * the physic params will be initialized to 0, which will cause bad behaviors.
     * @since ArkPets 2.2
     */
    @JSONField(serialize = false)
    public boolean isAllPhysicConfigZeroed() {
        return physic_gravity_acc == 0 &&
                physic_air_friction_acc == 0 &&
                physic_static_friction_acc == 0 &&
                physic_speed_limit_x == 0 &&
                physic_speed_limit_y == 0;
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

        /** Gets a content of a VBS script which can start ArkPets.
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


    @SuppressWarnings("unused")
    public static class Monitor {
        public String name;
        public int[]  size;
        public int[]  virtual;
        public int    hz;
        public int    bbp;

        private Monitor() {
        }

        /** Gets the information of all the existing monitors.
         * @return A list of Monitor objects.
         */
        public static Monitor[] getMonitors() {
            ArrayList<Monitor> list = new ArrayList<>();
            Graphics.Monitor[] monitors = Lwjgl3ApplicationConfiguration.getMonitors();
            for (Graphics.Monitor m : monitors) {
                Monitor monitor = new Monitor();
                monitor.name = m.name;
                Graphics.DisplayMode dm = Lwjgl3ApplicationConfiguration.getDisplayMode(m);
                monitor.size = new int[]{dm.width, dm.height};
                monitor.virtual = new int[]{m.virtualX, m.virtualY};
                monitor.hz = dm.refreshRate;
                monitor.bbp = dm.bitsPerPixel;
                list.add(monitor);
            }
            return list.toArray(new Monitor[0]);
        }

        public static Monitor fromJSONObject(JSONObject object) {
            return object.toJavaObject(Monitor.class);
        }

        public static Monitor[] fromJSONArray(JSONArray array) {
            ArrayList<Monitor> list = new ArrayList<>();
            for (Object o : array)
                if (o instanceof JSONObject)
                    list.add(fromJSONObject((JSONObject)o));
            return list.toArray(new Monitor[0]);
        }
        
        public static JSONObject toJSONObject(Monitor monitor) {
            return (JSONObject)JSON.toJSON(monitor);
        }
        
        public static JSONArray toJSONArray(Monitor[] monitors) {
            JSONArray array = new JSONArray();
            for (Monitor m : monitors)
                array.add(toJSONObject(m));
            return array;
        }
    }
}
