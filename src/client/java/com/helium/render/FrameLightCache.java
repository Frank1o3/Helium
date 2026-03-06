package com.helium.render;

public final class FrameLightCache {

    public static final int MISS = Integer.MIN_VALUE;

    private static final int SIZE = 4096;
    private static final int MASK = SIZE - 1;

    private static final long[] _keys = new long[SIZE];
    private static final int[] _values = new int[SIZE];

    private static long _frameTag = 0L;

    static {
        for (int i = 0; i < SIZE; i++) _keys[i] = Long.MIN_VALUE;
    }

    private FrameLightCache() {}

    public static void onFrameStart() {
        _frameTag++;
    }

    public static void invalidate() {
        _frameTag += SIZE;
    }

    public static int get(long packedPos) {
        int slot = slot(packedPos);
        if (_keys[slot] == (packedPos ^ _frameTag)) {
            return _values[slot];
        }
        return MISS;
    }

    public static void put(long packedPos, int light) {
        int slot = slot(packedPos);
        _keys[slot] = packedPos ^ _frameTag;
        _values[slot] = light;
    }

    private static int slot(long packedPos) {
        int h = (int)(packedPos ^ (packedPos >>> 32));
        h ^= h >>> 16;
        h *= 0x45d9f3b;
        h ^= h >>> 16;
        return h & MASK;
    }
}
