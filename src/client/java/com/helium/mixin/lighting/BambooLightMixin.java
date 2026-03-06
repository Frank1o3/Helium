package com.helium.mixin.lighting;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public abstract class BambooLightMixin {

    @Inject(method = "getAmbientOcclusionLightLevel", at = @At("HEAD"), cancellable = true, require = 0)
    private void helium$skipBambooLightCalc(BlockState state, BlockView world, BlockPos pos,
                                            CallbackInfoReturnable<Float> cir) {
        HeliumConfig config = HeliumClient.getConfig();
        if (config == null || !config.fastBambooLight) {
            return;
        }
        if (state.getBlock() instanceof BambooBlock) {
            cir.setReturnValue(1.0F);
        }
    }
}
