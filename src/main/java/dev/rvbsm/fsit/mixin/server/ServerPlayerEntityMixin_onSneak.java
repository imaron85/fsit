package dev.rvbsm.fsit.mixin.server;

import com.mojang.authlib.GameProfile;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.ModPose;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_onSneak extends PlayerEntity implements Configurable, Poseable {

    @Unique
    private boolean lastSneaking;
    @Unique
    private int ticksLeftToDoubleTapPose;

    //? if >=1.21.6 {
    private ServerPlayerEntityMixin_onSneak(World world, GameProfile profile) {
        super(world, profile);
    }
    //?} else {
    /*private ServerPlayerEntityMixin_onSneak(World world, net.minecraft.util.math.BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
    *///?}

    @Inject(method = "playerTick", at = @At("TAIL"))
    private void sneakTick(CallbackInfo ci) {
        // TODO: better riders dismounting
        if (this.isSneaking() && this.hasPassengers()) {
            this.removeAllPassengers();
            return;
        }

        // handled by client
        if (this.fsit$hasConfig()) {
            return;
        }

        final ConfigData config = FSitMod.getInstance().getConfig();
        if (!config.onSneak().sitting() && !config.onSneak().crawling()) {
            return;
        } else if (this.getPitch() <= config.onSneak().minPitch()) {
            return;
        }

        if (this.ticksLeftToDoubleTapPose > 0) {
            this.ticksLeftToDoubleTapPose--;
        }

        if (this.shouldStopPosing()) {
            this.ticksLeftToDoubleTapPose = 0;
        } else if (this.lastSneaking && !this.isSneaking()) {
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
