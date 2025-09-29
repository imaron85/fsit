package dev.rvbsm.fsit.client.compat.yacl3.api.controller;

import dev.isxander.yacl3.api.Option;
import dev.rvbsm.fsit.client.compat.yacl3.impl.controller.BlockControllerBuilderImpl;
import net.minecraft.block.Block;

public interface BlockControllerBuilder extends RegistryControllerBuilder<Block, BlockControllerBuilder> {

    static BlockControllerBuilder create(Option<String> option) {
        return new BlockControllerBuilderImpl(option);
    }
}
