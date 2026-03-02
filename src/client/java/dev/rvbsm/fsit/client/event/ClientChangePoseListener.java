package dev.rvbsm.fsit.client.event;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.event.ChangePoseCallback;
import dev.rvbsm.fsit.client.network.ClientModPacketListener;
import dev.rvbsm.fsit.network.protocol.game.ServerboundPlayerPosePayload;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class ClientChangePoseListener implements ChangePoseCallback {

    public static final ClientChangePoseListener INSTANCE = new ClientChangePoseListener();
    private static final Logger LOGGER = LogUtils.getLogger();

    private ClientChangePoseListener() {}

    @Override
    public @Nullable FSitPose changePose(Player player, FSitPose pose, FSitPose.@Nullable Context ctx) {
        if (player.level().isClientSide()) {
            LOGGER.debug("cs pose change {}[{}] {}", player, pose, ctx);

            var config = FSitMod.getInstance().getConfig();

            var position = ctx != null ? ctx.position() : player.position();
            boolean isCloseEnough = position.closerThan(player.getEyePosition(), player.blockInteractionRange());
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
                    boolean isInWall = BlockPos.betweenClosedStream(eyeBb).anyMatch(wallPos -> {
                        var wallState = player.level().getBlockState(wallPos);
                        return !wallState.isAir()
                                && wallState.isSuffocating(player.level(), wallPos)
                                && Shapes.joinIsNotEmpty(
                                        wallState
                                                .getCollisionShape(player.level(), wallPos)
                                                .move(wallPos),
                                        Shapes.create(eyeBb),
                                        BooleanOp.AND);
                    });

                    if (isInWall) {
                        return FSitPose.STANDING;
                    }
                }

                case CRAWLING -> {}
                case LAYING -> {}
                case SPINNING -> {}
                default -> {}
            }

            // todo: move to LocalPlayer#doTick?
            var finalPosition = position;
            ClientModPacketListener.send(
                    ServerboundPlayerPosePayload.TYPE, () -> new ServerboundPlayerPosePayload(pose, finalPosition));
        }

        return null;
    }
}
