package dev.rvbsm.fsit.mixin.client.access;

import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleOption.class)
public interface SimpleOptionAccess<T> {

    @Accessor
    Text getText();

    @Accessor
    T getDefaultValue();
}
