package dev.rvbsm.fsit.client.event;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.event.ChangePoseCallback;
import dev.rvbsm.fsit.client.network.ClientModPacketListener;
import dev.rvbsm.fsit.network.protocol.game.ServerboundChangePosePayload;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

public final class ClientChangePoseListener implements ChangePoseCallback {

    public static final ClientChangePoseListener INSTANCE = new ClientChangePoseListener();
    private static final Logger LOGGER = LogUtils.getLogger();

    private ClientChangePoseListener() {}

    @Override
    public @Nullable ModPose changePose(Player player, ModPose pose, Vec3 pos) {
        if (player.level().isClientSide()) {
            LOGGER.debug("cs pose change {}[{}] {}", player, pose, pos);

            // todo: cleanup last state
            player.stopRiding();

            var config = FSitMod.getInstance().getConfig();
            boolean isCloseEnough = pos.closerThan(player.getEyePosition(), config.blockInteractionDistance());
            if (!isCloseEnough) {
                return ModPose.STANDING;
            }

            if (pose == ModPose.SITTING) {
                var dimensions = player.getDimensions(Pose.STANDING);
                float checkWidth = dimensions.width() * 0.8f;
                float eyeHeight = dimensions.eyeHeight() * 0.8f;
                var eyeBb = AABB.ofSize(pos.add(0.0, eyeHeight, 0.0), checkWidth, 1.0e-6, checkWidth);
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
                    return ModPose.STANDING;
                }
            }

            ClientModPacketListener.send(
                ServerboundChangePosePayload.TYPE,
                () -> new ServerboundChangePosePayload(pose, Optional.of(pos)));
        }

        return null;
    }
}
