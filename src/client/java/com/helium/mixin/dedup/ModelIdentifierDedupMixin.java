package com.helium.mixin.dedup;

import com.helium.HeliumClient;
import com.helium.dedup.DeduplicationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

@Pseudo
@Mixin(targets = "net.minecraft.client.util.ModelIdentifier")
public abstract class ModelIdentifierDedupMixin {

    @Unique
    private static boolean helium$failed = false;

    @Unique
    private static boolean helium$resolved = false;

    @Unique
    private static Field helium$variantField = null;

    @Unique
    private String[] helium$dedupedprops = null;

    @Unique
    private static void helium$resolve() {
        if (helium$resolved) return;
        helium$resolved = true;

        try {
            Class<?> clazz = Class.forName("net.minecraft.client.util.ModelIdentifier");

            String[] names = {"variant", "field_3662"};
            for (String name : names) {
                try {
                    Field f = clazz.getDeclaredField(name);
                    if (f.getType() == String.class) {
                        f.setAccessible(true);
                        helium$variantField = f;
                        return;
                    }
                } catch (NoSuchFieldException ignored) {}
            }

            for (Field f : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                if (f.getType() == String.class) {
                    f.setAccessible(true);
                    helium$variantField = f;
                    return;
                }
            }
        } catch (Throwable ignored) {
            helium$failed = true;
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"), require = 0)
    private void helium$dedupvariant(CallbackInfo ci) {
        if (helium$failed) return;

        try {
            if (!DeduplicationManager.isenabled()) return;

            helium$resolve();
            if (helium$variantField == null) return;

            String variant = (String) helium$variantField.get(this);
            if (variant == null || variant.isEmpty()) return;

            String[] parts = variant.split(",");
            helium$dedupedprops = new String[parts.length];
            for (int i = 0; i < parts.length; i++) {
                helium$dedupedprops[i] = DeduplicationManager.PROPERTIES.deduplicate(parts[i]);
            }

            String rejoined = String.join(",", helium$dedupedprops);
            helium$variantField.set(this, rejoined);
        } catch (Throwable t) {
            if (!helium$failed) {
                helium$failed = true;
                HeliumClient.LOGGER.warn("model identifier dedup disabled ({})", t.getClass().getSimpleName());
            }
        }
    }

    @Inject(method = "hashCode", at = @At("RETURN"), cancellable = true, require = 0)
    private void helium$improvedhash(CallbackInfoReturnable<Integer> cir) {
        if (helium$failed || helium$dedupedprops == null) return;
        try {
            cir.setReturnValue(31 * cir.getReturnValue() + Arrays.hashCode(helium$dedupedprops));
        } catch (Throwable ignored) {}
    }
}
