package dev.rvbsm.fsit.network.protocol.game;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.PlayerConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundPlayerConfigPayload(PlayerConfig config) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = FSitMod.withDefaultNamespace("serverbound/player_config");
    public static final Type<ServerboundPlayerConfigPayload> TYPE = new Type<>(PAYLOAD_ID);
    public static final StreamCodec<ByteBuf, ServerboundPlayerConfigPayload> STREAM_CODEC = StreamCodec.composite(
        PlayerConfig.STREAM_CODEC, ServerboundPlayerConfigPayload::config,
        ServerboundPlayerConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
