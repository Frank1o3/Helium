package com.helium.render;

import com.helium.HeliumClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ModelCache {

    private static volatile ConcurrentHashMap<Long, Object> cache;
    private static volatile boolean initialized = false;
    private static volatile int maxEntries = 8192;
    private static final AtomicInteger hits = new AtomicInteger(0);
    private static final AtomicInteger misses = new AtomicInteger(0);

    private ModelCache() {}

    public static void init(int maxSizeMb) {
        if (initialized) return;

        maxEntries = Math.max(1024, (maxSizeMb * 1024 * 1024) / 512);

        cache = new ConcurrentHashMap<>(1024, 0.75f, Runtime.getRuntime().availableProcessors());

        initialized = true;
        HeliumClient.LOGGER.info("model cache initialized (max {} entries, ~{}mb)", maxEntries, maxSizeMb);
    }

    public static boolean isInitialized() {
        return initialized;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(long key) {
        if (!initialized) return null;
        Object val = cache.get(key);
        if (val != null) {
            hits.incrementAndGet();
            return (T) val;
        }
        misses.incrementAndGet();
        return null;
    }

    public static void put(long key, Object value) {
        if (!initialized || value == null) return;
        cache.put(key, value);
        if (cache.size() > maxEntries) {
            var it = cache.entrySet().iterator();
            int toRemove = cache.size() - maxEntries;
            while (it.hasNext() && toRemove > 0) {
                it.next();
                it.remove();
                toRemove--;
            }
        }
    }

    public static void invalidate(long key) {
        if (!initialized) return;
        cache.remove(key);
    }

    public static void invalidateAll() {
        if (!initialized) return;
        cache.clear();
        hits.set(0);
        misses.set(0);
    }

    public static int size() {
        if (!initialized) return 0;
        return cache.size();
    }

    public static int getHits() {
        return hits.get();
    }

    public static int getMisses() {
        return misses.get();
    }

    public static float getHitRate() {
        int total = hits.get() + misses.get();
        return total > 0 ? (float) hits.get() / total : 0f;
    }
}
