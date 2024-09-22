/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.utils.IOUtils.FileUtil;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import static cn.harryh.arkpets.Const.*;


public class ArkConfig implements Serializable {
    public static final ArkConfig defaultConfig;
    private static final URL configDefault = Objects.requireNonNull(ArkConfig.class.getResource(Const.configInternal));
    private static final File configCustom = new File(getWorkingDirectory() + Const.configExternal);
    private static boolean isNewcomer = false;

    static {
        ArkConfig defaultConfig_ = null;
        try {
            defaultConfig_ = JSONObject.parseObject(Objects.requireNonNull(configDefault).openStream(), ArkConfig.class);
        } catch (IOException | NullPointerException e) {
            Logger.error("Config", "Default config parsing failed, details see below.", e);
        }
        defaultConfig = defaultConfig_;
    }


    // Config items and default values:
    /** @since ArkPets 3.4 */ @JSONField(defaultValue = "#00000000")
    public String       background_color;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "8")
    public int          behavior_ai_activation;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "true")
    public boolean      behavior_allow_interact;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "true")
    public boolean      behavior_allow_sit;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "true")
    public boolean      behavior_allow_walk;
    /** @since ArkPets 1.6 */ @JSONField(defaultValue = "true")
    public boolean      behavior_do_peer_repulsion;
    /** @since ArkPets 3.1 */ @JSONField(defaultValue = "16")
    public int          canvas_fitting_samples;
    /** @since ArkPets 2.0 */ @JSONField()
    public String       character_asset;
    /** @since ArkPets 2.2 */ @JSONField()
    public JSONObject   character_files;
    /** @since ArkPets 2.0 */ @JSONField()
    public String       character_label;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "30")
    public int          display_fps;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "0")
    public int          display_margin_bottom;
    /** @since ArkPets 2.1 */ @JSONField(defaultValue = "true")
    public boolean      display_multi_monitors;
    /** @since ArkPets 3.3 */ @JSONField(defaultValue = "1")
    public int          display_render_outline;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "1.0")
    public float        display_scale;
    /** @since ArkPets 3.2 */ @JSONField(defaultValue = "0.2")
    public float        initial_position_x;
    /** @since ArkPets 3.2 */ @JSONField(defaultValue = "0.2")
    public float        initial_position_y;
    /** @since ArkPets 3.0 */ @JSONField(defaultValue = "true")
    public boolean      launcher_solid_exit;
    /** @since ArkPets 2.0 */ @JSONField(defaultValue = "INFO")
    public String       logging_level;
    /** @since ArkPets 2.2 */ @JSONField(defaultValue = "800.0")
    public float        physic_gravity_acc;
    /** @since ArkPets 2.2 */ @JSONField(defaultValue = "100.0")
    public float        physic_air_friction_acc;
    /** @since ArkPets 2.2 */ @JSONField(defaultValue = "500.0")
    public float        physic_static_friction_acc;
    /** @since ArkPets 2.2 */ @JSONField(defaultValue = "1000.0")
    public float        physic_speed_limit_x;
    /** @since ArkPets 2.2 */ @JSONField(defaultValue = "1000.0")
    public float        physic_speed_limit_y;
    /** @since ArkPets 3.2 */ @JSONField(defaultValue = "true")
    public boolean      window_style_toolwindow;
    /** @since ArkPets 3.2 */ @JSONField(defaultValue = "true")
    public boolean      window_style_topmost;
    /** @since ArkPets 3.3 */ @JSONField(defaultValue = "0")
    public int          window_system;

    private ArkConfig() {
    }

    /** Saves the custom config to the external config file.
     */
    @JSONField(serialize = false)
    public void save() {
        try {
            FileUtil.writeString(configCustom, charsetDefault, JSON.toJSONString(this, true), false);
            Logger.debug("Config", "Config saved");
        } catch (IOException e) {
            Logger.error("Config", "Config saving failed, details see below.", e);
        }
    }

    /** Returns true if the external config file was newly-generated.
     */
    @JSONField(serialize = false)
    public boolean isNewcomer() {
        return isNewcomer;
    }

    /** Returns converted background color.
     */
    @JSONField(serialize = false)
    public Color getBackgroundColor() {
        Color backgroundColor;
        try {
            backgroundColor = Color.valueOf(background_color);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            Logger.warn("System", "Invalid background color,using transparent");
            backgroundColor = Color.CLEAR;
        }
        return backgroundColor;
    }

    /** Returns working directory, ends up with separator.
     */
    @JSONField(serialize = false)
    public static String getWorkingDirectory() {
        return System.getProperty("arkpets.workdir", "");
    }

    /** Gets the custom ArkConfig object by reading the external config file.
     * If the external config file does not exist, a default config file will be generated.
     * @return An ArkConfig object. {@code null} if failed.
     */
    public static ArkConfig getConfig() {
        if (!configCustom.exists()) {
            // Use the default config if the external config file does not exist.
            isNewcomer = true;
            ArkConfig config = getDefaultConfig();
            if (config != null)
                config.save();
            return getDefaultConfig();
        } else {
            // Read and parse the external config file.
            try {
                return Objects.requireNonNull(
                        JSONObject.parseObject(FileUtil.readString(configCustom, charsetDefault), ArkConfig.class),
                        "JSON parsing returns null."
                );
            } catch (IOException | NullPointerException e) {
                Logger.error("Config", "Failed to get the custom config, details see below.", e);
            }
            return null;
        }
    }

    /** Gets the default ArkConfig object by reading the internal config file.
     * @return An ArkConfig object. {@code null} if failed.
     */
    public static ArkConfig getDefaultConfig() {
        try (InputStream inputStream = configDefault.openStream()) {
            return Objects.requireNonNull(
                    JSONObject.parseObject(new String(inputStream.readAllBytes(), charsetDefault), ArkConfig.class),
                    "JSON parsing returns null."
            );
        } catch (IOException e) {
            Logger.error("Config", "Failed to get the default config, details see below.", e);
        }
        return null;
    }

    /** Config options for render outline.
     */
    public enum RenderOutline {
        NEVER,
        DRAGGING,
        PRESSING,
        FOCUSED,
        _RESERVED,
        ALWAYS;

        public static RenderOutline from(int ordinal) {
            if (ordinal >= RenderOutline.values().length)
                ordinal = 0;
            return RenderOutline.values()[ordinal];
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
                FileUtil.writeString(startupFile, charsetVBS, script, false);
                Logger.info("Config", "Auto-startup was added: " + startupFile.getAbsolutePath());
                return true;
            } catch (Exception e) {
                Logger.error("Config", "Auto-startup adding failed, details see below.", e);
                return false;
            }
        }

        public static void removeStartup() {
            try {
                FileUtil.delete(startupFile.toPath(), false);
                Logger.info("Config", "Auto-startup was removed: " + startupFile.getAbsolutePath());
            } catch (Exception e) {
                Logger.error("Config", "Auto-startup removing failed, details see below.", e);
            }
        }

        public static boolean isSetStartup() {
            try {
                if (!startupFile.exists())
                    return false;
                String script = generateScript();
                if (script == null || startupDir == null)
                    throw new IOException("Generate script failed.");
                String checksum1 = FileUtil.getMD5(Objects.requireNonNull(script).getBytes(charsetVBS));
                String checksum2 = FileUtil.getMD5(startupFile);
                return checksum1.equals(checksum2);
            } catch (Exception e) {
                return false;
            }
        }

        /** Gets a content of a VBS script which can start ArkPets.
         * @return The script's content.
         */
        public static String generateScript() {
            if (!new File(startupTarget).exists())
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
