package dev.rvbsm.fsit.entity;

import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.config.ConfigData;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

public abstract class PoseEntity extends Entity {

    protected final Player player;

    public PoseEntity(EntityType<?> type, Player player) {
        super(type, player.level());
        this.player = player;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    //? if >=1.21.6 {
    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput view) {
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput view) {
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
    public boolean hurtServer(net.minecraft.server.level.ServerLevel world, net.minecraft.world.damagesource.DamageSource source, float amount) {
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
    public boolean hasExactlyOnePlayerPassenger() {
        return false;
    }

    @Override
    public float getYRot() {
        return this.player.getYRot();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    protected ConfigData getConfig() {
        return ((Configurable) this.player).fsit$configOrDefault();
    }
}
