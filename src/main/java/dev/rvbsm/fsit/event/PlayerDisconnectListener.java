package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.world.entity.decoration.Ride;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class PlayerDisconnectListener implements ServerPlayConnectionEvents.Disconnect{
    @Override
    public void onPlayDisconnect(ServerGamePacketListenerImpl listener, MinecraftServer server) {
        ServerPlayer player = listener.player;
        if (player.getVehicle() instanceof Ride rideEntity) {
            rideEntity.discard();
        }
    }
}
