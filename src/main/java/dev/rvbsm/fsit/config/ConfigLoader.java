package dev.rvbsm.fsit.config;

import dev.rvbsm.fsit.yaml.Yaml;
import dev.rvbsm.fsit.yaml.YamlOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs.TagOrElementLocation;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ConfigLoader {

    public static final ConfigData INIT_DATA = new ConfigData(
        false,
        SeatTrait.MOVEABLE, false,
        true, List.of(
            new TagOrElementLocation(Identifier.parse("minecraft:slabs"), true),
            new TagOrElementLocation(Identifier.parse("minecraft:stairs"), true),
            new TagOrElementLocation(Identifier.parse("minecraft:logs"), true),
            new TagOrElementLocation(Identifier.parse("minecraft:fences"), true)
        ),
        45,
        10, 60, true,
        new CommandConfig("fsit", "sit", "crawl", "lay")
    );

    private ConfigLoader() {
    }

    public static CompletableFuture<ConfigData> readConfig(Path path) {
        return CompletableFuture.supplyAsync(
                () -> {
                    if (Files.notExists(path)) {
                        throw new IllegalArgumentException("File " + path + " does not exist");
                    }

                    try (var reader = Files.newBufferedReader(path)) {
                        var node = Yaml.compose(reader).orElse(null);
                        if (node == null) {
                            throw new IllegalArgumentException("Could not parse configuration file.");
                        }

                        var result = ConfigData.CODEC.parse(YamlOps.INSTANCE, node);
                        if (result.isError()) {
                            throw new IllegalArgumentException("Could not decode configuration: "
                                    + result.error().orElse(null));
                        }

                        return result.getOrThrow();
                    } catch (IOException e) {
                        throw new UncheckedIOException("Error occurred while trying to read configuration file", e);
                    }
                });
    }

    public static CompletableFuture<Void> writeConfig(ConfigData config, Path path) {
        return CompletableFuture.runAsync(
            () -> {
                try (var writer = Files.newBufferedWriter(path)) {
                    var result = ConfigData.CODEC.encodeStart(YamlOps.INSTANCE, config);
                    if (result.isError()) {
                        throw new IllegalArgumentException("Could not encode configuration: " + result.error().orElse(null));
                    }

                    Yaml.serialize(result.getOrThrow(), writer);
                } catch (IOException e) {
                    throw new UncheckedIOException("Error occurred while trying to write configuration file", e);
                }
            }
        );
    }
}
