package com.helium.mixin.hotbar;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import com.helium.hotbar.HotbarOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class HotbarGameJoinMixin {

    @Inject(method = "onGameJoin", at = @At("TAIL"), require = 0)
    private void helium$warnhotbardisabled(GameJoinS2CPacket packet, CallbackInfo ci) {
        HeliumConfig config = HeliumClient.getConfig();
        if (config == null || !config.hotbarOptimizer) return;
        if (!HotbarOptimizer.isserverdisabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        client.player.sendMessage(
                Text.literal("[Helium] ").formatted(Formatting.GOLD)
                        .append(Text.literal("Hotbar Optimizer disabled on this server.").formatted(Formatting.RED)),
                false
        );
    }
}
