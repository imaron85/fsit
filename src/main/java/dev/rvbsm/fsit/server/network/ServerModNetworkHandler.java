package dev.rvbsm.fsit.server.network;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.config.ConfigLoader;
import dev.rvbsm.fsit.entity.RideEntity;
import dev.rvbsm.fsit.network.payload.c2s.ConfigUpdateJsonC2SPayload;
import dev.rvbsm.fsit.network.payload.c2s.PoseRequestC2SPayload;
import dev.rvbsm.fsit.network.payload.c2s.RidingResponseC2SPayload;
import dev.rvbsm.fsit.network.payload.s2c.RidingRequestS2CPayload;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ServerModNetworkHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final long REQUEST_TIMEOUT = 5_000; // ms
    private static final Map<Long, RidingRequest> RIDING_REQUESTS = new Long2ReferenceOpenHashMap<>();

    private ServerModNetworkHandler() {
    }

    public static <P extends CustomPayload> void trySend(ServerPlayerEntity player, CustomPayload.Id<P> type, Supplier<P> payloadFn, Runnable fallback) {
        if (!ServerPlayNetworking.canSend(player, type)) {
            fallback.run();
            return;
        }

        final P payload = payloadFn.get();
        ServerPlayNetworking.send(player, payload);
    }

    public static <V, P extends CustomPayload> void trySendFuture(ServerPlayerEntity player, CustomPayload.Id<P> type, Supplier<CompletableFuture<V>> futureFn, Function<? super V, P> payloadFn, Runnable fallback) {
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

    public static void onConfigUpdateJson(ConfigUpdateJsonC2SPayload payload, ServerPlayNetworking.Context ctx) {
        ConfigLoader.loadJson(payload.configJson()).whenComplete((config, e) -> {
            final ServerPlayerEntity player = ctx.player();

            if (e != null) {
                LOGGER.warn("Could not update player config for {}", player.getName(), e);
                return;
            }

            ((Configurable) player).fsit$setConfig(config);
        });
    }

    public static void onPoseRequest(PoseRequestC2SPayload payload, ServerPlayNetworking.Context ctx) {
        final ServerPlayerEntity player = ctx.player();
        final Vec3d pos = payload.pos().orElseGet(player::getEntityPos);

        if (player.canInteractWithBlockAt(BlockPos.ofFloored(pos), 1.0)) {
            ((Poseable) player).fsit$setPose(payload.pose(), pos);
        }
    }

    private static long getRidingRequestId(UUID a, UUID b) {
        return a.getMostSignificantBits() ^ a.getLeastSignificantBits()
            ^ b.getMostSignificantBits() ^ b.getLeastSignificantBits();
    }

    // todo test
    public static void requestRiding(ServerPlayerEntity player, ServerPlayerEntity other) {
        if (player == other || player.getUuid().equals(other.getUuid()) || player.hasPassengerDeep(other)) {
            player.sendMessage(Text.translatable("commands.ride.mount.failure.loop"), true);
            return;
        }

        final UUID riderUuid = player.getUuid();
        final UUID otherUuid = other.getUuid();

        final long requestId = getRidingRequestId(riderUuid, otherUuid);
        final var request = new RidingRequest(riderUuid, otherUuid);
        final RidingRequest danglingRequest = RIDING_REQUESTS.put(requestId, request);
        if (danglingRequest != null) {
            danglingRequest.refuseBoth();
        }

        trySend(player, RidingRequestS2CPayload.ID, () -> new RidingRequestS2CPayload(otherUuid),
            () -> request.accept(riderUuid));
        trySend(other, RidingRequestS2CPayload.ID, () -> new RidingRequestS2CPayload(riderUuid),
            () -> request.accept(otherUuid));

        request.thenCombineAsync().completeOnTimeout(false, REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
            .whenCompleteAsync((isAccepted, e) -> {
                RIDING_REQUESTS.remove(requestId);

                if (e != null) {
                    LOGGER.warn("Something went wrong", e);
                }

                if (isAccepted) {
                    player.getEntityWorld().spawnEntity(new RideEntity(player, other));
                }
            }, player.getEntityWorld().getServer()); // todo player.world.server is kinda meh
    }

    public static void onRidingResponse(RidingResponseC2SPayload payload, ServerPlayNetworking.Context ctx) {
        final ServerPlayerEntity player = ctx.player();
        if (player.getUuid().equals(payload.uuid())) {
            return;
        }

        final long requestId = getRidingRequestId(player.getUuid(), payload.uuid());
        final RidingRequest request = RIDING_REQUESTS.get(requestId);
        if (request == null) {
            return;
        }

        request.complete(player.getUuid(), payload.isAccepted());
    }

    private static final class RidingRequest {

        private final UUID leftId;
        private final UUID rightId;
        private final CompletableFuture<Boolean> left;
        private final CompletableFuture<Boolean> right;

        private RidingRequest(UUID leftId, UUID rightId) {
            this.leftId = leftId;
            this.rightId = rightId;
            this.left = new CompletableFuture<>();
            this.right = new CompletableFuture<>();
        }

        public CompletableFuture<Boolean> thenCombineAsync() {
            return this.left.thenCombineAsync(this.right, Boolean::logicalAnd);
        }

        public void acceptBoth() {
            this.complete(this.leftId, true);
            this.complete(this.rightId, true);
        }

        public void accept(UUID id) {
            this.complete(id, true);
        }

        public void refuseBoth() {
            this.complete(this.leftId, false);
            this.complete(this.rightId, false);
        }

        public void refuse(UUID id) {
            this.complete(id, false);
        }

        public void complete(UUID id, boolean value) {
            if (id == this.leftId) {
                this.left.complete(value);
            } else if (id == this.rightId) {
                this.right.complete(value);
            }
        }
    }
}
