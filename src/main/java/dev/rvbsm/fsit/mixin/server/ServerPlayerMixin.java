package dev.rvbsm.fsit.mixin.server;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitAttachable;
import dev.rvbsm.fsit.api.FSitClientConfig;
import dev.rvbsm.fsit.config.ClientConfig;
import dev.rvbsm.fsit.event.FSitPoseListener;
import dev.rvbsm.fsit.mixin.PlayerMixin;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPlayerPosePayload;
import dev.rvbsm.fsit.server.network.ServerModPacketListener;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements FSitAttachable, FSitClientConfig {

    @Unique
    private FSitPose fsit$lastSentPose = FSitPose.STANDING;

    @Unique
    private int fsit$sneakTriggerTime = 0;

    @Unique
    private boolean fsit$lastShiftKeyDown = false;

    @Unique
    private @Nullable Entity fsit$attachedEntity = null;

    @Unique
    private @Nullable ClientConfig fsit$clientConfig = null;

    @Inject(method = "tick", at = @At("TAIL"))
    private void sneakWindow(CallbackInfo ci) {
        boolean useClient = FSitMod.getInstance().isUsingClient((ServerPlayer) (Object) this);
        if (useClient) {
            return;
        }

        if (this.fsit$sneakTriggerTime > 0) {
            this.fsit$sneakTriggerTime -= 1;
        }

        if (!this.onGround() || this.isPassenger()) {
            this.fsit$sneakTriggerTime = 0;
        }

        if (this.fsit$canStartPosingBySneaking()) {
            if (this.fsit$lastShiftKeyDown) {
                if (this.fsit$sneakTriggerTime > 0) {
                    this.fsit$startPosingBySneaking();
                } else {
                    this.fsit$sneakTriggerTime = FSitMod.getInstance().getConfig().sneakWindow();
                }
            }
        }

        this.fsit$lastShiftKeyDown = this.isShiftKeyDown();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickAttached(CallbackInfo ci) {
        if (this.fsit$attachedEntity != null) {
            if (this.wantsToStopRiding()) {
                this.fsit$detach();
            } else {
                this.fsit$attachedEntity.tick();
            }
        }
    }

    // late joiners / dimension-returners won't have the fake bed packet from /lay — re-send it
    @Inject(method = "tick", at = @At("TAIL"))
    private void fsit$tickLayingVisuals(CallbackInfo ci) {
        if (this.fsit$getPose() != FSitPose.LAYING) return;
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (self.tickCount % 20 != 0) return;

        var bedPos = self.getSleepingPos().orElse(null);
        if (bedPos == null) return;
        if (!(self.level() instanceof ServerLevel level)) return;

        FSitPoseListener.broadcastBedUpdate(level, bedPos,
                FSitPoseListener.fakeBedStateFor(self.getYRot()));
    }

    @Override
    protected boolean fsit$canStartPosingBySneaking() {
        return this.getKnownSpeed().horizontalDistance() < 1.0e-3
            && super.fsit$canStartPosingBySneaking();
    }

    @Override
    protected void fsit$sendPose(Vec3 position) {
        if (this.fsit$pose != this.fsit$lastSentPose) {
            ServerModPacketListener.send(
                    (ServerPlayer) (Object) this,
                    ClientboundPlayerPosePayload.TYPE,
                    () -> new ClientboundPlayerPosePayload(this.fsit$pose));
            this.fsit$lastSentPose = this.fsit$pose;
        }
    }

    @Override
    public void fsit$attach(Entity entity) {
        this.fsit$attachedEntity = entity;
    }

    @Override
    public void fsit$detach() {
        if (this.fsit$attachedEntity != null) {
            this.fsit$attachedEntity.discard();
            this.fsit$attachedEntity = null;
        }
    }

    @Override
    public void fsit$setConfig(ClientConfig config) {
        this.fsit$clientConfig = config;
    }

    @Override
    public @Nullable ClientConfig fsit$getConfig() {
        return this.fsit$clientConfig;
    }
}
