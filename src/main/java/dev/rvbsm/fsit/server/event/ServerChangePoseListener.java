package dev.rvbsm.fsit.server.event;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.api.event.ChangePoseCallback;
import dev.rvbsm.fsit.network.protocol.game.ClientboundChangePosePayload;
import dev.rvbsm.fsit.server.network.ServerModPacketListener;
import dev.rvbsm.fsit.world.entity.ModPose;
import dev.rvbsm.fsit.world.entity.vehicle.Seat;
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
    public @Nullable ModPose changePose(Player player, ModPose pose, Vec3 pos) {
        if (!player.level().isClientSide()) {
            LOGGER.debug("ss pose change {}[{}] {}", player, pose, pos);

            // todo: cleanup last state
            player.stopRiding();

            boolean isCloseEnough = pos.closerThan(player.getEyePosition(), 5.0);
            if (!isCloseEnough) {
                return ModPose.STANDING;
            }

            switch (pose) {
                case SITTING -> {
                    var dimensions = player.getDimensions(Pose.STANDING);
                    float checkWidth = dimensions.width() * 0.8f;
                    float eyeHeight = dimensions.eyeHeight() * 0.8f;
                    var eyeBb = AABB.ofSize(pos.add(0.0, eyeHeight, 0.0), checkWidth, 1.0e-6, checkWidth);
                    boolean wouldSuffocate = !player.level().noBlockCollision(player, eyeBb);

                    if (wouldSuffocate) {
                        return ModPose.STANDING;
                    }

                    var seat = Seat.create(player.level());
                    seat.setPos(pos);

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

            ServerModPacketListener.send(
                    (ServerPlayer) player,
                    ClientboundChangePosePayload.TYPE,
                    () -> new ClientboundChangePosePayload(pose, pos));
        }

        return null;
    }
}
