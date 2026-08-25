package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.telemetry.HeartbeatSession;
import cn.harryh.arkpets.telemetry.wal.*;
import io.sentry.*;
import io.sentry.logger.SentryLogParameters;
import io.sentry.protocol.Feedback;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class SentryHelper {
    private static boolean enable = false;

    public static void init() {
        Sentry.init(options -> {
            // Set -Dsentry.dsn Java option or SENTRY_DSN environment variable to customize DSN.
            // If not set, telemetry features will be unavailable.
            options.setEnableExternalConfiguration(true);

            // Set -Dsentry.environment=production in releases to switch telemetry environment.
            // If not changed, "dev" will be used as the environment.
            options.setEnvironment("dev");

            options.setSendDefaultPii(true);
            options.setTracesSampleRate(1.0);
            options.getLogs().setEnabled(true);
            options.setRelease(Const.appVersion.toString());
        });
    }

    private static HeartbeatSession<WalDesktopHeartbeatCodec.WalDesktopHeartbeatEvent> desktopSession;

    public static void beginDesktopSession() {
        desktopSession = new HeartbeatSession<>(WalDesktopHeartbeatCodec.INSTANCE, WalDesktopHeartbeatCodec.WalDesktopHeartbeatEvent::new);
    }

    public static void endDesktopSession() {
        if (desktopSession != null)
            desktopSession.finish();
    }

    private static void reportModelSession(WalCoreHeartbeatCodec.WalHeartbeatEvent heartbeat, long endTimeMillis, SentryLogLevel level) {
        if (!enable) return;
        long durationSeconds = Math.max(0, (endTimeMillis - heartbeat.startTime()) / 1000);
        Sentry.logger().log(
                level,
                SentryLogParameters.create(
                        new SentryLongDate(endTimeMillis * 1_000_000L),
                        SentryAttributes.of(
                                SentryAttribute.stringAttribute("core.character_asset", normalizeAsset(heartbeat.asset())),
                                SentryAttribute.integerAttribute("core.session_duration", (int) durationSeconds)
                        )
                ),
                "MODEL_SESSION"
        );
    }

    private static void reportDesktopSession(WalDesktopHeartbeatCodec.WalDesktopHeartbeatEvent heartbeat, long endTimeMillis, SentryLogLevel level) {
        if (!enable) return;
        long durationSeconds = Math.max(0, (endTimeMillis - heartbeat.startTime()) / 1000);
        Sentry.logger().log(
                level,
                SentryLogParameters.create(
                        new SentryLongDate(endTimeMillis * 1_000_000L),
                        SentryAttributes.of(
                                SentryAttribute.integerAttribute("desktop.session_duration", (int) durationSeconds)
                        )
                ),
                "DESKTOP_SESSION"
        );
    }

    private static void reportConfig(Map<String, Object> config, long timestampMillis) {
        if (!enable) return;
        Sentry.logger().log(
                SentryLogLevel.INFO,
                SentryLogParameters.create(
                        new SentryLongDate(timestampMillis * 1_000_000L),
                        configAttributes(config)
                ),
                "CONFIG"
        );
    }

    private static String normalizeAsset(String asset) {
        return asset == null ? null : asset.replace('\\', '/');
    }

    private static SentryAttributes configAttributes(Map<String, Object> config) {
        SentryAttributes attributes = SentryAttributes.of();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = "core_config." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Boolean) {
                attributes.add(SentryAttribute.booleanAttribute(key, (Boolean) value));
            } else if (value instanceof Integer) {
                attributes.add(SentryAttribute.integerAttribute(key, (Integer) value));
            } else if (value instanceof Double) {
                attributes.add(SentryAttribute.doubleAttribute(key, (Double) value));
            } else if (value instanceof String) {
                attributes.add(SentryAttribute.stringAttribute(key, (String) value));
            }
        }
        return attributes;
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
        WalCoreHeartbeatCodec.WalHeartbeatEvent lastModelHeartbeat = null;
        long lastModelHeartbeatTime = 0;
        WalDesktopHeartbeatCodec.WalDesktopHeartbeatEvent lastDesktopHeartbeat = null;
        long lastDesktopHeartbeatTime = 0;
        WalRecord exceptionRecord = null;
        Map<String, Object> configSnapshot = null;
        long configTimestamp = 0;
        for (WalRecord record : records) {
            try {
                if (record.type().equals(WalCoreHeartbeatCodec.INSTANCE.type())) {
                    lastModelHeartbeat = WalCoreHeartbeatCodec.INSTANCE.decode(record.payload());
                    lastModelHeartbeatTime = record.timestamp();
                } else if (record.type().equals(WalDesktopHeartbeatCodec.INSTANCE.type())) {
                    lastDesktopHeartbeat = WalDesktopHeartbeatCodec.INSTANCE.decode(record.payload());
                    lastDesktopHeartbeatTime = record.timestamp();
                } else if (record.type().equals(WalExceptionCodec.INSTANCE.type())) {
                    exceptionRecord = record;
                } else if (record.type().equals(WalConfigCodec.INSTANCE.type())) {
                    configSnapshot = WalConfigCodec.INSTANCE.decode(record.payload());
                    configTimestamp = record.timestamp();
                }
            } catch (IOException ignored) {
            }
        }
        if (configSnapshot != null)
            reportConfig(configSnapshot, configTimestamp);
        if (lastModelHeartbeat != null) {
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
            } else if (lastModelHeartbeat.stopped()) {
                endTimeMillis = lastModelHeartbeatTime;
                level = SentryLogLevel.INFO;
            } else {
                endTimeMillis = lastModelHeartbeatTime;
                level = SentryLogLevel.WARN;
            }
            reportModelSession(lastModelHeartbeat, endTimeMillis, level);
        } else if (lastDesktopHeartbeat != null) {
            reportDesktopSession(
                    lastDesktopHeartbeat,
                    lastDesktopHeartbeatTime,
                    lastDesktopHeartbeat.stopped() ? SentryLogLevel.INFO : SentryLogLevel.WARN
            );
        }
    }

    public static boolean isEnable() {
        return enable;
    }

    public static void setEnable(boolean enable) {
        SentryHelper.enable = enable;
    }
}
