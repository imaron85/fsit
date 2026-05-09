package dev.rvbsm.fsit.event;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitPlayerSettings;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class BlockListener implements BlockEvents.UseWithoutItemCallback {

    public static final BlockListener INSTANCE = new BlockListener();
    private static final Logger LOGGER = LogUtils.getLogger();

    private BlockListener() {}

    @Override
    public @Nullable InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if (!level.isClientSide() && FSitMod.getInstance().isUsingClient((ServerPlayer) player)) {
            return null;
        }

        LOGGER.debug("use block {} {} {} {} {}", state, level, pos, player, hitResult);

        var config = FSitMod.getInstance().getConfig();
        if (level.isClientSide() && config.useServer()) {
            return null;
        }

        if (!config.blockInteraction()) {
            return null;
        }

        boolean isCloseEnough = player.isWithinBlockInteractionRange(hitResult.getBlockPos(), 1.0);
        if (!isCloseEnough) {
            return null;
        }

        boolean handsAreIdle = FSitPlayerSettings.get(player).handsAreIdle(player, player.registryAccess());
        boolean suppressUsingBlock = player.isSecondaryUseActive() || !handsAreIdle;
        if (suppressUsingBlock) {
            return null;
        }

        if (hitResult.getDirection() != Direction.UP) {
            return null;
        }

        if (!state.is(config.collectInteractableBlocks(player.registryAccess()))) {
            return null;
        }

        LOGGER.debug("success with {} at {}", state, hitResult);

        FSitPoseHolder.set(player, FSitPose.SITTING, new FSitPose.Context.Positioned(resolveSeatPosition(pos, hitResult)));
        return InteractionResult.SUCCESS;
    }

    // clamp X/Z inside the block so seats don't spawn on the edge between two blocks
    private static Vec3 resolveSeatPosition(BlockPos pos, BlockHitResult hit) {
        double seatX = Mth.clamp(hit.getLocation().x, pos.getX() + 0.05, pos.getX() + 0.95);
        double seatZ = Mth.clamp(hit.getLocation().z, pos.getZ() + 0.05, pos.getZ() + 0.95);
        return new Vec3(seatX, hit.getLocation().y, seatZ);
    }
}
