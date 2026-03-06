package com.helium.idle;

import com.helium.HeliumClient;

import java.util.concurrent.atomic.AtomicBoolean;

public final class IdleManager {

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private IdleManager() {}

    public static void init() {
        if (initialized.getAndSet(true)) return;
        HeliumClient.LOGGER.info("idle manager initialized");
    }

    public static boolean isInitialized() {
        return initialized.get();
    }

    public static void handleclienttick() {
    }

    public static void setIdleFpsLimit(int limit) {
    }

    public static void setTimeoutSeconds(int timeout) {
    }

    public static void shutdown() {
        initialized.set(false);
    }
}
