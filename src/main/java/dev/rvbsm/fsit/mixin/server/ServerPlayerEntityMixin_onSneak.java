package dev.rvbsm.fsit.mixin.server;

import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.ModPose;
import dev.rvbsm.fsit.mixin.common.PlayerEntityMixin_onSneak;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_onSneak extends PlayerEntityMixin_onSneak implements Configurable, Poseable {

    @Unique
    private boolean lastSneaking;
    @Unique
    private int ticksLeftToDoubleTapPose;

    private ServerPlayerEntityMixin_onSneak(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "playerTick", at = @At("TAIL"))
    private void sneakTick(CallbackInfo ci) {
        if (this.ticksLeftToDoubleTapPose > 0) {
            this.ticksLeftToDoubleTapPose--;
        }

        if (this.shouldStopPosing()) {
            this.ticksLeftToDoubleTapPose = 0;
        }
    }

    @Override
    protected void onTrackedDataSet(TrackedData<?> data, CallbackInfo ci) {
        if (FLAGS.equals(data) && this.lastSneaking != this.isSneaking()) {
            if (this.isSneaking()) {
                if (this.fsit$getPose() != ModPose.STANDING) {
                    this.fsit$setPose(ModPose.STANDING);
                    return;
                }

                if (this.hasPassengers()) {
                    this.removeAllPassengers();
                    return;
                }
            }

            final ConfigData config = this.fsit$configOrDefault();
            if (!config.onSneak().sitting() && !config.onSneak().crawling()) {
                return;
            } else if (this.getPitch() <= config.onSneak().minPitch()) {
                return;
            }

            if (this.lastSneaking && !this.isSneaking()) {
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

            this.lastSneaking = this.isSneaking();
        }
    }

    @Unique
    private boolean shouldStopPosing() {
        return !this.isOnGround()
            || this.getAbilities().flying
            || this.hasVehicle()
            || this.isUsingItem()
            || this.fsit$getPose() != ModPose.STANDING;
    }

    // TODO there should be a better approach
    @Unique
    private boolean isNearCrawlableGap() {
        final EntityDimensions crawlingDimensions = this.getDimensions(EntityPose.SWIMMING);
        final EntityDimensions crouchingDimensions = this.getDimensions(EntityPose.CROUCHING);

        final double yawRads = Math.toRadians(this.getYaw());
        final double offsetX = Math.sin(yawRads) * -.1d;
        final double offsetZ = Math.cos(yawRads) * 0.1d;
        final double offsetY = crouchingDimensions.height();

        final Vec3d pos = this.getEntityPos();
        final Vec3d expectEmpty = pos.add(offsetX, 0.0d, offsetZ);
        final Vec3d expectFully = pos.add(offsetX, offsetY, offsetZ);

        final World world = this.getEntityWorld();
        return world.isSpaceEmpty(this, crawlingDimensions.getBoxAt(expectEmpty))
            && !world.isSpaceEmpty(this, crawlingDimensions.getBoxAt(expectFully));
    }
}
