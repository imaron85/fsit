package dev.rvbsm.fsit.serialization;

import com.mojang.serialization.RecordBuilder;

import java.util.List;

public interface RecordCommentBuilder<T> extends RecordBuilder<T> {

    RecordCommentBuilder<T> appendComment(List<String> lines);
}
