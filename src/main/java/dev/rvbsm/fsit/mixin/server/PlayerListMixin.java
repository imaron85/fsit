package dev.rvbsm.fsit.mixin.server;

import dev.rvbsm.fsit.world.entity.decoration.Seat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "remove", at = @At("HEAD"))
    private void fsit$dismountSeatBeforeSave(ServerPlayer player, CallbackInfo ci) {
        if (!(player.getVehicle() instanceof Seat seat)) return;
        var pos = seat.getDismountLocationForPassenger(player);
        player.stopRiding();
        player.setPos(pos.x, pos.y, pos.z);
        seat.discard();
    }
}
