package dev.rvbsm.fsit.mixin.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_onSneak extends Entity {

    protected LivingEntityMixin_onSneak(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean isUsingItem();

    @Inject(method = "onTrackedDataSet", at = @At("TAIL"))
    protected void onTrackedDataSet(TrackedData<?> data, CallbackInfo ci) {
    }
}
