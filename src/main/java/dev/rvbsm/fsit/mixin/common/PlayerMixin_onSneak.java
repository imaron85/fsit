package dev.rvbsm.fsit.mixin.common;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Player.class)
public abstract class PlayerMixin_onSneak extends LivingEntityMixin_onSneak {

    protected PlayerMixin_onSneak(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    public abstract Abilities getAbilities();
}
