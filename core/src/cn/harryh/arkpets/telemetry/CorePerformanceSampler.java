package cn.harryh.arkpets.telemetry;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;


/** Aggregates Core frame and process metrics without doing I/O on the render thread. */
public final class CorePerformanceSampler {
    private final Runtime runtime = Runtime.getRuntime();
    private final OperatingSystemMXBean operatingSystem;
    private final ConcurrentMap<Resolution, Accumulator> accumulators = new ConcurrentHashMap<>();

    private volatile Accumulator activeAccumulator;
    private int activeWidth;
    private int activeHeight;
    private long frameStartNanos;

    public CorePerformanceSampler() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        operatingSystem = bean instanceof OperatingSystemMXBean candidate ? candidate : null;
    }

    /** Starts measuring one complete Core render callback. */
    public void beginFrame() {
        frameStartNanos = System.nanoTime();
    }

    /** Records one completed Core render callback. */
    public void endFrame(float frameDeltaSeconds, int width, int height) {
        if (width <= 0 || height <= 0)
            return;

        Accumulator accumulator = activeAccumulator;
        if (accumulator == null || width != activeWidth || height != activeHeight) {
            activeWidth = width;
            activeHeight = height;
            Resolution resolution = new Resolution(width, height);
            accumulator = accumulators.computeIfAbsent(resolution, ignored -> new Accumulator());
            activeAccumulator = accumulator;
        }

        double frameDeltaMillis = frameDeltaSeconds * 1_000.0;
        if (!Double.isFinite(frameDeltaMillis) || frameDeltaMillis < 0)
            frameDeltaMillis = 0;

        double renderTimeMillis = (System.nanoTime() - frameStartNanos) / 1_000_000.0;
        if (!Double.isFinite(renderTimeMillis) || renderTimeMillis < 0)
            renderTimeMillis = 0;

        accumulator.record(frameDeltaMillis, renderTimeMillis);
    }

    /** Captures and resets the current interval's metrics. */
    public CorePerformanceSnapshot snapshot() {
        ArrayList<CorePerformanceSnapshot.RenderMetrics> renderMetrics = new ArrayList<>();
        for (var entry : accumulators.entrySet()) {
            CorePerformanceSnapshot.RenderMetrics metrics = entry.getValue().snapshot(entry.getKey());
            if (metrics != null)
                renderMetrics.add(metrics);
        }

        long heapUsedBytes = Math.max(0, runtime.totalMemory() - runtime.freeMemory());
        Double processCpuRatio = null;
        if (operatingSystem != null) {
            double value = operatingSystem.getProcessCpuLoad();
            if (Double.isFinite(value) && value >= 0 && value <= 1)
                processCpuRatio = value;
        }
        return new CorePerformanceSnapshot(heapUsedBytes, processCpuRatio, renderMetrics);
    }

    private record Resolution(int width, int height) {
    }

    private static final class Accumulator {
        private final LongAdder frameCount = new LongAdder();
        private final DoubleAdder frameIntervalTotalMillis = new DoubleAdder();
        private final DoubleAdder renderTimeTotalMillis = new DoubleAdder();

        private void record(double frameIntervalMillis, double renderTimeMillis) {
            frameCount.increment();
            frameIntervalTotalMillis.add(frameIntervalMillis);
            renderTimeTotalMillis.add(renderTimeMillis);
        }

        private CorePerformanceSnapshot.RenderMetrics snapshot(Resolution resolution) {
            long frames = frameCount.sumThenReset();
            double totalFrameInterval = frameIntervalTotalMillis.sumThenReset();
            double totalRenderTime = renderTimeTotalMillis.sumThenReset();
            if (frames == 0)
                return null;

            double averageRenderTime = totalRenderTime / frames;
            double fps = totalFrameInterval > 0 ? frames * 1_000.0 / totalFrameInterval : 0;
            return new CorePerformanceSnapshot.RenderMetrics(
                    resolution.width,
                    resolution.height,
                    averageRenderTime,
                    fps
            );
        }
    }
}
