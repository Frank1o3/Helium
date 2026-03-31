package com.helium.mixin.math;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import com.helium.math.GBFMath;
import net.minecraft.util.math.GivensPair;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GivensPair.class)
public abstract class GivensPairMixin {

    @Unique
    private static final GivensPair helium$IDENTITY = new GivensPair(0.0F, 1.0F);

    @Inject(method = "normalize", at = @At("HEAD"), cancellable = true, require = 0)
    private static void helium$fastNormalize(float a, float b, CallbackInfoReturnable<GivensPair> cir) {
        HeliumConfig config = HeliumClient.getConfig();
        if (config != null && config.fastMath) {
            float f = GBFMath.lengthsquared(a, b);
            if (f < GBFMath.NORMALIZE_EPSILON) {
                cir.setReturnValue(helium$IDENTITY);
                return;
            }
            float inv = GBFMath.invsqrt(f);
            cir.setReturnValue(new GivensPair(a * inv, b * inv));
        }
    }

    @Inject(method = "fromAngle", at = @At("HEAD"), cancellable = true, require = 0)
    private static void helium$fastFromAngle(float radians, CallbackInfoReturnable<GivensPair> cir) {
        HeliumConfig config = HeliumClient.getConfig();
        if (config != null && config.fastMath) {
            float f = 0.5F * radians;
            float sin = MathHelper.sin(f);
            float cos = GBFMath.cosfromsin(sin, f);
            cir.setReturnValue(new GivensPair(sin, cos));
        }
    }
}
