package dev.rvbsm.fsit.entity;

import dev.rvbsm.fsit.mixin.common.EntityAccess;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.event.GameEvent;

public final class RideEntity extends PoseEntity {

    public RideEntity(ServerPlayerEntity player, ServerPlayerEntity other) {
        super(EntityType.AREA_EFFECT_CLOUD, player);
        this.setCustomName(Text.literal("FSit_RideEntity"));

        this.setPosition(player.getEntityPos());
        player.startRiding(this);
        this.startRiding(other);
    }

    @Override
    public void tick() {
        if (!this.getEntityWorld().isClient()) {
            if (!this.hasPassengers() && !this.hasVehicle()) {
                this.discard();
            }

            // if either passenger or vehicle missing
            if (this.hasPassengers() == !this.hasVehicle()) {
                this.detach();
            }
        }
    }

    @Override
    //? if >=1.21.9 {
    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
    //?} else {
    /*public boolean startRiding(Entity entity, boolean force) {
        final boolean emitEvent = true;
    *///?}
        if (this == entity) {
            return false;
        }

        if (entity.hasPassengerDeep(this)) {
            return false;
        }

        if (!this.hasVehicle() && !entity.hasPassengers()) {
            this.setPose(EntityPose.STANDING);
            ((EntityAccess) (Entity) this).setVehicle(entity);
            ((EntityAccess) entity).callAddPassenger(this);
            if (emitEvent) {
                this.getEntityWorld().emitGameEvent(this, GameEvent.ENTITY_MOUNT, entity.getEntityPos());
                if (this.player instanceof ServerPlayerEntity serverPlayer) {
                    Criteria.STARTED_RIDING.trigger(serverPlayer);
                }
            }

            return true;
        }

        return false;
    }
}
