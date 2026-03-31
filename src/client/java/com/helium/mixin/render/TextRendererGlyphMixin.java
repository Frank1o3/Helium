package com.helium.mixin.render;

import com.helium.render.TextRenderOptimizer;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public class TextRendererGlyphMixin {

    @Inject(method = "getGlyph", at = @At("HEAD"), cancellable = true, require = 0)
    private void helium$cacheglyphlookup(int codePoint, Style style, CallbackInfoReturnable<BakedGlyph> cir) {
        if (!TextRenderOptimizer.isenabled()) return;

        long key = TextRenderOptimizer.glyphkey(codePoint, style);
        Object cached = TextRenderOptimizer.getcached(key);
        if (cached != null) {
            cir.setReturnValue((BakedGlyph) cached);
        }
    }

    @Inject(method = "getGlyph", at = @At("RETURN"), require = 0)
    private void helium$storeglyphlookup(int codePoint, Style style, CallbackInfoReturnable<BakedGlyph> cir) {
        if (!TextRenderOptimizer.isenabled()) return;

        BakedGlyph result = cir.getReturnValue();
        if (result != null) {
            long key = TextRenderOptimizer.glyphkey(codePoint, style);
            TextRenderOptimizer.cache(key, result);
        }
    }
}
