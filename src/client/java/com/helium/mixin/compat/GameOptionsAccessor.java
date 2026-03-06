package com.helium.mixin.compat;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameOptions.class)
public interface GameOptionsAccessor {
    
    @Accessor("allKeys")
    KeyBinding[] helium$getallkeys();
    
    @Mutable
    @Accessor("allKeys")
    void helium$setallkeys(KeyBinding[] keys);
}
