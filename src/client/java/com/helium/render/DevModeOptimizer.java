package com.helium.render;

import com.helium.HeliumClient;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLCapabilities;

public final class DevModeOptimizer {

    private static volatile boolean initialized = false;
    private static volatile boolean active = false;

    private static volatile boolean hasDebugOutput = false;
    private static volatile boolean hasBufferStorage = false;
    private static volatile boolean vboOrphaningEnabled = false;
    private static volatile boolean lightCacheEnabled = false;

    private DevModeOptimizer() {}

    public static void init() {
        if (initialized) return;
        try {
            GLCapabilities caps = GL.getCapabilities();
            hasDebugOutput = caps.GL_KHR_debug || caps.OpenGL43;
            hasBufferStorage = caps.GL_ARB_buffer_storage || caps.OpenGL44;
            initialized = true;
            HeliumClient.LOGGER.info("devmode caps: debugOutput={}, bufferStorage={}", hasDebugOutput, hasBufferStorage);
        } catch (Throwable t) {
            initialized = true;
            HeliumClient.LOGGER.warn("devmode cap query failed", t);
        }
    }

    public static void activate() {
        if (!initialized) return;
        active = true;
        vboOrphaningEnabled = true;
        lightCacheEnabled = true;
        applyDriverHints();
        if (GLStateCache.isInitialized()) GLStateCache.setAggressiveMode(true);
        HeliumClient.LOGGER.info("devmode optimizer activated (vboOrphaning={}, lightCache={})", vboOrphaningEnabled, lightCacheEnabled);
    }

    public static void deactivate() {
        active = false;
        vboOrphaningEnabled = false;
        lightCacheEnabled = false;
        if (GLStateCache.isInitialized()) GLStateCache.setAggressiveMode(false);
        HeliumClient.LOGGER.info("devmode optimizer deactivated");
    }

    public static boolean isActive() {
        return active && initialized;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static boolean isVboOrphaningEnabled() {
        return vboOrphaningEnabled;
    }

    public static boolean isLightCacheEnabled() {
        return lightCacheEnabled;
    }

    private static void applyDriverHints() {
        try {
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_FASTEST);
            GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_FASTEST);
            GL11.glHint(GL14.GL_GENERATE_MIPMAP_HINT, GL11.GL_FASTEST);
            GL11.glHint(GL20.GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL11.GL_FASTEST);

            if (hasDebugOutput) {
                try {
                    GL43.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE,
                            GL43.GL_DEBUG_SEVERITY_NOTIFICATION, (int[]) null, false);
                    GL43.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE,
                            GL43.GL_DEBUG_SEVERITY_LOW, (int[]) null, false);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable t) {
            HeliumClient.LOGGER.warn("driver hints failed", t);
        }
    }

    public static void onFrameStart() {
        if (!active) return;
        FrameLightCache.onFrameStart();
    }

    public static void onResourceReload() {
        FrameLightCache.invalidate();
        if (active) applyDriverHints();
    }

    public static void shutdown() {
        if (active) deactivate();
        initialized = false;
    }
}
