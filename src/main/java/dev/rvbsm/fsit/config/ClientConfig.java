package dev.rvbsm.fsit.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientConfig(boolean useServer, SeatTrait seatTrait) {

    public static final StreamCodec<ByteBuf, ClientConfig> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, ClientConfig::useServer,
        SeatTrait.STREAM_CODEC, ClientConfig::seatTrait,
        ClientConfig::new
    );
}
