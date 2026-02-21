package dev.rvbsm.fsit.server.compat;

import net.fabricmc.loader.api.FabricLoader;

public enum ServerModCompat {

    PERMISSIONS_API("fabric-permissions-api-v0");

    private final boolean isLoaded;

    ServerModCompat(String id) {
        this.isLoaded = FabricLoader.getInstance().isModLoaded(id);
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }
}
