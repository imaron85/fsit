package dev.rvbsm.fsit.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.PoseHolder;
import dev.rvbsm.fsit.api.event.ChangePoseCallback;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(Player.class)
public abstract class PlayerMixin extends Avatar implements PoseHolder {

    @Unique
    private ModPose modPose = ModPose.STANDING;

    protected PlayerMixin() {
        //noinspection DataFlowIssue
        super(null, null);
    }

    @Shadow
    protected abstract boolean wantsToStopRiding();

    @ModifyExpressionValue(method = "getDesiredPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSwimming()Z"))
    private boolean isCrawling(boolean isSwimming) {
        return isSwimming || this.fsit$getPose() == ModPose.CRAWLING;
    }

    @Override
    public void fsit$setPose(ModPose pose, @Nullable Vec3 pos) {
        if (this.modPose != pose) {
            var newPose = ChangePoseCallback.EVENT.invoker().changePose((Player) (Object) this, pose, Objects.requireNonNullElseGet(pos, this::position));
            this.modPose = newPose != null ? newPose : pose;
        }
    }

    @Override
    public ModPose fsit$getPose() {
        return this.modPose;
    }

    @Unique
    protected boolean canStartPosing() {
        return this.fsit$getPose() == ModPose.STANDING
            && !this.isPassenger()
            && (!this.isFallFlying() || this.isUnderWater());
    }

    @Unique
    // fixme: triggered on dismount
    protected boolean canStartPosingBySneaking() {
        return this.canStartPosing()
            && !this.isShiftKeyDown()
            && this.onGround()
            && this.getXRot() > FSitMod.getInstance().getConfig().sneakMinimumPitch();
    }

    @Unique
    protected boolean canPlayerFitWithinBlocksAndEntitiesWhen(Pose newPose, Vec3 pos) {
        return this.level()
            .noCollision(
                this, this.getDimensions(newPose).makeBoundingBox(pos).deflate(1.0e-7));
    }

    @Unique
    protected void startPosingBySneaking() {
        var desiredPose = ModPose.SITTING;
        if (FSitMod.getInstance().getConfig().sneakCrawlingDetection()) {
            var offset = getCollisionHorizontalEscapeVector(
                this.getBbWidth() * Mth.SQRT_OF_TWO, this.getBbWidth(), this.getYRot());
            var offsetPos = this.position().add(offset);

            var canFitStanding = this.canPlayerFitWithinBlocksAndEntitiesWhen(Pose.STANDING, offsetPos);
            var canFitSwimming = this.canPlayerFitWithinBlocksAndEntitiesWhen(Pose.SWIMMING, offsetPos);
            if (!canFitStanding && canFitSwimming) {
                desiredPose = ModPose.CRAWLING;
            }
        }

        this.fsit$setPose(desiredPose, this.position());
    }
}
