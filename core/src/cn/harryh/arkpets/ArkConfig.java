/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.transitions.EasingFunction;
import cn.harryh.arkpets.utils.IOUtils.FileUtil;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.SecretUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.badlogic.gdx.graphics.Color;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Objects;

import static cn.harryh.arkpets.Const.charsetDefault;
import static cn.harryh.arkpets.Const.hexColorRegex;
import static com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat;


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
    /** @since ArkPets 3.11 */ @JSONField(defaultValue = "1")
    public int          behavior_direction_switching;
    /** @since ArkPets 1.6 */ @JSONField(defaultValue = "true")
    public boolean      behavior_do_peer_repulsion;
    /** @since ArkPets 3.9 */ @JSONField(defaultValue = "30.0")
    public float        behavior_walk_speed;
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
    /** @since ArkPets 3.8 */ @JSONField(defaultValue = "true")
    public boolean      render_enable_angle;
    /** @since ArkPets 3.8 */ @JSONField(defaultValue = "true")
    public boolean      render_enable_mipmap;
    /** @since ArkPets 3.3 */ @JSONField(defaultValue = "1")
    public int          render_outline;
    /** @since ArkPets 3.3 */ @JSONField(defaultValue = "#FFFF00FF")
    public String       render_outline_color;
    /** @since ArkPets 3.9 */ @JSONField(defaultValue = "3")
    public int          render_outline_emphasis;
    /** @since ArkPets 3.9 */ @JSONField(defaultValue = "#FFBB00FF")
    public String       render_outline_emphasis_color;
    /** @since ArkPets 3.3 */ @JSONField(defaultValue = "2.0")
    public float        render_outline_width;
    /** @since ArkPets 3.12 */@JSONField(defaultValue = "true")
    public boolean      render_shader_high_quality;
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
            FileUtil.writeString(configCustom, charsetDefault, JSON.toJSONString(this, PrettyFormat), false);
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
    @JSONField(deserialize = false)
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

    /** Gets the custom ArkConfig object by reading the default custom config file.
     * If the default custom config file does not exist, a default config file will be generated.
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
            return getConfig(configCustom);
        }
    }

    /** Gets the ArkConfig object by reading the specific config file.
     * @return An ArkConfig object. {@code null} if failed or config file not found.
     */
    public static ArkConfig getConfig(File configFile) {
        if (configFile.exists()) {
            // Read and parse the config file.
            try {
                return Objects.requireNonNull(
                        JSONObject.parseObject(FileUtil.readString(configFile, charsetDefault), ArkConfig.class),
                        "JSON parsing returns null."
                );
            } catch (IOException | NullPointerException e) {
                Logger.error("Config", "Failed to get the custom config, details see below.", e);
            }
        }
        return null;
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
}
