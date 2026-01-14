package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.client.option.HybridKeyMapping;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin_hybridKeyMapping {

    @ModifyVariable(method = "resetToggleKeys", at = @At("STORE"), name = "keyMapping")
    private static KeyMapping untoggleHybridKeys(final KeyMapping keyMapping) {
        if (keyMapping instanceof HybridKeyMapping hybridKeyMapping) {
            hybridKeyMapping.reset();
        }

        return keyMapping;
    }
}
