package com.helium.mixin.hotbar;

import com.helium.hotbar.HotbarOptimizer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class HotbarRenderSyncMixin {

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void helium$checkscrollsync(CallbackInfo ci) {
        HotbarOptimizer.checkscrollsync((MinecraftClient) (Object) this);
    }
}
