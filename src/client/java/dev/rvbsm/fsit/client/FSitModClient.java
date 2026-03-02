package dev.rvbsm.fsit.client;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.event.ChangePoseCallback;
import dev.rvbsm.fsit.client.event.ClientChangePoseListener;
import dev.rvbsm.fsit.client.network.ClientModPacketListener;
import dev.rvbsm.fsit.client.option.ModOptions;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.network.protocol.game.ClientboundPlayerPosePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class FSitModClient implements ClientModInitializer {

    private static @Nullable FSitModClient instance = null;
    private final ModOptions options;

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

        ChangePoseCallback.EVENT.register(ClientChangePoseListener.INSTANCE);

        KeyMappingHelper.registerKeyMapping(this.options.sitKey);
        KeyMappingHelper.registerKeyMapping(this.options.crawlKey);
    }


    public ConfigData getConfig() {
        return FSitMod.getInstance().getConfig();
    }

    public ModOptions getOptions() {
        return this.options;
    }
}
