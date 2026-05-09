package dev.rvbsm.fsit.mixin.client;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Unique
    private static final Logger fsit$LOGGER = LogUtils.getLogger();


    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void fsit$cleanupRideOnDimensionChange(ClientboundRespawnPacket packet, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var packetDim = packet.commonPlayerSpawnInfo().dimension();
        var oldDim = player.level().dimension();
        if (packetDim == oldDim) return;
        var vehicle = player.getVehicle();
        if (vehicle != null && vehicle.entityTags().contains(FSitMod.MOD_ID)) {
            fsit$LOGGER.debug("dismounting fsit vehicle before cross-dimension respawn ({} -> {})", oldDim, packetDim);
            player.stopRiding();
        }
    }
}
