package dev.rvbsm.fsit.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

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
}
