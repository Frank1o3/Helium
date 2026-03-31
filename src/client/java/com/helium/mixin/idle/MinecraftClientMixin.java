package com.helium.mixin.idle;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import com.helium.util.VersionMethodResolver;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public abstract boolean isWindowFocused();

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void helium$checkInactiveFps(CallbackInfo ci) {
        HeliumConfig config = HeliumClient.getConfig();
        if (config == null || !config.reduceFpsWhenInactive) return;

        MinecraftClient client = (MinecraftClient) (Object) this;
        if (!client.isWindowFocused()) {
            VersionMethodResolver.applyinactivefpslimit(client, config.inactiveFpsLimit);
        }
    }
}
