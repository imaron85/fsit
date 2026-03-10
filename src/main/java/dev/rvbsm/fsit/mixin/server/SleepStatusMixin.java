package dev.rvbsm.fsit.mixin.server;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SleepStatus.class)
public abstract class SleepStatusMixin {

    // HACK: extremely silly fix for skipping the night during the day
    @Definition(id = "player", local = @Local(type = ServerPlayer.class, name = "player"))
    @Definition(id = "isSleeping", method = "Lnet/minecraft/server/level/ServerPlayer;isSleeping()Z")
    @Expression("player.isSleeping()")
    @ModifyExpressionValue(method = "update", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean canSleep(boolean isSleeping, @Local(name = "player") ServerPlayer player) {
        if (!isSleeping) {
            return false;
        }

        return player.getSleepingPos().map(bedPos ->
            player.level()
                .environmentAttributes()
                .getValue(EnvironmentAttributes.BED_RULE, bedPos)
                .canSleep(player.level())
        ).orElse(false);
    }
}
