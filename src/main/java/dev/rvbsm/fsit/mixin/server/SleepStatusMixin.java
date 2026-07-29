package dev.rvbsm.fsit.mixin.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// /lay shouldn't skip the night — drop laying players from the sleep tally
@Mixin(SleepStatus.class)
public abstract class SleepStatusMixin {

    @WrapOperation(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSleeping()Z"))
    private boolean fsit$excludeLayingPlayers(ServerPlayer instance, Operation<Boolean> original) {
        return FSitPoseHolder.get(instance) != FSitPose.LAYING && original.call(instance);
    }
}
