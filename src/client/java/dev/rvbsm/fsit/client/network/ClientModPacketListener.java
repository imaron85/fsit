package dev.rvbsm.fsit.client.network;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.api.PoseHolder;
import dev.rvbsm.fsit.network.protocol.game.ClientboundChangePosePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.slf4j.Logger;

import java.util.function.Supplier;

public final class ClientModPacketListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ClientModPacketListener() {
    }

    public static boolean canSend(CustomPacketPayload.Type<?> type) {
        return ClientPlayNetworking.canSend(type);
    }

    public static void send(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static boolean send(CustomPacketPayload.Type<?> type, Supplier<CustomPacketPayload> supplier) {
        if (!canSend(type)) {
            return false;
        }

        var payload = supplier.get();
        send(payload);
        return true;
    }

    public static void handleChangePose(ClientboundChangePosePayload payload, ClientPlayNetworking.Context ctx) {
        PoseHolder.set(ctx.player(), payload.pose(), payload.pos());
    }
}
