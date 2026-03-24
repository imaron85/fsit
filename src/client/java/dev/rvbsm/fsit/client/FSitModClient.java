package dev.rvbsm.fsit.client;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.client.network.ClientModPacketListener;
import dev.rvbsm.fsit.client.option.ModOptions;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.network.protocol.game.ClientboundConfigurationUseServerPayload;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPlayerPosePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class FSitModClient implements ClientModInitializer {

    private static @Nullable FSitModClient instance = null;
    private final ModOptions options;
    private boolean enabled;

    @SuppressWarnings("this-escape")
    public FSitModClient() {
        instance = this;
        this.options = new ModOptions(Minecraft.getInstance(), this);
    }

    public static FSitModClient getInstance() {
        return Objects.requireNonNull(instance, "too early");
    }

    @Override
    public void onInitializeClient() {
        this.options.load();

        ClientPlayNetworking.registerGlobalReceiver(ClientboundPlayerPosePayload.TYPE, ClientModPacketListener::handleChangePose);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundConfigurationUseServerPayload.TYPE, ClientModPacketListener::handleConfigurationUseServer);

        // hashset lookups are cheap, but booleans are even cheaper
        ClientPlayConnectionEvents.JOIN.register((_, _, _) -> this.enabled = ClientModPacketListener.isConnected());
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> this.enabled = false);

        KeyMappingHelper.registerKeyMapping(this.options.sitKey);
        KeyMappingHelper.registerKeyMapping(this.options.crawlKey);
    }

    public ConfigData getConfig() {
        return FSitMod.getInstance().getConfig();
    }

    public void setConfig(ConfigData config) {
        FSitMod.getInstance().setConfig(config);
    }

    public void saveConfig() {
        FSitMod.getInstance().saveConfig();
    }

    public ModOptions getOptions() {
        return this.options;
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}
