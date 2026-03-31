package com.helium.mixin.crafting;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import com.helium.crafting.OneClickCraftingManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.StonecutterScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerCraftingMixin {

    @Inject(at = @At("TAIL"), method = "onScreenHandlerSlotUpdate(Lnet/minecraft/network/packet/s2c/play/ScreenHandlerSlotUpdateS2CPacket;)V", require = 0)
    private void helium$onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        HeliumConfig config = HeliumClient.getConfig();
        if (config == null || !config.oneClickCrafting) return;
        if (!OneClickCraftingManager.isinitialized()) return;

        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen == null) {
            OneClickCraftingManager.reset();
            return;
        }
        if (screen instanceof CraftingScreen || screen instanceof InventoryScreen) {
            if (packet.getSlot() == 0 && packet.getStack() != null) {
                OneClickCraftingManager.onresultslotupdated(packet.getStack());
            }
        } else if (screen instanceof StonecutterScreen) {
            if (packet.getSlot() == 1 && packet.getStack() != null) {
                OneClickCraftingManager.onstonecutterresultupdated(packet.getStack());
            }
        }
    }
}
