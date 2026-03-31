package com.helium.mixin.hotbar;

import com.helium.hotbar.HotbarOptimizer;
import com.helium.hotbar.HotbarServerDatabase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class HotbarConnectScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), require = 0)
    private void helium$checkhotbarserver(CallbackInfo ci) {
        HotbarOptimizer.setserverdisabled(false);
        HotbarOptimizer.resetslot();

        MinecraftClient client = MinecraftClient.getInstance();
        ServerInfo entry = client.getCurrentServerEntry();
        if (entry == null) return;

        String address = entry.address;
        if (HotbarServerDatabase.isblocked(address) || HotbarServerDatabase.isflagged(address)) {
            HotbarOptimizer.setserverdisabled(true);
        }
    }
}
