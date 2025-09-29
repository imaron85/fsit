package dev.rvbsm.fsit.client.compat.yacl3.impl.controller;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;
import dev.rvbsm.fsit.client.compat.yacl3.gui.controller.dropdown.BlockController;
import dev.rvbsm.fsit.client.compat.yacl3.api.controller.BlockControllerBuilder;
import net.minecraft.block.Block;

public final class BlockControllerBuilderImpl extends AbstractControllerBuilderImpl<String> implements BlockControllerBuilder {

    public BlockControllerBuilderImpl(Option<String> option) {
        super(option);
    }

    @Override
    public Controller<String> build() {
        return new BlockController(this.option);
    }
}
