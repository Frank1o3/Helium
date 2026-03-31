package com.helium.mixin.hotbar;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import com.helium.hotbar.HotbarOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public abstract class HotbarKeybindingMixin {

    @Inject(method = "setPressed", at = @At("HEAD"), require = 0)
    private void helium$onhotbarkeypressed(boolean pressed, CallbackInfo ci) {
        if (!pressed) return;

        HeliumConfig config = HeliumClient.getConfig();
        if (config == null || !config.hotbarOptimizer) return;

        KeyBinding bind = (KeyBinding) (Object) this;
        String key = bind.getId();
        if (!key.startsWith("key.hotbar.")) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isInSingleplayer()) return;
        if (client.interactionManager == null) return;

        ClientPlayerEntity player = client.player;
        if (player == null || player.isInCreativeMode()) return;

        try {
            int slot = Integer.parseInt(key.substring("key.hotbar.".length())) - 1;
            if (slot < 0 || slot > 8) return;
            HotbarOptimizer.syncslot(client, slot);
        } catch (NumberFormatException ignored) {}
    }
}
