package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.world.entity.decoration.Ride;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UseEntityListener implements UseEntityCallback {

    public static final UseEntityListener INSTANCE = new UseEntityListener();
    private static final Logger LOGGER = LoggerFactory.getLogger(UseEntityListener.class);

    private static @Nullable Vec3 getClickedRidingOffset(LivingEntity vehicle, Vec3 location) {
        // todo: offsets
        double clickYPosition = location.y / vehicle.getScale();
        if (clickYPosition >= 0.9 && clickYPosition < 1.6) {
            return new Vec3(-0.5, 0.9, 0.0);
        } else if (clickYPosition >= 1.6) {
            return new Vec3(-0.2, 1.6, 0.0);
        }

        return null;
    }

    @Override
    public InteractionResult interact(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (player.isSpectator()) {
            return InteractionResult.PASS;
        }

        if (!(entity instanceof Player target)) {
            return InteractionResult.PASS;
        }

        boolean interactWithSelf = player.equals(entity) || player.getUUID().equals(entity.getUUID());
        if (interactWithSelf) {
            return InteractionResult.PASS;
        }

        LOGGER.debug("use entity {} {} {} {} {}", player, level, hand, entity, hitResult != null ? hitResult.getLocation() : null);
        var config = FSitMod.getInstance().getConfig();
        if (level.isClientSide() && config.useServer()) {
            return InteractionResult.PASS;
        }

        if (config.ridingMinimumYaw() == 0) {
            return level.isClientSide() ? InteractionResult.FAIL : InteractionResult.PASS;
        }

        boolean isCloseEnough = player.isWithinEntityInteractionRange(entity, 3.0)
            && (Math.abs(player.getYRot() - entity.getYRot()) < config.ridingMinimumYaw());
        if (!isCloseEnough) {
            return level.isClientSide() ? InteractionResult.FAIL : InteractionResult.PASS;
        }

        boolean haveNothingInOurHands =
            player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty();
        boolean suppressUsingPlayer = player.isSecondaryUseActive() || !haveNothingInOurHands;
        if (suppressUsingPlayer) {
            return level.isClientSide() ? InteractionResult.FAIL : InteractionResult.PASS;
        }

        // todo: hitResult#location is not relative
        // var offset = config.ridingNew() ? getClickedRidingOffset(target, hitResult.getLocation()) : Vec3.ZERO;
        // if (offset == null) {
        //     return level.isClientSide() ? InteractionResult.FAIL : InteractionResult.PASS;
        // }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS_SERVER;
        }

        // TODO: start riding with specified offset
        // ffs riders position is client-side for riders
        var seat = new Ride(level, target.getX(), target.getY(), target.getZ());
        target.startRiding(seat, true, true);
        seat.startRiding(entity, true, true);
        level.addFreshEntity(seat);

        return InteractionResult.SUCCESS;
    }
}
