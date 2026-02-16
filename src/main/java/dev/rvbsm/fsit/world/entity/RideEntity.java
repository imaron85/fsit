package dev.rvbsm.fsit.world.entity;

import dev.rvbsm.fsit.mixin.EntityAccessor;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.gameevent.GameEvent;

public final class RideEntity extends PoseEntity {

    public RideEntity(final ServerPlayer player, final ServerPlayer other) {
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
    public boolean startRiding(final Entity entity, final boolean force, final boolean emitEvent) {
        if (this == entity) {
            return false;
        }

        if (entity.hasIndirectPassenger(this)) {
            return false;
        }

        if (!this.isPassenger() && !entity.isVehicle()) {
            this.setPose(Pose.STANDING);
            ((EntityAccessor) (Entity) this).setVehicle(entity);
            ((EntityAccessor) entity).callAddPassenger(this);
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
