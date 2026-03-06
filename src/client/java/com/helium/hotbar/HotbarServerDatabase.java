package com.helium.hotbar;

import java.util.List;

public final class HotbarServerDatabase {

    private static final List<String> FLAGGED_SERVERS = List.of(
            "mcpvp.club"
    );

    private static final List<String> BLOCKED_SERVERS = List.of();

    private HotbarServerDatabase() {}

    public static boolean isflagged(String address) {
        if (address == null) return false;
        String lower = address.toLowerCase();
        for (String s : FLAGGED_SERVERS) {
            if (lower.contains(s)) return true;
        }
        return false;
    }

    public static boolean isblocked(String address) {
        if (address == null) return false;
        String lower = address.toLowerCase();
        for (String s : BLOCKED_SERVERS) {
            if (lower.contains(s)) return true;
        }
        return false;
    }
}
