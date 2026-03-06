package com.helium.tweaks;

import com.helium.HeliumClient;

public final class SmoothHotbar {

    private static final float SLOT_WIDTH = 20.0f;
    private static final float SMOOTHING_SPEED = 18.0f;
    private static final float SNAP_THRESHOLD = 0.5f;

    private static float currentx = -1.0f;
    private static float targetx = 0.0f;
    private static int lastslot = -1;

    private SmoothHotbar() {}

    public static void update(int selectedslot, float deltaticks) {
        com.helium.config.HeliumConfig config = HeliumClient.getConfig();
        if (config == null || !config.smoothHotbar) return;

        targetx = selectedslot * SLOT_WIDTH;

        if (currentx < 0 || lastslot < 0) {
            currentx = targetx;
            lastslot = selectedslot;
            return;
        }

        if (selectedslot != lastslot) {
            lastslot = selectedslot;
        }

        float diff = targetx - currentx;
        if (Math.abs(diff) < SNAP_THRESHOLD) {
            currentx = targetx;
            return;
        }

        float dt = Math.min(deltaticks / 20.0f, 0.05f);
        currentx = lerpdamped(currentx, targetx, dt, SMOOTHING_SPEED);
    }

    public static int getoffsetx(int basex) {
        com.helium.config.HeliumConfig config = HeliumClient.getConfig();
        if (config == null || !config.smoothHotbar || currentx < 0) {
            return basex;
        }
        return Math.round(basex + currentx);
    }

    public static float getcurrentx() {
        return currentx;
    }

    public static void reset() {
        currentx = -1.0f;
        lastslot = -1;
    }

    private static float lerpdamped(float current, float target, float dt, float speed) {
        float factor = 1.0f - (float) Math.exp(-speed * dt);
        return current + (target - current) * factor;
    }
}
