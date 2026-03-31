package com.helium.mixin.tweaks;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class SkinPartsDimensionMixin {

    @Inject(method = "setWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("RETURN"), require = 0)
    private void helium$refreshskinondimchange(ClientWorld world, CallbackInfo ci) {
        if (world == null) return;

        HeliumConfig config = HeliumClient.getConfig();
        if (config == null || !config.forceSkinParts) return;

        MinecraftClient mc = (MinecraftClient) (Object) this;
        if (mc.player == null) return;

        mc.options.sendClientSettings();
    }
}
