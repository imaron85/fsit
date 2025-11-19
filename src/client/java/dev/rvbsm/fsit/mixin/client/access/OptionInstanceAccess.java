package dev.rvbsm.fsit.mixin.client.access;

import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionInstance.class)
public interface OptionInstanceAccess<T> {

    @Accessor
    Component getCaption();

    @Accessor
    T getInitialValue();
}
