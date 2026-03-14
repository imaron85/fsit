package dev.rvbsm.fsit.client.compat;

import net.fabricmc.loader.api.FabricLoader;

public enum ClientModCompat {

    YACL("yet_another_config_lib_v3");

    private final boolean isLoaded;

    ClientModCompat(String id) {
        this.isLoaded = FabricLoader.getInstance().isModLoaded(id);
    }

    public boolean isLoaded() {
        return isLoaded;
    }
}
