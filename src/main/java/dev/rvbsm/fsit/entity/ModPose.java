//~ create_index_to_value_function

package dev.rvbsm.fsit.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public enum ModPose {
    STANDING,
    @Deprecated _SNEAKING,
    SITTING,
    CRAWLING,
    LAYING,
    SPINNING,
    ;

    private static final IntFunction<ModPose> ID_MAPPER = ByIdMap.continuous(
        ModPose::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO
    );
    public static final StreamCodec<ByteBuf, ModPose> PACKET_CODEC = ByteBufCodecs.idMapper(ID_MAPPER, ModPose::ordinal);
}
