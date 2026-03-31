package com.helium.mixin.dedup;

import com.helium.dedup.DeduplicationManager;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {
        "net.minecraft.block.ComposterBlock$EmptyComposterInventory",
        "net.minecraft.block.ComposterBlock$EmptyContainer",
        "net.minecraft.block.ComposterBlock.EmptyComposterInventory"
})
public abstract class ComposterEmptyMixin {

    @Unique
    private static final int[] HELIUM_EMPTY = new int[0];

    @Inject(method = "getAvailableSlots", at = @At("RETURN"), cancellable = true, require = 0)
    private void helium$fixemptyslots(Direction direction, CallbackInfoReturnable<int[]> cir) {
        if (!DeduplicationManager.isenabled()) return;
        cir.setReturnValue(HELIUM_EMPTY);
    }
}
