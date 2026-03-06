package com.helium.feature;

import com.helium.HeliumClient;

import java.util.concurrent.atomic.AtomicBoolean;

public final class FullbrightManager {

    private static final AtomicBoolean enabled = new AtomicBoolean(false);
    private static volatile int strength = 10;

    private FullbrightManager() {}

    public static void toggle() {
        if (enabled.get()) {
            disable();
        } else {
            enable();
        }
    }

    public static void enable() {
        enabled.set(true);
        HeliumClient.LOGGER.info("fullbright enabled (strength={})", strength);
    }

    public static void disable() {
        enabled.set(false);
        HeliumClient.LOGGER.info("fullbright disabled");
    }

    public static void setEnabled(boolean state) {
        if (state == enabled.get()) return;
        if (state) enable(); else disable();
    }

    public static boolean isEnabled() {
        return enabled.get();
    }

    public static void setStrength(int value) {
        strength = Math.clamp(value, 0, 10);
    }

    public static int getStrength() {
        return strength;
    }

    public static float getEffectiveGamma() {
        return (float) strength;
    }
}
