package dev.rvbsm.fsit.event;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

        if (!level.isClientSide() && FSitMod.getInstance().handledByClient(player.getUUID())) {
            return null;
        }

        LOGGER.debug("use block {} cs:{}", player, level.isClientSide());

        var config = FSitMod.getInstance().getConfig();
        boolean isCloseEnough = player.isWithinBlockInteractionRange(hitResult.getBlockPos(), 1.0);

        boolean haveNothingInOurHands =
                player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty();
        boolean suppressUsingBlock = player.isSecondaryUseActive() || !haveNothingInOurHands;

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

        if (isCloseEnough && !suppressUsingBlock && isCorrectState) {
            LOGGER.debug("success with {} at {}", state, hitResult);

            FSitPoseHolder.set(player, FSitPose.SITTING, new FSitPose.Context.Vec3(hitResult.getLocation()));
            return InteractionResult.SUCCESS_SERVER;
        }

        return null;
    }
}
