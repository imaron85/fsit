package dev.rvbsm.fsit.server.network;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.config.ConfigLoader;
import dev.rvbsm.fsit.network.protocol.game.ServerboundConfigJsonUpdatePayload;
import dev.rvbsm.fsit.network.protocol.game.ServerboundPoseRequestPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ServerModNetworkListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ServerModNetworkListener() {
    }

    public static <P extends CustomPacketPayload> void trySend(final ServerPlayer player, final CustomPacketPayload.Type<P> type, final Supplier<P> payloadFn, final Runnable fallback) {
        if (!ServerPlayNetworking.canSend(player, type)) {
            fallback.run();
            return;
        }

        final P payload = payloadFn.get();
        ServerPlayNetworking.send(player, payload);
    }

    public static <V, P extends CustomPacketPayload> void trySendFuture(final ServerPlayer player, final CustomPacketPayload.Type<P> type, final Supplier<CompletableFuture<V>> futureFn, final Function<? super V, P> payloadFn, final Runnable fallback) {
        if (!ServerPlayNetworking.canSend(player, type)) {
            fallback.run();
            return;
        }

        final CompletableFuture<V> payloadFuture = futureFn.get();
        payloadFuture.thenApply(payloadFn).whenComplete((payload, e) -> {
            if (e != null) {
                LOGGER.warn("Could not create a payload {}", type, e);
                return;
            }

            ServerPlayNetworking.send(player, payload);
        });
    }

    public static void handleConfigJsonUpdate(final ServerboundConfigJsonUpdatePayload payload, final ServerPlayNetworking.Context ctx) {
        ConfigLoader.loadJson(payload.configJson()).whenComplete((config, e) -> {
            final ServerPlayer player = ctx.player();

            if (e != null) {
                LOGGER.warn("Could not update player config for {}", player.getName(), e);
                return;
            }

            ((Configurable) player).fsit$setConfig(config);
        });
    }

    public static void handleRequestPose(final ServerboundPoseRequestPayload payload, final ServerPlayNetworking.Context ctx) {
        final ServerPlayer player = ctx.player();
        final Vec3 pos = payload.pos().orElseGet(player::position);

        if (player.isWithinBlockInteractionRange(BlockPos.containing(pos), 1.0)) {
            ((Poseable) player).fsit$setPose(payload.pose(), pos);
        }
    }
}
