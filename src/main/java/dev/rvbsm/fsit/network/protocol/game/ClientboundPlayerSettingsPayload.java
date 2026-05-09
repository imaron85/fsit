package dev.rvbsm.fsit.network.protocol.game;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.PlayerSettings;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundPlayerSettingsPayload(PlayerSettings settings) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = FSitMod.withDefaultNamespace("clientbound/player_settings");
    public static final Type<ClientboundPlayerSettingsPayload> TYPE = new Type<>(PAYLOAD_ID);
    public static final StreamCodec<ByteBuf, ClientboundPlayerSettingsPayload> STREAM_CODEC = StreamCodec.composite(
        PlayerSettings.STREAM_CODEC, ClientboundPlayerSettingsPayload::settings,
        ClientboundPlayerSettingsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
