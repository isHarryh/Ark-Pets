package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.telemetry.CorePerformanceSnapshot;
import cn.harryh.arkpets.telemetry.HeartbeatSession;
import cn.harryh.arkpets.telemetry.wal.*;
import io.sentry.*;
import io.sentry.logger.SentryLogParameters;
import io.sentry.metrics.MetricsUnit;
import io.sentry.metrics.SentryMetricsParameters;
import io.sentry.protocol.Feedback;
import io.sentry.protocol.SentryId;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class SentryHelper {
    private static boolean enable = false;
    private static boolean sdkAvailable = false;

    public static void init() {
        try {
            Sentry.init(options -> {
                // An empty DSN will disable the SDK gracefully.
                options.setDsn("");
                // A non-empty value from external configuration will override it.
                // Set -Dsentry.dsn Java option or SENTRY_DSN environment variable to customize DSN.
                options.setEnableExternalConfiguration(true);

                // Set -Dsentry.environment=production in releases to switch telemetry environment.
                // If not changed, "dev" will be used as the environment.
                options.setEnvironment("dev");

                options.setSendDefaultPii(true);
                options.setTracesSampleRate(1.0);
                options.getLogs().setEnabled(true);
                options.setRelease("arkpets@"+ Const.appVersion);
            });
        } catch (Exception | LinkageError e) {
            Logger.warn("Telemetry", "Failed to initialize the Sentry SDK, telemetry is unavailable. " + e);
        }
        sdkAvailable = Sentry.isEnabled();
        if (!sdkAvailable)
            Logger.info("Telemetry", "Sentry SDK is not active due to missing, invalid or disabled configuration, telemetry is unavailable");
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
        Logger.debug("Telemetry", "Uploaded a MODEL_SESSION event");
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
        Logger.debug("Telemetry", "Uploaded a DESKTOP_SESSION event");
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
        Logger.debug("Telemetry", "Uploaded a CONFIG event");
    }

    private static void reportSystemInfo(WalSystemInfoCodec.SystemInfo info, long timestampMillis) {
        if (!enable) return;
        Sentry.logger().log(
                SentryLogLevel.INFO,
                SentryLogParameters.create(
                        new SentryLongDate(timestampMillis * 1_000_000L),
                        SentryAttributes.of(
                                SentryAttribute.stringAttribute("core.gpu.name", info.gpuName()),
                                SentryAttribute.stringAttribute("core.gpu.version", info.gpuVersion()),
                                SentryAttribute.stringAttribute("core.os.name", info.osName()),
                                SentryAttribute.stringAttribute("core.os.arch", info.osArch()),
                                SentryAttribute.stringAttribute("core.gpu.renderer", info.gpuRenderer())
                        )
                ),
                "SYSTEM_INFO"
        );
        Logger.debug("Telemetry", "Uploaded a SYSTEM_INFO event");
    }

    private static void reportCorePerformance(
            CorePerformanceSnapshot performance,
            String asset,
            long timestampMillis
    ) {
        if (!enable || performance == null) return;

        SentryAttributes resourceAttributes = SentryAttributes.of(
                SentryAttribute.stringAttribute("core.character_asset", normalizeAsset(asset))
        );
        reportGauge(
                "core.memory.heap_used",
                (double) performance.heapUsedBytes(),
                MetricsUnit.Information.BYTE,
                timestampMillis,
                resourceAttributes
        );
        if (performance.processCpuRatio() != null) {
            reportGauge(
                    "core.cpu.process_usage",
                    performance.processCpuRatio(),
                    MetricsUnit.Fraction.RATIO,
                    timestampMillis,
                    resourceAttributes
            );
        }

        for (CorePerformanceSnapshot.RenderMetrics metrics : performance.renderMetrics()) {
            SentryAttributes attributes = SentryAttributes.of(
                    SentryAttribute.stringAttribute("core.character_asset", normalizeAsset(asset)),
                    SentryAttribute.integerAttribute("core.render.width", metrics.width()),
                    SentryAttribute.integerAttribute("core.render.height", metrics.height()),
                    SentryAttribute.integerAttribute("core.render.pixels", (int) metrics.pixels())
            );
            reportDistribution(
                    "core.render.callback_time",
                    metrics.renderTimeAverageMillis(),
                    MetricsUnit.Duration.MILLISECOND,
                    timestampMillis,
                    attributes
            );
            reportDistribution(
                    "core.render.callback_time_ppx",
                    metrics.renderTimeAveragePpx(),
                    null,
                    timestampMillis,
                    attributes
            );

            reportDistribution("core.render.fps", metrics.fps(), null, timestampMillis, attributes);
        }
        Logger.debug("Telemetry", "Uploaded a core performance metrics");
    }

    private static void reportGauge(
            String name,
            double value,
            String unit,
            long timestampMillis,
            SentryAttributes attributes
    ) {
        Sentry.metrics().gauge(
                name,
                value,
                unit,
                SentryMetricsParameters.create(new SentryLongDate(timestampMillis * 1_000_000L), attributes)
        );
    }

    private static void reportDistribution(
            String name,
            double value,
            String unit,
            long timestampMillis,
            SentryAttributes attributes
    ) {
        if (!Double.isFinite(value)) return;
        Sentry.metrics().distribution(
                name,
                value,
                unit,
                SentryMetricsParameters.create(new SentryLongDate(timestampMillis * 1_000_000L), attributes)
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

    public static boolean captureLogFeedback(List<String> fileList) {
        if (!sdkAvailable) {
            Logger.warn("Telemetry", "Sentry SDK unavailable, unable to upload the user log feedback");
            return false;
        }
        SentryId sentryId = Sentry.feedback().capture(
                new Feedback("User uploaded ArkPets log files."),
                Hint.withAttachments(fileList.stream().map(Attachment::new).toList())
        );
        if (SentryId.EMPTY_ID.equals(sentryId)) {
            Logger.warn("Telemetry", "Failed to submit a user log feedback to the Sentry SDK");
            return false;
        }
        Logger.info("Telemetry", "Submitted a user log feedback, Sentry ID is " + sentryId);
        return true;
    }

    public static void consumePendingWal() {
        if (!sdkAvailable) {
            Logger.debug("Telemetry", "Sentry SDK unavailable, now keeping existing WAL files");
            return;
        }

        // If telemetry features were disabled, delete all WAL files and skip consuming.
        if (!enable) {
            Logger.debug("Telemetry", "Telemetry disabled, now deleting existing WAL files");
            for (File file : WalReader.listWalFiles())
                if (!file.delete())
                    Logger.warn("Telemetry", "Failed to delete existing WAL file " + file.getName());
            return;
        }

        for (File file : WalReader.listWalFiles()) {
            // Skip WAL file whose process is still alive
            if (ProcessHandle.of(WalReader.parsePid(file)).map(ProcessHandle::isAlive).orElse(false))
                continue;
            try (WalReader reader = WalReader.open(file)) {
                Logger.debug("Telemetry", "Consuming WAL file " + file.getName());
                consumeWalRecords(reader.readAll());
            } catch (IOException e) {
                Logger.warn("Telemetry", "Failed to consume WAL file " + file.getName() + ", will retry later");
                continue;
            }
            if (!file.delete())
                Logger.warn("Telemetry", "Failed to delete consumed WAL file " + file.getName());
        }
    }

    private static void consumeWalRecords(List<WalRecord> records) {
        WalData data = collectWalRecords(records);
        if (data.configSnapshot() != null)
            reportConfig(data.configSnapshot(), data.configTimestamp());
        if (data.systemInfo() != null)
            reportSystemInfo(data.systemInfo(), data.systemInfoTimestamp());
        for (ModelHeartbeatRecord heartbeatRecord : data.modelHeartbeats()) {
            reportCorePerformance(
                    heartbeatRecord.heartbeat().performance(),
                    heartbeatRecord.heartbeat().asset(),
                    heartbeatRecord.timestampMillis()
            );
        }
        reportSession(data);
    }

    private static WalData collectWalRecords(List<WalRecord> records) {
        WalCoreHeartbeatCodec.WalHeartbeatEvent lastModelHeartbeat = null;
        long lastModelHeartbeatTime = 0;
        WalDesktopHeartbeatCodec.WalDesktopHeartbeatEvent lastDesktopHeartbeat = null;
        long lastDesktopHeartbeatTime = 0;
        WalRecord exceptionRecord = null;
        Map<String, Object> configSnapshot = null;
        long configTimestamp = 0;
        List<ModelHeartbeatRecord> modelHeartbeats = new ArrayList<>();
        WalSystemInfoCodec.SystemInfo systemInfo = null;
        long systemInfoTimestamp = 0;
        for (WalRecord record : records) {
            try {
                if (record.type().equals(WalCoreHeartbeatCodec.INSTANCE.type())) {
                    lastModelHeartbeat = WalCoreHeartbeatCodec.INSTANCE.decode(record.payload());
                    lastModelHeartbeatTime = record.timestamp();
                    modelHeartbeats.add(new ModelHeartbeatRecord(lastModelHeartbeat, record.timestamp()));
                } else if (record.type().equals(WalDesktopHeartbeatCodec.INSTANCE.type())) {
                    lastDesktopHeartbeat = WalDesktopHeartbeatCodec.INSTANCE.decode(record.payload());
                    lastDesktopHeartbeatTime = record.timestamp();
                } else if (record.type().equals(WalExceptionCodec.INSTANCE.type())) {
                    exceptionRecord = record;
                } else if (record.type().equals(WalConfigCodec.INSTANCE.type())) {
                    configSnapshot = WalConfigCodec.INSTANCE.decode(record.payload());
                    configTimestamp = record.timestamp();
                } else if (record.type().equals(WalSystemInfoCodec.INSTANCE.type())) {
                    systemInfo = WalSystemInfoCodec.INSTANCE.decode(record.payload());
                    systemInfoTimestamp = record.timestamp();
                }
            } catch (IOException ignored) {
            }
        }
        return new WalData(
                lastModelHeartbeat, lastModelHeartbeatTime,
                lastDesktopHeartbeat, lastDesktopHeartbeatTime,
                exceptionRecord,
                configSnapshot, configTimestamp,
                modelHeartbeats,
                systemInfo, systemInfoTimestamp
        );
    }

    private static void reportSession(WalData data) {
        WalCoreHeartbeatCodec.WalHeartbeatEvent lastModelHeartbeat = data.lastModelHeartbeat();
        if (lastModelHeartbeat != null) {
            long endTimeMillis;
            SentryLogLevel level;
            if (data.exceptionRecord() != null) {
                endTimeMillis = data.exceptionRecord().timestamp();
                level = SentryLogLevel.ERROR;
                try {
                    Exception exception = WalExceptionCodec.INSTANCE.decode(data.exceptionRecord().payload());
                    SentryEvent event = new SentryEvent(exception);
                    event.setTimestamp(new Date(data.exceptionRecord().timestamp()));
                    Sentry.captureEvent(event);
                } catch (IOException ignored) {
                }
            } else if (lastModelHeartbeat.stopped()) {
                endTimeMillis = data.lastModelHeartbeatTime();
                level = SentryLogLevel.INFO;
            } else {
                endTimeMillis = data.lastModelHeartbeatTime();
                level = SentryLogLevel.WARN;
            }
            reportModelSession(lastModelHeartbeat, endTimeMillis, level);
        } else if (data.lastDesktopHeartbeat() != null) {
            reportDesktopSession(
                    data.lastDesktopHeartbeat(),
                    data.lastDesktopHeartbeatTime(),
                    data.lastDesktopHeartbeat().stopped() ? SentryLogLevel.INFO : SentryLogLevel.WARN
            );
        }
    }

    private record WalData(
            WalCoreHeartbeatCodec.WalHeartbeatEvent lastModelHeartbeat,
            long lastModelHeartbeatTime,
            WalDesktopHeartbeatCodec.WalDesktopHeartbeatEvent lastDesktopHeartbeat,
            long lastDesktopHeartbeatTime,
            WalRecord exceptionRecord,
            Map<String, Object> configSnapshot,
            long configTimestamp,
            List<ModelHeartbeatRecord> modelHeartbeats,
            WalSystemInfoCodec.SystemInfo systemInfo,
            long systemInfoTimestamp
    ) {
    }

    private record ModelHeartbeatRecord(
            WalCoreHeartbeatCodec.WalHeartbeatEvent heartbeat,
            long timestampMillis
    ) {
    }

    public static boolean isEnable() {
        return enable;
    }

    public static boolean isSdkAvailable() {
        return sdkAvailable;
    }

    public static void setEnable(boolean enable) {
        SentryHelper.enable = enable && sdkAvailable;
    }
}
