package dev.rvbsm.fsit.network.protocol.game;

import dev.rvbsm.fsit.FSitMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundServerConfigPayload(boolean useServer) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = FSitMod.withDefaultNamespace("clientbound/server_config");
    public static final Type<ClientboundServerConfigPayload> TYPE = new Type<>(PAYLOAD_ID);
    public static final StreamCodec<ByteBuf, ClientboundServerConfigPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, ClientboundServerConfigPayload::useServer,
        ClientboundServerConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
