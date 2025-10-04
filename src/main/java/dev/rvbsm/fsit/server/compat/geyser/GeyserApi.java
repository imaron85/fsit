package dev.rvbsm.fsit.server.compat.geyser;

import dev.rvbsm.fsit.server.compat.ServerModCompat;

import java.util.UUID;

public final class GeyserApi {

    public static boolean isBedrockPlayer(final UUID uuid) {
        if (ServerModCompat.GEYSER.isLoaded()) {
            return org.geysermc.geyser.api.GeyserApi.api().isBedrockPlayer(uuid);
        }

        return false;
    }
}
