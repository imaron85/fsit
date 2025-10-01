package dev.rvbsm.fsit;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.ConfigLoader;
import dev.rvbsm.fsit.network.payload.c2s.ConfigUpdateJsonC2SPayload;
import dev.rvbsm.fsit.network.payload.c2s.PoseRequestC2SPayload;
import dev.rvbsm.fsit.network.payload.s2c.RidingRequestS2CPayload;
import dev.rvbsm.fsit.network.payload.c2s.RidingResponseC2SPayload;
import dev.rvbsm.fsit.server.command.PoseCommand;
import dev.rvbsm.fsit.server.command.FSitCommand;
import dev.rvbsm.fsit.server.network.ServerModNetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class FSitMod implements ModInitializer {

    public static final String MOD_ID = "fsit";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static FSitMod instance = null;
    private ConfigData config = ConfigData.DEFAULT;

    public static FSitMod getInstance() {
        return Objects.requireNonNull(instance, "too early");
    }

    public FSitMod() {
        instance = this;
    }

    @Override
    public void onInitialize() {
        final CompletableFuture<Void> configLoader = loadConfig();

        PayloadTypeRegistry.playC2S().register(ConfigUpdateJsonC2SPayload.ID, ConfigUpdateJsonC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ConfigUpdateJsonC2SPayload.ID, ServerModNetworkHandler::onConfigUpdateJson);

        PayloadTypeRegistry.playC2S().register(PoseRequestC2SPayload.ID, PoseRequestC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PoseRequestC2SPayload.ID, ServerModNetworkHandler::onPoseRequest);

        PayloadTypeRegistry.playS2C().register(RidingRequestS2CPayload.ID, RidingRequestS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RidingResponseC2SPayload.ID, RidingResponseC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RidingResponseC2SPayload.ID, ServerModNetworkHandler::onRidingResponse);

        try {
            configLoader.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.warn("Error when loading configuration", e);
        }

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, env) -> {
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
        return this.config;
    }

    public CompletableFuture<Void> setConfig(ConfigData config) {
        this.config = config;
        return ConfigLoader.saveYaml(config);
    }

    public CompletableFuture<Void> loadConfig() {
        return ConfigLoader.loadYaml().thenAccept(this::setConfig);
    }
}
