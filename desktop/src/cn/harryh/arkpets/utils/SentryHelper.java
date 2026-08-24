package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.wal.*;
import io.sentry.*;
import io.sentry.logger.SentryLogParameters;
import io.sentry.protocol.Feedback;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;


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
}
