package dev.rvbsm.fsit.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor
    void setVehicle(final Entity vehicle);

    @Invoker
    void callAddPassenger(final Entity passenger);
}
