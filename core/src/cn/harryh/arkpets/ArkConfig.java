/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.transitions.EasingFunction;
import cn.harryh.arkpets.utils.IOUtils.FileUtil;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.SecretUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Objects;

import static cn.harryh.arkpets.Const.charsetDefault;
import static cn.harryh.arkpets.Const.hexColorRegex;


public class ArkConfig implements Serializable {
    private static final URL configDefault = Objects.requireNonNull(ArkConfig.class.getResource(Const.configInternal));
    private static final File configCustom = new File(Const.configExternal);
    private static boolean isNewcomer = false;

    // Config items and default values:
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "4")
    public int          behavior_ai_activation;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "true")
    public boolean      behavior_allow_interact;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "true")
    public boolean      behavior_allow_sit;
    /** @since ArkPets 3.6 */ @JSONField(defaultValue = "false")
    public boolean      behavior_allow_sleep;
    /** @since ArkPets 3.6 */ @JSONField(defaultValue = "true")
    public boolean      behavior_allow_special;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "true")
    public boolean      behavior_allow_walk;
    /** @since ArkPets 1.6 */ @JSONField(defaultValue = "true")
    public boolean      behavior_do_peer_repulsion;
    /** @since ArkPets 3.3 */ @JSONField(defaultValue = "#00000000")
    public String       canvas_color;
    /** @since ArkPets 3.8 */ @JSONField(defaultValue = "0.8")
    public float        canvas_coverage;
    /** @since ArkPets 3.8 */ @JSONField(defaultValue = "4")
    public int          canvas_sampling_interval;
    /** @since ArkPets 2.0 */ @JSONField()
    public String       character_asset;
    /** @since ArkPets 3.5 */ @JSONField()
    public JSONObject   character_favorites;
    /** @since ArkPets 2.2 */ @JSONField()
    public JSONObject   character_files;
    /** @since ArkPets 2.0 */ @JSONField()
    public String       character_label;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "60")
    public int          display_fps;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "0")
    public int          display_margin_bottom;
    /** @since ArkPets 2.1 */ @JSONField(defaultValue = "true")
    public boolean      display_multi_monitors;
    /** @since ArkPets 1.0 */ @JSONField(defaultValue = "1.0")
    public float        display_scale;
    /** @since ArkPets 3.9 */ @JSONField()
    public String       download_mc_cdk;
    /** @since ArkPets 3.9 */ @JSONField(defaultValue = "false")
    public boolean      eco_mode;
    /** @since ArkPets 3.2 */ @JSONField(defaultValue = "0.2")
    public float        initial_position_x;
    /** @since ArkPets 3.2 */ @JSONField(defaultValue = "0.2")
    public float        initial_position_y;
    /** @since ArkPets 3.0 */ @JSONField(defaultValue = "true")
    public boolean      launcher_solid_exit;
    /** @since ArkPets 2.0 */ @JSONField(defaultValue = "INFO")
    public String       logging_level;
    /** @since ArkPets 3.3 */ @JSONField(defaultValue = "0.75")
    public float        opacity_dim;
    /** @since ArkPets 3.3 */ @JSONField(defaultValue = "1.0")
    public float        opacity_normal;
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
    /** @since ArkPets 3.5 */ @JSONField(defaultValue = "0.3")
    public float        render_animation_mixture;
    /** @since ArkPets 3.8 */ @JSONField(defaultValue = "false")
    public boolean      render_enable_angle;
    /** @since ArkPets 3.8 */ @JSONField(defaultValue = "true")
    public boolean      render_enable_mipmap;
    /** @since ArkPets 3.3 */ @JSONField(defaultValue = "1")
    public int          render_outline;
    /** @since ArkPets 3.3 */ @JSONField(defaultValue = "#FFFF00FF")
    public String       render_outline_color;
    /** @since ArkPets 3.3 */ @JSONField(defaultValue = "2.0")
    public float        render_outline_width;
    /** @since ArkPets 3.6 */ @JSONField(defaultValue = "#000000BB")
    public String       render_shadow_color;
    /** @since ArkPets 3.5 */ @JSONField(defaultValue = "0.3")
    public float        transition_duration;
    /** @since ArkPets 3.5 */ @JSONField(defaultValue = "EASE_OUT_CUBIC")
    public String       transition_type;
    /** @since ArkPets 3.7 */ @JSONField()
    public JSONObject   user_announcement_read;
    /** @since ArkPets 3.2 */ @JSONField(defaultValue = "true")
    public boolean      window_style_toolwindow;
    /** @since ArkPets 3.2 */ @JSONField(defaultValue = "true")
    public boolean      window_style_topmost;

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

    /** Gets the MirrorChyan CDK.
     * @return The decrypted CDK, or {@code null} if the CDK is not set or decryption failed.
     */
    @JSONField(serialize = false)
    public String getMcCdk() {
        if (download_mc_cdk != null && !download_mc_cdk.isEmpty()) {
            try {
                String result = new SecretUtils.WeakEncryptionV0().decrypt(download_mc_cdk);
                Logger.debug("Config", "Decrypt MirrorChyan CDK okay");
                return result;
            } catch (GeneralSecurityException e) {
                Logger.error("Config", "Failed to decrypt MirrorChyan CDK, details see below.", e);
            }
        }
        return null;
    }

    /** Sets the MirrorChyan CDK.
     * @param string The CDK to set. If the string is {@code null} or empty, the CDK will be cleared.
     */
    public void setMcCdk(String string) throws GeneralSecurityException {
        if (string != null && !string.isEmpty()) {
            try {
                download_mc_cdk = new SecretUtils.WeakEncryptionV0().encrypt(string);
                Logger.debug("Config", "Encrypt MirrorChyan CDK okay");
                return;
            } catch (GeneralSecurityException e) {
                Logger.error("Config", "Failed to encrypt MirrorChyan CDK, details see below.", e);
                throw e;
            }
        }
        download_mc_cdk = "";
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

    /** @see EasingFunction
     */
    public static EasingFunction getEasingFunctionFrom(String string) {
        try {
            return EasingFunction.valueOf(string);
        } catch (IllegalArgumentException e) {
            Logger.warn("Config", "Invalid easing function, using linear");
            return EasingFunction.LINEAR;
        }
    }

    /** @see Color
     */
    public static Color getGdxColorFrom(String string) {
        Color color;
        if (hexColorRegex.matcher(string).matches()) {
            color = Color.valueOf(string);
        } else {
            Logger.warn("Config", "Invalid color config, using transparent");
            color = Color.CLEAR;
        }
        return color;
    }

    /** @see RenderOutline
     */
    public static RenderOutline getRenderOutlineFrom(int ordinal) {
        if (ordinal >= RenderOutline.values().length)
            ordinal = 0;
        return RenderOutline.values()[ordinal];
    }


    /** Config options for render outline.
     */
    public enum RenderOutline {
        NEVER,
        DRAGGING,
        PRESSING,
        FOCUSED,
        _RESERVED,
        ALWAYS
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
                    list.add(fromJSONObject((JSONObject) o));
            return list.toArray(new Monitor[0]);
        }

        public static JSONObject toJSONObject(Monitor monitor) {
            return (JSONObject) JSON.toJSON(monitor);
        }

        public static JSONArray toJSONArray(Monitor[] monitors) {
            JSONArray array = new JSONArray();
            for (Monitor m : monitors)
                array.add(toJSONObject(m));
            return array;
        }
    }
}
