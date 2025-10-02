package dev.rvbsm.fsit.entity;

import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public final class SeatEntity extends PoseEntity {

    private static final float VELOCITY_BAND = 0.003f;
    private static final float VELOCITY_IN_WATER_MULTIPLIER = 0.8f;
    private static final float VELOCITY_AFFECTIVE_MULTIPLIER = 0.91f;

    public SeatEntity(PlayerEntity player, Vec3d pos) {
        // todo geyser
        super(EntityType.BLOCK_DISPLAY, player);
        this.setCustomName(Text.literal("FSit_SeatEntity"));

        this.calculateDimensions();
        this.setPosition(pos);
        this.setVelocity(this.player.getVelocity());

        this.player.startRiding(this);
    }

    @Override
    public void tick() {
        if (!this.getEntityWorld().isClient()) {
            if (!this.hasPassengers()) {
                this.discard();
            }

            if (this.hasPassengers() && this.shouldRemovePassengers()) {
                this.removeAllPassengers();
            }

            if (!this.isRemoved() && this.canMoveVoluntarily()) {
                super.updateWaterState();
                this.tickMovement();
            }
        }
    }

    @Override
    public boolean hasNoGravity() {
        return !this.getConfig().sitting().behaviour().hasGravity();
    }

    @Override
    protected double getGravity() {
        return this.player.getFinalGravity();
    }

    private double getEffectiveGravity() {
        if (this.getVelocity().y <= 0.0 && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            return Math.min(0.01, this.getFinalGravity());
        }

        return this.getFinalGravity();
    }

    // TODO pushable by other players?
    @Override
    public boolean isPushable() {
        return this.canMoveVoluntarily();
    }

    @Override
    public boolean canMoveVoluntarily() {
        return this.getConfig().sitting().behaviour().hasGravity();
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        final Vec3d offset = getPassengerDismountOffset(passenger.getWidth(), passenger.getWidth(), passenger.getYaw());
        final double finalX = this.getX() + offset.x;
        final double finalZ = this.getZ() + offset.z;

        final BlockPos blockPos = BlockPos.ofFloored(finalX, this.getY(), finalZ);
        final BlockPos blockPos2 = blockPos.down();

        final List<Vec3d> dismountPositions = new ArrayList<>();
        final double dismountHeight = this.getEntityWorld().getDismountHeight(blockPos);
        if (Dismounting.canDismountInBlock(dismountHeight)) {
            dismountPositions.add(new Vec3d(finalX, blockPos.getY() + dismountHeight, finalZ));
        }

        final double dismountHeight2 = this.getEntityWorld().getDismountHeight(blockPos2);
        if (Dismounting.canDismountInBlock(dismountHeight2)) {
            dismountPositions.add(new Vec3d(finalX, blockPos2.getY() + dismountHeight2, finalZ));
        }

        for (final var passengerPose : passenger.getPoses()) {
            for (final var dismountPos : dismountPositions) {
                if (Dismounting.canPlaceEntityAt(this.getEntityWorld(), dismountPos, passenger, passengerPose)) {
                    passenger.setPose(passengerPose);
                    return dismountPos;
                }
            }
        }

        return this.getEntityPos();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return this.player.getDimensions(player.getPose());
    }

    public boolean hasStatusEffect(RegistryEntry<StatusEffect> effect) {
        return this.player.hasStatusEffect(effect);
    }

    public StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect) {
        return this.player.getStatusEffect(effect);
    }

    private void tickMovement() {
        final Vec3d unbandedVelocity = this.getVelocity();
        this.setVelocity(
            Math.abs(unbandedVelocity.x) < VELOCITY_BAND ? 0.0 : unbandedVelocity.x,
            Math.abs(unbandedVelocity.y) < VELOCITY_BAND ? 0.0 : unbandedVelocity.y,
            Math.abs(unbandedVelocity.z) < VELOCITY_BAND ? 0.0 : unbandedVelocity.z
        );

        if (!this.getEntityWorld().isClient()) {
            this.move(MovementType.SELF, this.getVelocity());

            final Vec3d velocity = this.getVelocity();
            if (this.isTouchingWater()) {
                this.setVelocity(
                    velocity.x * VELOCITY_IN_WATER_MULTIPLIER,
                    velocity.y * VELOCITY_IN_WATER_MULTIPLIER - this.getEffectiveGravity() / 16.0,
                    velocity.z * VELOCITY_IN_WATER_MULTIPLIER
                );
            } else {
                final double velocityY;
                if (this.hasStatusEffect(StatusEffects.LEVITATION)) {
                    final int effectAmplifier = this.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1;
                    velocityY = velocity.y * 0.8 + effectAmplifier * 0.01;
                } else {
                    velocityY = velocity.y - this.getEffectiveGravity();
                }

                final BlockPos affectivePos = this.getVelocityAffectingPos();
                final float affectiveSlipperiness = this.isOnGround()
                    ? this.getEntityWorld().getBlockState(affectivePos).getBlock().getSlipperiness()
                    : 1.0f;
                final float velocityMultiplier = affectiveSlipperiness * VELOCITY_AFFECTIVE_MULTIPLIER;

                this.setVelocity(
                    velocity.x * velocityMultiplier,
                    velocityY * velocityMultiplier,
                    velocity.z * velocityMultiplier
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
            final Box groundCollisionBox = Box.of(this.getEntityPos(), this.getWidth(), 1.0e-3, this.getWidth());
            for (var shape : this.getEntityWorld().getCollisions(this, groundCollisionBox)) {
                if (shape.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }
}
