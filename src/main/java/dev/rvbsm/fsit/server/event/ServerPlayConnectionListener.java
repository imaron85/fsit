package dev.rvbsm.fsit.server.event;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.network.protocol.game.ClientboundServerConfigPayload;
import dev.rvbsm.fsit.server.network.ServerModPacketListener;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public final class ServerPlayConnectionListener implements ServerPlayConnectionEvents.Join {

    public static final ServerPlayConnectionListener INSTANCE = new ServerPlayConnectionListener();

    private ServerPlayConnectionListener() {}

    @Override
    public void onPlayReady(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        var player = listener.getPlayer();
        ServerModPacketListener.send(
                player,
                ClientboundServerConfigPayload.TYPE,
                () -> new ClientboundServerConfigPayload(
                        FSitMod.getInstance().getConfig().useServer()));
        ServerModPacketListener.sendPlayerSettings(player);
    }
}
