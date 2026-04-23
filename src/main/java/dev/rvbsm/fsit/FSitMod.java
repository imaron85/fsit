package dev.rvbsm.fsit;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.api.FSitClientConfig;
import dev.rvbsm.fsit.api.event.FSitPoseCallback;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.ConfigLoader;
import dev.rvbsm.fsit.event.BlockListener;
import dev.rvbsm.fsit.event.CommandRegistrationListener;
import dev.rvbsm.fsit.event.FSitPoseListener;
import dev.rvbsm.fsit.event.PlayerDisconnectListener;
import dev.rvbsm.fsit.event.UseEntityListener;
import dev.rvbsm.fsit.network.protocol.game.ClientboundServerConfigPayload;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPlayerPosePayload;
import dev.rvbsm.fsit.network.protocol.game.ServerboundPlayerConfigPayload;
import dev.rvbsm.fsit.network.protocol.game.ServerboundPlayerPosePayload;
import dev.rvbsm.fsit.server.event.ServerPlayConnectionListener;
import dev.rvbsm.fsit.server.network.ServerModPacketListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class FSitMod implements ModInitializer {

    public static final String MOD_ID = "fsit";
    private static final String CONFIG_NAME = "fsit.yml";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static @Nullable FSitMod instance = null;
    private final AtomicReference<ConfigData> configRef = new AtomicReference<>(ConfigLoader.INIT_DATA);

    public FSitMod() {
        instance = this;
    }

    public static FSitMod getInstance() {
        return Objects.requireNonNull(instance, "too early");
    }

    public static Identifier withDefaultNamespace(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        var configLoader = this.reloadConfig();

        PayloadTypeRegistry.clientboundPlay().register(ClientboundPlayerPosePayload.TYPE, ClientboundPlayerPosePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundServerConfigPayload.TYPE, ClientboundServerConfigPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPlayerPosePayload.TYPE, ServerboundPlayerPosePayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPlayerConfigPayload.TYPE, ServerboundPlayerConfigPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundPlayerPosePayload.TYPE, ServerModPacketListener::handleChangePose);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundPlayerConfigPayload.TYPE, ServerModPacketListener::handleConfig);

        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionListener.INSTANCE);
        BlockEvents.USE_WITHOUT_ITEM.register(BlockListener.INSTANCE);
        UseEntityCallback.EVENT.register(UseEntityListener.INSTANCE);
        ServerPlayConnectionEvents.DISCONNECT.register(PlayerDisconnectListener.INSTANCE);

        FSitPoseCallback.EVENT.register(FSitPoseListener.INSTANCE);

        // commands depend on the config, but we don't want to block other mods from loading
        configLoader.thenRunAsync(
            () -> CommandRegistrationCallback.EVENT.register(CommandRegistrationListener.INSTANCE),
            Runnable::run
        );
    }

    public ConfigData getConfig() {
        return this.configRef.get();
    }

    public void setConfig(ConfigData config) {
        this.configRef.set(config);
    }

    /// @return `true` if configuration was reloaded; `false` if configuration exists but could not be reloaded.
    /// @apiNote Does not broadcast changes to the client.
    /// @see ServerModPacketListener#broadcastConfig
    public CompletableFuture<Boolean> reloadConfig() {
        var started = Instant.now();

        var configDir = FabricLoader.getInstance().getConfigDir();
        var configPath = configDir.resolve(CONFIG_NAME);
        return ConfigLoader.readConfig(configPath).handle((data, e) -> {
            LOGGER.debug("Configuration load took {} milliseconds", Duration.between(started, Instant.now()).toMillis());

            if (e == null) {
                this.setConfig(data);
                return true;
            }

            var didExist = Files.exists(configPath);
            var cause = e instanceof java.util.concurrent.CompletionException ? e.getCause() : e;

            if (didExist && cause instanceof ConfigLoader.VersionMismatchException vme) {
                // Schema changed in a breaking way — back up the old file and write fresh defaults.
                var backup = configPath.resolveSibling(CONFIG_NAME + "_v" + vme.found + "_bak");
                LOGGER.warn("Config version {} is not {}, backing up to {} and regenerating",
                    vme.found, ConfigData.CURRENT_VERSION, backup.getFileName());
                try {
                    Files.move(configPath, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException moveErr) {
                    LOGGER.warn("Failed to back up old configuration", moveErr);
                }
                ConfigLoader.writeConfig(this.getConfig(), configPath).exceptionally(fex -> {
                    LOGGER.warn("Failed to write configuration file {}", configPath, fex);
                    //noinspection DataFlowIssue
                    return null;
                });
                return true;
            }

            if (didExist) {
                LOGGER.warn("Failed to reload configuration file (keeping it unchanged, using defaults in memory)", e);
                return false;
            }

            ConfigLoader.writeConfig(this.getConfig(), configPath).exceptionally(fex -> {
                LOGGER.warn("Failed to write configuration file {}", configPath, fex);
                //noinspection DataFlowIssue
                return null;
            });

            return true;
        });
    }

    public void saveConfig() {
        var configDir = FabricLoader.getInstance().getConfigDir();
        var configPath = configDir.resolve(CONFIG_NAME);
        ConfigLoader.writeConfig(this.configRef.get(), configPath).exceptionally(e -> {
            LOGGER.warn("Failed to save configuration file", e);
            //noinspection DataFlowIssue
            return null;
        });
    }

    public boolean isClient(ServerPlayer player) {
        return FSitClientConfig.get(player) != null;
    }

    public boolean isUsingClient(ServerPlayer player) {
        var config = FSitClientConfig.get(player);
        return !this.getConfig().useServer() && (config != null && !config.useServer());
    }
}
