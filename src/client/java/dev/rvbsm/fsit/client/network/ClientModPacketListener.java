package dev.rvbsm.fsit.client.network;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.client.option.HybridKeyMapping;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.network.protocol.game.ClientboundServerConfigPayload;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPlayerPosePayload;
import dev.rvbsm.fsit.network.protocol.game.ServerboundPlayerConfigPayload;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
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

    public static void handleChangePose(ClientboundPlayerPosePayload payload, ClientPlayNetworking.Context ctx) {
        var player = ctx.player();
        var pose = payload.pose();
        var lastPose = FSitPoseHolder.get(player);
        FSitPoseHolder.set(player, pose);

        if (pose == FSitPose.LAYING) {
            // Match the server's target `yBodyRot` so the sleeping renderer's fallback angle
            // (no bedOrientation) picks the direction the player was facing. The `tickHeadTurn`
            // cancel in `LivingEntityMixin` keeps it frozen.
            player.setYBodyRot(Mth.wrapDegrees(90.0f - player.getYRot()));
        }

        if (pose != lastPose) {
            HybridKeyMapping.resetHybridKeys();
        }
    }

    public static boolean sendConfig(ConfigData config) {
        return ClientModPacketListener.send(ServerboundPlayerConfigPayload.TYPE, () -> new ServerboundPlayerConfigPayload(config.makeClientConfig()));
    }

    public static void handleServerConfig(ClientboundServerConfigPayload payload, ClientPlayNetworking.Context ctx) {
        FSitModClient.getInstance().getOptions().useServerRemote = payload.useServer();
    }
}
