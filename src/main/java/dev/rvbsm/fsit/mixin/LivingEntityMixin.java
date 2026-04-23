package dev.rvbsm.fsit.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        if ((Object) this instanceof Player p && FSitPoseHolder.get(p) == FSitPose.LAYING) {
            ci.cancel();
        }
    }
}
