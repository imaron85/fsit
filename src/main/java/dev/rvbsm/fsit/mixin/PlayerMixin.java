package dev.rvbsm.fsit.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.api.event.FSitPoseCallback;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends AvatarMixin implements FSitPoseHolder {

    @Unique
    protected FSitPose fsit$pose = FSitPose.STANDING;

    @Shadow
    protected abstract boolean wantsToStopRiding();

    @Definition(id = "isSwimming", method = "Lnet/minecraft/world/entity/player/Player;isSwimming()Z")
    @Expression("this.isSwimming()")
    @ModifyExpressionValue(method = "getDesiredPose", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean isCrawling(boolean swimming) {
        return swimming || this.fsit$getPose() == FSitPose.CRAWLING;
    }

    @Definition(
            id = "canSleep",
            method = "Lnet/minecraft/world/attribute/BedRule;canSleep(Lnet/minecraft/world/level/Level;)Z")
    @Expression("?.canSleep(?)")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean canSleepOrLaying(boolean original) {
        return original || this.fsit$getPose() == FSitPose.LAYING;
    }

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void stopLaying(CallbackInfo ci) {
        this.getSleepingPos().ifPresent(bedPosition -> {
            this.fsit$setPose(FSitPose.STANDING, new FSitPose.Context.Positioned(bedPosition.getCenter()), true);
        });
    }

    @Override
    public void fsit$setPose(FSitPose pose, FSitPose.@Nullable Context ctx, boolean emitEvent) {
        if (this.fsit$pose != pose) {
            this.fsit$pose = pose;

            if (emitEvent) {
                var position = FSitPoseCallback.EVENT.invoker().createPose((Player) (Object) this, pose, ctx);
                if (position != null) {
                    this.fsit$sendPose(position);
                } else {
                    this.fsit$pose = FSitPose.STANDING;
                }
            }
        }
    }

    @Override
    public FSitPose fsit$getPose() {
        return this.fsit$pose;
    }

    @Unique
    protected void fsit$sendPose(Vec3 position) {}

    @Unique
    protected boolean fsit$canStartPosing() {
        return this.fsit$getPose() == FSitPose.STANDING
                && !this.isPassenger()
                && (!this.isFallFlying() || this.isUnderWater());
    }

    @Unique
    // fixme: triggered after dismount
    protected boolean fsit$canStartPosingBySneaking() {
        return this.fsit$canStartPosing()
                && !this.isShiftKeyDown()
                && this.onGround()
                && this.getXRot() > FSitMod.getInstance().getConfig().sneakMinimumPitch();
    }

    @Unique
    protected boolean fsit$canPlayerFitWithinBlocksAndEntitiesWhen(Pose newPose, Vec3 pos) {
        return this.level()
                .noCollision(
                        this, this.getDimensions(newPose).makeBoundingBox(pos).deflate(1.0e-7));
    }

    @Unique
    protected void fsit$startPosingBySneaking() {
        var desiredPose = FSitPose.SITTING;
        if (FSitMod.getInstance().getConfig().sneakCrawling()) {
            var offset = getCollisionHorizontalEscapeVector(
                    this.getBbWidth() * Mth.SQRT_OF_TWO, this.getBbWidth(), this.getYRot());
            var offsetPos = this.position().add(offset);

            var canFitStanding = this.fsit$canPlayerFitWithinBlocksAndEntitiesWhen(Pose.STANDING, offsetPos);
            var canFitSwimming = this.fsit$canPlayerFitWithinBlocksAndEntitiesWhen(Pose.SWIMMING, offsetPos);
            if (!canFitStanding && canFitSwimming) {
                desiredPose = FSitPose.CRAWLING;
            }
        }

        this.fsit$setPose(desiredPose, new FSitPose.Context.Positioned(this.position()), true);
    }
}
