package com.helium.mixin.hotbar;

import com.helium.hotbar.HotbarOptimizer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class HotbarTickResetMixin {

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void helium$resethotbarsent(CallbackInfo ci) {
        HotbarOptimizer.resettick();
    }
}
