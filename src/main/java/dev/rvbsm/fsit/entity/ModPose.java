//~ create_index_to_value_function

package dev.rvbsm.fsit.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

import java.util.function.IntFunction;

public enum ModPose {
    STANDING,
    @Deprecated _SNEAKING,
    SITTING,
    CRAWLING,
    LAYING,
    SPINNING,
    ;

    private static final IntFunction<ModPose> ID_MAPPER = ValueLists.createIndexToValueFunction(
        ModPose::ordinal, values(), ValueLists.OutOfBoundsHandling.ZERO
    );
    public static final PacketCodec<ByteBuf, ModPose> PACKET_CODEC = PacketCodecs.indexed(ID_MAPPER, ModPose::ordinal);
}
