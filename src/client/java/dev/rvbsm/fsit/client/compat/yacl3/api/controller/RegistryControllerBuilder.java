package dev.rvbsm.fsit.client.compat.yacl3.api.controller;

import dev.isxander.yacl3.api.controller.ControllerBuilder;
import net.minecraft.world.level.ItemLike;

public interface RegistryControllerBuilder<T extends ItemLike, B extends RegistryControllerBuilder<T, B>> extends ControllerBuilder<String> {
}
