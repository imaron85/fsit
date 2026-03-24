package dev.rvbsm.fsit.api;

import dev.rvbsm.fsit.config.PlayerConfig;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public interface FSitPlayerConfig {

    static void set(ServerPlayer player, PlayerConfig config) {
        ((FSitPlayerConfig) player).fsit$setConfig(config);
    }

    static @Nullable PlayerConfig get(ServerPlayer player) {
        return ((FSitPlayerConfig) player).fsit$getConfig();
    }

    void fsit$setConfig(PlayerConfig config);

    @Nullable PlayerConfig fsit$getConfig();
}
