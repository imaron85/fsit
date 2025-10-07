package dev.rvbsm.fsit.client;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.client.network.ClientModNetworkHandler;
import dev.rvbsm.fsit.client.network.RidingInteractionsManager;
import dev.rvbsm.fsit.client.option.HybridKeyBinding;
import dev.rvbsm.fsit.client.option.ModOptions;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.ModPose;
import dev.rvbsm.fsit.network.payload.s2c.PoseUpdateS2CPayload;
import dev.rvbsm.fsit.network.payload.s2c.RidingRequestS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class FSitModClient implements ClientModInitializer {

    private static FSitModClient instance;
    private final ModOptions options;
    private final RidingInteractionsManager restrictionsManager;

    public FSitModClient() {
        instance = this;

        this.options = new ModOptions(MinecraftClient.getInstance());
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

        KeyBindingHelper.registerKeyBinding(this.options.sitKey);
        KeyBindingHelper.registerKeyBinding(this.options.crawlKey);
        ClientTickEvents.END_CLIENT_TICK.register(this::handleInputEvents);
    }

    private void handleInputEvents(MinecraftClient client) {
        if (client.player != null && client.getOverlay() == null && client.currentScreen == null) {
            if (this.getConfig().sitting().behaviour().hasGravity() || client.player.isOnGround()) {
                if (this.options.sitKey.isPressed()) {
                    ((Poseable) client.player).fsit$setPose(ModPose.SITTING);
                } else if (this.options.crawlKey.isPressed()) {
                    ((Poseable) client.player).fsit$setPose(ModPose.CRAWLING);
                } else {
                    ((Poseable) client.player).fsit$setPose(ModPose.STANDING);
                }
            }

            HybridKeyBinding.tick();
        }
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

    public ModOptions getOptions() {
        return this.options;
    }
}
