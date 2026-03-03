package dev.rvbsm.fsit.world.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.function.IntFunction;

public enum FSitPose implements StringRepresentable {
    STANDING(0, "standing"),
    SITTING(1, "sitting"),
    CRAWLING(2, "crawling"),
    LAYING(3, "laying"),
    SPINNING(4, "spinning"),
    ;

    public static final Codec<FSitPose> CODEC = StringRepresentable.fromEnum(FSitPose::values);
    private static final IntFunction<FSitPose> BY_ID = ByIdMap.continuous(FSitPose::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, FSitPose> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FSitPose::id);

    private final int id;
    private final String name;

    FSitPose(int id, String name) {
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

    public sealed interface Context {

        Vec3 position();

        record Positioned(Vec3 position) implements Context {}
        record Attached(Player attachable, Vec3 position) implements Context {}
    }
}
