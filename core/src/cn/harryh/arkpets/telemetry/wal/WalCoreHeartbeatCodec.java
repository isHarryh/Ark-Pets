package cn.harryh.arkpets.telemetry.wal;

import cn.harryh.arkpets.telemetry.CorePerformanceSnapshot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/** Encodes and decodes core session heartbeats.
 */
public final class WalCoreHeartbeatCodec implements WalCodec<WalCoreHeartbeatCodec.WalHeartbeatEvent> {
    public static final WalCoreHeartbeatCodec INSTANCE = new WalCoreHeartbeatCodec();

    private WalCoreHeartbeatCodec() {
    }

    @Override
    public String type() {
        return "heartbeat";
    }

    @Override
    public byte[] encode(WalHeartbeatEvent value) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeUTF(value.asset());
            dos.writeLong(value.startTime());
            dos.writeBoolean(value.stopped());
            CorePerformanceSnapshot performance = value.performance();
            dos.writeBoolean(performance != null);
            if (performance != null)
                writePerformance(dos, performance);
        }
        return bos.toByteArray();
    }

    @Override
    public WalHeartbeatEvent decode(byte[] payload) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(payload))) {
            String asset = dis.readUTF();
            long startTime = dis.readLong();
            boolean stopped = dis.readBoolean();
            CorePerformanceSnapshot performance = null;
            if (dis.available() > 0 && dis.readBoolean())
                performance = readPerformance(dis);
            return new WalHeartbeatEvent(asset, startTime, stopped, performance);
        }
    }

    private static void writePerformance(DataOutputStream out, CorePerformanceSnapshot performance) throws IOException {
        out.writeLong(performance.heapUsedBytes());
        Double processCpuRatio = performance.processCpuRatio();
        out.writeBoolean(processCpuRatio != null);
        if (processCpuRatio != null)
            out.writeDouble(processCpuRatio);

        List<CorePerformanceSnapshot.RenderMetrics> metrics = performance.renderMetrics();
        out.writeInt(metrics.size());
        for (CorePerformanceSnapshot.RenderMetrics metric : metrics) {
            out.writeInt(metric.width());
            out.writeInt(metric.height());
            out.writeDouble(metric.renderTimeAverageMillis());
            out.writeDouble(metric.fps());
        }
    }

    private static CorePerformanceSnapshot readPerformance(DataInputStream in) throws IOException {
        long heapUsedBytes = in.readLong();
        Double processCpuRatio = in.readBoolean() ? in.readDouble() : null;
        int metricCount = in.readInt();
        if (metricCount < 0 || metricCount > 10_000)
            throw new IOException("Invalid Core performance metric count: " + metricCount);

        ArrayList<CorePerformanceSnapshot.RenderMetrics> metrics = new ArrayList<>(metricCount);
        for (int i = 0; i < metricCount; i++) {
            metrics.add(new CorePerformanceSnapshot.RenderMetrics(
                    in.readInt(),
                    in.readInt(),
                    in.readDouble(),
                    in.readDouble()
            ));
        }
        return new CorePerformanceSnapshot(heapUsedBytes, processCpuRatio, metrics);
    }

    /** A heartbeat snapshot of a model session.
     * @param asset The model asset id.
     * @param startTime The session start time in epoch milliseconds.
     * @param stopped Whether this heartbeat is written on a normal exit.
     */
    public record WalHeartbeatEvent(
            String asset,
            long startTime,
            boolean stopped,
            CorePerformanceSnapshot performance
    ) {
    }
}
