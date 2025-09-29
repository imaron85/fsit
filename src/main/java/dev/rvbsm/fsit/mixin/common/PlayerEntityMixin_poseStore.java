package dev.rvbsm.fsit.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.entity.ModPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin_poseStore extends LivingEntity implements Poseable {

    @Unique
    private ModPose currentPose = ModPose.STANDING;

    protected PlayerEntityMixin_poseStore(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method = "isSwimming", at = @At("RETURN"))
    private boolean isCrawling(boolean isSwimming) {
        return isSwimming && this.fsit$getPose() == ModPose.CRAWLING;
    }

    @Override
    public void fsit$setPose(ModPose pose, @Nullable Vec3d pos) {
        final var prevPose = this.currentPose;
        this.currentPose = pose;

        if (this.currentPose != prevPose) {
            this.onPoseUpdate(pose, pos != null ? pos : this.getEntityPos());
        }
    }

    @Override
    public ModPose fsit$getPose() {
        return this.currentPose;
    }

    @Unique
    protected void onPoseUpdate(ModPose pose, Vec3d pos) {
    }
}
