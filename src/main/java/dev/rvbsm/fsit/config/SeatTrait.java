package dev.rvbsm.fsit.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum SeatTrait implements StringRepresentable {
    STATIC("static"),
    ATTACHED("attached"),
    MOVEABLE("moveable");

    public static final EnumCodec<SeatTrait> CODEC = StringRepresentable.fromEnum(SeatTrait::values);
    private final String name;

    SeatTrait(String name) {
        this.name = name;
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
