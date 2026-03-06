package com.helium.util;

import net.minecraft.client.render.Camera;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Method;

public final class VersionCompat {

    private static Method legacyCameraPos = null;
    private static boolean cameraFallbackResolved = false;

    private VersionCompat() {}

    public static Identifier createIdentifier(String namespace, String path) {
        return Identifier.of(namespace, path);
    }

    public static Vec3d getCameraPosition(Camera camera) {
        try {
            return camera.getCameraPos();
        } catch (NoSuchMethodError e) {
            return getCameraPositionLegacy(camera);
        }
    }

    private static Vec3d getCameraPositionLegacy(Camera camera) {
        if (!cameraFallbackResolved) {
            cameraFallbackResolved = true;
            try {
                String mapped = net.fabricmc.loader.api.FabricLoader.getInstance()
                        .getMappingResolver()
                        .mapMethodName("intermediary",
                                "net.minecraft.class_4184",
                                "method_19326",
                                "()Lnet/minecraft/class_243;");
                legacyCameraPos = Camera.class.getMethod(mapped);
            } catch (Throwable ignored) {}
        }

        if (legacyCameraPos != null) {
            try {
                return (Vec3d) legacyCameraPos.invoke(camera);
            } catch (Throwable ignored) {}
        }

        return Vec3d.ZERO;
    }
}
