package dev.rvbsm.fsit.server.event;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.event.ChangePoseCallback;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPlayerPosePayload;
import dev.rvbsm.fsit.server.network.ServerModPacketListener;
import dev.rvbsm.fsit.world.entity.FSitPose;
import dev.rvbsm.fsit.world.entity.Seat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class ServerChangePoseListener implements ChangePoseCallback {

    public static final ServerChangePoseListener INSTANCE = new ServerChangePoseListener();
    private static final Logger LOGGER = LogUtils.getLogger();

    private ServerChangePoseListener() {}

    @Override
    public @Nullable FSitPose changePose(Player player, FSitPose pose, FSitPose.@Nullable Context ctx) {
        if (!player.level().isClientSide()) {
            LOGGER.debug("ss pose change {}[{}] {}", player, pose, ctx);

            var config = FSitMod.getInstance().getConfig();

            var position = ctx != null ? ctx.position() : player.position();
            boolean isCloseEnough = position.closerThan(player.getEyePosition(), 5.0);
            if (!isCloseEnough) {
                return FSitPose.STANDING;
            }

            switch (pose) {
                case STANDING -> {
                    player.stopRiding();
                }

                case SITTING -> {
                    if (!FSitMod.getInstance().handledByClient(player.getUUID()) && config.seatsCentered()) {
                        position = new Vec3(Math.floor(position.x) + 0.5, position.y, Math.floor(position.z) + 0.5);
                    }

                    var dimensions = player.getDimensions(Pose.STANDING);
                    float checkWidth = dimensions.width() * 0.8f;
                    float eyeHeight = dimensions.eyeHeight() * 0.8f;
                    var eyeBb = AABB.ofSize(position.add(0.0, eyeHeight, 0.0), checkWidth, 1.0e-6, checkWidth);
                    boolean wouldSuffocate = !player.level().noBlockCollision(player, eyeBb);

                    if (wouldSuffocate) {
                        return FSitPose.STANDING;
                    }

                    var seat = new Seat(player.level());
                    seat.setPos(position);

                    player.startRiding(seat, true, true);
                    player.level().addFreshEntity(seat);
                }

                case CRAWLING -> {
                    // todo: server-only
                }

                case LAYING -> {}

                case SPINNING -> {}

                default -> {}
            }

            // todo: move to ServerPlayer#tick?
            ServerModPacketListener.send(
                    (ServerPlayer) player,
                    ClientboundPlayerPosePayload.TYPE,
                    () -> new ClientboundPlayerPosePayload(pose));
        }

        return null;
    }
}
