package dev.rvbsm.fsit.world.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum ModPose implements StringRepresentable {
    STANDING(0, "standing"),
    SITTING(1, "sitting"),
    CRAWLING(2, "crawling"),
    LAYING(3, "laying"),
    SPINNING(4, "spinning");

    private static final IntFunction<ModPose> BY_ID = ByIdMap.continuous(ModPose::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<ModPose> CODEC = StringRepresentable.fromEnum(ModPose::values);
    public static final StreamCodec<ByteBuf, ModPose> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ModPose::id);
    private final int id;
    private final String name;

    ModPose(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
