package dev.rvbsm.fsit.client;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.client.network.ClientModNetworkListener;
import dev.rvbsm.fsit.client.network.RidingInteractionsManager;
import dev.rvbsm.fsit.client.option.HybridKeyMapping;
import dev.rvbsm.fsit.client.option.ModOptions;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPoseUpdatePayload;
import dev.rvbsm.fsit.network.protocol.game.ClientboundRidingRequestPayload;
import dev.rvbsm.fsit.network.protocol.game.ServerboundConfigJsonUpdatePayload;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class FSitModClient implements ClientModInitializer {

    private static @MonotonicNonNull FSitModClient instance = null;
    private final ModOptions options;
    private final RidingInteractionsManager restrictionsManager;

    public FSitModClient() {
        instance = this;

        this.options = new ModOptions(Minecraft.getInstance());
        this.restrictionsManager = new RidingInteractionsManager(Minecraft.getInstance());
    }

    public static FSitModClient getInstance() {
        return Objects.requireNonNull(instance, "too early");
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundPoseUpdatePayload.ID, ClientModNetworkListener::onPoseUpdate);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundRidingRequestPayload.ID, ClientModNetworkListener::onRidingRequest);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientModNetworkListener.sendConfig(this.getConfig());
        });

        KeyMappingHelper.registerKeyMapping(this.options.sitKey);
        KeyMappingHelper.registerKeyMapping(this.options.crawlKey);
        ClientTickEvents.END_CLIENT_TICK.register(this::handleKeybinds);
    }

    private void handleKeybinds(final Minecraft client) {
        if (client.player != null && client.getOverlay() == null && client.screen == null) {
            if (this.getConfig().sitting().behaviour().hasGravity() || client.player.onGround()) {
                if (this.options.sitKey.isDown()) {
                    ((Poseable) client.player).fsit$setPose(ModPose.SITTING);
                } else if (this.options.crawlKey.isDown()) {
                    ((Poseable) client.player).fsit$setPose(ModPose.CRAWLING);
                } else {
                    ((Poseable) client.player).fsit$setPose(ModPose.STANDING);
                }
            }

            HybridKeyMapping.tick();
        }
    }

    public ConfigData getConfig() {
        return FSitMod.getInstance().getConfig();
    }

    @CanIgnoreReturnValue
    public CompletableFuture<Void> setConfig(final ConfigData config) {
        return FSitMod.getInstance().setConfig(config).thenRun(() -> ClientModNetworkListener.sendConfig(config));
    }

    public RidingInteractionsManager getRestrictionsManager() {
        return this.restrictionsManager;
    }

    public ModOptions getOptions() {
        return this.options;
    }
}
