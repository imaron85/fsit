package dev.rvbsm.fsit.api;

import dev.rvbsm.fsit.config.ClientConfig;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public interface FSitClientConfig {

    static void set(ServerPlayer player, ClientConfig config) {
        ((FSitClientConfig) player).fsit$setConfig(config);
    }

    static @Nullable ClientConfig get(ServerPlayer player) {
        return ((FSitClientConfig) player).fsit$getConfig();
    }

    void fsit$setConfig(ClientConfig config);

    @Nullable ClientConfig fsit$getConfig();
}
