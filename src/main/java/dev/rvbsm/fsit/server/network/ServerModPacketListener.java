package dev.rvbsm.fsit.server.network;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.network.protocol.game.ServerboundPlayerPosePayload;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.function.Supplier;

public final class ServerModPacketListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ServerModPacketListener() {
    }

    public static boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return ServerPlayNetworking.canSend(player, type);
    }

    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static boolean send(
        ServerPlayer player,
        CustomPacketPayload.Type<?> type,
        Supplier<CustomPacketPayload> supplier
    ) {
        if (!canSend(player, type)) {
            return false;
        }

        var payload = supplier.get();
        send(player, payload);
        return true;
    }

    public static void handleChangePose(ServerboundPlayerPosePayload payload, ServerPlayNetworking.Context ctx) {
        var player = ctx.player();
        var position = payload.position();

        if (player.isWithinBlockInteractionRange(BlockPos.containing(position), 1.0)) {
            FSitPoseHolder.set(player, payload.pose(), new FSitPose.Context.Vec3(position));
        }
    }
}
