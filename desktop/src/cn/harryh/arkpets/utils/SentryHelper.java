package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.wal.*;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import io.sentry.*;
import io.sentry.logger.SentryLogParameters;
import io.sentry.protocol.Feedback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
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
            options.setRelease(Const.appVersion.toString());
        });
    }

    public static void reportApp(boolean isStopping) {
        if (!enable) return;
        Sentry.logger().info("%s_APP", isStopping ? "STOP" : "START");
    }

    private static void reportModelSession(WalHeartbeatEvent heartbeat, long endTimeMillis, SentryLogLevel level) {
        if (!enable) return;
        long durationSeconds = Math.max(0, (endTimeMillis - heartbeat.startTime()) / 1000);
        Sentry.logger().log(
                level,
                SentryLogParameters.create(
                        new SentryLongDate(endTimeMillis * 1_000_000L),
                        SentryAttributes.of(
                                SentryAttribute.stringAttribute("arkpets.character_asset", heartbeat.asset()),
                                SentryAttribute.stringAttribute("arkpets.character_label", heartbeat.label()),
                                SentryAttribute.integerAttribute("arkpets.session_duration", (int) durationSeconds)
                        )
                ),
                "MODEL_SESSION"
        );
    }

    public static void captureLogFeedback(List<String> fileList) {
        Sentry.feedback().capture(
                new Feedback("User uploaded ArkPets log files."),
                Hint.withAttachments(fileList.stream().map(Attachment::new).toList())
        );
    }

    public static void consumePendingWal() {
        if (!enable) {
            for (File file : WalReader.listWalFiles())
                file.delete();
            return;
        }
        for (File file : WalReader.listWalFiles()) {
            if (isProcessAlive(WalReader.parsePid(file)))
                continue;
            try (WalReader reader = WalReader.open(file)) {
                consumeWalRecords(reader.readAll());
            } catch (IOException e) {
                Logger.warn("System", "Failed to consume WAL file " + file.getName() + ", will retry later");
                continue;
            }
            if (!file.delete())
                Logger.warn("System", "Failed to delete consumed WAL file " + file.getName());
        }
    }

    private static boolean isProcessAlive(long pid) {
        return ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false);
    }

    private static void consumeWalRecords(List<WalRecord> records) {
        WalHeartbeatEvent lastHeartbeat = null;
        long lastHeartbeatTime = 0;
        WalRecord exceptionRecord = null;
        for (WalRecord record : records) {
            try {
                if (record.type().equals(WalHeartbeatCodec.INSTANCE.type())) {
                    lastHeartbeat = WalHeartbeatCodec.INSTANCE.decode(record.payload());
                    lastHeartbeatTime = record.timestamp();
                } else if (record.type().equals(WalExceptionCodec.INSTANCE.type())) {
                    exceptionRecord = record;
                }
            } catch (IOException ignored) {
            }
        }
        if (lastHeartbeat == null)
            return;
        long endTimeMillis;
        SentryLogLevel level;
        if (exceptionRecord != null) {
            endTimeMillis = exceptionRecord.timestamp();
            level = SentryLogLevel.ERROR;
            try {
                Exception exception = WalExceptionCodec.INSTANCE.decode(exceptionRecord.payload());
                SentryEvent event = new SentryEvent(exception);
                event.setTimestamp(new Date(exceptionRecord.timestamp()));
                Sentry.captureEvent(event);
            } catch (IOException ignored) {
            }
        } else if (lastHeartbeat.stopped()) {
            endTimeMillis = lastHeartbeatTime;
            level = SentryLogLevel.INFO;
        } else {
            endTimeMillis = lastHeartbeatTime;
            level = SentryLogLevel.WARN;
        }
        reportModelSession(lastHeartbeat, endTimeMillis, level);
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
