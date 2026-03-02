package dev.rvbsm.fsit;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.api.event.ChangePoseCallback;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.ConfigLoader;
import dev.rvbsm.fsit.event.BlockListener;
import dev.rvbsm.fsit.event.CommandRegistrationListener;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPlayerPosePayload;
import dev.rvbsm.fsit.network.protocol.game.ServerboundPlayerPosePayload;
import dev.rvbsm.fsit.server.event.ServerChangePoseListener;
import dev.rvbsm.fsit.server.event.ServerPlayConnectionListener;
import dev.rvbsm.fsit.server.network.ServerModPacketListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class FSitMod implements ModInitializer {

    public static final String MOD_ID = "fsit";
    private static final String CONFIG_NAME = "fsit.yml";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static @Nullable FSitMod instance = null;
    private final AtomicReference<ConfigData> configRef = new AtomicReference<>(ConfigLoader.INIT_DATA);
    private final Set<UUID> moddedClients = new HashSet<>();

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
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPlayerPosePayload.TYPE, ServerboundPlayerPosePayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundPlayerPosePayload.TYPE, ServerModPacketListener::handleChangePose);

        BlockEvents.USE_WITHOUT_ITEM.register(BlockListener.INSTANCE);

        ChangePoseCallback.EVENT.register(ServerChangePoseListener.INSTANCE);
        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionListener.INSTANCE);
        ServerPlayConnectionEvents.DISCONNECT.register(ServerPlayConnectionListener.INSTANCE);

        // commands depend on the config, but we don't want to block other mods from loading
        configLoader.thenRunAsync(
            () -> CommandRegistrationCallback.EVENT.register(CommandRegistrationListener.INSTANCE),
            Runnable::run
        );
    }

    public ConfigData getConfig() {
        return this.configRef.get();
    }

    /**
     * @return `true` if configuration was reloaded; `false` if configuration exists but could not be reloaded
     */
    public CompletableFuture<Boolean> reloadConfig() {
        var started = Instant.now();

        var configDir = FabricLoader.getInstance().getConfigDir();
        var configPath = configDir.resolve(CONFIG_NAME);
        return ConfigLoader.readConfig(configPath).handle((c, e) -> {
            LOGGER.debug("Configuration load took {}", Duration.between(started, Instant.now()));

            if (e == null) {
                this.configRef.set(c);
                return true;
            }

            var didExist = Files.exists(configPath);
            if (didExist) {
                LOGGER.warn("Failed to reload configuration file", e);
            }

            try {
                var configTmp = Files.createTempFile(configDir, CONFIG_NAME, null);
                ConfigLoader.writeConfig(this.getConfig(), configTmp).exceptionally(fex -> {
                    LOGGER.warn("Failed to write configuration file {}", configTmp, fex);
                    return null;
                }).thenRun(() -> {
                    var configBak = configPath.resolveSibling(CONFIG_NAME + "_old");
                    Util.safeReplaceFile(configPath, configTmp, configBak);
                });
            } catch (IOException ex) {
                LOGGER.warn("Failed to back up configuration file. Overwriting {}", configPath, ex);

                ConfigLoader.writeConfig(this.getConfig(), configPath).exceptionally(fex -> {
                    LOGGER.warn("Failed to write configuration file {}", configPath, fex);
                    return null;
                });
            }

            return !didExist;
        });
    }

    public boolean handledByClient(UUID id) {
        return this.moddedClients.contains(id);
    }

    public void addClient(ServerPlayer player) {
        this.moddedClients.add(player.getUUID());
    }

    public void removeClient(ServerPlayer player) {
        this.moddedClients.remove(player.getUUID());
    }
}
