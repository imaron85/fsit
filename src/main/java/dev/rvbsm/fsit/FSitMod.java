package dev.rvbsm.fsit;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.ConfigLoader;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPoseUpdatePayload;
import dev.rvbsm.fsit.network.protocol.game.ServerboundConfigJsonUpdatePayload;
import dev.rvbsm.fsit.network.protocol.game.ServerboundPoseRequestPayload;
import dev.rvbsm.fsit.server.command.FSitCommand;
import dev.rvbsm.fsit.server.command.PoseCommand;
import dev.rvbsm.fsit.server.network.ServerModNetworkListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public final class FSitMod implements ModInitializer {

    public static final String MOD_ID = "fsit";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static @MonotonicNonNull FSitMod instance = null;
    private final AtomicReference<ConfigData> configRef = new AtomicReference<>(ConfigData.DEFAULT);

    public FSitMod() {
        instance = this;
    }

    public static FSitMod getInstance() {
        return Objects.requireNonNull(instance, "too early");
    }

    @Override
    public void onInitialize() {
        final var configLoader = this.loadConfig();

        PayloadTypeRegistry.serverboundPlay().register(ServerboundConfigJsonUpdatePayload.TYPE, ServerboundConfigJsonUpdatePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundConfigJsonUpdatePayload.TYPE, ServerModNetworkListener::handleConfigJsonUpdate);

        PayloadTypeRegistry.clientboundPlay().register(ClientboundPoseUpdatePayload.ID, ClientboundPoseUpdatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPoseRequestPayload.ID, ServerboundPoseRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPoseRequestPayload.ID_LEGACY, ServerboundPoseRequestPayload.CODEC_LEGACY);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundPoseRequestPayload.ID, ServerModNetworkListener::handleRequestPose);

        try {
            configLoader.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.warn("Error when loading configuration", e);
        }

        CommandRegistrationCallback.EVENT.register(((dispatcher, _, _) -> {
            if (this.getConfig().commands().root()) {
                FSitCommand.register(dispatcher);
            }

            if (this.getConfig().commands().sit()) {
                PoseCommand.SIT.register(dispatcher);
            }

            if (this.getConfig().commands().crawl()) {
                PoseCommand.CRAWL.register(dispatcher);
            }
        }));
    }

    public ConfigData getConfig() {
        return this.configRef.get();
    }

    public CompletableFuture<Void> setConfig(final ConfigData config) {
        this.configRef.set(config);
        return ConfigLoader.saveYaml(config);
    }

    public CompletableFuture<Void> loadConfig() {
        return ConfigLoader.loadYaml().thenAccept(this::setConfig);
    }
}
