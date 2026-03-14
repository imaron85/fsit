package dev.rvbsm.fsit.client.compat.yacl.impl.controller;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import dev.rvbsm.fsit.client.compat.yacl.api.controller.KeyMappingControllerBuilder;
import dev.rvbsm.fsit.client.compat.yacl.gui.controllers.KeyMappingController;

public class KeyMappingControllerBuilderImpl extends AbstractControllerBuilderImpl<InputConstants.Key> implements KeyMappingControllerBuilder {

    public KeyMappingControllerBuilderImpl(Option<InputConstants.Key> option) {
        super(option);
    }

    @Override
    public Controller<InputConstants.Key> build() {
        return new KeyMappingController(this.option);
    }
}
