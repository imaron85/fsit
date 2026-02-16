package dev.rvbsm.fsit.world.entity;

import dev.rvbsm.fsit.api.ConfigHolder;
import dev.rvbsm.fsit.api.PoseHolder;
import dev.rvbsm.fsit.config.ConfigData;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class PoseEntity extends Entity {

    protected final ServerPlayer player;

    public PoseEntity(final EntityType<?> type, final ServerPlayer player) {
        super(type, player.level());
        this.player = player;
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(final ValueInput view) {
    }

    @Override
    protected void addAdditionalSaveData(final ValueOutput view) {
    }

    @Override
    public boolean hurtServer(final ServerLevel world, final DamageSource source, final float amount) {
        return false;
    }

    @Override
    public void remove(final RemovalReason reason) {
        PoseHolder.set(this.player, ModPose.STANDING);
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
        return ConfigHolder.getOrServer(this.player);
    }
}
