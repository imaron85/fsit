package dev.rvbsm.fsit.client.network;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.api.PoseHolder;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.ConfigLoader;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPoseUpdatePayload;
import dev.rvbsm.fsit.network.protocol.game.ServerboundConfigJsonUpdatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ClientModNetworkListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ClientModNetworkListener() {
    }

    public static <P extends CustomPacketPayload> void trySend(final CustomPacketPayload.Type<P> type, final Supplier<P> payloadFn, final Runnable fallback) {
        if (ClientPlayNetworking.canSend(type)) {
            fallback.run();
            return;
        }

        final P payload = payloadFn.get();
        ClientPlayNetworking.send(payload);
    }

    public static <V, P extends CustomPacketPayload> void trySendFuture(final CustomPacketPayload.Type<P> type, final Supplier<CompletableFuture<V>> futureFn, final Function<? super V, P> payloadFn, final Runnable fallback) {
        if (!ClientPlayNetworking.canSend(type)) {
            fallback.run();
            return;
        }

        final CompletableFuture<V> payloadFuture = futureFn.get();
        payloadFuture.thenApply(payloadFn).whenComplete((payload, e) -> {
            if (e != null) {
                LOGGER.warn("Could not create a payload {}", type, e);
                return;
            }

            ClientPlayNetworking.send(payload);
        });
    }

    public static void sendConfig(final ConfigData config) {
        ClientModNetworkListener.trySendFuture(
            ServerboundConfigJsonUpdatePayload.TYPE,
            () -> ConfigLoader.saveJson(config), ServerboundConfigJsonUpdatePayload::new,
            () -> {
            }
        );
    }

    public static void onPoseUpdate(final ClientboundPoseUpdatePayload payload, final ClientPlayNetworking.Context ctx) {
        final LocalPlayer player = ctx.player();
        PoseHolder.set(player, payload.pose(), payload.pos());
    }
}
