package dev.rvbsm.fsit.mixin.server;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitAttachable;
import dev.rvbsm.fsit.api.FSitClientConfig;
import dev.rvbsm.fsit.api.FSitPlayerSettings;
import dev.rvbsm.fsit.api.PlayerSettings;
import dev.rvbsm.fsit.config.ClientConfig;
import dev.rvbsm.fsit.event.FSitPoseListener;
import dev.rvbsm.fsit.mixin.PlayerMixin;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPlayerPosePayload;
import dev.rvbsm.fsit.server.network.ServerModPacketListener;
import dev.rvbsm.fsit.world.entity.FSitPose;
import dev.rvbsm.fsit.world.entity.decoration.Ride;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements FSitAttachable, FSitClientConfig, FSitPlayerSettings {

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

    @Unique
    private @Nullable PlayerSettings fsit$settings = null;

    @Unique
    private static final String FSIT_SETTINGS_KEY = "fsit_settings";

    @Unique
    private @Nullable ServerPlayer fsit$pendingRider = null;

    @Unique
    private static final Logger fsit$LOGGER = LogUtils.getLogger();

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

    @Override
    public PlayerSettings fsit$getSettings() {
        if (this.fsit$settings == null) {
            this.fsit$settings = PlayerSettings.DEFAULT;
        }
        return this.fsit$settings;
    }

    @Override
    public void fsit$setSettings(PlayerSettings settings) {
        this.fsit$settings = settings;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void fsit$saveSettings(ValueOutput output, CallbackInfo ci) {
        // null is treated the same as DEFAULT — both mean "no overrides yet, skip writing"
        if (this.fsit$settings == null || this.fsit$settings.equals(PlayerSettings.DEFAULT)) return;
        output.store(FSIT_SETTINGS_KEY, PlayerSettings.CODEC, this.fsit$settings);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void fsit$loadSettings(ValueInput input, CallbackInfo ci) {
        this.fsit$settings = input.read(FSIT_SETTINGS_KEY, PlayerSettings.CODEC).orElse(PlayerSettings.DEFAULT);
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At("HEAD"))
    private void fsit$preTeleportColumn(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        var self = (ServerPlayer) (Object) this;
        var rider = fsit$directRider(self);
        if (rider == null) return;

        var mode = FSitMod.getInstance().getConfig().ridingTeleportMode();
        var crossDim = transition.newLevel().dimension() != self.level().dimension();
        switch (mode) {
            case DISABLED -> {
                // Drop the rider regardless of dimension — they stay where they were.
                fsit$LOGGER.debug("dropping rider {} of {} (DISABLED) before teleport",
                    rider.getGameProfile().name(), self.getGameProfile().name());
                rider.stopRiding();
            }
            case ENHANCED -> {
                // Same-dim already drags via Ride.tick; we only need an explicit follow-up
                // for cross-dim (vanilla detaches the chain inside removePlayerImmediately).
                if (crossDim) {
                    this.fsit$pendingRider = rider;
                }
            }
            case VANILLA -> {
                // No intervention — vanilla quirks apply (drag along same-dim, leave
                // behind cross-dim).
            }
        }
    }


    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At("RETURN"))
    private void fsit$postTeleportColumn(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        var rider = this.fsit$pendingRider;
        this.fsit$pendingRider = null;
        if (rider == null) return;
        var self = cir.getReturnValue();
        //noinspection ConstantValue
        if (self == null) return;          // teleport failed
        if (rider.isRemoved()) return;     // rider died/disconnected mid-teleport
        var followTransition = new TeleportTransition(
            self.level(), self.position(), Vec3.ZERO, self.getYRot(), self.getXRot(),
            Set.of(), TeleportTransition.DO_NOTHING);
        var movedRider = rider.teleport(followTransition);
        if (movedRider == null) {
            fsit$LOGGER.debug("ENHANCED follow-teleport for rider {} failed", rider.getGameProfile().name());
            return;
        }

        var seat = new Ride(self.level(), self.getX(), self.getY(), self.getZ());
        self.level().addFreshEntity(seat);
        seat.startRiding(self, true, true);
        movedRider.startRiding(seat, true, true);
        fsit$LOGGER.debug("re-attached rider {} to {} after cross-dim teleport",
            movedRider.getGameProfile().name(), self.getGameProfile().name());
    }

    @Unique
    private static @Nullable ServerPlayer fsit$directRider(ServerPlayer player) {
        if (player.getFirstPassenger() instanceof Ride ride
            && ride.getFirstPassenger() instanceof ServerPlayer rider) {
            return rider;
        }
        return null;
    }
}
