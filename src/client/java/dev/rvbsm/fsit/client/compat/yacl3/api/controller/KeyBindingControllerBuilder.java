package dev.rvbsm.fsit.client.compat.yacl3.api.controller;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.rvbsm.fsit.client.compat.yacl3.impl.controller.KeyBindingControllerBuilderImpl;

public interface KeyBindingControllerBuilder extends ControllerBuilder<InputConstants.Key> {

    static KeyBindingControllerBuilder create(Option<InputConstants.Key> option) {
        return new KeyBindingControllerBuilderImpl(option);
    }
}
