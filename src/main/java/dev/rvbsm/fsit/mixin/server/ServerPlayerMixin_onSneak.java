package dev.rvbsm.fsit.mixin.server;

import dev.rvbsm.fsit.api.ConfigHolder;
import dev.rvbsm.fsit.api.PoseHolder;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.mixin.PlayerMixin_onSneak;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin_onSneak extends PlayerMixin_onSneak implements ConfigHolder, PoseHolder {

    @Unique
    private boolean lastSneaking;
    @Unique
    private int ticksLeftToDoubleTapPose;

    private ServerPlayerMixin_onSneak(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "doTick", at = @At("TAIL"))
    private void sneakTick(final CallbackInfo ci) {
        if (this.ticksLeftToDoubleTapPose > 0) {
            this.ticksLeftToDoubleTapPose--;
        }

        if (this.shouldStopPosing()) {
            this.ticksLeftToDoubleTapPose = 0;
        }
    }

    @Override
    protected void onSyncedDataSet(final EntityDataAccessor<?> data, final CallbackInfo ci) {
        if (DATA_SHARED_FLAGS_ID.equals(data) && this.lastSneaking != this.isShiftKeyDown()) {
            if (this.isShiftKeyDown()) {
                if (this.fsit$getPose() != ModPose.STANDING) {
                    this.fsit$setPose(ModPose.STANDING);
                    return;
                }

                if (this.isVehicle()) {
                    this.ejectPassengers();
                    return;
                }
            }

            final ConfigData config = this.fsit$getConfigOrServer();
            if (!config.onSneak().sitting() && !config.onSneak().crawling()) {
                return;
            } else if (this.getXRot() <= config.onSneak().minPitch()) {
                return;
            }

            if (this.lastSneaking && !this.isShiftKeyDown()) {
                if (this.ticksLeftToDoubleTapPose == 0) {
                    this.ticksLeftToDoubleTapPose = config.onSneak().window();
                } else {
                    if (config.onSneak().crawling() && this.isNearCrawlableGap()) {
                        this.fsit$setPose(ModPose.CRAWLING);
                    } else if (config.onSneak().sitting()) {
                        this.fsit$setPose(ModPose.SITTING);
                    }
                }
            }

            this.lastSneaking = this.isShiftKeyDown();
        }
    }

    @Unique
    private boolean shouldStopPosing() {
        return !this.onGround()
            || this.getAbilities().flying
            || this.isPassenger()
            || this.isUsingItem()
            || this.fsit$getPose() != ModPose.STANDING;
    }

    // TODO there should be a better approach
    @Unique
    private boolean isNearCrawlableGap() {
        final EntityDimensions crawlingDimensions = this.getDimensions(Pose.SWIMMING);
        final EntityDimensions crouchingDimensions = this.getDimensions(Pose.CROUCHING);

        final float yawRadians = (float) Math.toRadians(this.getYRot());
        final float directionX = -Mth.sin(yawRadians);
        final float directionZ = Mth.cos(yawRadians);
        final double magnitude = Mth.absMax(directionX, directionZ);

        final double offsetX = directionX * this.getBbWidth() / magnitude;
        final double offsetY = crouchingDimensions.height();
        final double offsetZ = directionZ * this.getBbWidth() / magnitude;

        final Vec3 pos = this.position();
        final Vec3 expectEmpty = pos.add(offsetX, 0.0d, offsetZ);
        final Vec3 expectFully = pos.add(offsetX, offsetY, offsetZ);

        final Level world = this.level();
        return world.noCollision(this, crawlingDimensions.makeBoundingBox(expectEmpty))
            && !world.noCollision(this, crawlingDimensions.makeBoundingBox(expectFully));
    }
}
