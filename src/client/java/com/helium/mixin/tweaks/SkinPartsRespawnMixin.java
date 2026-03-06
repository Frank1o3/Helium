package com.helium.mixin.tweaks;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class SkinPartsRespawnMixin {

    @Unique
    private boolean helium$dead = false;

    @Inject(method = "onPlayerRespawn(Lnet/minecraft/network/packet/s2c/play/PlayerRespawnS2CPacket;)V", at = @At("RETURN"), require = 0)
    private void helium$refreshskinonrespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        if (!helium$dead) return;
        helium$dead = false;

        HeliumConfig config = HeliumClient.getConfig();
        if (config == null || !config.forceSkinParts) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        mc.options.sendClientSettings();
    }

    @Inject(method = "onDeathMessage(Lnet/minecraft/network/packet/s2c/play/DeathMessageS2CPacket;)V", at = @At("RETURN"), require = 0)
    private void helium$trackdeath(DeathMessageS2CPacket packet, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            helium$dead = true;
        }
    }
}
