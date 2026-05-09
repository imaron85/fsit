package dev.rvbsm.fsit.event;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitAttachable;
import dev.rvbsm.fsit.api.event.FSitPoseCallback;
import dev.rvbsm.fsit.server.level.ServerCrawl;
import dev.rvbsm.fsit.world.entity.FSitPose;
import dev.rvbsm.fsit.world.entity.decoration.Seat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class FSitPoseListener implements FSitPoseCallback {

    public static final FSitPoseListener INSTANCE = new FSitPoseListener();
    private static final Logger LOGGER = LogUtils.getLogger();

    private FSitPoseListener() {}

    // FACING opposite of yaw-cardinal — renderer shifts model by -step*headOffset, so this
    // makes the body extend in the player's look direction with the head near them.
    public static BlockState fakeBedStateFor(float yRot) {
        var facing = Direction.fromYRot(yRot).getOpposite();
        return Blocks.BLACK_BED.defaultBlockState()
                .setValue(BedBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.HEAD);
    }

    // stuff at getMinY is inside bedrock — invisible during normal play
    public static BlockPos fakeBedPosFor(ServerLevel level, Vec3 playerPos) {
        return new BlockPos(
                (int) Math.floor(playerPos.x),
                level.getMinY(),
                (int) Math.floor(playerPos.z));
    }

    /// Sends a block update for the fake bed (or its revert to the real block) to every
    /// player in the level. Required for other clients to render the laying player with the
    /// correct body orientation — without the fake bed, the renderer falls back to the
    ///
    /// The position is at (bedrock layer), inaccessible during normal
    /// play; the packet is purely a client-side visual.
    public static void broadcastBedUpdate(ServerLevel level, BlockPos pos, BlockState state) {
        var packet = new ClientboundBlockUpdatePacket(pos, state);
        for (var viewer : level.players()) {
            viewer.connection.send(packet);
        }
    }

    // do NOT route through stopSleepInBed — its bed-block check would teleport to a stand-up
    // position if anyone put a real bed at our fake sleepingPos
    public static void completeLayWakeUp(ServerPlayer player) {
        var bedPos = player.getSleepingPos().orElse(null);
        if (bedPos == null) return;

        player.clearSleepingPos();
        player.setPose(Pose.STANDING);
        if (player.level() instanceof ServerLevel level) {
            level.updateSleepingPlayerList();
            broadcastBedUpdate(level, bedPos, level.getBlockState(bedPos));
        }
        if (player.connection != null) {
            player.connection.teleport(player.getX(), player.getY(), player.getZ(),
                    player.getYRot(), player.getXRot());
        }
    }

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
                if (player instanceof ServerPlayer serverPlayer && serverPlayer.isSleeping()) {
                    completeLayWakeUp(serverPlayer);
                }

                player.stopRiding();
                if (player instanceof ServerPlayer serverPlayer) {
                    FSitAttachable.detach(serverPlayer);

                    var pos = BlockPos.containing(position);
                    var state = player.level().getBlockState(pos);
                    serverPlayer.connection.send(new ClientboundBlockUpdatePacket(pos, state));
                }
            }

            case SITTING -> {
                if (!(player instanceof ServerPlayer serverPlayer) || !FSitMod.getInstance().isUsingClient(serverPlayer)) {
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
                    var seat = new Seat(player.level(), position.x, position.y, position.z, player.getRotationVector());
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
                if (!(player instanceof ServerPlayer serverPlayer)) {
                    break;
                }
                if (!(player.level() instanceof ServerLevel serverLevel)) {
                    break;
                }

                var bedPosition = fakeBedPosFor(serverLevel, position);
                broadcastBedUpdate(serverLevel, bedPosition, fakeBedStateFor(serverPlayer.getYRot()));

                // skip startSleeping — its setPosToBed shifts the player to bedPos+0.6875,
                // dragging chunk tracking down to the bedrock layer and crashing Lithium's palette
                if (serverPlayer.isPassenger()) serverPlayer.stopRiding();
                serverPlayer.setPose(Pose.SLEEPING);
                serverPlayer.setSleepingPos(bedPosition);
                serverPlayer.setDeltaMovement(Vec3.ZERO);
                // updateSleepingPlayerList not needed — SleepStatusMixin excludes us from the tally
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
