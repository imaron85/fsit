package dev.rvbsm.fsit.server.event;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitAttachable;
import dev.rvbsm.fsit.api.event.FSitPoseCallback;
import dev.rvbsm.fsit.server.level.ServerCrawl;
import dev.rvbsm.fsit.world.entity.FSitPose;
import dev.rvbsm.fsit.world.entity.decoration.Seat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class FSitPoseListener implements FSitPoseCallback {

    public static final FSitPoseListener INSTANCE = new FSitPoseListener();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BlockState BED_STATE =
            Blocks.BLACK_BED.defaultBlockState().setValue(BlockStateProperties.BED_PART, BedPart.HEAD);

    private FSitPoseListener() {}

    @Override
    public @Nullable Vec3 createPose(Player player, FSitPose pose, FSitPose.@Nullable Context ctx) {
        LOGGER.debug("pose change {} {} {}", player, pose, ctx);

        var config = FSitMod.getInstance().getConfig();

        var position =
                switch (ctx) {
                    case FSitPose.Context.Positioned(Vec3 pos) -> pos;
                    case FSitPose.Context.Attached(Player attachable, Vec3 offset) ->
                        attachable.position().add(offset);
                    case null -> player.position();
                };

        boolean isCloseEnough = position.closerThan(player.getEyePosition(), 5.0);
        if (!isCloseEnough) {
            return null;
        }

        switch (pose) {
            case STANDING -> {
                player.stopRiding();
                if (player instanceof ServerPlayer serverPlayer) {
                    FSitAttachable.detach(serverPlayer);

                    var pos = BlockPos.containing(position);
                    var state = player.level().getBlockState(pos);
                    serverPlayer.connection.send(new ClientboundBlockUpdatePacket(pos, state));
                }
            }

            case SITTING -> {
                if (!(player instanceof ServerPlayer serverPlayer)
                        || !FSitMod.getInstance().isClient(serverPlayer)) {
                    if (config.seatsCentered()) {
                        position = new Vec3(Math.floor(position.x) + 0.5, position.y, Math.floor(position.z) + 0.5);
                    }
                }

                var dimensions = player.getDimensions(Pose.STANDING);
                float checkWidth = dimensions.width() * 0.8f;
                float eyeHeight = dimensions.eyeHeight() * 0.8f;
                var eyeBb = AABB.ofSize(position.add(0.0, eyeHeight, 0.0), checkWidth, 1.0e-6, checkWidth);
                boolean wouldSuffocate = !player.level().noBlockCollision(player, eyeBb);

                if (wouldSuffocate) {
                    return null;
                }

                if (!player.level().isClientSide()) {
                    var seat = new Seat(player.level(), position.x, position.y, position.z);
                    player.startRiding(seat, true, true);
                    player.level().addFreshEntity(seat);
                }
            }

            case CRAWLING -> {
                if (player instanceof ServerPlayer serverPlayer
                        && !FSitMod.getInstance().isClient(serverPlayer)) {
                    var crawl = new ServerCrawl(serverPlayer);
                    FSitAttachable.attach(serverPlayer, crawl);
                }
            }

            case LAYING -> {
                // TODO: rotation seems random but also stable
                // FIXME: player floats in the air

                if (player instanceof ServerPlayer serverPlayer) {
                    var bedPosition = BlockPos.containing(position);
                    serverPlayer.connection.send(new ClientboundBlockUpdatePacket(bedPosition, BED_STATE));
                    serverPlayer.startSleeping(bedPosition);
                    // TODO: award stat and trigger `slept_in_bed` criteria?
                    // FIXME: skips when all players are laying
                    serverPlayer.level().updateSleepingPlayerList();
                }
            }

            case SPINNING -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    // TODO: requires restricting movement / spawning a shadow player
                }
            }

            default -> {}
        }

        return position;
    }
}
