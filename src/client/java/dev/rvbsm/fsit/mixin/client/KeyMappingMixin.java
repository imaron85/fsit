package dev.rvbsm.fsit.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.rvbsm.fsit.client.option.HybridKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {

    @Definition(id = "keyMapping", local = @Local(type = KeyMapping.class, name = "keyMapping"))
    @Definition(id = "ToggleKeyMapping", type = ToggleKeyMapping.class)
    @Expression("keyMapping instanceof ToggleKeyMapping")
    @ModifyExpressionValue(method = "resetToggleKeys", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean instanceofHybrid(boolean original, @Local(name = "keyMapping") KeyMapping keyMapping) {
        return original || keyMapping instanceof HybridKeyMapping;
    }
}
