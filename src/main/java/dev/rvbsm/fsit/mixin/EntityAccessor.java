package dev.rvbsm.fsit.mixin;

import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("vehicle")
    void setVehicle(@Nullable Entity vehicle);

    @Invoker("addPassenger")
    void callAddPassenger(Entity entity);
}
