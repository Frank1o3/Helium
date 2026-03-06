package com.helium.reflex;

import com.helium.HeliumClient;
import com.helium.gpu.GpuDetector;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ReflexManager {

    public static final int MODE_DISABLED = 0;
    public static final int MODE_TIMESTAMP = 1;
    public static final int MODE_ELAPSED = 2;

    private static final long MAX_WAIT_NS = 2_000_000L;
    private static final double SMOOTH_ALPHA = 0.15;

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final int[] queryIds = new int[2];

    private static volatile int timingMode = MODE_DISABLED;
    private static volatile int queryIndex = 0;
    private static volatile long lastGpuDoneNs = -1L;
    private static volatile long lastFrameEndNs = -1L;
    private static volatile double smoothedDeltaNs = 0.0;
    private static volatile boolean isNvidiaOptimal = false;

    private ReflexManager() {}

    public static void init() {
        if (initialized.getAndSet(true)) return;

        try {
            if (GL.getCapabilities().GL_ARB_timer_query) {
                timingMode = MODE_TIMESTAMP;
                GL15.glGenQueries(queryIds);
                isNvidiaOptimal = GpuDetector.isNvidia();
                HeliumClient.LOGGER.info("reflex: using timestamp queries (optimal={})", isNvidiaOptimal);
            } else if (GL.getCapabilities().GL_EXT_timer_query || GL.getCapabilities().GL_ARB_occlusion_query) {
                timingMode = MODE_ELAPSED;
                GL15.glGenQueries(queryIds);
                isNvidiaOptimal = false;
                HeliumClient.LOGGER.info("reflex: using elapsed time queries (fallback mode)");
            } else {
                timingMode = MODE_DISABLED;
                HeliumClient.LOGGER.warn("reflex: no GPU timing support, disabled");
            }
        } catch (Throwable t) {
            timingMode = MODE_DISABLED;
            HeliumClient.LOGGER.warn("reflex init failed: {}", t.getMessage());
        }
    }

    public static boolean isInitialized() {
        return initialized.get();
    }

    public static boolean isEnabled() {
        return timingMode != MODE_DISABLED;
    }

    public static boolean isNvidiaOptimal() {
        return isNvidiaOptimal;
    }

    public static int getTimingMode() {
        return timingMode;
    }

    public static String getTimingModeString() {
        return switch (timingMode) {
            case MODE_TIMESTAMP -> "TIMESTAMP";
            case MODE_ELAPSED -> "ELAPSED";
            default -> "DISABLED";
        };
    }

    public static void onFrameStart(long offsetNs) {
        if (timingMode == MODE_DISABLED) return;

        long cpuNow = System.nanoTime();
        long gpuDone = -1;

        try {
            gpuDone = switch (timingMode) {
                case MODE_TIMESTAMP -> getGpuTimestamp(cpuNow);
                case MODE_ELAPSED -> getGpuElapsedTime(cpuNow);
                default -> -1;
            };
        } catch (Throwable ignored) {}

        if (gpuDone > 0 && gpuDone < cpuNow) {
            lastGpuDoneNs = gpuDone;
            long cpuElapsed = cpuNow - lastGpuDoneNs;
            smoothedDeltaNs = SMOOTH_ALPHA * cpuElapsed + (1.0 - SMOOTH_ALPHA) * smoothedDeltaNs;

            long waitNs = (long) (smoothedDeltaNs + offsetNs);
            waitNs = Math.max(-MAX_WAIT_NS, Math.min(MAX_WAIT_NS, waitNs));

            if (waitNs > 0) {
                smartWait(cpuNow, waitNs);
            }
        }
    }

    public static void onFrameEnd() {
        if (timingMode == MODE_DISABLED) return;

        try {
            switch (timingMode) {
                case MODE_TIMESTAMP -> GL33.glQueryCounter(queryIds[queryIndex], GL33.GL_TIMESTAMP);
                case MODE_ELAPSED -> {
                    GL15.glBeginQuery(GL33.GL_TIME_ELAPSED, queryIds[queryIndex]);
                    GL15.glEndQuery(GL33.GL_TIME_ELAPSED);
                }
            }
            queryIndex ^= 1;
            lastFrameEndNs = System.nanoTime();
        } catch (Throwable ignored) {}
    }

    private static long getGpuTimestamp(long cpuNow) {
        int prev = queryIndex ^ 1;
        if (!GL15.glIsQuery(queryIds[prev])) return -1;

        int[] ready = {0};
        GL15.glGetQueryObjectiv(queryIds[prev], GL15.GL_QUERY_RESULT_AVAILABLE, ready);
        if (ready[0] == 0) return -1;

        long gpuTime = GL33.glGetQueryObjecti64(queryIds[prev], GL15.GL_QUERY_RESULT);
        return (gpuTime > 0 && gpuTime < cpuNow) ? gpuTime : -1;
    }

    private static long getGpuElapsedTime(long cpuNow) {
        int prev = queryIndex ^ 1;
        if (!GL15.glIsQuery(queryIds[prev])) return -1;

        int[] ready = {0};
        GL15.glGetQueryObjectiv(queryIds[prev], GL15.GL_QUERY_RESULT_AVAILABLE, ready);
        if (ready[0] == 0) return -1;

        int[] timeNs = {0};
        GL15.glGetQueryObjectiv(queryIds[prev], GL15.GL_QUERY_RESULT, timeNs);
        return (lastFrameEndNs > 0) ? lastFrameEndNs + timeNs[0] : -1;
    }

    private static void smartWait(long startTime, long waitNs) {
        long endTime = startTime + waitNs;

        while (System.nanoTime() < endTime - 100_000L) {
            Thread.onSpinWait();
        }

        while (System.nanoTime() < endTime) {
            try {
                Thread.sleep(0, 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public static void shutdown() {
        if (timingMode != MODE_DISABLED) {
            try {
                GL15.glDeleteQueries(queryIds);
            } catch (Throwable ignored) {}
        }
        timingMode = MODE_DISABLED;
        initialized.set(false);
    }
}
