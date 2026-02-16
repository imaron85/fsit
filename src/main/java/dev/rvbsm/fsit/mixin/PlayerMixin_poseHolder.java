package dev.rvbsm.fsit.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.rvbsm.fsit.api.PoseHolder;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin_poseHolder extends LivingEntity implements PoseHolder {

    @Unique
    private ModPose currentPose = ModPose.STANDING;

    protected PlayerMixin_poseHolder(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
        method = "getDesiredPose",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;isSwimming()Z"))
    private boolean isCrawling(final boolean isSwimming) {
        return isSwimming || this.fsit$getPose() == ModPose.CRAWLING;
    }

    @Override
    public void fsit$setPose(final ModPose pose, final @Nullable Vec3 pos) {
        final var prevPose = this.currentPose;
        this.currentPose = pose;

        if (this.currentPose != prevPose) {
            this.onPoseUpdate(pose, pos != null ? pos : this.position());
        }
    }

    @Override
    public ModPose fsit$getPose() {
        return this.currentPose;
    }

    @Unique
    protected void onPoseUpdate(final ModPose pose, final Vec3 pos) {
    }
}
