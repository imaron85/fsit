package dev.rvbsm.fsit.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    public abstract boolean isFallFlying();

    @Shadow
    public abstract Optional<BlockPos> getSleepingPos();

    protected LivingEntityMixin() {
        //noinspection DataFlowIssue
        super(null, null);
    }

    @ModifyReturnValue(method = "checkBedExists", at = @At("RETURN"))
    protected boolean checkIsLaying(boolean bedExists) {
        return bedExists;
    }

    // keep yBodyRot where we set it — otherwise tickHeadTurn drags it back to yaw
    @Inject(method = "tickHeadTurn", at = @At("HEAD"), cancellable = true)
    private void fsit$freezeBodyRotWhenLaying(float yBodyRotT, CallbackInfo ci) {
        //noinspection ConstantValue — at runtime `this` may be Player (mixin target is LivingEntity)
        if ((Object) this instanceof Player p && FSitPoseHolder.get(p) == FSitPose.LAYING) {
            ci.cancel();
        }
    }

    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void fsit$inWallForSittingPlayer(CallbackInfoReturnable<Boolean> cir) {
        var vehicle = this.getVehicle();
        if (vehicle == null || !vehicle.entityTags().contains(FSitMod.MOD_ID)) return;
        if (this.noPhysics) {
            cir.setReturnValue(false);
            return;
        }

        float checkWidth = this.getBbWidth() * 0.8f;
        boolean inWall = fsit$probeSuffocation(this.getEyePosition(), checkWidth)
            || fsit$probeSuffocation(this.position().add(0.0, 0.7, 0.0), checkWidth);
        cir.setReturnValue(inWall);
    }

    @Unique
    private boolean fsit$probeSuffocation(Vec3 center, float width) {
        AABB box = AABB.ofSize(center, width, 1.0e-6, width);
        return BlockPos.betweenClosedStream(box).anyMatch(pos -> {
            var state = this.level().getBlockState(pos);
            return !state.isAir()
                && state.isSuffocating(this.level(), pos)
                && Shapes.joinIsNotEmpty(
                    state.getCollisionShape(this.level(), pos).move(pos.getX(), pos.getY(), pos.getZ()),
                    Shapes.create(box),
                    BooleanOp.AND);
        });
    }
}
