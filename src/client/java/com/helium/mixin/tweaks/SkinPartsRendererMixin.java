package com.helium.mixin.tweaks;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntityRenderer.class)
public abstract class SkinPartsRendererMixin {

    @Redirect(
            method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/PlayerLikeEntity;isModelPartVisible(Lnet/minecraft/entity/player/PlayerModelPart;)Z"),
            require = 0
    )
    private boolean helium$forceskinparts(PlayerLikeEntity instance, PlayerModelPart part) {
        HeliumConfig config = HeliumClient.getConfig();
        if (config != null && config.forceSkinParts) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && instance == mc.player) {
                return mc.options.isPlayerModelPartEnabled(part);
            }
        }
        return instance.isModelPartVisible(part);
    }
}
