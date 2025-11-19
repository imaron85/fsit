package dev.rvbsm.fsit.client.network;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.ConfigLoader;
import dev.rvbsm.fsit.network.payload.c2s.ConfigUpdateJsonC2SPayload;
import dev.rvbsm.fsit.network.payload.c2s.RidingResponseC2SPayload;
import dev.rvbsm.fsit.network.payload.s2c.PoseUpdateS2CPayload;
import dev.rvbsm.fsit.network.payload.s2c.RidingRequestS2CPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ClientModNetworkHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ClientModNetworkHandler() {
    }

    public static <P extends CustomPacketPayload> void trySend(CustomPacketPayload.Type<P> type, Supplier<P> payloadFn, Runnable fallback) {
        if (ClientPlayNetworking.canSend(type)) {
            fallback.run();
            return;
        }

        final P payload = payloadFn.get();
        ClientPlayNetworking.send(payload);
    }

    public static <V, P extends CustomPacketPayload> void trySendFuture(CustomPacketPayload.Type<P> type, Supplier<CompletableFuture<V>> futureFn, Function<? super V, P> payloadFn, Runnable fallback) {
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
        ClientModNetworkHandler.trySendFuture(
            ConfigUpdateJsonC2SPayload.ID,
            () -> ConfigLoader.saveJson(config), ConfigUpdateJsonC2SPayload::new,
            () -> {}
        );
    }

    public static void onPoseUpdate(PoseUpdateS2CPayload payload, ClientPlayNetworking.Context ctx) {
        final LocalPlayer player = ctx.player();
        ((Poseable) player).fsit$setPose(payload.pose(), payload.pos());
    }

    public static void onRidingRequest(RidingRequestS2CPayload payload, ClientPlayNetworking.Context ctx) {
        final UUID targetUuid = payload.uuid();
        final boolean isBlocked = FSitModClient.getInstance().getRestrictionsManager().isRestricted(targetUuid);

        ctx.responseSender().sendPacket(new RidingResponseC2SPayload(targetUuid, !isBlocked));
    }
}
