package dev.rvbsm.fsit.api.access;

import dev.rvbsm.fsit.config.ConfigData;
import org.jetbrains.annotations.Nullable;

public interface Configurable {

    void fsit$setConfig(ConfigData config);

    @Nullable ConfigData fsit$getConfig();

    boolean fsit$hasConfig();

    ConfigData fsit$configOrDefault();
}
