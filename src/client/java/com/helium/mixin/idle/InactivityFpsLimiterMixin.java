package com.helium.mixin.idle;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InactivityFpsLimiter.class)
public class InactivityFpsLimiterMixin {

    @Inject(method = "update", at = @At("RETURN"), cancellable = true, require = 0)
    private void helium$limitInactiveFps(CallbackInfoReturnable<Integer> cir) {
        HeliumConfig config = HeliumClient.getConfig();
        if (config == null || !config.reduceFpsWhenInactive) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && !client.isWindowFocused()) {
            int limit = Math.max(1, config.inactiveFpsLimit);
            if (cir.getReturnValue() > limit) {
                cir.setReturnValue(limit);
            }
        }
    }
}
