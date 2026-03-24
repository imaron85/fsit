package dev.rvbsm.fsit.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PlayerConfig(boolean useServer, SeatTrait seatTrait) {

    public static final StreamCodec<ByteBuf, PlayerConfig> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, PlayerConfig::useServer,
        SeatTrait.STREAM_CODEC, PlayerConfig::seatTrait,
        PlayerConfig::new
    );
}
