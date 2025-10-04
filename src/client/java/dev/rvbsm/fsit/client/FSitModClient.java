package dev.rvbsm.fsit.client;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.client.network.ClientModNetworkHandler;
import dev.rvbsm.fsit.client.network.RidingInteractionsManager;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.network.payload.s2c.PoseUpdateS2CPayload;
import dev.rvbsm.fsit.network.payload.s2c.RidingRequestS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class FSitModClient implements ClientModInitializer {

    private static FSitModClient instance;
    private final RidingInteractionsManager restrictionsManager;

    public FSitModClient() {
        instance = this;

        this.restrictionsManager = new RidingInteractionsManager(MinecraftClient.getInstance());
    }

    public static FSitModClient getInstance() {
        return Objects.requireNonNull(instance, "too early");
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PoseUpdateS2CPayload.ID, ClientModNetworkHandler::onPoseUpdate);
        ClientPlayNetworking.registerGlobalReceiver(RidingRequestS2CPayload.ID, ClientModNetworkHandler::onRidingRequest);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientModNetworkHandler.sendConfig(this.getConfig());
        });
    }

    public ConfigData getConfig() {
        return FSitMod.getInstance().getConfig();
    }

    @CanIgnoreReturnValue
    public CompletableFuture<Void> setConfig(ConfigData config) {
        return FSitMod.getInstance().setConfig(config).thenRun(() -> ClientModNetworkHandler.sendConfig(config));
    }

    public RidingInteractionsManager getRestrictionsManager() {
        return this.restrictionsManager;
    }
}
