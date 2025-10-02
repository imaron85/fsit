package dev.rvbsm.fsit.mixin.common;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin_onSneak extends LivingEntityMixin_onSneak {

    protected PlayerEntityMixin_onSneak(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract PlayerAbilities getAbilities();
}
