package dev.rvbsm.fsit.mixin.server;

import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// /lay shouldn't skip the night — drop laying players from the sleep tally
@Mixin(SleepStatus.class)
public abstract class SleepStatusMixin {

    @Redirect(
        method = "update",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSleeping()Z")
    )
    private boolean fsit$excludeLayingPlayers(ServerPlayer player) {
        if (FSitPoseHolder.get(player) == FSitPose.LAYING) {
            return false;
        }
        return player.isSleeping();
    }
}
