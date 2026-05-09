package dev.rvbsm.fsit.client.event;

import dev.rvbsm.fsit.api.PlayerSettings;
import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.client.network.ClientModPacketListener;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public final class ClientPlayConnectionListener implements ClientPlayConnectionEvents.Join, ClientPlayConnectionEvents.Disconnect {

    public static final ClientPlayConnectionListener INSTANCE = new ClientPlayConnectionListener();

    private ClientPlayConnectionListener() {}

    @Override
    public void onPlayReady(ClientPacketListener listener, PacketSender sender, Minecraft client) {
        var config = FSitModClient.getInstance().getConfig();
        boolean success = ClientModPacketListener.sendConfig(config);
        if (success) {
            FSitModClient.getInstance().setEnabled(true);
        }
    }

    @Override
    public void onPlayDisconnect(ClientPacketListener listener, Minecraft client) {
        FSitModClient.getInstance().setEnabled(false);
        FSitModClient.getInstance().setPlayerSettings(PlayerSettings.DEFAULT);
    }
}
