package dev.rvbsm.fsit.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.function.Function;

public final class FSitCodecs {

    private FSitCodecs() {}

    // like optionalFieldOf but always emits the field on encode (self-documenting yaml)
    public static <A> MapCodec<A> defaultFieldOf(String name, Codec<A> codec, A defaultValue) {
        // keep it lenient?
        return Codec.optionalField(name, codec, true).xmap(o -> o.orElse(defaultValue), Optional::of);
    }

    public static <O, A> RecordCodecBuilder<O, A> config(
            String name, Codec<A> codec, A defaultValue, Function<O, A> getter, String... comments) {
        return CommentCodec.of(defaultFieldOf(name, codec, defaultValue), comments)
                .forGetter(getter);
    }
}
