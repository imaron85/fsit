package dev.rvbsm.fsit.client.compat;

import net.fabricmc.loader.api.FabricLoader;

public enum ClientModCompat {
    ;

    private final boolean isLoaded;

    ClientModCompat(final String id) {
        this.isLoaded = FabricLoader.getInstance().isModLoaded(id);
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }
}
