package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.world.entity.decoration.Ride;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.List;

public final class PlayerDisconnectListener implements ServerPlayConnectionEvents.Disconnect {

    public static final PlayerDisconnectListener INSTANCE = new PlayerDisconnectListener();

    private PlayerDisconnectListener() {}

    @Override
    public void onPlayDisconnect(ServerGamePacketListenerImpl listener, MinecraftServer server) {
        ServerPlayer player = listener.player;

        // disconnecting rider — drop the bridge entity off the target
        if (player.getVehicle() instanceof Ride ride) {
            ride.discard();
        }

        // disconnecting target — clear bridges sitting on them
        for (var passenger : List.copyOf(player.getPassengers())) {
            if (passenger instanceof Ride ride) {
                ride.discard();
            }
        }

        // revert fake bed so observers don't carry a stale bed in their chunk copy
        if (player.isSleeping() && player.level() instanceof ServerLevel level) {
            player.getSleepingPos().ifPresent(bedPos ->
                    FSitPoseListener.broadcastBedUpdate(level, bedPos, level.getBlockState(bedPos)));
        }
    }
}
