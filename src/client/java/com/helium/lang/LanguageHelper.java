package com.helium.lang;

import java.util.concurrent.atomic.AtomicBoolean;

public final class LanguageHelper {

    private static final AtomicBoolean skipFullReload = new AtomicBoolean(false);

    private LanguageHelper() {}

    public static void setSkipFullReload(boolean skip) {
        skipFullReload.set(skip);
    }

    public static boolean shouldSkipFullReload() {
        return skipFullReload.getAndSet(false);
    }
}
