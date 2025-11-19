package dev.rvbsm.fsit.entity;

import dev.rvbsm.fsit.mixin.common.access.EntityAccess;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.gameevent.GameEvent;

public final class RideEntity extends PoseEntity {

    public RideEntity(ServerPlayer player, ServerPlayer other) {
        super(EntityType.AREA_EFFECT_CLOUD, player);
        this.setCustomName(Component.literal("FSit_RideEntity"));

        this.setPos(player.position());
        player.startRiding(this);
        this.startRiding(other);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            if (!this.isVehicle() && !this.isPassenger()) {
                this.discard();
            }

            // if either passenger or vehicle missing
            if (this.isVehicle() == !this.isPassenger()) {
                this.unRide();
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

        if (entity.hasIndirectPassenger(this)) {
            return false;
        }

        if (!this.isPassenger() && !entity.isVehicle()) {
            this.setPose(Pose.STANDING);
            ((EntityAccess) (Entity) this).setVehicle(entity);
            ((EntityAccess) entity).callAddPassenger(this);
            if (emitEvent) {
                this.level().gameEvent(this, GameEvent.ENTITY_MOUNT, entity.position());
                if (this.player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.START_RIDING_TRIGGER.trigger(serverPlayer);
                }
            }

            return true;
        }

        return false;
    }
}
