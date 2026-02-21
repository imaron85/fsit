package dev.rvbsm.fsit.world.entity.vehicle;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.PoseHolder;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public class Seat extends VehicleEntity {

    private static final int TICKS_TO_DISCARD = 20;
    private int noRiderTicks = 0;

    public Seat(EntityType<? extends Entity> type, Level level) {
        super(type, level);
    }

    public static Seat create(Level level) {
        return new Seat(EntityType.BLOCK_DISPLAY, level);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public void tick() {
        if (this.firstTick) {
            this.refreshDimensions();
        }

        if (!this.isVehicle()) {
            this.noRiderTicks += 1;
            if (this.noRiderTicks >= TICKS_TO_DISCARD) {
                this.discard();
            }

            return;
        }

        // todo:
        var config = FSitMod.getInstance().getConfig();
        if (config.seatsTrait().needsGround()) {
            var checkGround = AABB.ofSize(this.position(), this.getBbWidth(), 1.0e-3, this.getBbWidth());
            for (var shape : this.level().getCollisions(this, checkGround)) {
                if (shape.isEmpty()) {
                    this.discard();
                }
            }
        }

        super.tick();

        if (!this.level().isClientSide()) {
            if (this.canSimulateMovement()) {
                this.tickMovement();

                this.move(MoverType.SELF, this.getDeltaMovement());
            }
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        if (passenger instanceof Player player) {
            PoseHolder.set(player, ModPose.STANDING);
        }

        super.removePassenger(passenger);
    }

    private void tickMovement() {
        var friction = 0.91;
        var dy = -this.getGravity();
        if (this.getFirstPassenger() instanceof Player passenger && passenger.hasEffect(MobEffects.LEVITATION)) {
            //noinspection DataFlowIssue
            var levitationLevel = passenger.getEffect(MobEffects.LEVITATION).getAmplifier() + 1;
            dy = dy * 0.8 + levitationLevel * 0.01;
        } else if (this.isInWater()) {
            friction = 0.8;
            dy /= 8;
        } else if (this.onGround()) {
            friction *= this.level()
                .getBlockState(this.getBlockPosBelowThatAffectsMyMovement())
                .getBlock()
                .getFriction();
        }

        var movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x * friction, movement.y * friction + dy, movement.z * friction);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        for (var passengerPose : passenger.getDismountPoses()) {
            if (DismountHelper.canDismountTo(this.level(), this.position(), passenger, passengerPose)) {
                passenger.setPose(passengerPose);
                return this.position();
            }
        }

        var offset = getCollisionHorizontalEscapeVector(
            passenger.getBbWidth() * Mth.SQRT_OF_TWO, passenger.getBbWidth(), passenger.getYRot());
        var finalX = this.getX() + offset.x;
        var finalZ = this.getZ() + offset.z;

        var finalBlockPos = BlockPos.containing(finalX, this.getY(), finalZ);
        var belowBlockPos = finalBlockPos.below();

        var dismountPositions = new ArrayList<Vec3>();
        var finalFloor = this.level().getBlockFloorHeight(finalBlockPos);
        if (DismountHelper.isBlockFloorValid(finalFloor)) {
            dismountPositions.add(new Vec3(finalX, finalBlockPos.getY() + finalFloor, finalZ));
        }

        var belowFloor = this.level().getBlockFloorHeight(finalBlockPos);
        if (DismountHelper.isBlockFloorValid(belowFloor)) {
            dismountPositions.add(new Vec3(finalX, belowBlockPos.getY() + belowFloor, finalZ));
        }

        for (var passengerPose : passenger.getDismountPoses()) {
            for (var dismountPos : dismountPositions) {
                if (DismountHelper.canDismountTo(this.level(), dismountPos, passenger, passengerPose)) {
                    passenger.setPose(passengerPose);
                    return dismountPos;
                }
            }
        }

        return this.position();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean hasExactlyOnePlayerPassenger() {
        return false; // do not save as "RootVehicle"
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public boolean canSimulateMovement() {
        return FSitMod.getInstance().getConfig().seatsTrait().canMove();
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return FSitMod.getInstance().getConfig().seatsTrait().canMove();
    }

    @Override
    public boolean isPushedByFluid() {
        return FSitMod.getInstance().getConfig().seatsTrait().canMove();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (this.getFirstPassenger() instanceof Player passenger) {
            return passenger.getDimensions(pose);
        }

        return super.getDimensions(pose);
    }

    @Override
    protected Item getDropItem() {
        return Items.AIR;
    }
}
