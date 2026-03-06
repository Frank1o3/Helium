package com.helium.mixin.compat;

import com.helium.HeliumClient;
import com.helium.render.Cullable;
import com.helium.render.LeafCullingEngine;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache", remap = false)
public class SodiumLeafCullingMixin {

    @Unique
    private static boolean helium$failed = false;

    @Unique
    private static boolean helium$logged = false;

    @Inject(
            method = "shouldDrawSide",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void helium$cullLeafFace(BlockState state, BlockView view, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> cir) {
        if (helium$failed) return;

        try {
            if (!LeafCullingEngine.isEnabled()) return;

            Block block = state.getBlock();
            if (block instanceof Cullable cullable) {
                if (!helium$logged) {
                    helium$logged = true;
                    HeliumClient.LOGGER.info("sodium leaf culling mixin active - processing leaves");
                }
                if (cullable.helium$shouldCullSide(state, view, pos, facing)) {
                    cir.setReturnValue(false);
                }
            }
        } catch (Throwable t) {
            if (!helium$failed) {
                helium$failed = true;
                HeliumClient.LOGGER.warn("sodium leaf culling failed ({})", t.getClass().getSimpleName());
            }
        }
    }
}
