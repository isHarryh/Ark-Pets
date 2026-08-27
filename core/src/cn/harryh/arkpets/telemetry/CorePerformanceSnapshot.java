package cn.harryh.arkpets.telemetry;

import java.util.List;


/** A snapshot of Core rendering and process resource metrics. */
public record CorePerformanceSnapshot(
        long heapUsedBytes,
        Double processCpuRatio,
        List<RenderMetrics> renderMetrics
) {
    public CorePerformanceSnapshot {
        renderMetrics = renderMetrics == null ? List.of() : List.copyOf(renderMetrics);
    }

    /** A rendering metric aggregate for one Core resolution. */
    public record RenderMetrics(
            int width,
            int height,
            double renderTimeAverageMillis,
            double fps
    ) {
        public long pixels() {
            return (long) width * height;
        }

        public double renderTimeAveragePpx() {
            return renderTimeAverageMillis / pixels();
        }
    }
}
