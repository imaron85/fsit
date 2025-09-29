package dev.rvbsm.fsit.client.compat.yacl3.impl.controller;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import dev.rvbsm.fsit.client.compat.yacl3.api.controller.KeyBindingControllerBuilder;
import dev.rvbsm.fsit.client.compat.yacl3.gui.controller.KeyBindingController;
import net.minecraft.client.util.InputUtil;

public class KeyBindingControllerBuilderImpl extends AbstractControllerBuilderImpl<InputUtil.Key> implements KeyBindingControllerBuilder {

    public KeyBindingControllerBuilderImpl(Option<InputUtil.Key> option) {
        super(option);
    }

    @Override
    public Controller<InputUtil.Key> build() {
        return new KeyBindingController(this.option);
    }
}
