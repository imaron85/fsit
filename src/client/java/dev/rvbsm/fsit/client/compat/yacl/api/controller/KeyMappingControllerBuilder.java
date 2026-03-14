package dev.rvbsm.fsit.client.compat.yacl.api.controller;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.rvbsm.fsit.client.compat.yacl.impl.controller.KeyMappingControllerBuilderImpl;

public interface KeyMappingControllerBuilder extends ControllerBuilder<InputConstants.Key> {

    static KeyMappingControllerBuilder create(Option<InputConstants.Key> option) {
        return new KeyMappingControllerBuilderImpl(option);
    }
}
