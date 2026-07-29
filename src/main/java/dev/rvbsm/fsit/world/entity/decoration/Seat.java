package dev.rvbsm.fsit.world.entity.decoration;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitClientConfig;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.config.SeatTrait;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public final class Seat extends ArmorStand {
    private static final EntityDimensions SEAT_DIMENSIONS = EntityDimensions.fixed(0.6f, 0.1f);

    private SeatTrait trait = FSitMod.getInstance().getConfig().seatsTrait();

    public Seat(Level level, double x, double y, double z, Vec2 rot) {
        super(level, x, y, z);
        this.setInvisible(true);
        this.entityData.set(DATA_CLIENT_FLAGS, (byte) 0b10000); // marker
        this.addTag(FSitMod.MOD_ID);
        this.absSnapRotationTo(rot.y, rot.x);
    }

    @Override
    protected boolean hasPhysics() {
        return !this.isNoGravity() && this.getTrait().canMove();
    }

    @Override
    public boolean isEffectiveAi() {
        return this.hasPhysics();
    }

    @Override
    public boolean isPushable() {
        return this.hasPhysics();
    }

    @Override
    protected void doPush(Entity entity) {
        entity.push(this);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        this.ejectPassengers();
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return SEAT_DIMENSIONS;
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return false;
    }

    @Override
    public void tick() {
        if (this.firstTick) {
            this.refreshDimensions();
        }

        if (this.tickCount % 60 == 0) {
            if (this.getFirstPassenger() instanceof ServerPlayer player && FSitClientConfig.get(player) != null) {
                //noinspection DataFlowIssue
                this.trait = FSitClientConfig.get(player).seatTrait();
            } else {
                this.trait = FSitMod.getInstance().getConfig().seatsTrait();
            }
        }

        if (!this.isVehicle()) {
            // FIXME: `remove_entities` might be sent earlier than `player_position`?
            this.discard();
        }

        if (this.getTrait().needsGround()) {
            var checkGround = AABB.ofSize(this.position(), this.getBbWidth(), 1.0e-3, this.getBbWidth());
            if (this.level().noBlockCollision(this, checkGround)) {
                this.ejectPassengers();
                this.discard();
            }
        }

        super.aiStep();

        // threshold — absSnapRotationTo marks the entity dirty every call
        if (this.getFirstPassenger() instanceof Player passenger) {
            Vec2 rot = passenger.getRotationVector();
            if (Math.abs(Mth.wrapDegrees(this.getYRot() - rot.y)) > 1.0f || Math.abs(this.getXRot() - rot.x) > 1.0f) {
                this.absSnapRotationTo(rot.y, rot.x);
            }
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        if (passenger instanceof Player player) {
            FSitPoseHolder.set(player, FSitPose.STANDING, null, true);
        }

        super.removePassenger(passenger);
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

        var belowFloor = this.level().getBlockFloorHeight(belowBlockPos);
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
    public boolean canSimulateMovement() {
        return this.getTrait().canMove();
    }

    private SeatTrait getTrait() {

        return this.trait;
    }
}
