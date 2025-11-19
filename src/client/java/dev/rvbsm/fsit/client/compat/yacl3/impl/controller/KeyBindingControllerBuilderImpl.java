package dev.rvbsm.fsit.client.compat.yacl3.impl.controller;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import dev.rvbsm.fsit.client.compat.yacl3.api.controller.KeyBindingControllerBuilder;
import dev.rvbsm.fsit.client.compat.yacl3.gui.controller.KeyBindingController;

public class KeyBindingControllerBuilderImpl extends AbstractControllerBuilderImpl<InputConstants.Key> implements KeyBindingControllerBuilder {

    public KeyBindingControllerBuilderImpl(Option<InputConstants.Key> option) {
        super(option);
    }

    @Override
    public Controller<InputConstants.Key> build() {
        return new KeyBindingController(this.option);
    }
}
