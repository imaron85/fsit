package dev.rvbsm.fsit.world.entity;

import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.config.ConfigData;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class PoseEntity extends Entity {

    protected final Player player;

    public PoseEntity(final EntityType<?> type, final Player player) {
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
