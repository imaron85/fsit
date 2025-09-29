package dev.rvbsm.fsit.client.compat.yacl3.api.controller;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.rvbsm.fsit.client.compat.yacl3.impl.controller.KeyBindingControllerBuilderImpl;
import net.minecraft.client.util.InputUtil;

public interface KeyBindingControllerBuilder extends ControllerBuilder<InputUtil.Key> {

    static KeyBindingControllerBuilder create(Option<InputUtil.Key> option) {
        return new KeyBindingControllerBuilderImpl(option);
    }
}
