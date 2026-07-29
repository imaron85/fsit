package dev.rvbsm.fsit.world.entity.decoration;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.mixin.EntityAccessor;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public final class Ride extends AreaEffectCloud {

    public Ride(Level level, double x, double y, double z) {
        super(level, x, y, z);
        this.setInvisible(true);
        this.setRadius(0.0f);
        this.addTag(FSitMod.MOD_ID);
    }

    @Override
    public boolean startRiding(Entity entityToRide, boolean force, boolean sendEventAndTriggers) {
        if (entityToRide == this.getVehicle()) {
            return false;
        }
        for (Entity v = entityToRide; v.getVehicle() != null; v = v.getVehicle()) {
            if (v.getVehicle() == this) {
                return false;
            }
        }

        if (this.isPassenger()) {
            this.stopRiding();
        }
        // EntityAccessor is mixed into Entity at runtime; javac doesn't see it on a final class,
        // so we route through Object to keep the unchecked cast valid.
        ((EntityAccessor) (Object) this).setVehicle(entityToRide);
        ((EntityAccessor) entityToRide).callAddPassenger(this);
        if (this.level() instanceof ServerLevel sl) {
            sl.getChunkSource().sendToTrackingPlayersAndSelf(entityToRide,
                new ClientboundSetPassengersPacket(entityToRide));
        }

        if (sendEventAndTriggers) {
            this.level().gameEvent(this, GameEvent.ENTITY_MOUNT, entityToRide.position());
            for (var entity : entityToRide.getIndirectPassengers()) {
                if (entity instanceof ServerPlayer player) {
                    CriteriaTriggers.START_RIDING_TRIGGER.trigger(player);
                }
            }
        }

        return true;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            return;
        }

        if (!this.isPassenger() || !this.isVehicle()) {
            this.discard();
            return;
        }
        var vehicle = this.getVehicle();
        if (vehicle != null) {
            if (vehicle.isShiftKeyDown()) {
                this.ejectPassengers();
                this.discard();
                return;
            }
            this.setPos(vehicle.getX(), vehicle.getY(), vehicle.getZ());
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        var root = this.getRootVehicle();
        var offset = getCollisionHorizontalEscapeVector(
            root.getBbWidth() * Mth.SQRT_OF_TWO, passenger.getBbWidth(), passenger.getYRot());
        var finalX = root.getX() + offset.x;
        var finalZ = root.getZ() + offset.z;

        var finalBlockPos = BlockPos.containing(finalX, root.getY(), finalZ);
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

        return root.getDismountLocationForPassenger(passenger);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean hasExactlyOnePlayerPassenger() {
        return false; // do not save as "RootVehicle"
    }
}
