package dev.rvbsm.fsit.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.text2speech.Narrator;
import net.minecraft.client.GameNarrator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameNarrator.class)
public class GameNarratorMixin_DONOTFORGETTOREMOVEPLEASE {

    @Definition(
        id = "getNarrator",
        method = "Lcom/mojang/text2speech/Narrator;getNarrator()Lcom/mojang/text2speech/Narrator;"
    )
    @Expression("getNarrator()")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static Narrator shutthefuckup(Operation<Narrator> original) {
        return Narrator.EMPTY;
    }
}
