package dev.rvbsm.fsit.mixin.common.access;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccess {

    @Accessor
    void setVehicle(Entity vehicle);

    @Invoker
    void callAddPassenger(Entity passenger);
}
