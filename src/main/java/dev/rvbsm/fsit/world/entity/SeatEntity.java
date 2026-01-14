package dev.rvbsm.fsit.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class SeatEntity extends PoseEntity {

    private static final float DELTA_MOVEMENT_BAND = 0.003f;
    private static final float DELTA_MOVEMENT_IN_WATER_MULTIPLIER = 0.8f;
    private static final float DELTA_MOVEMENT_AFFECTIVE_MULTIPLIER = 0.91f;

    public SeatEntity(final Player player, final Vec3 pos) {
        // todo geyser
        super(EntityType.BLOCK_DISPLAY, player);
        this.setCustomName(Component.literal("FSit_SeatEntity"));

        this.refreshDimensions();
        this.setPos(pos);
        this.setDeltaMovement(this.player.getDeltaMovement());

        this.player.startRiding(this);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            if (!this.isVehicle()) {
                this.discard();
            }

            if (this.isVehicle() && this.shouldRemovePassengers()) {
                this.ejectPassengers();
            }

            if (!this.isRemoved() && this.canSimulateMovement()) {
                super.updateInWaterStateAndDoFluidPushing();
                this.tickMovement();
            }
        }
    }

    @Override
    public boolean isNoGravity() {
        return !this.getConfig().sitting().behaviour().hasGravity();
    }

    @Override
    protected double getDefaultGravity() {
        return this.player.getGravity();
    }

    private double getEffectiveGravity() {
        if (this.getDeltaMovement().y <= 0.0 && this.hasStatusEffect(MobEffects.SLOW_FALLING)) {
            return Math.min(0.01, this.getGravity());
        }

        return this.getGravity();
    }

    // TODO pushable by other players?
    @Override
    public boolean isPushable() {
        return this.canSimulateMovement();
    }

    @Override
    public boolean canSimulateMovement() {
        return this.getConfig().sitting().behaviour().hasGravity();
    }

    @Override
    public Vec3 getDismountLocationForPassenger(final LivingEntity passenger) {
        final Vec3 offset = getCollisionHorizontalEscapeVector(passenger.getBbWidth(), passenger.getBbWidth(), passenger.getYRot());
        final double finalX = this.getX() + offset.x;
        final double finalZ = this.getZ() + offset.z;

        final BlockPos blockPos = BlockPos.containing(finalX, this.getY(), finalZ);
        final BlockPos blockPos2 = blockPos.below();

        final List<Vec3> dismountPositions = new ArrayList<>();
        final double dismountHeight = this.level().getBlockFloorHeight(blockPos);
        if (DismountHelper.isBlockFloorValid(dismountHeight)) {
            dismountPositions.add(new Vec3(finalX, blockPos.getY() + dismountHeight, finalZ));
        }

        final double dismountHeight2 = this.level().getBlockFloorHeight(blockPos2);
        if (DismountHelper.isBlockFloorValid(dismountHeight2)) {
            dismountPositions.add(new Vec3(finalX, blockPos2.getY() + dismountHeight2, finalZ));
        }

        for (final var passengerPose : passenger.getDismountPoses()) {
            for (final var dismountPos : dismountPositions) {
                if (DismountHelper.canDismountTo(this.level(), dismountPos, passenger, passengerPose)) {
                    passenger.setPose(passengerPose);
                    return dismountPos;
                }
            }
        }

        return this.position();
    }

    @Override
    public EntityDimensions getDimensions(final Pose pose) {
        return this.player.getDimensions(player.getPose());
    }

    public boolean hasStatusEffect(final Holder<MobEffect> effect) {
        return this.player.hasEffect(effect);
    }

    public @Nullable MobEffectInstance getStatusEffect(final Holder<MobEffect> effect) {
        return this.player.getEffect(effect);
    }

    private void tickMovement() {
        final Vec3 unbandedDeltaMovement = this.getDeltaMovement();
        this.setDeltaMovement(
            Math.abs(unbandedDeltaMovement.x) < DELTA_MOVEMENT_BAND ? 0.0 : unbandedDeltaMovement.x,
            Math.abs(unbandedDeltaMovement.y) < DELTA_MOVEMENT_BAND ? 0.0 : unbandedDeltaMovement.y,
            Math.abs(unbandedDeltaMovement.z) < DELTA_MOVEMENT_BAND ? 0.0 : unbandedDeltaMovement.z
        );

        if (!this.level().isClientSide()) {
            this.move(MoverType.SELF, this.getDeltaMovement());

            final Vec3 deltaMovement = this.getDeltaMovement();
            if (this.isInWater()) {
                this.setDeltaMovement(
                    deltaMovement.x * DELTA_MOVEMENT_IN_WATER_MULTIPLIER,
                    deltaMovement.y * DELTA_MOVEMENT_IN_WATER_MULTIPLIER - this.getEffectiveGravity() / 16.0,
                    deltaMovement.z * DELTA_MOVEMENT_IN_WATER_MULTIPLIER
                );
            } else {
                final double deltaMovementY;
                if (this.hasStatusEffect(MobEffects.LEVITATION)) {
                    final int effectAmplifier = this.getStatusEffect(MobEffects.LEVITATION).getAmplifier() + 1;
                    deltaMovementY = deltaMovement.y * 0.8 + effectAmplifier * 0.01;
                } else {
                    deltaMovementY = deltaMovement.y - this.getEffectiveGravity();
                }

                final BlockPos affectivePos = this.getBlockPosBelowThatAffectsMyMovement();
                final float affectiveFriction = this.onGround()
                    ? this.level().getBlockState(affectivePos).getBlock().getFriction()
                    : 1.0f;
                final float deltaMovementMultiplier = affectiveFriction * DELTA_MOVEMENT_AFFECTIVE_MULTIPLIER;

                this.setDeltaMovement(
                    deltaMovement.x * deltaMovementMultiplier,
                    deltaMovementY * deltaMovementMultiplier,
                    deltaMovement.z * deltaMovementMultiplier
                );
            }
        }
    }

    // TODO surely there is something better that this
    private boolean shouldRemovePassengers() {
        if (this.isInLava()) {
            return true;
        }

        if (this.getConfig().sitting().behaviour().requireSupport()) {
            final AABB groundCollisionBox = AABB.ofSize(this.position(), this.getBbWidth(), 1.0e-3, this.getBbWidth());
            for (var shape : this.level().getCollisions(this, groundCollisionBox)) {
                if (shape.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }
}
