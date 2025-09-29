package dev.rvbsm.fsit.mixin.client.access;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyBindingAccess {

    @Accessor
    InputUtil.Key getBoundKey();
}
