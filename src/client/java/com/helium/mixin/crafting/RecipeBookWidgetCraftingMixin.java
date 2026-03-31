package com.helium.mixin.crafting;

import com.helium.HeliumClient;
import com.helium.config.HeliumConfig;
import com.helium.crafting.OneClickCraftingManager;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.recipe.NetworkRecipeId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetCraftingMixin {

    @Inject(at = @At("TAIL"), method = "select(Lnet/minecraft/client/gui/screen/recipebook/RecipeResultCollection;Lnet/minecraft/recipe/NetworkRecipeId;Z)Z", require = 0)
    private void helium$clickRecipeTail(RecipeResultCollection results, NetworkRecipeId recipeId, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        HeliumConfig config = HeliumClient.getConfig();
        if (config == null || !config.oneClickCrafting) return;
        if (!OneClickCraftingManager.isinitialized()) return;
        if (!OneClickCraftingManager.haslastbutton()) {
            OneClickCraftingManager.setlastbutton(1);
        }
        OneClickCraftingManager.recipeclicked(recipeId);
    }
}
