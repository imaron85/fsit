package dev.rvbsm.fsit.config;

import dev.rvbsm.fsit.yaml.Yaml;
import dev.rvbsm.fsit.yaml.YamlOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs.TagOrElementLocation;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.ScalarNode;

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

    public static final class VersionMismatchException extends RuntimeException {
        public final int found;
        public VersionMismatchException(int found) {
            super("Config version mismatch: found " + found + ", expected " + ConfigData.CURRENT_VERSION);
            this.found = found;
        }
    }

    private ConfigLoader() {
    }

    private static int peekVersion(org.snakeyaml.engine.v2.nodes.Node node) {
        if (!(node instanceof MappingNode map)) return -1;
        for (var tuple : map.getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode k
                && "version".equals(k.getValue())
                && tuple.getValueNode() instanceof ScalarNode v) {
                try {
                    return Integer.parseInt(v.getValue());
                } catch (NumberFormatException ignored) {
                    return -1;
                }
            }
        }
        return -1;
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

                        int version = peekVersion(node);
                        if (version != ConfigData.CURRENT_VERSION) {
                            throw new VersionMismatchException(version);
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

                    writer.write(annotate(Yaml.serialize(result.getOrThrow())));
                } catch (IOException e) {
                    throw new UncheckedIOException("Error occurred while trying to write configuration file", e);
                }
            }
        );
    }

    /// Injects documentation comments into the serialized YAML. Comments are attached to section headings
    /// so they travel with the relevant block and don't get visually misattributed to the neighbour above.
    private static String annotate(String yaml) {
        var header = String.join("\n",
            "# FSit configuration. Do not edit `version` by hand.",
            "# On a dedicated server: `/fsit reload` re-reads this file.",
            "# On the client: reloading local config requires a game restart.",
            ""
        );

        var annotations = new java.util.LinkedHashMap<String, String>();
        annotations.put("use-server-config:", join(
            "# If true, clients with the mod installed will use this server config instead of their own."));
        annotations.put("sitting:", join(
            "# Sitting on blocks (e.g. slabs, stairs)."));
        annotations.put("riding:", join(
            "# Riding other players (interact with a player to sit on their head)."));
        annotations.put("sneak-activation:", join(
            "# Double-sneak shortcuts for entering sit/crawl poses."));
        annotations.put("commands:", join(
            "# Commands.",
            "#   true       -> enabled with default name",
            "#   false      -> command is not registered",
            "#   \"name\"     -> enabled with a custom name (e.g. lay: sleep)",
            "# A server restart is required for command changes to take effect."));

        var out = new StringBuilder(header.length() + yaml.length() + 256);
        out.append(header);
        for (var line : yaml.split("\n", -1)) {
            var comment = annotations.get(line.stripTrailing());
            if (comment != null) {
                out.append(comment);
            }
            out.append(line).append('\n');
        }
        if (out.length() >= 2 && out.charAt(out.length() - 1) == '\n' && out.charAt(out.length() - 2) == '\n') {
            out.setLength(out.length() - 1);
        }
        return out.toString();
    }

    private static String join(String... lines) {
        var sb = new StringBuilder();
        for (var line : lines) sb.append(line).append('\n');
        return sb.toString();
    }
}
