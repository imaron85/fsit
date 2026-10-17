package dev.rvbsm.fsit.serialization;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class CommentCodec<A> extends MapCodec<A> {

    private final MapCodec<A> delegate;
    private final List<String> lines;

    public static <A> MapCodec<A> of(MapCodec<A> mapCodec, String... lines) {
        // snakeyaml uses "#" as a suffix, but we want "# "
        return new CommentCodec<>(mapCodec, Arrays.stream(lines).map(line -> " " + line).toList());
    }

    private CommentCodec(MapCodec<A> delegate, List<String> lines) {
        this.delegate = delegate;
        this.lines = lines;
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return this.delegate.keys(ops);
    }

    @Override
    public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
        return this.delegate.decode(ops, input);
    }

    @Override
    public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        var builder = this.delegate.encode(input, ops, prefix);
        if (prefix instanceof RecordCommentBuilder<T> commentsPrefix && !this.lines.isEmpty()) {
            commentsPrefix.appendComment(this.lines);
        }
        return builder;
    }

    @Override
    public String toString() {
        return "CommentCodec[" + this.delegate.toString() + ']';
    }
}
