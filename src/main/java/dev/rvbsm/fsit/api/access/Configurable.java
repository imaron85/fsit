package dev.rvbsm.fsit.api.access;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.ConfigData;
import org.jetbrains.annotations.Nullable;

public interface Configurable {

    void fsit$setConfig(ConfigData config);

    @Nullable ConfigData fsit$getConfig();

    boolean fsit$hasConfig();

    default ConfigData fsit$configOrDefault() {
        final ConfigData config = this.fsit$getConfig();
        final ConfigData modConfig = FSitMod.getInstance().getConfig();
        if (config == null || modConfig.useServer() || config.useServer()) {
            return modConfig;
        }

        return config;
    }
}
