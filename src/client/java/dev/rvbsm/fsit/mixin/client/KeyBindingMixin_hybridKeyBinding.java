package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.client.option.HybridKeyBinding;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin_hybridKeyBinding {

    @ModifyVariable(method = "untoggleStickyKeys", at = @At("STORE"))
    private static KeyBinding untoggleHybridKeys(KeyBinding binding) {
        if (binding instanceof HybridKeyBinding hybridBinding) {
            hybridBinding.setPressed(false);
        }

        return binding;
    }
}
