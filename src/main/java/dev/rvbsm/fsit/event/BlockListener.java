package dev.rvbsm.fsit.event;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class BlockListener implements BlockEvents.UseWithoutItemCallback {

    public static final BlockListener INSTANCE = new BlockListener();
    private static final Logger LOGGER = LogUtils.getLogger();

    private BlockListener() {}

    @Override
    public @Nullable InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if (!level.isClientSide() && FSitMod.getInstance().isClient((ServerPlayer) player)) {
            return null;
        }

        LOGGER.debug("use block {} {} {} {} {}", state, level, pos, player, hitResult);

        var config = FSitMod.getInstance().getConfig();
        if (!config.blockInteraction()) {
            return null;
        }

        boolean isCloseEnough = player.isWithinBlockInteractionRange(hitResult.getBlockPos(), 1.0);
        if (!isCloseEnough) {
            return null;
        }

        boolean haveNothingInOurHands =
                player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty();
        boolean suppressUsingBlock = player.isSecondaryUseActive() || !haveNothingInOurHands;
        if (suppressUsingBlock) {
            return null;
        }

        boolean isCorrectState = hitResult.getDirection() == Direction.UP
                && state.getProperties().stream().allMatch(property -> {
                    if (property == BlockStateProperties.HALF) {
                        return state.getValue(BlockStateProperties.HALF) == Half.BOTTOM;
                    }
                    if (property == BlockStateProperties.SLAB_TYPE) {
                        return state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM;
                    }
                    if (property == BlockStateProperties.AXIS) {
                        return state.getValue(BlockStateProperties.AXIS).isHorizontal();
                    }

                    return true;
                })
                && state.is(config.collectInteractableBlocks(player.registryAccess()));

        if (!isCorrectState) {
            return null;
        }

        LOGGER.debug("success with {} at {}", state, hitResult);

        FSitPoseHolder.set(player, FSitPose.SITTING, new FSitPose.Context.Positioned(hitResult.getLocation()));
        return InteractionResult.SUCCESS;
    }
}
