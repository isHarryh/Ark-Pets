package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.Const;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import io.sentry.Sentry;
import io.sentry.SentryAttribute;
import io.sentry.SentryAttributes;
import io.sentry.SentryLogLevel;
import io.sentry.logger.SentryLogParameters;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import static cn.harryh.arkpets.Const.charsetDefault;


public class SentryHelper {
    private static final URL configDefault = Objects.requireNonNull(ArkConfig.class.getResource(Const.configSentry));
    private static SentryConfig config;
    private static boolean enable = false;

    public static void init() {
        config = getDefaultConfig();
        Sentry.init(options -> {
            if (config != null && config.dsn != null) {
                options.setDsn(config.dsn);
                options.setEnvironment(config.env);
            } else {
                Logger.warn("System", "Failed to load Sentry Config");
                options.setEnabled(false);
            }
            options.setSendDefaultPii(true);
            options.setTracesSampleRate(1.0);
            options.getLogs().setEnabled(true);
        });
        Sentry.setAttribute("arkpets.version", Const.appVersion.toString());
    }

    public static void reportApp(boolean isStopping) {
        if (!enable) return;
        Sentry.logger().info("%s_APP", isStopping ? "STOP" : "START");
    }

    public static void reportModel(String asset,String label,boolean isStopping) {
        if (!enable) return;
        Sentry.logger().log(
                SentryLogLevel.INFO,
                SentryLogParameters.create(
                        SentryAttributes.of(
                                SentryAttribute.stringAttribute("arkpets.character_asset", asset),
                                SentryAttribute.stringAttribute("arkpets.character_label", label)
                        )
                ),
                "%s_MODEL",
                isStopping ? "STOP" : "START"
        );
    }

    public static boolean isEnable() {
        return enable;
    }

    public static void setEnable(boolean enable) {
        SentryHelper.enable = enable;
    }

    private static SentryConfig getDefaultConfig() {
        try (InputStream inputStream = configDefault.openStream()) {
            return Objects.requireNonNull(
                    JSONObject.parseObject(new String(inputStream.readAllBytes(), charsetDefault), SentryConfig.class),
                    "JSON parsing returns null."
            );
        } catch (IOException e) {
            Logger.error("Config", "Failed to get the sentry config, details see below.", e);
        }
        return null;
    }

    private static class SentryConfig {
        @JSONField
        public String dsn;
        @JSONField(defaultValue = "production")
        public String env;
    }
}
