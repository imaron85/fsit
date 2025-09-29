package dev.rvbsm.fsit.entity;

import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.config.ConfigData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;

public abstract class PoseEntity extends Entity {

    protected final PlayerEntity player;

    public PoseEntity(EntityType<?> type, PlayerEntity player) {
        super(type, player.getEntityWorld());
        this.player = player;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    //? if >=1.21.6 {
    @Override
    protected void readCustomData(net.minecraft.storage.ReadView view) {
    }

    @Override
    protected void writeCustomData(net.minecraft.storage.WriteView view) {
    }
    //?} else {
    /*@Override
    protected void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {
    }
    *///?}

    //? if >=1.21.2 {
    @Override
    public boolean damage(net.minecraft.server.world.ServerWorld world, net.minecraft.entity.damage.DamageSource source, float amount) {
        return false;
    }
    //?}

    @Override
    public void remove(RemovalReason reason) {
        ((Poseable) this.player).fsit$setPose(ModPose.STANDING);

        super.remove(reason);
    }

    // prevents from saving to player data
    @Override
    public boolean hasPlayerRider() {
        return false;
    }

    @Override
    public float getYaw() {
        return this.player.getYaw();
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    protected ConfigData getConfig() {
        return ((Configurable) this.player).fsit$configOrDefault();
    }
}
