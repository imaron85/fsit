package dev.rvbsm.fsit.client.compat.yacl3.api.controller;

import dev.isxander.yacl3.api.controller.ControllerBuilder;
import net.minecraft.item.ItemConvertible;

public interface RegistryControllerBuilder<T extends ItemConvertible, B extends RegistryControllerBuilder<T, B>>  extends ControllerBuilder<String> {
}
