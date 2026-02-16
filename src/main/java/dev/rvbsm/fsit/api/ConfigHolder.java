package dev.rvbsm.fsit.api;

import dev.rvbsm.fsit.config.ConfigData;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public interface ConfigHolder {

    static void set(final ServerPlayer player, final ConfigData config) {
        ((ConfigHolder) player).fsit$setConfig(config);
    }

    static @Nullable ConfigData get(final ServerPlayer player) {
        return ((ConfigHolder) player).fsit$getConfig();
    }

    static ConfigData getOrServer(final ServerPlayer player) {
        return ((ConfigHolder) player).fsit$getConfigOrServer();
    }

    static boolean has(final ServerPlayer player) {
        return ((ConfigHolder) player).fsit$hasConfig();
    }

    void fsit$setConfig(final ConfigData config);

    @Nullable ConfigData fsit$getConfig();

    ConfigData fsit$getServerConfig();

    default boolean fsit$hasConfig() {
        return this.fsit$getConfig() != null;
    }

    default ConfigData fsit$getConfigOrServer() {
        final var playerConfig = this.fsit$getConfig();
        final var serverConfig = this.fsit$getServerConfig();
        if (playerConfig == null || serverConfig.useServer() || playerConfig.useServer()) {
            return serverConfig;
        }

        return playerConfig;
    }
}
