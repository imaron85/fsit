package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.client.option.HybridKeyBinding;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin_hybridKeyBinding {

    @ModifyVariable(method = "resetToggleKeys", at = @At("STORE"))
    private static KeyMapping untoggleHybridKeys(KeyMapping binding) {
        if (binding instanceof HybridKeyBinding hybridBinding) {
            hybridBinding.reset();
        }

        return binding;
    }
}
