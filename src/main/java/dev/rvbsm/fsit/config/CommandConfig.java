package dev.rvbsm.fsit.config;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.function.Function;

public record CommandConfig(String root, String sit, String crawl, String lay) {

    public static final CommandConfig DEFAULT = new CommandConfig("fsit", "sit", "crawl", "lay");

    /// Field codec for a command entry. Accepts:
    /// - `true`  — enabled with the default name,
    /// - `false` — disabled (stored as empty string),
    /// - a string — a custom command name,
    /// - missing — same as `true`.
    /// On save: default name is written as `true`, empty as `false`, otherwise as the custom string.
    private static MapCodec<String> commandField(String key, String defaultName) {
        Function<Either<Boolean, String>, DataResult<String>> decode = either -> {
            if (either.left().isPresent()) {
                boolean enabled = either.left().get();
                return DataResult.success(enabled ? defaultName : "");
            }
            return DataResult.success(either.right().orElseThrow());
        };
        Function<String, DataResult<Either<Boolean, String>>> encode = name -> {
            Either<Boolean, String> value;
            if (name.isEmpty()) {
                value = Either.left(false);
            } else if (name.equals(defaultName)) {
                value = Either.left(true);
            } else {
                value = Either.right(name);
            }
            return DataResult.success(value);
        };
        Codec<String> codec = Codec.either(Codec.BOOL, Codec.STRING).flatXmap(decode, encode);
        return codec.optionalFieldOf(key).xmap(opt -> opt.orElse(defaultName), Optional::of);
    }

    public static final Codec<CommandConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        commandField("root", "fsit").forGetter(CommandConfig::root),
        commandField("sit", "sit").forGetter(CommandConfig::sit),
        commandField("crawl", "crawl").forGetter(CommandConfig::crawl),
        commandField("lay", "lay").forGetter(CommandConfig::lay)
    ).apply(instance, CommandConfig::new));
}
