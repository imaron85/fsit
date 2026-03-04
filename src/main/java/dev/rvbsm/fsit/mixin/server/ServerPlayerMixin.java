package dev.rvbsm.fsit.mixin.server;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitAttachable;
import dev.rvbsm.fsit.mixin.PlayerMixin;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPlayerPosePayload;
import dev.rvbsm.fsit.server.network.ServerModPacketListener;
import dev.rvbsm.fsit.world.entity.FSitPose;
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
public abstract class ServerPlayerMixin extends PlayerMixin implements FSitAttachable {

    @Unique
    private FSitPose fsit$lastSentPose = FSitPose.STANDING;

    @Unique
    private int fsit$sneakTriggerTime = 0;

    @Unique
    private boolean fsit$lastShiftKeyDown = false;

    @Unique
    private @Nullable Entity fsit$attachedEntity = null;

    @Inject(method = "tick", at = @At("TAIL"))
    private void sneakWindow(CallbackInfo ci) {
        if (FSitMod.getInstance().isClient((ServerPlayer) (Object) this)) {
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
                    this.fsit$sneakTriggerTime =
                            FSitMod.getInstance().getConfig().sneakWindow();
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

    @Override
    protected void fsit$syncPose(Vec3 position) {
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
}
