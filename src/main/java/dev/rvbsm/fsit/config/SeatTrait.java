package dev.rvbsm.fsit.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum SeatTrait implements StringRepresentable {
    STATIC(0, "static"),
    ATTACHED(1, "attached"),
    MOVEABLE(2, "moveable");

    public static final EnumCodec<SeatTrait> CODEC = StringRepresentable.fromEnum(SeatTrait::values);
    private static final IntFunction<SeatTrait> BY_ID = ByIdMap.continuous(SeatTrait::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, SeatTrait> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, SeatTrait::id);

    private final int id;
    private final String name;

    SeatTrait(int id, String name) {
        this.id = id;
        this.name = name;
    }

    private int id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean canMove() {
        return this == MOVEABLE;
    }

    public boolean canStick() {
        return this == ATTACHED || this == MOVEABLE;
    }

    public boolean needsGround() {
        return this == ATTACHED;
    }
}
