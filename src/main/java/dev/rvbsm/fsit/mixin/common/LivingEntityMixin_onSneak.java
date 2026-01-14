package dev.rvbsm.fsit.mixin.common;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_onSneak extends Entity {

    protected LivingEntityMixin_onSneak(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean isUsingItem();

    @Inject(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At("TAIL"))
    protected void onTrackedDataSet(final EntityDataAccessor<?> data, final CallbackInfo ci) {
    }
}
